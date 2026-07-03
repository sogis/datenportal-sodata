import {tableFromArrays} from 'apache-arrow';
import {describe, expect, it, vi} from 'vitest';
import type {DuckDbConnector} from '@sqlrooms/duckdb';
import type {ExploreColumnDto} from '../app/ExploreContext';
import {sampleExploreContext} from '../test/sampleExploreContext';
import {
  buildCountRowsSql,
  buildDescribeTableSql,
  loadRuntimeSchemas,
  mergeRuntimeColumns,
  parseDescribeColumns,
  parseRuntimeRowCount
} from './runtimeSchema';

describe('runtimeSchema', () => {
  it('builds DESCRIBE SQL with a quoted table identifier', () => {
    expect(buildDescribeTableSql({
      ...sampleExploreContext.tables[0],
      name: 'table"with_quote'
    })).toBe('DESCRIBE "table""with_quote";');
  });

  it('builds row-count SQL with a quoted table identifier', () => {
    expect(buildCountRowsSql({
      ...sampleExploreContext.tables[0],
      name: 'table"with_quote'
    })).toBe('SELECT count(*) AS row_count FROM "table""with_quote";');
  });

  it('parses DuckDB DESCRIBE columns', () => {
    const table = tableFromArrays({
      column_name: ['egid', 'schutzstatus'],
      column_type: ['INTEGER', 'VARCHAR'],
      null: ['YES', 'YES']
    });

    expect(parseDescribeColumns(table)).toEqual([
      {name: 'egid', type: 'INTEGER'},
      {name: 'schutzstatus', type: 'VARCHAR'}
    ]);
  });

  it('parses DuckDB row count results', () => {
    expect(parseRuntimeRowCount(tableFromArrays({row_count: [2]}))).toBe(2);
    expect(parseRuntimeRowCount(tableFromArrays({row_count: ['36176']}))).toBe(36176);
    expect(parseRuntimeRowCount(tableFromArrays({'count_star()': ['2']}))).toBe(2);
  });

  it('merges stale catalog metadata into the runtime schema', () => {
    const catalogColumns: ExploreColumnDto[] = [
      {
        name: 'egid',
        type: 'INTEGER',
        required: true,
        description: 'Gebäude-ID',
        example: '1001',
        roles: ['identifier']
      },
      {
        name: 'metadata_only',
        type: 'VARCHAR',
        description: 'Veraltete Katalogspalte',
        roles: ['label']
      }
    ];

    const merged = mergeRuntimeColumns(catalogColumns, [
      {name: 'egid', type: 'BIGINT'},
      {name: 'schutzstatus', type: 'VARCHAR'}
    ]);

    expect(merged).toEqual([
      {
        name: 'egid',
        type: 'BIGINT',
        nullable: undefined,
        required: true,
        description: 'Gebäude-ID',
        example: '1001',
        roles: ['identifier']
      },
      {
        name: 'schutzstatus',
        type: 'VARCHAR',
        nullable: undefined,
        required: undefined,
        description: undefined,
        example: undefined,
        roles: ['unknown']
      }
    ]);
    expect(merged).not.toContainEqual(expect.objectContaining({name: 'metadata_only'}));
  });

  it('loads runtime schema and row count for registered tables', async () => {
    const connector = {
      query: vi.fn()
        .mockResolvedValueOnce(tableFromArrays({
          column_name: ['egid', 'schutzstatus'],
          column_type: ['INTEGER', 'VARCHAR']
        }))
        .mockResolvedValueOnce(tableFromArrays({row_count: [2]}))
    } as unknown as DuckDbConnector;

    const tables = await loadRuntimeSchemas(connector, [
      {
        table: sampleExploreContext.tables[0],
        status: 'registered',
        sql: 'create view'
      }
    ]);

    expect(tables[0].columns).toEqual([
      {
        name: 'egid',
        type: 'INTEGER',
        nullable: undefined,
        required: undefined,
        description: undefined,
        example: undefined,
        roles: ['identifier']
      },
      {
        name: 'schutzstatus',
        type: 'VARCHAR',
        nullable: undefined,
        required: undefined,
        description: undefined,
        example: undefined,
        roles: ['unknown']
      }
    ]);
    expect(tables[0].rowCountEstimate).toBe(2);
    expect(connector.query).toHaveBeenNthCalledWith(1, 'DESCRIBE "ch_so_bauinventar";');
    expect(connector.query).toHaveBeenNthCalledWith(2, 'SELECT count(*) AS row_count FROM "ch_so_bauinventar";');
  });

  it('keeps runtime columns visible when the row count cannot be read', async () => {
    const connector = {
      query: vi.fn()
        .mockResolvedValueOnce(tableFromArrays({
          column_name: ['egid'],
          column_type: ['INTEGER']
        }))
        .mockRejectedValueOnce(new Error('count failed'))
    } as unknown as DuckDbConnector;
    const onError = vi.fn();

    const tables = await loadRuntimeSchemas(connector, [
      {
        table: sampleExploreContext.tables[0],
        status: 'registered',
        sql: 'create view'
      }
    ], onError);

    expect(tables[0].columns).toEqual([
      expect.objectContaining({name: 'egid', type: 'INTEGER', roles: ['identifier']})
    ]);
    expect(tables[0].rowCountEstimate).toBeUndefined();
    expect(onError).toHaveBeenCalledWith(sampleExploreContext.tables[0], expect.any(Error), 'rowCount');
  });

  it('keeps the runtime metadata empty when a runtime schema cannot be read', async () => {
    const connector = {
      query: vi.fn().mockRejectedValue(new Error('DESCRIBE failed'))
    } as unknown as DuckDbConnector;
    const onError = vi.fn();

    const tables = await loadRuntimeSchemas(connector, [
      {
        table: sampleExploreContext.tables[0],
        status: 'registered',
        sql: 'create view'
      }
    ], onError);

    expect(tables).toEqual([{...sampleExploreContext.tables[0], columns: [], rowCountEstimate: undefined}]);
    expect(onError).toHaveBeenCalledWith(sampleExploreContext.tables[0], expect.any(Error), 'schema');
  });
});
