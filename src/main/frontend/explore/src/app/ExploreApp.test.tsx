import {render, screen} from '@testing-library/react';
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

  it('initializes DuckDB and renders the compact SQL workbench', async () => {
    render(<ExploreApp context={sampleExploreContext} />);

    expect(screen.getByLabelText('Erkunden SQL-Labor')).toBeInTheDocument();
    expect(screen.queryByText('DATA')).not.toBeInTheDocument();
    expect(screen.getByLabelText('Daten und Schema')).toBeInTheDocument();
    expect(screen.getByLabelText('Schema und SQL-Labor Grösse anpassen')).toBeInTheDocument();
    expect(screen.getByRole('article', {name: 'ch_so_bauinventar'})).toBeInTheDocument();
    const egidRow = screen.getByText('egid').closest('.dp-explore-schema-card__column');
    expect(egidRow?.querySelector('dt')).toHaveTextContent('egid');
    expect(egidRow?.querySelector('dd')).toHaveTextContent('INT');
    expect(screen.getByText('VARCHAR')).toBeInTheDocument();
    expect(screen.getByLabelText('SQL bearbeiten')).toHaveValue('select * from ch_so_bauinventar;');
    expect(screen.getByRole('button', {name: 'Ausführen'})).toHaveClass('dp-explore-button--primary');
    expect(screen.queryByRole('tab', {name: 'Vorschau'})).not.toBeInTheDocument();
    expect(screen.queryByRole('tab', {name: 'Diagramm'})).not.toBeInTheDocument();
    expect(screen.queryByRole('tab', {name: 'Code'})).not.toBeInTheDocument();
    expect(screen.queryByRole('link', {name: 'Zur Datensatzseite'})).not.toBeInTheDocument();

    expect(await screen.findByText('Bereit')).toBeInTheDocument();
    expect(screen.getByText('Geladen')).toBeInTheDocument();
    expect(screen.getByLabelText(/Tabelle ch_so_bauinventar: Geladen/)).toHaveAttribute(
      'title',
      'Parquet ist als lokaler DuckDB-View im Browser geladen.'
    );
  });

  it('runs the initial registered-view query and renders result rows', async () => {
    const user = userEvent.setup();
    render(<ExploreApp context={sampleExploreContext} />);

    expect(await screen.findByText('Bereit')).toBeInTheDocument();
    await user.click(screen.getByRole('button', {name: 'Ausführen'}));

    expect(await screen.findByLabelText('SQL Ergebnis')).toBeInTheDocument();
    expect(screen.getByText('Solothurn')).toBeInTheDocument();
    expect(screen.getByText('Olten')).toBeInTheDocument();
    expect(screen.getAllByText('egid').length).toBeGreaterThanOrEqual(1);
    expect(screen.getAllByText('gemeindename').length).toBeGreaterThanOrEqual(1);
    expect(mocks.connector.query).toHaveBeenLastCalledWith(
      expect.stringContaining('limit 1000'),
      expect.objectContaining({signal: expect.any(AbortSignal)})
    );
  });

  it('shows registration errors without crashing the workbench', async () => {
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
    expect(screen.getAllByText(/CORS blocked/).length).toBeGreaterThan(0);
    expect(screen.getByText('Fehler')).toBeInTheDocument();
  });

  it('renders an unavailable state without Parquet tables', () => {
    render(<ExploreApp context={{...sampleExploreContext, tables: [], recipes: [], codeSnippets: []}} />);

    expect(screen.getByRole('heading', {name: 'Bauinventar'})).toBeInTheDocument();
    expect(screen.getByText('Erkunden ist für dieses Datenthema noch nicht verfügbar, weil keine Parquet-Datei publiziert ist.')).toBeInTheDocument();
    expect(screen.getByRole('link', {name: 'Downloads und Metadaten auf der Datensatzseite anzeigen'})).toHaveAttribute('href', '/datasets/ch.so.bauinventar');
  });
});
