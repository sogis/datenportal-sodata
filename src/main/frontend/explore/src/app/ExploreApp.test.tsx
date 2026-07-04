import {render, screen, waitFor} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {tableFromArrays} from 'apache-arrow';
import {beforeEach, describe, expect, it, vi} from 'vitest';
import type {DataTable, DbSchemaNode} from '@sqlrooms/duckdb';
import {ExploreApp} from './ExploreApp';
import {sampleExploreContext} from '../test/sampleExploreContext';

const mocks = vi.hoisted(() => {
  const connection = {
    query: vi.fn(),
    cancelSent: vi.fn()
  };
  const connector = {
    type: 'wasm',
    query: vi.fn(),
    getConnection: vi.fn(() => connection),
    getDb: vi.fn(() => ({
      dropFile: vi.fn(),
      registerFileBuffer: vi.fn()
    }))
  };
  return {
    connector,
    connection,
    initialize: vi.fn(),
    destroy: vi.fn(),
    getConnector: vi.fn(),
    refreshTableSchemas: vi.fn(),
    attachCatalogDatabase: vi.fn(),
    schemaTrees: [] as DbSchemaNode[],
    isRefreshingTableSchemas: false
  };
});

vi.mock('../duckdb/createExploreRoomStore', () => ({
  createExploreRoomStore: () => ({
    roomStore: {
      getState: () => ({
        db: {
          initialize: mocks.initialize,
          destroy: mocks.destroy,
          getConnector: mocks.getConnector,
          refreshTableSchemas: mocks.refreshTableSchemas
        }
      })
    },
    useRoomStore: (selector: (state: {db: {schemaTrees: DbSchemaNode[]; isRefreshingTableSchemas: boolean}}) => unknown) =>
      selector({
        db: {
          schemaTrees: mocks.schemaTrees,
          isRefreshingTableSchemas: mocks.isRefreshingTableSchemas
        }
      })
  })
}));

vi.mock('../duckdb/attachCatalogDatabase', () => ({
  attachCatalogDatabase: mocks.attachCatalogDatabase
}));

