import type {DuckDbConnector} from '@sqlrooms/duckdb';
import type {ExploreTableDto} from '../app/ExploreContext';

const SAFE_TABLE_NAME_PATTERN = /^[a-z_][a-z0-9_]*$/;

export type RegistrationStatus = 'pending' | 'registered' | 'failed';

export interface RegisteredTable {
  table: ExploreTableDto;
  status: RegistrationStatus;
  sql: string;
  error?: string;
}

export async function registerParquetTables(
  db: DuckDbConnector,
  tables: ExploreTableDto[],
  baseUrl: string = globalThis.location?.href ?? 'http://localhost/'
): Promise<RegisteredTable[]> {
  const results: RegisteredTable[] = [];

  for (const table of tables) {
    const sql = buildCreateViewSql(table, baseUrl);
    try {
      await db.query(sql);
      results.push({table, status: 'registered', sql});
    } catch (error) {
      results.push({
        table,
        status: 'failed',
        sql,
        error: toErrorMessage(error)
      });
    }
  }

  return results;
}

export function buildCreateViewSql(
  table: ExploreTableDto,
  baseUrl: string = globalThis.location?.href ?? 'http://localhost/'
): string {
  assertSafeTableName(table.name);
  const url = resolveParquetUrl(table.parquetUrl, baseUrl);
  return `create or replace view ${table.name} as
select *
from read_parquet('${escapeSqlStringLiteral(url)}');`;
}

export function assertSafeTableName(tableName: string): void {
  if (!SAFE_TABLE_NAME_PATTERN.test(tableName)) {
    throw new Error(`Unsafe table name for DuckDB registration: ${tableName}`);
  }
}

export function resolveParquetUrl(url: string, baseUrl: string = globalThis.location?.href ?? 'http://localhost/'): string {
  return new URL(url, baseUrl).href;
}

function escapeSqlStringLiteral(value: string): string {
  return value.replace(/'/g, "''");
}

function toErrorMessage(error: unknown): string {
  if (error instanceof Error) {
    return error.message;
  }
  return String(error);
}
