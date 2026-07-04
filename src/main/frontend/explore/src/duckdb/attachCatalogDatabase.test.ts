import {afterEach, beforeEach, describe, expect, it, vi} from 'vitest';
import type {DuckDbConnector} from '@sqlrooms/duckdb';
import {attachCatalogDatabase, resolveCatalogUrl} from './attachCatalogDatabase';
import {sampleExploreContext} from '../test/sampleExploreContext';

describe('attachCatalogDatabase', () => {
  beforeEach(() => {
    vi.stubGlobal('fetch', vi.fn());
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('fetches the catalog, registers it in DuckDB-Wasm and attaches it read-only', async () => {
    const wasmDb = {
      dropFile: vi.fn().mockResolvedValue(undefined),
      registerFileBuffer: vi.fn().mockResolvedValue(undefined)
    };
    const connection = {
      query: vi.fn().mockResolvedValue(undefined)
    };
    const connector = {
      type: 'wasm',
      getDb: vi.fn(() => wasmDb),
      getConnection: vi.fn(() => connection)
    } as unknown as DuckDbConnector;
    vi.mocked(fetch).mockResolvedValue({
      ok: true,
      arrayBuffer: async () => new Uint8Array([1, 2, 3]).buffer
    } as Response);

    const registration = await attachCatalogDatabase(
      connector,
      sampleExploreContext.catalogDatabase,
      'http://localhost:8080/datasets/ch.so.bauinventar/explore'
    );

    expect(registration).toEqual({
      status: 'registered',
      url: 'http://localhost:8080/catalog/catalog.duckdb',
      database: 'catalog',
      schema: 'opendata'
    });
    expect(fetch).toHaveBeenCalledWith('http://localhost:8080/catalog/catalog.duckdb', {credentials: 'same-origin'});
    expect(wasmDb.dropFile).toHaveBeenCalledWith('catalog.duckdb');
    expect(wasmDb.registerFileBuffer).toHaveBeenCalledWith('catalog.duckdb', new Uint8Array([1, 2, 3]));
    expect(connection.query).toHaveBeenNthCalledWith(1, 'INSTALL httpfs;');
    expect(connection.query).toHaveBeenNthCalledWith(2, 'LOAD httpfs;');
    expect(connection.query).toHaveBeenNthCalledWith(3, 'ATTACH \'catalog.duckdb\' AS "catalog" (READ_ONLY);');
    expect(connection.query).toHaveBeenNthCalledWith(4, 'USE "catalog"."opendata";');
  });

  it('returns a failed registration when the catalog download is unavailable', async () => {
    const getConnection = vi.fn();
    const connector = {
      type: 'wasm',
      getDb: vi.fn(),
      getConnection
    } as unknown as DuckDbConnector;
    vi.mocked(fetch).mockResolvedValue({
      ok: false,
      status: 404
    } as Response);

    const registration = await attachCatalogDatabase(connector, sampleExploreContext.catalogDatabase);

    expect(registration.status).toBe('failed');
    expect(registration.error).toContain('DuckDB catalog could not be attached');
    expect(registration.error).toContain('HTTP 404');
    expect(getConnection).not.toHaveBeenCalled();
  });

  it('resolves relative catalog URLs against the page URL', () => {
    expect(resolveCatalogUrl('/catalog/catalog.duckdb', 'http://localhost:8080/datasets/test/explore')).toBe(
      'http://localhost:8080/catalog/catalog.duckdb'
    );
  });
});
