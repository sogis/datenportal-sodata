import {render, screen, waitFor} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {tableFromArrays} from 'apache-arrow';
import {beforeEach, describe, expect, it, vi} from 'vitest';
import {ExploreApp} from './ExploreApp';
import {sampleExploreContext} from '../test/sampleExploreContext';

const mocks = vi.hoisted(() => {
  const connector = {
    query: vi.fn()
  };
  return {
    connector,
    initialize: vi.fn(),
    destroy: vi.fn(),
    getConnector: vi.fn(),
    registerParquetTables: vi.fn()
  };
});

vi.mock('../duckdb/createExploreRoomStore', () => ({
  createExploreRoomStore: () => ({
    roomStore: {
      getState: () => ({
        db: {
          initialize: mocks.initialize,
          destroy: mocks.destroy,
          getConnector: mocks.getConnector
        }
      })
    }
  })
}));

vi.mock('../duckdb/registerParquetTables', async (importOriginal) => {
  const actual = await importOriginal<typeof import('../duckdb/registerParquetTables')>();
  return {
    ...actual,
    registerParquetTables: mocks.registerParquetTables
  };
});

describe('ExploreApp', () => {
  beforeEach(() => {
    mocks.connector.query.mockReset();
    mocks.initialize.mockReset().mockResolvedValue(undefined);
    mocks.destroy.mockReset().mockResolvedValue(undefined);
    mocks.getConnector.mockReset().mockResolvedValue(mocks.connector);
    mocks.registerParquetTables.mockReset().mockResolvedValue([
      {
        table: sampleExploreContext.tables[0],
        status: 'registered',
        sql: 'create view'
      }
    ]);
    mocks.connector.query.mockResolvedValue(tableFromArrays({
      egid: [1001, 1002],
      gemeindename: ['Solothurn', 'Olten']
    }));
  });

  it('initializes DuckDB and renders the registered preview state', async () => {
    render(<ExploreApp context={sampleExploreContext} />);

    expect(screen.getByRole('heading', {name: 'Bauinventar'})).toBeInTheDocument();
    expect(screen.getByText('1 Tabelle')).toBeInTheDocument();
    expect(screen.getByRole('tab', {name: 'Vorschau'})).toHaveAttribute('aria-selected', 'true');
    expect(screen.getByRole('tab', {name: 'SQL-Labor'})).toBeInTheDocument();
    expect(screen.getByRole('tab', {name: 'Diagramm'})).toBeInTheDocument();
    expect(screen.getByRole('tab', {name: 'Code'})).toBeInTheDocument();
    expect(screen.getByRole('status', {name: 'Erkunden Status'})).toHaveTextContent('DuckDB wird initialisiert');
    expect(screen.getByText('DuckDB wird initialisiert')).toBeInTheDocument();
    expect(screen.queryByLabelText('Vorbereitete Erweiterungen')).not.toBeInTheDocument();
    expect(screen.getByLabelText('SQL Vorschau')).toHaveTextContent('select *');
    expect(await screen.findByText('Bereit')).toBeInTheDocument();
    expect(screen.getByText('Registriert')).toBeInTheDocument();
    expect(screen.getByText('Solothurn')).toBeInTheDocument();
    expect(screen.getByText('Olten')).toBeInTheDocument();
  });

  it('supports keyboard navigation in the main tab list', async () => {
    const user = userEvent.setup();
    render(<ExploreApp context={sampleExploreContext} />);

    expect(await screen.findByText('Bereit')).toBeInTheDocument();
    const previewTab = screen.getByRole('tab', {name: 'Vorschau'});
    previewTab.focus();

    await user.keyboard('{ArrowRight}');
    await waitFor(() => expect(screen.getByRole('tab', {name: 'SQL-Labor'})).toHaveFocus());
    expect(screen.getByRole('tab', {name: 'SQL-Labor'})).toHaveAttribute('aria-selected', 'true');
    expect(screen.getByRole('tabpanel', {name: 'SQL-Labor'})).toBeInTheDocument();

    await user.keyboard('{End}');
    await waitFor(() => expect(screen.getByRole('tab', {name: 'Code'})).toHaveFocus());
    expect(screen.getByRole('tab', {name: 'Code'})).toHaveAttribute('aria-selected', 'true');

    await user.keyboard('{Home}');
    await waitFor(() => expect(screen.getByRole('tab', {name: 'Vorschau'})).toHaveFocus());
    expect(screen.getByRole('tab', {name: 'Vorschau'})).toHaveAttribute('aria-selected', 'true');
  });

  it('switches to the SQL laboratory and runs the selected recipe', async () => {
    const user = userEvent.setup();
    render(<ExploreApp context={sampleExploreContext} />);

    expect(await screen.findByText('Bereit')).toBeInTheDocument();
    await user.click(screen.getByRole('tab', {name: 'SQL-Labor'}));
    await user.click(screen.getByRole('button', {name: 'Ausführen'}));

    expect(screen.getByRole('tab', {name: 'SQL-Labor'})).toHaveAttribute('aria-selected', 'true');
    expect(screen.getByLabelText('Beispielabfragen')).toBeInTheDocument();
    expect(screen.getByLabelText('SQL Ergebnis')).toBeInTheDocument();
    expect(screen.getByLabelText('Diagramm aus Resultat')).toBeInTheDocument();
    expect(screen.getByRole('button', {name: 'Resultat als CSV'})).toBeEnabled();
    expect(mocks.connector.query).toHaveBeenLastCalledWith(
      expect.stringContaining('select * from ch_so_bauinventar limit 100'),
      expect.objectContaining({signal: expect.any(AbortSignal)})
    );

    await user.click(screen.getByRole('tab', {name: 'Diagramm'}));
    expect(screen.getByRole('tab', {name: 'Diagramm'})).toHaveAttribute('aria-selected', 'true');
    expect(screen.getByLabelText('Diagramm aus Resultat')).toBeInTheDocument();
  });

  it('renders static code snippets in the Code tab', async () => {
    const user = userEvent.setup();
    render(<ExploreApp context={sampleExploreContext} />);

    expect(await screen.findByText('Bereit')).toBeInTheDocument();
    await user.click(screen.getByRole('tab', {name: 'Code'}));

    expect(screen.getByRole('tab', {name: 'Code'})).toHaveAttribute('aria-selected', 'true');
    expect(screen.getByRole('heading', {name: 'Weiterverwenden'})).toBeInTheDocument();
    expect(screen.getByRole('tab', {name: 'DuckDB CLI'})).toBeInTheDocument();
    expect(screen.getByRole('tab', {name: 'Python mit DuckDB'})).toBeInTheDocument();
    expect(screen.getByRole('tab', {name: 'R mit duckdb'})).toBeInTheDocument();
    expect(screen.queryByText('Reproduzierbare Codebeispiele werden in Phase 6 interaktiv kopierbar.')).not.toBeInTheDocument();
  });

  it('shows registration errors without crashing the island', async () => {
    mocks.registerParquetTables.mockResolvedValue([
      {
        table: sampleExploreContext.tables[0],
        status: 'failed',
        sql: 'create view',
        error: 'CORS blocked'
      }
    ]);

    render(<ExploreApp context={sampleExploreContext} />);

    expect(await screen.findByText('DuckDB-Hinweis')).toBeInTheDocument();
    expect(screen.getByRole('alert', {name: 'Erkunden Status'})).toHaveTextContent('DuckDB-Hinweis');
    expect(screen.getAllByText('Parquet-Datei konnte wegen CORS nicht im Browser geladen werden.').length).toBeGreaterThan(0);
    expect(screen.getAllByText('CORS blocked').length).toBeGreaterThan(0);
    expect(screen.getByText('Fehler')).toBeInTheDocument();
  });

  it('renders an unavailable state without Parquet tables', () => {
    render(<ExploreApp context={{...sampleExploreContext, tables: [], recipes: [], codeSnippets: []}} />);

    expect(screen.getByRole('heading', {name: 'Bauinventar'})).toBeInTheDocument();
    expect(screen.getByText('Erkunden ist für dieses Datenthema noch nicht verfügbar, weil keine Parquet-Datei publiziert ist.')).toBeInTheDocument();
    expect(screen.getByRole('link', {name: 'Downloads und Metadaten auf der Datensatzseite anzeigen'})).toHaveAttribute('href', '/datasets/ch.so.bauinventar');
  });
});