describe('ExploreApp', () => {
  beforeEach(() => {
    mocks.connector.query.mockReset().mockResolvedValue(tableFromArrays({
      egid: [1001, 1002],
      gemeindename: ['Solothurn', 'Olten']
    }));
    mocks.connection.query.mockReset().mockResolvedValue(tableFromArrays({
      egid: [1001, 1002],
      gemeindename: ['Solothurn', 'Olten']
    }));
    mocks.connection.cancelSent.mockReset().mockResolvedValue(false);
    mocks.connector.getConnection.mockClear();
    mocks.connector.getDb.mockClear();
    mocks.initialize.mockReset().mockResolvedValue(undefined);
    mocks.destroy.mockReset().mockResolvedValue(undefined);
    mocks.getConnector.mockReset().mockResolvedValue(mocks.connector);
    mocks.refreshTableSchemas.mockReset().mockResolvedValue(sampleCatalogTables());
    mocks.attachCatalogDatabase.mockReset().mockResolvedValue({
      status: 'registered',
      url: '/catalog/catalog.duckdb',
      database: 'catalog',
      schema: 'opendata'
    });
    mocks.schemaTrees = sampleSchemaTrees();
    mocks.isRefreshingTableSchemas = false;
  });

  it('initializes DuckDB, attaches the catalog and renders the schema explorer workbench', async () => {
    render(<ExploreApp context={sampleExploreContext} />);

    expect(screen.getByLabelText('Erkunden SQL-Labor')).toBeInTheDocument();
    expect(screen.getByRole('status', {name: 'Erkunden Status'})).toHaveTextContent('Erkunden wird vorbereitet');
    expect(screen.getByRole('status', {name: 'Erkunden Status'})).toHaveTextContent('DuckDB wird initialisiert');
    expect(screen.getByRole('progressbar', {name: 'Ladevorgang'})).toBeInTheDocument();
    expect(screen.getByLabelText('Daten und Schema')).toBeInTheDocument();
    expect(screen.getByLabelText('Schema und SQL-Labor Grösse anpassen')).toBeInTheDocument();
    expect(screen.getByLabelText('SQL bearbeiten')).toHaveValue('SELECT *\nFROM opendata.ch_so_bauinventar;');

    expect(await screen.findByText('SCHEMA EXPLORER')).toBeInTheDocument();
    expect(await screen.findByText('catalog')).toBeInTheDocument();
    expect(screen.getByText('opendata')).toBeInTheDocument();
    expect(screen.getByRole('treeitem', {selected: true})).toHaveTextContent('ch_so_bauinventar');
    expect(screen.getByText(/36.?176 rows/)).toBeInTheDocument();
    expect(screen.getByText('integer')).toBeInTheDocument();
    expect(screen.getAllByText('varchar').length).toBeGreaterThanOrEqual(2);
    expect(screen.getByText('schutzstatus')).toBeInTheDocument();
    expect(screen.getByLabelText('SQL bearbeiten')).toHaveAttribute(
      'data-table-columns',
      'egid,gemeindename,schutzstatus'
    );
    expect(screen.queryByLabelText('Erkunden Status')).not.toBeInTheDocument();
    expect(mocks.attachCatalogDatabase).toHaveBeenCalledWith(mocks.connector, sampleExploreContext.catalogDatabase);
    expect(mocks.refreshTableSchemas).toHaveBeenCalledTimes(1);
  });

  it('shows a centered overlay while the DuckDB catalog is being attached', async () => {
    let resolveRegistration!: (value: {status: 'registered'; url: string; database: string; schema: string}) => void;
    const registration = new Promise<{status: 'registered'; url: string; database: string; schema: string}>((resolve) => {
      resolveRegistration = resolve;
    });
    mocks.attachCatalogDatabase.mockReturnValue(registration);

    render(<ExploreApp context={sampleExploreContext} />);

    await waitFor(() => {
      expect(screen.getByRole('status', {name: 'Erkunden Status'})).toHaveTextContent('DuckDB-Catalog wird geladen');
    });
    expect(screen.getByRole('progressbar', {name: 'Ladevorgang'})).toBeInTheDocument();
    expect(screen.getByLabelText('SQL bearbeiten')).toHaveAttribute('data-table-columns', '');

    resolveRegistration!({
      status: 'registered',
      url: '/catalog/catalog.duckdb',
      database: 'catalog',
      schema: 'opendata'
    });

    expect(await screen.findByText('schutzstatus')).toBeInTheDocument();
    expect(screen.queryByLabelText('Erkunden Status')).not.toBeInTheDocument();
  });

  it('refreshes SQLRooms schema trees from the explorer header action', async () => {
    const user = userEvent.setup();
    render(<ExploreApp context={sampleExploreContext} />);

    expect(await screen.findByText('SCHEMA EXPLORER')).toBeInTheDocument();
    await user.click(screen.getByRole('button', {name: 'Schema Explorer aktualisieren'}));

    expect(mocks.refreshTableSchemas).toHaveBeenCalledTimes(2);
  });

  it('runs the initial catalog-view query and renders result rows', async () => {
    const user = userEvent.setup();
    render(<ExploreApp context={sampleExploreContext} />);

    expect(await screen.findByText('SCHEMA EXPLORER')).toBeInTheDocument();
    await user.click(screen.getByRole('button', {name: 'Ausführen'}));

    expect(await screen.findByLabelText('SQL Ergebnis')).toBeInTheDocument();
    expect(screen.getByText('Solothurn')).toBeInTheDocument();
    expect(screen.getByText('Olten')).toBeInTheDocument();
    expect(mocks.connection.query).toHaveBeenLastCalledWith(expect.stringContaining('limit 1000'));
  });

  it('shows catalog attach errors without crashing the workbench', async () => {
    mocks.attachCatalogDatabase.mockResolvedValue({
      status: 'failed',
      url: '/catalog/catalog.duckdb',
      database: 'catalog',
      schema: 'opendata',
      error: 'DuckDB catalog could not be attached: network unavailable'
    });

    render(<ExploreApp context={sampleExploreContext} />);

    expect(await screen.findByText('DuckDB-Hinweis')).toBeInTheDocument();
    expect(screen.getByRole('alert', {name: 'Erkunden Status'})).toHaveTextContent('DuckDB-Hinweis');
    expect(screen.queryByRole('progressbar', {name: 'Ladevorgang'})).not.toBeInTheDocument();
    expect(screen.getAllByText('DuckDB-Catalog konnte im Browser nicht geladen werden.').length).toBeGreaterThan(0);
    expect(screen.getAllByText(/network unavailable/).length).toBeGreaterThan(0);
    expect(mocks.refreshTableSchemas).not.toHaveBeenCalled();
  });

  it('renders an unavailable state without Parquet tables', () => {
    render(<ExploreApp context={{...sampleExploreContext, tables: [], recipes: [], codeSnippets: []}} />);

    expect(screen.getByRole('heading', {name: 'Bauinventar'})).toBeInTheDocument();
    expect(screen.getByText('Erkunden ist für dieses Datenthema noch nicht verfügbar, weil keine Parquet-Datei publiziert ist.')).toBeInTheDocument();
    expect(screen.getByRole('link', {name: 'Downloads und Metadaten auf der Datensatzseite anzeigen'})).toHaveAttribute('href', '/datasets/ch.so.bauinventar');
  });
});

function sampleCatalogTables(): DataTable[] {
  return [
    {
      table: {
        database: 'catalog',
        schema: 'opendata',
        table: 'ch_so_bauinventar',
        toString: () => 'catalog.opendata.ch_so_bauinventar'
      },
      database: 'catalog',
      schema: 'opendata',
      tableName: 'ch_so_bauinventar',
      isView: true,
      columns: [
        {name: 'egid', type: 'INTEGER'},
        {name: 'gemeindename', type: 'VARCHAR'},
        {name: 'schutzstatus', type: 'VARCHAR'}
      ]
    }
  ] as DataTable[];
}

function sampleSchemaTrees(): DbSchemaNode[] {
  return [
    {
      key: 'database:catalog',
      object: {
        type: 'database',
        name: 'catalog'
      },
      children: [
        {
          key: 'schema:opendata',
          object: {
            type: 'schema',
            name: 'opendata'
          },
          children: [
            {
              key: 'table:ch_so_bauinventar',
              object: {
                type: 'table',
                name: 'ch_so_bauinventar',
                ...sampleCatalogTables()[0]
              },
              children: [
                {
                  key: 'column:egid',
                  object: {
                    type: 'column',
                    name: 'egid',
                    columnType: 'INTEGER'
                  }
                },
                {
                  key: 'column:gemeindename',
                  object: {
                    type: 'column',
                    name: 'gemeindename',
                    columnType: 'VARCHAR'
                  }
                },
                {
                  key: 'column:schutzstatus',
                  object: {
                    type: 'column',
                    name: 'schutzstatus',
                    columnType: 'VARCHAR'
                  }
                }
              ]
            }
          ]
        }
      ]
    }
  ] as DbSchemaNode[];
}
