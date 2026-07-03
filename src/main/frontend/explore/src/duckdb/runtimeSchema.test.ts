import {tableFromArrays} from 'apache-arrow';
import {describe, expect, it, vi} from 'vitest';
import type {DuckDbConnector} from '@sqlrooms/duckdb';
import type {ExploreColumnDto} from '../app/ExploreContext';
import {sampleExploreContext} from '../test/sampleExploreContext';
import {
  buildDescribeTableSql,
  loadRuntimeSchemas,
  mergeRuntimeColumns,
  parseDescribeColumns
} from './runtimeSchema';

describe('runtimeSchema', () => {
  it('builds DESCRIBE SQL with a quoted table identifier', () => {
    expect(buildDescribeTableSql({
      ...sampleExploreContext.tables[0],
      name: 'table"with_quote'
    })).toBe('DESCRIBE "table""with_quote";');
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

  it('keeps the schema empty when a runtime schema cannot be read', async () => {
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

    expect(tables).toEqual([{...sampleExploreContext.tables[0], columns: []}]);
    expect(onError).toHaveBeenCalledWith(sampleExploreContext.tables[0], expect.any(Error));
  });
});
