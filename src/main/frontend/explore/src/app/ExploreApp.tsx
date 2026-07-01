import {useEffect, useMemo, useState} from 'react';
import type {Table} from 'apache-arrow';
import type {DuckDbConnector} from '@sqlrooms/duckdb';
import type {ExploreContextDto, ExploreTableDto} from './ExploreContext';
import {createExploreRoomStore} from '../duckdb/createExploreRoomStore';
import {assertSafeTableName, registerParquetTables, type RegisteredTable} from '../duckdb/registerParquetTables';
import {SqlLaboratory} from '../sql/SqlLaboratory';

type ExploreTab = 'preview' | 'sql' | 'chart' | 'code';
type RuntimePhase = 'idle' | 'initializing' | 'registering' | 'previewing' | 'ready' | 'error';

interface PreviewResult {
  sql: string;
  columns: string[];
  rows: Array<Record<string, unknown>>;
  rowCount: number;
}

const tabs: Array<{id: ExploreTab; label: string}> = [
  {id: 'preview', label: 'Vorschau'},
  {id: 'sql', label: 'SQL-Labor'},
  {id: 'chart', label: 'Diagramm'},
  {id: 'code', label: 'Code'}
];

export function ExploreApp({context}: {context: ExploreContextDto}) {
  const [activeTab, setActiveTab] = useState<ExploreTab>('preview');
  const [phase, setPhase] = useState<RuntimePhase>('idle');
  const [runtimeError, setRuntimeError] = useState<string | null>(null);
  const [registeredTables, setRegisteredTables] = useState<RegisteredTable[]>([]);
  const [connector, setConnector] = useState<DuckDbConnector | undefined>(undefined);
  const [previewResult, setPreviewResult] = useState<PreviewResult | null>(null);
  const primaryTable = useMemo(() => selectPrimaryTable(context.tables), [context.tables]);
  const room = useMemo(() => createExploreRoomStore(context), [context]);

  useEffect(() => {
    if (context.tables.length === 0 || !primaryTable) {
      return undefined;
    }

    let active = true;
    const primary = primaryTable;

    async function initializeDuckDb() {
      setPhase('initializing');
      setRuntimeError(null);
      setRegisteredTables(context.tables.map((table) => ({table, status: 'pending', sql: ''})));
      setConnector(undefined);
      setPreviewResult(null);

      try {
        await room.roomStore.getState().db.initialize();
        if (!active) {
          return;
        }

        const connector = await room.roomStore.getState().db.getConnector();
        setConnector(connector);
        setPhase('registering');
        const registrations = await registerParquetTables(connector, context.tables);
        if (!active) {
          return;
        }
        setRegisteredTables(registrations);

        const primaryRegistration = registrations.find((registration) => registration.table.id === primary.id);
        if (!primaryRegistration || primaryRegistration.status === 'failed') {
          throw new Error(primaryRegistration?.error ?? 'Die primäre Parquet-Datei konnte nicht registriert werden.');
        }

        setPhase('previewing');
        const previewSql = buildPreviewSql(primary, context.execution.maxPreviewRows);
        const timeoutController = new AbortController();
        const timeoutId = window.setTimeout(() => timeoutController.abort(), context.execution.queryTimeoutMs);
        try {
          const arrowTable = await connector.query(previewSql, {signal: timeoutController.signal});
          if (!active) {
            return;
          }
          setPreviewResult(toPreviewResult(previewSql, arrowTable));
          setPhase(registrations.some((registration) => registration.status === 'failed') ? 'error' : 'ready');
          if (registrations.some((registration) => registration.status === 'failed')) {
            setRuntimeError('Mindestens eine Parquet-Datei konnte nicht registriert werden. Die Vorschau der primären Tabelle ist verfügbar.');
          }
        } finally {
          window.clearTimeout(timeoutId);
        }
      } catch (error) {
        if (!active) {
          return;
        }
        setRuntimeError(toErrorMessage(error));
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
      <section className="dp-explore-island dp-explore-island--unavailable" aria-labelledby="explore-unavailable-title">
        <div className="dp-explore-island__header">
          <p className="dp-detail-kicker">Erkunden</p>
          <h2 id="explore-unavailable-title">{context.title}</h2>
        </div>
        <p>Erkunden ist für dieses Datenthema noch nicht verfügbar, weil keine Parquet-Datei publiziert ist.</p>
        <a href={context.canonicalUrl}>Downloads und Metadaten auf der Datensatzseite anzeigen</a>
      </section>
    );
  }

  return (
    <section className="dp-explore-island" aria-labelledby="explore-island-title">
      <div className="dp-explore-island__header">
        <p className="dp-detail-kicker">Lokales SQL-Labor</p>
        <div>
          <h2 id="explore-island-title">{context.title}</h2>
          <p>Läuft lokal im Browser mit DuckDB-Wasm direkt auf den Parquet-Dateien.</p>
        </div>
      </div>

      <div className="dp-explore-island__facts" aria-label="Erkunden Übersicht">
        <span>{context.tables.length === 1 ? '1 Tabelle' : `${context.tables.length} Tabellen`}</span>
        <span>{context.recipes.length === 1 ? '1 Beispielabfrage' : `${context.recipes.length} Beispielabfragen`}</span>
        <span>Maximal {formatSwissNumber(context.execution.maxResultRows)} Zeilen angezeigt</span>
      </div>

      <div className="dp-explore-tabs" role="tablist" aria-label="Erkunden Bereiche">
        {tabs.map((tab) => (
          <button
            key={tab.id}
            type="button"
            role="tab"
            aria-selected={activeTab === tab.id}
            className={activeTab === tab.id ? 'dp-explore-tabs__tab is-active' : 'dp-explore-tabs__tab'}
            onClick={() => setActiveTab(tab.id)}
          >
            {tab.label}
          </button>
        ))}
      </div>

      <div className="dp-explore-shell">
        <aside className="dp-explore-panel" aria-labelledby="explore-topic-title">
          <h3 id="explore-topic-title">Datenthema</h3>
          <dl>
            <div>
              <dt>Format</dt>
              <dd>Parquet</dd>
            </div>
            <div>
              <dt>Dateien</dt>
              <dd>{context.tables.length}</dd>
            </div>
            <div>
              <dt>Ausführung</dt>
              <dd>Lokal im Browser</dd>
            </div>
          </dl>
        </aside>

        <main className="dp-explore-workspace" aria-live="polite">
          <p className={`dp-explore-status dp-explore-status--${phase}`}>
            {statusText(phase)}
          </p>
          {runtimeError && <p className="dp-explore-runtime-error">{runtimeError}</p>}
          <h3>{activeTabLabel(activeTab)}</h3>
          {renderActiveTab(activeTab, context, primaryTable, phase, previewResult, connector)}
        </main>

        <aside className="dp-explore-panel" aria-labelledby="explore-tables-title">
          <h3 id="explore-tables-title">Tabellen</h3>
          <TableCatalog tables={context.tables} registrations={registeredTables} />
        </aside>
      </div>
    </section>
  );
}

function selectPrimaryTable(tables: ExploreTableDto[]): ExploreTableDto | undefined {
  return tables.find((table) => table.primary) ?? tables[0];
}

function activeTabLabel(activeTab: ExploreTab): string {
  return tabs.find((tab) => tab.id === activeTab)?.label ?? 'Vorschau';
}

function renderActiveTab(
  activeTab: ExploreTab,
  context: ExploreContextDto,
  primaryTable: ExploreTableDto | undefined,
  phase: RuntimePhase,
  previewResult: PreviewResult | null,
  connector: DuckDbConnector | undefined
) {
  switch (activeTab) {
    case 'preview':
      return <PreviewPanel context={context} primaryTable={primaryTable} phase={phase} previewResult={previewResult} />;
    case 'sql':
      return <SqlLaboratory context={context} connector={connector} ready={phase === 'ready' || phase === 'error'} />;
    case 'chart':
      return <p>Diagramme entstehen in Phase 5 aus SQL-Resultaten.</p>;
    case 'code':
      return <p>Reproduzierbare Codebeispiele werden in Phase 6 interaktiv kopierbar.</p>;
  }
}

function formatSwissNumber(value: number): string {
  return new Intl.NumberFormat('de-CH').format(value);
}

function statusText(phase: RuntimePhase): string {
  switch (phase) {
    case 'idle':
    case 'initializing':
      return 'DuckDB wird initialisiert';
    case 'registering':
      return 'Parquet-Dateien werden registriert';
    case 'previewing':
      return 'Vorschau wird geladen';
    case 'ready':
      return 'Bereit';
    case 'error':
      return 'DuckDB-Hinweis';
  }
}

function buildPreviewSql(table: ExploreTableDto, maxPreviewRows: number): string {
  assertSafeTableName(table.name);
  return `select *
from ${table.name}
limit ${maxPreviewRows};`;
}

function PreviewPanel({
  context,
  primaryTable,
  phase,
  previewResult
}: {
  context: ExploreContextDto;
  primaryTable: ExploreTableDto | undefined;
  phase: RuntimePhase;
  previewResult: PreviewResult | null;
}) {
  if (!primaryTable) {
    return <p>Keine Parquet-Tabelle verfügbar.</p>;
  }
  return (
    <>
      <p>Standardvorschau für {primaryTable.title}. Maximal {formatSwissNumber(context.execution.maxPreviewRows)} Zeilen.</p>
      <pre className="dp-explore-sql" aria-label="SQL Vorschau">{buildPreviewSql(primaryTable, context.execution.maxPreviewRows)}</pre>
      {previewResult ? (
        <PreviewTable result={previewResult} />
      ) : (
        <p className="dp-explore-muted">{phase === 'error' ? 'Keine Vorschau verfügbar.' : 'Die Vorschau wird vorbereitet.'}</p>
      )}
    </>
  );
}

function PreviewTable({result}: {result: PreviewResult}) {
  return (
    <div className="dp-explore-preview" aria-label="Tabellenvorschau">
      <div className="dp-explore-preview__summary">
        {result.rowCount === 1 ? '1 Zeile geladen' : `${formatSwissNumber(result.rowCount)} Zeilen geladen`}
      </div>
      <div className="dp-explore-preview__scroll">
        <table>
          <thead>
            <tr>
              {result.columns.map((column) => (
                <th key={column} scope="col">{column}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {result.rows.map((row, rowIndex) => (
              <tr key={rowIndex}>
                {result.columns.map((column) => (
                  <td key={column}>{formatCell(row[column])}</td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

function TableCatalog({tables, registrations}: {tables: ExploreTableDto[]; registrations: RegisteredTable[]}) {
  return (
    <ul className="dp-explore-table-list">
      {tables.map((table) => {
        const registration = registrations.find((item) => item.table.id === table.id);
        return (
          <li key={table.id}>
            <strong>{table.title}</strong>
            <span>{table.columns.length === 1 ? '1 Attribut' : `${table.columns.length} Attribute`}</span>
            <span className={registration?.status === 'registered' ? 'dp-explore-table-status is-ok' : 'dp-explore-table-status'}>
              {registrationStatusText(registration)}
            </span>
            {registration?.status === 'failed' && registration.error && (
              <small>{registration.error}</small>
            )}
          </li>
        );
      })}
    </ul>
  );
}

function registrationStatusText(registration: RegisteredTable | undefined): string {
  if (!registration) {
    return 'Wartet';
  }
  if (registration.status === 'registered') {
    return 'Registriert';
  }
  return registration.status === 'pending' ? 'Wird registriert' : 'Fehler';
}

function toPreviewResult(sql: string, table: Table): PreviewResult {
  const columns = table.schema.fields.map((field) => field.name);
  const rows: Array<Record<string, unknown>> = [];
  for (let rowIndex = 0; rowIndex < table.numRows; rowIndex++) {
    const row: Record<string, unknown> = {};
    columns.forEach((column, columnIndex) => {
      row[column] = table.getChildAt(columnIndex)?.get(rowIndex) ?? null;
    });
    rows.push(row);
  }
  return {sql, columns, rows, rowCount: table.numRows};
}

function formatCell(value: unknown): string {
  if (value === null || value === undefined) {
    return '';
  }
  if (value instanceof Date) {
    return value.toISOString();
  }
  if (typeof value === 'bigint') {
    return value.toString();
  }
  return String(value);
}

function toErrorMessage(error: unknown): string {
  if (error instanceof Error) {
    return error.message;
  }
  return String(error);
}
