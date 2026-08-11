import {useCallback, useEffect, useMemo, useRef, useState} from 'react';
import type {DataTable, DbSchemaNode, DuckDbConnector} from '@sqlrooms/duckdb';
import {Panel, PanelGroup, PanelResizeHandle} from 'react-resizable-panels';
import type {ExploreContextDto, ExploreTableDto} from './ExploreContext';
import {
  classifyExploreQueryError,
  classifyExploreRuntimeError,
  isSourceQueryError,
  type ExploreRuntimeError
} from './ExploreRuntimeError';
import {SchemaExplorerPanel} from './SchemaExplorerPanel';
import {attachCatalogDatabase, type CatalogDatabaseRegistration} from '../duckdb/attachCatalogDatabase';
import {createExploreRoomStore} from '../duckdb/createExploreRoomStore';
import {mergeRuntimeColumns} from '../duckdb/runtimeSchema';
import {SqlLaboratory} from '../sql/SqlLaboratory';
import {RDataFramePanel} from '../webr/RDataFramePanel';
import {RPanel} from '../webr/RPanel';
import type {RDataFrameInfo} from '../webr/WebRBridge';
import type {SqlResultSnapshot} from '../results/sqlResultSnapshot';

type RuntimePhase = 'idle' | 'initializing' | 'registering' | 'ready' | 'error';
type ActiveLab = 'sql' | 'r';

const EMPTY_SCHEMA_TREES: DbSchemaNode[] = [];

