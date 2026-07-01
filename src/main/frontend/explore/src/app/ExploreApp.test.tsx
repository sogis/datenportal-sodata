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

  it('initializes DuckDB and renders the registered preview state', async () => {
    render(<ExploreApp context={sampleExploreContext} />);

    expect(screen.getByRole('heading', {name: 'Bauinventar'})).toBeInTheDocument();
    expect(screen.getByText('1 Tabelle')).toBeInTheDocument();
    expect(screen.getByRole('tab', {name: 'Vorschau'})).toHaveAttribute('aria-selected', 'true');
    expect(screen.getByRole('tab', {name: 'SQL-Labor'})).toBeInTheDocument();
    expect(screen.getByRole('tab', {name: 'Diagramm'})).toBeInTheDocument();
    expect(screen.getByRole('tab', {name: 'Code'})).toBeInTheDocument();
    expect(screen.getByText('DuckDB wird initialisiert')).toBeInTheDocument();
    expect(screen.getByLabelText('SQL Vorschau')).toHaveTextContent('select *');
    expect(await screen.findByText('Bereit')).toBeInTheDocument();
    expect(screen.getByText('Registriert')).toBeInTheDocument();
    expect(screen.getByText('Solothurn')).toBeInTheDocument();
    expect(screen.getByText('Olten')).toBeInTheDocument();
  });

  it('switches placeholder text when tabs change', async () => {
    const user = userEvent.setup();
    render(<ExploreApp context={sampleExploreContext} />);

    await user.click(screen.getByRole('tab', {name: 'SQL-Labor'}));

    expect(screen.getByRole('tab', {name: 'SQL-Labor'})).toHaveAttribute('aria-selected', 'true');
    expect(screen.getByText('Der SQL-Editor folgt in Phase 4. Die Standardabfrage läuft bereits lokal mit DuckDB-Wasm.')).toBeInTheDocument();
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
