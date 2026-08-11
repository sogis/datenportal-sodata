import {isWasmDuckDbConnector, type DuckDbConnector} from '@sqlrooms/duckdb';
import type {ExploreCatalogDatabaseDto} from '../app/ExploreContext';

export type CatalogDatabaseRegistrationStatus = 'pending' | 'registered' | 'failed';

export interface CatalogDatabaseRegistration {
  status: CatalogDatabaseRegistrationStatus;
  url: string;
  database: string;
  schema: string;
  error?: string;
}

const CATALOG_FILE_NAME = 'catalog.duckdb';

export async function attachCatalogDatabase(
  connector: DuckDbConnector,
  catalogDatabase: ExploreCatalogDatabaseDto,
  baseUrl: string = globalThis.location?.href ?? 'http://localhost/',
  signal?: AbortSignal
): Promise<CatalogDatabaseRegistration> {
  const url = resolveCatalogUrl(catalogDatabase.url, baseUrl);
  try {
    if (!isWasmDuckDbConnector(connector)) {
      throw new Error('DuckDB-Wasm connector is required to attach the catalog database.');
    }

    const fetchOptions: RequestInit = {credentials: 'same-origin'};
    if (signal) {
      fetchOptions.signal = signal;
    }
    const response = await fetch(url, fetchOptions);
    if (!response.ok) {
      throw new Error(`DuckDB catalog download failed with HTTP ${response.status}.`);
    }

    const bytes = new Uint8Array(await response.arrayBuffer());
    const wasmDb = connector.getDb();
    await wasmDb.dropFile(CATALOG_FILE_NAME).catch(() => undefined);
    await wasmDb.registerFileBuffer(CATALOG_FILE_NAME, bytes);

    const connection = connector.getConnection();
    await connection.query('INSTALL httpfs;');
    await connection.query('LOAD httpfs;');
    await connection.query(
      `ATTACH '${escapeSqlStringLiteral(CATALOG_FILE_NAME)}' AS ${quoteIdentifier(catalogDatabase.database)} (READ_ONLY);`
    );
    await connection.query(`USE ${quoteIdentifier(catalogDatabase.database)}.${quoteIdentifier(catalogDatabase.schema)};`);

    return {
      status: 'registered',
      url,
      database: catalogDatabase.database,
      schema: catalogDatabase.schema
    };
  } catch (error) {
    return {
      status: 'failed',
      url,
      database: catalogDatabase.database,
      schema: catalogDatabase.schema,
      error: `DuckDB catalog could not be attached: ${toErrorMessage(error)}`
    };
  }
}

export function resolveCatalogUrl(url: string, baseUrl: string = globalThis.location?.href ?? 'http://localhost/'): string {
  return new URL(url, baseUrl).href;
}

function quoteIdentifier(identifier: string): string {
  return `"${identifier.replace(/"/g, '""')}"`;
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
