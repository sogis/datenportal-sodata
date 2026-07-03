import {describe, expect, it} from 'vitest';
import {bundledDuckDbAssetUrls, createLocalDuckDbBundles} from './duckdbBundles';

describe('createLocalDuckDbBundles', () => {
  it('keeps the runtime on mvp while eh remains available as a bundled asset', () => {
    const bundles = createLocalDuckDbBundles();

    expect(bundles.mvp.mainModule).toContain('duckdb-mvp.wasm');
    expect(bundles.mvp.mainWorker).toContain('duckdb-browser-mvp.worker.js');
    expect(bundles.eh).toBeUndefined();
    expect(bundles.coi).toBeUndefined();
    expect(bundledDuckDbAssetUrls.eh.mainModule).toContain('duckdb-eh.wasm');
    expect(bundledDuckDbAssetUrls.eh.mainWorker).toContain('duckdb-browser-eh.worker.js');
  });
});