export function ExploreApp({context}: {context: ExploreContextDto}) {
  const [phase, setPhase] = useState<RuntimePhase>('idle');
  const [runtimeError, setRuntimeError] = useState<ExploreRuntimeError | null>(null);
  const [catalogRegistration, setCatalogRegistration] = useState<CatalogDatabaseRegistration | undefined>(undefined);
  const [runtimeTables, setRuntimeTables] = useState<ExploreTableDto[]>(() => withoutVisibleRuntimeMetadata(context.tables));
  const [connector, setConnector] = useState<DuckDbConnector | undefined>(undefined);
  const [activeLab, setActiveLab] = useState<ActiveLab>('sql');
  const [rLabMounted, setRLabMounted] = useState(false);
  const [rSnapshot, setRSnapshot] = useState<SqlResultSnapshot | undefined>(undefined);
  const [rDataFrameInfo, setRDataFrameInfo] = useState<RDataFrameInfo | undefined>(undefined);
  const initializationGeneration = useRef(0);
  const mountedRef = useRef(true);
  const primaryTable = useMemo(() => selectPrimaryTable(context.tables), [context.tables]);
  const room = useMemo(() => createExploreRoomStore(context), [context]);
  const schemaTrees = room.useRoomStore((state) => state.db.schemaTrees) ?? EMPTY_SCHEMA_TREES;
  const catalogSchemaTrees = useMemo(
    () => schemaTrees.filter((node) => node.object.type !== 'database' || node.object.name === context.catalogDatabase.database),
    [context.catalogDatabase.database, schemaTrees]
  );
  const refreshingSchemas = room.useRoomStore((state) => state.db.isRefreshingTableSchemas);
  const isNarrowWorkbench = useIsNarrowWorkbench();
  const runtimeContext = useMemo(() => ({
    ...context,
    tables: runtimeTables
  }), [context, runtimeTables]);

  useEffect(() => {
    return () => {
      mountedRef.current = false;
    };
  }, []);

  useEffect(() => {
    const generation = ++initializationGeneration.current;
    if (context.tables.length === 0 || !primaryTable) {
      return undefined;
    }

    const controller = new AbortController();
    const db = room.roomStore.getState().db;
    let active = true;
    let destroyRequested = false;
    let initializationFinished = false;
    let destroyPromise: Promise<void> | undefined;

    const isCurrent = () => active && generation === initializationGeneration.current;
    const destroyOnce = () => {
      destroyPromise ??= Promise.resolve().then(() => db.destroy()).catch(() => undefined);
      return destroyPromise;
    };

    async function initializeDuckDb() {
      try {
        setPhase('initializing');
        setRuntimeError(null);
        setRuntimeTables(withoutVisibleRuntimeMetadata(context.tables));
        setCatalogRegistration({
          status: 'pending',
          url: context.catalogDatabase.url,
          database: context.catalogDatabase.database,
          schema: context.catalogDatabase.schema
        });
        setConnector(undefined);

        await db.initialize();
        if (!isCurrent()) {
          return;
        }

        const nextConnector = await db.getConnector();
        if (!isCurrent()) {
          return;
        }
        setConnector(nextConnector);
        setPhase('registering');

        const registration = await attachCatalogDatabase(nextConnector, context.catalogDatabase, undefined, controller.signal);
        if (!isCurrent()) {
          return;
        }
        setCatalogRegistration(registration);
        if (registration.status === 'failed') {
          setRuntimeError(classifyExploreRuntimeError(registration.error ?? 'Der DuckDB-Catalog konnte nicht geladen werden.'));
          setPhase('error');
          return;
        }

        let catalogTables: DataTable[];
        try {
          catalogTables = await db.refreshTableSchemas();
        } catch (schemaError) {
          const queryError = classifyExploreQueryError(schemaError);
          if (isSourceQueryError(queryError)) {
            if (!isCurrent()) {
              return;
            }
            setRuntimeTables(context.tables);
            setPhase('ready');
            return;
          }
          throw schemaError;
        }
        if (!isCurrent()) {
          return;
        }

        const nextRuntimeTables = mergeCatalogRuntimeTables(context.tables, catalogTables, context.catalogDatabase);
        const nextPrimaryTable = selectPrimaryTable(nextRuntimeTables);
        if (!nextPrimaryTable || nextPrimaryTable.columns.length === 0) {
          setRuntimeTables(context.tables);
          setPhase('ready');
          return;
        }

        setRuntimeTables(nextRuntimeTables);
        setPhase('ready');
      } catch (error) {
        if (!isCurrent()) {
          return;
        }
        setRuntimeError(classifyExploreRuntimeError(error));
        setPhase('error');
      } finally {
        initializationFinished = true;
        if (destroyRequested) {
          await destroyOnce();
        }
      }
    }

    const initialization = initializeDuckDb();

    return () => {
      active = false;
      controller.abort();
      destroyRequested = true;
      if (initializationFinished) {
        void destroyOnce();
      } else {
        void initialization;
      }
    };
  }, [context, primaryTable, room]);

  async function refreshSchemas() {
    const generation = initializationGeneration.current;
    try {
      const catalogTables = await room.roomStore.getState().db.refreshTableSchemas();
      if (!mountedRef.current || generation !== initializationGeneration.current) {
        return;
      }
      setRuntimeTables(mergeCatalogRuntimeTables(context.tables, catalogTables, context.catalogDatabase));
    } catch (schemaError) {
      const queryError = classifyExploreQueryError(schemaError);
      if (isSourceQueryError(queryError)) {
        if (!mountedRef.current || generation !== initializationGeneration.current) {
          return;
        }
        setRuntimeTables(context.tables);
        return;
      }
      throw schemaError;
    }
  }

  const transferToR = useCallback((snapshot: SqlResultSnapshot) => {
    setRSnapshot(snapshot);
    setRDataFrameInfo(undefined);
    setRLabMounted(true);
    setActiveLab('r');
  }, []);

  if (context.tables.length === 0) {
    return (
      <section className="dp-explore-workbench dp-explore-workbench--unavailable" aria-labelledby="explore-unavailable-title">
        <div className="dp-explore-empty-state">
          <p className="dp-explore-kicker">Erkunden</p>
          <h1 id="explore-unavailable-title">{context.title}</h1>
          <p>Erkunden ist für dieses Datenthema noch nicht verfügbar, weil keine Parquet-Datei publiziert ist.</p>
          <a href={context.canonicalUrl}>Downloads und Metadaten auf der Datensatzseite anzeigen</a>
        </div>
      </section>
    );
  }

  const ready = phase === 'ready' && Boolean(connector && catalogRegistration?.status === 'registered');
  const activeTable = selectPrimaryTable(runtimeTables);
  const schemaPanel = (
    <SchemaExplorerPanel
      schemaTrees={catalogSchemaTrees}
      catalogDatabase={context.catalogDatabase}
      activeTable={activeTable}
      refreshing={refreshingSchemas}
      onRefresh={refreshSchemas}
    />
  );
  const dataPanel = activeLab === 'r' ? (
    <RDataFramePanel snapshot={rSnapshot} info={rDataFrameInfo} laboratory={context.rLaboratory} />
  ) : schemaPanel;
  const laboratoryPanel = (
    <div className="dp-explore-lab-shell">
      <div className="dp-explore-lab-tabs" role="tablist" aria-label="Labor auswählen">
        <button
          type="button"
          role="tab"
          id="dp-explore-tab-sql"
          aria-controls="dp-explore-panel-sql"
          aria-selected={activeLab === 'sql'}
          className={activeLab === 'sql' ? 'is-active' : undefined}
          onClick={() => setActiveLab('sql')}
        >
          SQL-Labor
        </button>
        <button
          type="button"
          role="tab"
          id="dp-explore-tab-r"
          aria-controls="dp-explore-panel-r"
          aria-selected={activeLab === 'r'}
          className={activeLab === 'r' ? 'is-active' : undefined}
          onClick={() => {
            setRLabMounted(true);
            setActiveLab('r');
          }}
        >
          R-Labor
        </button>
      </div>
      <div className="dp-explore-lab-panels">
        <div
          id="dp-explore-panel-sql"
          role="tabpanel"
          aria-labelledby="dp-explore-tab-sql"
          hidden={activeLab !== 'sql'}
          className="dp-explore-lab-panel"
        >
          <SqlLaboratory context={runtimeContext} connector={connector} ready={ready} onTransferToR={transferToR} />
        </div>
        {rLabMounted && (
          <div
            id="dp-explore-panel-r"
            role="tabpanel"
            aria-labelledby="dp-explore-tab-r"
            hidden={activeLab !== 'r'}
            className="dp-explore-lab-panel"
          >
            <RPanel
              context={runtimeContext}
              snapshot={rSnapshot}
              onDataFrameInfoChange={setRDataFrameInfo}
              onBackToSql={() => setActiveLab('sql')}
            />
          </div>
        )}
      </div>
    </div>
  );
  const workbenchBody = isNarrowWorkbench ? (
    <div className="dp-explore-workbench__body">
      <aside className="dp-explore-data-panel" aria-label="Daten und Schema">
        {dataPanel}
      </aside>

      <section className="dp-explore-workbench__main" aria-busy={isBusyPhase(phase)} aria-label="SQL Arbeitsbereich">
        {laboratoryPanel}
      </section>
    </div>
  ) : (
    <PanelGroup
      autoSaveId={`datenportal.explore.${context.datasetId}.workbench.v3`}
      className="dp-explore-workbench__body dp-explore-resizable-group dp-explore-resizable-group--horizontal"
      direction="horizontal"
    >
      <Panel
        className="dp-explore-data-panel"
        defaultSize={25}
        id="schema"
        maxSize={42}
        minSize={16}
        order={1}
        tagName="aside"
        aria-label="Daten und Schema"
      >
        {dataPanel}
      </Panel>
      <ExploreResizeHandle direction="vertical" label="Schema und SQL-Labor Grösse anpassen" />
      <Panel
        className="dp-explore-workbench__main"
        defaultSize={75}
        id="laboratory"
        minSize={45}
        order={2}
        tagName="section"
        aria-busy={isBusyPhase(phase)}
        aria-label="SQL Arbeitsbereich"
      >
        {laboratoryPanel}
      </Panel>
    </PanelGroup>
  );

  return (
    <section className="dp-explore-workbench" aria-label="Erkunden SQL-Labor">
      {workbenchBody}
      {phase !== 'ready' && <ExploreRuntimeOverlay phase={phase} error={runtimeError} />}
    </section>
  );
}

