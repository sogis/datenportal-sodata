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
    mockRuntimeSchema();
  });

  it('initializes DuckDB and renders the compact SQL workbench', async () => {
    render(<ExploreApp context={sampleExploreContext} />);

    expect(screen.getByLabelText('Erkunden SQL-Labor')).toBeInTheDocument();
    expect(screen.getByRole('status', {name: 'Erkunden Status'})).toHaveTextContent('Erkunden wird vorbereitet');
    expect(screen.getByRole('status', {name: 'Erkunden Status'})).toHaveTextContent('DuckDB wird initialisiert');
    expect(screen.getByRole('progressbar', {name: 'Ladevorgang'})).toBeInTheDocument();
    expect(screen.queryByText('DATA')).not.toBeInTheDocument();
    expect(screen.getByLabelText('Daten und Schema')).toBeInTheDocument();
    expect(screen.getByLabelText('Schema und SQL-Labor Grösse anpassen')).toBeInTheDocument();
    expect(screen.getByRole('article', {name: 'ch_so_bauinventar'})).toBeInTheDocument();
    expect(screen.getByLabelText('SQL bearbeiten')).toHaveValue('SELECT *\nFROM ch_so_bauinventar;');
    expect(screen.getByRole('button', {name: 'Ausführen'})).toHaveClass('dp-explore-button--primary');
    expect(screen.queryByRole('tab', {name: 'Vorschau'})).not.toBeInTheDocument();
    expect(screen.queryByRole('tab', {name: 'Diagramm'})).not.toBeInTheDocument();
    expect(screen.queryByRole('tab', {name: 'Code'})).not.toBeInTheDocument();
    expect(screen.queryByRole('link', {name: 'Zur Datensatzseite'})).not.toBeInTheDocument();
    expect(screen.queryByText("36'176 rows")).not.toBeInTheDocument();

    expect(await screen.findByText('Tabelle geladen')).toBeInTheDocument();
    const egidRow = screen.getByText('egid').closest('.dp-explore-schema-card__column');
    expect(egidRow?.querySelector('dt')).toHaveTextContent('egid');
    expect(egidRow?.querySelector('dd')).toHaveTextContent('INT');
    expect(screen.getAllByText('VARCHAR').length).toBeGreaterThanOrEqual(2);
    expect(screen.getByText('schutzstatus')).toBeInTheDocument();
    expect(screen.getByText('3 columns')).toBeInTheDocument();
    expect(screen.getByText('2 rows')).toBeInTheDocument();
    expect(screen.getByLabelText('SQL bearbeiten')).toHaveAttribute(
      'data-table-columns',
      'egid,gemeindename,schutzstatus'
    );
    expect(screen.queryByText('Bereit')).not.toBeInTheDocument();
    expect(screen.queryByLabelText('Erkunden Status')).not.toBeInTheDocument();
    expect(screen.getByLabelText(/Tabelle ch_so_bauinventar: Tabelle geladen/)).toHaveAttribute(
      'title',
      'Parquet ist als lokaler DuckDB-View im Browser geladen.'
    );
    expect(mocks.connector.query).toHaveBeenCalledWith('DESCRIBE "ch_so_bauinventar";');
    expect(mocks.connector.query).toHaveBeenCalledWith('SELECT count(*) AS row_count FROM "ch_so_bauinventar";');
  });

  it('shows a centered overlay while Parquet files are being registered', async () => {
    let resolveRegistrations!: (value: Array<{table: typeof sampleExploreContext.tables[number]; status: 'registered'; sql: string}>) => void;
    const registrations = new Promise((resolve) => {
      resolveRegistrations = resolve;
    });
    mocks.registerParquetTables.mockReturnValue(registrations);

    render(<ExploreApp context={sampleExploreContext} />);

    await waitFor(() => {
      expect(screen.getByRole('status', {name: 'Erkunden Status'})).toHaveTextContent('Erkunden wird vorbereitet');
      expect(screen.getByRole('status', {name: 'Erkunden Status'})).toHaveTextContent('Parquet-Dateien werden registriert');
      expect(screen.getByRole('progressbar', {name: 'Ladevorgang'})).toBeInTheDocument();
    });
    expect(screen.queryByText('egid')).not.toBeInTheDocument();
    expect(screen.queryByText('gemeindename')).not.toBeInTheDocument();
    expect(screen.queryByText('0 columns')).not.toBeInTheDocument();
    expect(screen.queryByText("36'176 rows")).not.toBeInTheDocument();
    expect(screen.queryByText('2 rows')).not.toBeInTheDocument();
    expect(screen.getByLabelText('SQL bearbeiten')).toHaveAttribute('data-table-columns', '');

    resolveRegistrations!([
      {
        table: sampleExploreContext.tables[0],
        status: 'registered',
        sql: 'create view'
      }
    ]);

    expect(await screen.findByText('Tabelle geladen')).toBeInTheDocument();
    expect(screen.queryByLabelText('Erkunden Status')).not.toBeInTheDocument();
  });

  it('runs the initial registered-view query and renders result rows', async () => {
    const user = userEvent.setup();
    render(<ExploreApp context={sampleExploreContext} />);

    expect(await screen.findByText('Tabelle geladen')).toBeInTheDocument();
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
    expect(screen.queryByRole('progressbar', {name: 'Ladevorgang'})).not.toBeInTheDocument();
    expect(screen.getAllByText('Parquet-Datei konnte wegen CORS nicht im Browser geladen werden.').length).toBeGreaterThan(0);
    expect(screen.getAllByText(/CORS blocked/).length).toBeGreaterThan(0);
    expect(screen.getByText('Fehler')).toBeInTheDocument();
    expect(mocks.connector.query).not.toHaveBeenCalled();
  });

  it('keeps visible runtime metadata empty when runtime schema loading fails', async () => {
    const warn = vi.spyOn(console, 'warn').mockImplementation(() => undefined);
    mocks.connector.query.mockRejectedValue(new Error('DESCRIBE unavailable'));

    try {
      render(<ExploreApp context={sampleExploreContext} />);

      expect(await screen.findByText('Tabelle geladen')).toBeInTheDocument();
      expect(screen.queryByText('2 columns')).not.toBeInTheDocument();
      expect(screen.queryByText('0 columns')).not.toBeInTheDocument();
      expect(screen.queryByText("36'176 rows")).not.toBeInTheDocument();
      expect(screen.queryByText('2 rows')).not.toBeInTheDocument();
      expect(screen.queryByText('egid')).not.toBeInTheDocument();
      expect(screen.queryByText('gemeindename')).not.toBeInTheDocument();
      expect(screen.queryByText('schutzstatus')).not.toBeInTheDocument();
      expect(screen.getByLabelText('SQL bearbeiten')).toHaveAttribute('data-table-columns', '');
      expect(screen.queryByLabelText('Erkunden Status')).not.toBeInTheDocument();
      expect(warn).toHaveBeenCalledWith(
        'Explore runtime schema could not be read for table ch_so_bauinventar. Keeping visible schema empty.',
        expect.any(Error)
      );
    } finally {
      warn.mockRestore();
    }
  });

  it('keeps runtime columns visible but hides row count when runtime row-count loading fails', async () => {
    const warn = vi.spyOn(console, 'warn').mockImplementation(() => undefined);
    mocks.connector.query.mockImplementation((sql: string) => {
      if (/^DESCRIBE\b/i.test(sql)) {
        return Promise.resolve(tableFromArrays({
          column_name: ['egid', 'gemeindename', 'schutzstatus'],
          column_type: ['INTEGER', 'VARCHAR', 'VARCHAR']
        }));
      }
      if (/^SELECT count\(\*\) AS row_count FROM\b/i.test(sql)) {
        return Promise.reject(new Error('count unavailable'));
      }
      return Promise.resolve(tableFromArrays({
        egid: [1001, 1002],
        gemeindename: ['Solothurn', 'Olten']
      }));
    });

    try {
      render(<ExploreApp context={sampleExploreContext} />);

      expect(await screen.findByText('Tabelle geladen')).toBeInTheDocument();
      expect(screen.getByText('egid')).toBeInTheDocument();
      expect(screen.getByText('schutzstatus')).toBeInTheDocument();
      expect(screen.getByText('3 columns')).toBeInTheDocument();
      expect(screen.queryByText("36'176 rows")).not.toBeInTheDocument();
      expect(screen.queryByText('2 rows')).not.toBeInTheDocument();
      expect(warn).toHaveBeenCalledWith(
        'Explore runtime row count could not be read for table ch_so_bauinventar. Keeping visible row count empty.',
        expect.any(Error)
      );
    } finally {
      warn.mockRestore();
    }
  });

  it('renders an unavailable state without Parquet tables', () => {
    render(<ExploreApp context={{...sampleExploreContext, tables: [], recipes: [], codeSnippets: []}} />);

    expect(screen.getByRole('heading', {name: 'Bauinventar'})).toBeInTheDocument();
    expect(screen.getByText('Erkunden ist für dieses Datenthema noch nicht verfügbar, weil keine Parquet-Datei publiziert ist.')).toBeInTheDocument();
    expect(screen.getByRole('link', {name: 'Downloads und Metadaten auf der Datensatzseite anzeigen'})).toHaveAttribute('href', '/datasets/ch.so.bauinventar');
  });
});

function mockRuntimeSchema() {
  mocks.connector.query.mockImplementation((sql: string) => {
    if (/^DESCRIBE\b/i.test(sql)) {
      return Promise.resolve(tableFromArrays({
        column_name: ['egid', 'gemeindename', 'schutzstatus'],
        column_type: ['INTEGER', 'VARCHAR', 'VARCHAR']
      }));
    }
    if (/^SELECT count\(\*\) AS row_count FROM\b/i.test(sql)) {
      return Promise.resolve(tableFromArrays({row_count: [2]}));
    }
    return Promise.resolve(tableFromArrays({
      egid: [1001, 1002],
      gemeindename: ['Solothurn', 'Olten']
    }));
  });
}
