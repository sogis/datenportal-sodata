import {useEffect, useMemo, useState} from 'react';
import type {DuckDbConnector} from '@sqlrooms/duckdb';
import {Panel, PanelGroup, PanelResizeHandle} from 'react-resizable-panels';
import type {ExploreColumnDto, ExploreContextDto, ExploreTableDto} from './ExploreContext';
import {classifyExploreRuntimeError, type ExploreRuntimeError} from './ExploreRuntimeError';
import {createExploreRoomStore} from '../duckdb/createExploreRoomStore';
import {registerParquetTables, type RegisteredTable} from '../duckdb/registerParquetTables';
import {loadRuntimeSchemas, type RuntimeSchemaReadStage} from '../duckdb/runtimeSchema';
import {SqlLaboratory} from '../sql/SqlLaboratory';

type RuntimePhase = 'idle' | 'initializing' | 'registering' | 'ready' | 'error';

export function ExploreApp({context}: {context: ExploreContextDto}) {
  const [phase, setPhase] = useState<RuntimePhase>('idle');
  const [runtimeError, setRuntimeError] = useState<ExploreRuntimeError | null>(null);
  const [registeredTables, setRegisteredTables] = useState<RegisteredTable[]>([]);
  const [runtimeTables, setRuntimeTables] = useState<ExploreTableDto[]>(() => withoutVisibleRuntimeMetadata(context.tables));
  const [connector, setConnector] = useState<DuckDbConnector | undefined>(undefined);
  const primaryTable = useMemo(() => selectPrimaryTable(context.tables), [context.tables]);
  const room = useMemo(() => createExploreRoomStore(context), [context]);
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
    const primary = primaryTable;

    async function initializeDuckDb() {
      setPhase('initializing');
      setRuntimeError(null);
      setRuntimeTables(withoutVisibleRuntimeMetadata(context.tables));
      setRegisteredTables(context.tables.map((table) => ({table, status: 'pending', sql: ''})));
      setConnector(undefined);

      try {
        await room.roomStore.getState().db.initialize();
        if (!active) {
          return;
        }

        const nextConnector = await room.roomStore.getState().db.getConnector();
        setConnector(nextConnector);
        setPhase('registering');
        const registrations = await registerParquetTables(nextConnector, context.tables);
        if (!active) {
          return;
        }
        setRegisteredTables(registrations);

        const primaryRegistration = registrations.find((registration) => registration.table.id === primary.id);
        if (!primaryRegistration || primaryRegistration.status === 'failed') {
          throw new Error(primaryRegistration?.error ?? 'Die primäre Parquet-Datei konnte nicht registriert werden.');
        }

        const failedRegistration = registrations.find((registration) => registration.status === 'failed');
        if (failedRegistration) {
          setRuntimeError(classifyExploreRuntimeError(failedRegistration.error ?? 'Mindestens eine Parquet-Datei konnte nicht registriert werden.'));
          setPhase('error');
          return;
        }

        const nextRuntimeTables = await loadRuntimeSchemas(nextConnector, registrations, (table, error, stage) => {
          console.warn(runtimeMetadataWarning(table, stage), error);
        });
        if (!active) {
          return;
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

  const ready = phase === 'ready' && Boolean(connector && primaryTable && isTableRegistered(primaryTable, registeredTables));
  const schemaPanel = <SchemaPanel tables={runtimeTables} registrations={registeredTables} />;
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
      autoSaveId={`datenportal.explore.${context.datasetId}.workbench.v2`}
      className="dp-explore-workbench__body dp-explore-resizable-group dp-explore-resizable-group--horizontal"
      direction="horizontal"
    >
      <Panel
        className="dp-explore-data-panel"
        defaultSize={22}
        id="schema"
        maxSize={40}
        minSize={14}
        order={1}
        tagName="aside"
        aria-label="Daten und Schema"
      >
        {schemaPanel}
      </Panel>
      <ExploreResizeHandle direction="vertical" label="Schema und SQL-Labor Grösse anpassen" />
      <Panel
        className="dp-explore-workbench__main"
        defaultSize={78}
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
      return 'Parquet-Dateien werden registriert';
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

function SchemaPanel({tables, registrations}: {tables: ExploreTableDto[]; registrations: RegisteredTable[]}) {
  return (
    <div className="dp-explore-schema-panel">
      {tables.map((table) => {
        const registration = registrations.find((item) => item.table.id === table.id);
        const registrationError = registration?.status === 'failed' && registration.error
          ? classifyExploreRuntimeError(registration.error)
          : undefined;
        return (
          <article className="dp-explore-schema-card" key={table.id} aria-labelledby={`schema-card-${table.id}`}>
            <header className="dp-explore-schema-card__header">
              <h2 id={`schema-card-${table.id}`}>{table.name}</h2>
              <span
                aria-label={registrationStatusAriaLabel(table, registration)}
                className={registrationStatusClassName(registration)}
                title={registrationStatusDescription(registration)}
              >
                {registrationStatusText(registration)}
              </span>
            </header>
            <dl className="dp-explore-schema-card__columns" aria-label={`Schema ${table.name}`}>
              {schemaColumns(table).map((column) => (
                <div className="dp-explore-schema-card__column" key={column.name}>
                  <dt title={column.description ?? column.name}>{column.name}</dt>
                  <dd title={column.type}>
                    {shortTypeLabel(column.type)}
                  </dd>
                </div>
              ))}
            </dl>
            {(table.columns.length > 0 || typeof table.rowCountEstimate === 'number') && (
              <footer className="dp-explore-schema-card__footer">
                {table.columns.length > 0 && (
                  <span>{table.columns.length === 1 ? '1 column' : `${formatSwissNumber(table.columns.length)} columns`}</span>
                )}
                {typeof table.rowCountEstimate === 'number' && (
                  <span>{formatSwissNumber(table.rowCountEstimate)} rows</span>
                )}
              </footer>
            )}
            {registrationError && (
              <p className="dp-explore-schema-card__error">
                {registrationError.summary}
                {registrationError.detail && registrationError.detail !== registrationError.summary ? ` ${registrationError.detail}` : ''}
              </p>
            )}
          </article>
        );
      })}
    </div>
  );
}

function schemaColumns(table: ExploreTableDto): Array<{name: string; type: string; description?: string}> {
  return table.columns.map((column: ExploreColumnDto) => ({
    name: column.name,
    type: column.type,
    description: column.description
  }));
}

function runtimeMetadataWarning(table: ExploreTableDto, stage: RuntimeSchemaReadStage): string {
  if (stage === 'rowCount') {
    return `Explore runtime row count could not be read for table ${table.name}. Keeping visible row count empty.`;
  }
  return `Explore runtime schema could not be read for table ${table.name}. Keeping visible schema empty.`;
}

function withoutVisibleRuntimeMetadata(tables: ExploreTableDto[]): ExploreTableDto[] {
  return tables.map((table) => ({...table, columns: [], rowCountEstimate: undefined}));
}

function shortTypeLabel(type: string): string {
  const normalized = type.toUpperCase();
  if (normalized.includes('TIMESTAMP') || normalized.includes('DATE')) {
    return normalized.includes('TIMESTAMP') ? 'TIMESTAMP' : 'DATE';
  }
  if (normalized.includes('DOUBLE') || normalized.includes('FLOAT') || normalized.includes('DECIMAL')) {
    return 'DOUBLE';
  }
  if (normalized.includes('BIGINT') || normalized.includes('LONG')) {
    return 'BIGINT';
  }
  if (normalized.includes('INT')) {
    return 'INT';
  }
  if (normalized.includes('BOOL')) {
    return 'BOOLEAN';
  }
  if (normalized.includes('CHAR') || normalized.includes('TEXT') || normalized.includes('STRING')) {
    return 'VARCHAR';
  }
  return normalized.length > 12 ? normalized.slice(0, 12) : normalized;
}

function isTableRegistered(table: ExploreTableDto, registrations: RegisteredTable[]): boolean {
  return registrations.some((registration) => registration.table.id === table.id && registration.status === 'registered');
}

function registrationStatusText(registration: RegisteredTable | undefined): string {
  if (!registration) {
    return 'Wartet';
  }
  if (registration.status === 'registered') {
    return 'Tabelle geladen';
  }
  return registration.status === 'pending' ? 'Wird registriert' : 'Fehler';
}

function registrationStatusClassName(registration: RegisteredTable | undefined): string {
  if (registration?.status === 'registered') {
    return 'dp-explore-table-status is-ok';
  }
  if (registration?.status === 'failed') {
    return 'dp-explore-table-status is-error';
  }
  return 'dp-explore-table-status';
}

function registrationStatusDescription(registration: RegisteredTable | undefined): string {
  if (!registration) {
    return 'Die Parquet-Datei wartet auf die lokale DuckDB-Wasm-Registrierung.';
  }
  if (registration.status === 'registered') {
    return 'Parquet ist als lokaler DuckDB-View im Browser geladen.';
  }
  return registration.status === 'pending'
    ? 'Die Parquet-Datei wird gerade als lokaler DuckDB-View im Browser geladen.'
    : 'Die Parquet-Datei konnte nicht als lokaler DuckDB-View im Browser geladen werden.';
}

function registrationStatusAriaLabel(table: ExploreTableDto, registration: RegisteredTable | undefined): string {
  return `Tabelle ${table.name}: ${registrationStatusText(registration)}. ${registrationStatusDescription(registration)}`;
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

function formatSwissNumber(value: number): string {
  return new Intl.NumberFormat('de-CH').format(value);
}
