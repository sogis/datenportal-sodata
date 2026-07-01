import {useMemo, useState} from 'react';
import type {ExploreContextDto, ExploreTableDto} from './ExploreContext';

type ExploreTab = 'preview' | 'sql' | 'chart' | 'code';

const tabs: Array<{id: ExploreTab; label: string}> = [
  {id: 'preview', label: 'Vorschau'},
  {id: 'sql', label: 'SQL-Labor'},
  {id: 'chart', label: 'Diagramm'},
  {id: 'code', label: 'Code'}
];

export function ExploreApp({context}: {context: ExploreContextDto}) {
  const [activeTab, setActiveTab] = useState<ExploreTab>('preview');
  const primaryTable = useMemo(() => selectPrimaryTable(context.tables), [context.tables]);

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
          <p className="dp-explore-status">DuckDB wird vorbereitet</p>
          <h3>{activeTabLabel(activeTab)}</h3>
          <p>{placeholderText(activeTab)}</p>
          {primaryTable && (
            <pre className="dp-explore-sql" aria-label="SQL Vorschau">{`select *\nfrom ${primaryTable.name}\nlimit ${context.execution.maxPreviewRows};`}</pre>
          )}
        </main>

        <aside className="dp-explore-panel" aria-labelledby="explore-tables-title">
          <h3 id="explore-tables-title">Tabellen</h3>
          <ul className="dp-explore-table-list">
            {context.tables.map((table) => (
              <li key={table.id}>
                <strong>{table.title}</strong>
                <span>{table.columns.length === 1 ? '1 Attribut' : `${table.columns.length} Attribute`}</span>
              </li>
            ))}
          </ul>
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

function placeholderText(activeTab: ExploreTab): string {
  switch (activeTab) {
    case 'preview':
      return 'Die Tabellenübersicht ist bereit. Die DuckDB-Registrierung folgt in der nächsten Phase.';
    case 'sql':
      return 'Der SQL-Editor wird hier eingebunden, sobald DuckDB-Wasm initialisiert wird.';
    case 'chart':
      return 'Diagramme entstehen später aus SQL-Resultaten. Für Phase 2 bleibt dies ein Platzhalter.';
    case 'code':
      return 'Reproduzierbare Codebeispiele werden in einer späteren Phase interaktiv kopierbar.';
  }
}

function formatSwissNumber(value: number): string {
  return new Intl.NumberFormat('de-CH').format(value);
}