function selectPrimaryTable(tables: ExploreTableDto[]): ExploreTableDto | undefined {
  return tables.find((table) => table.primary) ?? tables[0];
}

function isBusyPhase(phase: RuntimePhase): boolean {
  return phase === 'idle' || phase === 'initializing' || phase === 'registering';
}

function statusText(phase: RuntimePhase): string {
  switch (phase) {
    case 'idle':
    case 'initializing':
      return 'DuckDB wird initialisiert';
    case 'registering':
      return 'DuckDB-Catalog wird geladen';
    case 'ready':
      return 'Bereit';
    case 'error':
      return 'DuckDB-Hinweis';
  }
}

function ExploreRuntimeOverlay({phase, error}: {phase: RuntimePhase; error: ExploreRuntimeError | null}) {
  const isError = phase === 'error';
  return (
    <div className="dp-explore-runtime-overlay">
      <div
        className={`dp-explore-runtime-overlay__card${isError ? ' is-error' : ''}`}
        role={isError ? 'alert' : 'status'}
        aria-label="Erkunden Status"
        aria-live={isError ? 'assertive' : 'polite'}
      >
        <p className="dp-explore-runtime-overlay__title">
          {isError ? statusText(phase) : 'Erkunden wird vorbereitet'}
        </p>
        {!isError && (
          <>
            <div className="dp-explore-runtime-progress" role="progressbar" aria-label="Ladevorgang">
              <span className="dp-explore-runtime-progress__bar" />
            </div>
            <p className="dp-explore-runtime-overlay__message">{statusText(phase)}</p>
          </>
        )}
        {isError && error && (
          <div className="dp-explore-runtime-overlay__details">
            <p>{error.summary}</p>
            {error.detail && error.detail !== error.summary && (
              <details>
                <summary>Technische Details</summary>
                <p>{error.detail}</p>
              </details>
            )}
          </div>
        )}
      </div>
    </div>
  );
}

