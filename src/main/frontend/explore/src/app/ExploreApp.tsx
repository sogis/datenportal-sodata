import {useEffect, useMemo, useState} from 'react';
import type {DataTable, DbSchemaNode, DuckDbConnector} from '@sqlrooms/duckdb';
import {Panel, PanelGroup, PanelResizeHandle} from 'react-resizable-panels';
import type {ExploreContextDto, ExploreTableDto} from './ExploreContext';
import {classifyExploreRuntimeError, type ExploreRuntimeError} from './ExploreRuntimeError';
import {SchemaExplorerPanel} from './SchemaExplorerPanel';
import {attachCatalogDatabase, type CatalogDatabaseRegistration} from '../duckdb/attachCatalogDatabase';
import {createExploreRoomStore} from '../duckdb/createExploreRoomStore';
import {mergeRuntimeColumns} from '../duckdb/runtimeSchema';
import {SqlLaboratory} from '../sql/SqlLaboratory';

type RuntimePhase = 'idle' | 'initializing' | 'registering' | 'ready' | 'error';

const EMPTY_SCHEMA_TREES: DbSchemaNode[] = [];

export function ExploreApp({context}: {context: ExploreContextDto}) {
  const [phase, setPhase] = useState<RuntimePhase>('idle');
  const [runtimeError, setRuntimeError] = useState<ExploreRuntimeError | null>(null);
  const [catalogRegistration, setCatalogRegistration] = useState<CatalogDatabaseRegistration | undefined>(undefined);
  const [runtimeTables, setRuntimeTables] = useState<ExploreTableDto[]>(() => withoutVisibleRuntimeMetadata(context.tables));
  const [connector, setConnector] = useState<DuckDbConnector | undefined>(undefined);
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
    if (context.tables.length === 0 || !primaryTable) {
      return undefined;
    }

    let active = true;
    const primaryTableName = primaryTable.name;

    async function initializeDuckDb() {
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

      try {
        await room.roomStore.getState().db.initialize();
        if (!active) {
          return;
        }

        const nextConnector = await room.roomStore.getState().db.getConnector();
        setConnector(nextConnector);
        setPhase('registering');

        const registration = await attachCatalogDatabase(nextConnector, context.catalogDatabase);
        if (!active) {
          return;
        }
        setCatalogRegistration(registration);
        if (registration.status === 'failed') {
          setRuntimeError(classifyExploreRuntimeError(registration.error ?? 'Der DuckDB-Catalog konnte nicht geladen werden.'));
          setPhase('error');
          return;
        }

        const catalogTables = await room.roomStore.getState().db.refreshTableSchemas();
        if (!active) {
          return;
        }

        const nextRuntimeTables = mergeCatalogRuntimeTables(context.tables, catalogTables, context.catalogDatabase);
        const nextPrimaryTable = selectPrimaryTable(nextRuntimeTables);
        if (!nextPrimaryTable || nextPrimaryTable.columns.length === 0) {
          throw new Error(`Die View ${primaryTableName} wurde im DuckDB-Catalog nicht gefunden.`);
        }

        setRuntimeTables(nextRuntimeTables);
        setPhase('ready');
      } catch (error) {
        if (!active) {
          return;
        }
        setRuntimeError(classifyExploreRuntimeError(error));
        setPhase('error');
      }
    }

    void initializeDuckDb();

    return () => {
      active = false;
      void room.roomStore.getState().db.destroy();
    };
  }, [context, primaryTable, room]);

  async function refreshSchemas() {
    const catalogTables = await room.roomStore.getState().db.refreshTableSchemas();
    setRuntimeTables(mergeCatalogRuntimeTables(context.tables, catalogTables, context.catalogDatabase));
  }

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
  const laboratoryPanel = <SqlLaboratory context={runtimeContext} connector={connector} ready={ready} />;
  const workbenchBody = isNarrowWorkbench ? (
    <div className="dp-explore-workbench__body">
      <aside className="dp-explore-data-panel" aria-label="Daten und Schema">
        {schemaPanel}
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
        {schemaPanel}
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
