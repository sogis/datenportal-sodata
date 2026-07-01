import {describe, expect, it, vi} from 'vitest';
import type {DuckDbConnector} from '@sqlrooms/duckdb';
import {buildCreateViewSql, registerParquetTables} from './registerParquetTables';
import {sampleExploreContext} from '../test/sampleExploreContext';

describe('registerParquetTables', () => {
  it('builds create view SQL with escaped absolute Parquet URL', () => {
    const table = {
      ...sampleExploreContext.tables[0],
      parquetUrl: "/fixtures/o'hara.parquet"
    };

    expect(buildCreateViewSql(table, 'http://localhost:8080/datasets/test/explore')).toBe(`create or replace view ch_so_bauinventar as
select *
from read_parquet('http://localhost:8080/fixtures/o''hara.parquet');`);
  });

  it('rejects unsafe table names', () => {
    const table = {
      ...sampleExploreContext.tables[0],
      name: 'unsafe-name'
    };

    expect(() => buildCreateViewSql(table)).toThrow('Unsafe table name');
  });

  it('records per-table success and failure states', async () => {
    const connector = {
      query: vi.fn()
        .mockResolvedValueOnce({})
        .mockRejectedValueOnce(new Error('CORS blocked'))
    } as unknown as DuckDbConnector;
    const tables = [
      sampleExploreContext.tables[0],
      {
        ...sampleExploreContext.tables[0],
        id: 'second',
        name: 'second_table',
        parquetUrl: '/fixtures/second.parquet'
      }
    ];

    const results = await registerParquetTables(connector, tables, 'http://localhost:8080/datasets/test/explore');

    expect(results).toHaveLength(2);
    expect(results[0]).toMatchObject({status: 'registered'});
    expect(results[1]).toMatchObject({status: 'failed', error: 'CORS blocked'});
    expect(connector.query).toHaveBeenCalledTimes(2);
  });
});