function mergeCatalogRuntimeTables(
  contextTables: ExploreTableDto[],
  catalogTables: DataTable[],
  catalogDatabase: ExploreContextDto['catalogDatabase']
): ExploreTableDto[] {
  return contextTables.map((table) => {
    const catalogTable = catalogTables.find((candidate) =>
      candidate.tableName === table.name
      && candidate.schema === catalogDatabase.schema
      && (candidate.database ?? candidate.table.database) === catalogDatabase.database
    );
    if (!catalogTable) {
      return withoutVisibleRuntimeMetadata([table])[0];
    }
    return {
      ...table,
      columns: mergeRuntimeColumns(table.columns, catalogTable.columns),
      rowCountEstimate: table.rowCountEstimate
    };
  });
}

function withoutVisibleRuntimeMetadata(tables: ExploreTableDto[]): ExploreTableDto[] {
  return tables.map((table) => ({...table, columns: [], rowCountEstimate: undefined}));
}

function useIsNarrowWorkbench(): boolean {
  const [isNarrow, setIsNarrow] = useState(false);

  useEffect(() => {
    if (typeof window === 'undefined' || typeof window.matchMedia !== 'function') {
      return undefined;
    }

    const query = window.matchMedia('(max-width: 56rem)');
    const update = () => setIsNarrow(query.matches);
    update();
    query.addEventListener('change', update);
    return () => query.removeEventListener('change', update);
  }, []);

  return isNarrow;
}

function ExploreResizeHandle({direction, label}: {direction: 'horizontal' | 'vertical'; label: string}) {
  return (
    <PanelResizeHandle
      aria-label={label}
      className={`dp-explore-resize-handle dp-explore-resize-handle--${direction}`}
      hitAreaMargins={{coarse: 12, fine: 8}}
    >
      <span className="dp-explore-resize-handle__knob" aria-hidden="true" />
    </PanelResizeHandle>
  );
}
