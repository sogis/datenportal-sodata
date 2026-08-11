import {describe, expect, it} from 'vitest';
import {createLocalDuckDbBundles} from './duckdbBundles';

describe('createLocalDuckDbBundles', () => {
  it('contains exactly the mvp runtime bundle', () => {
    const bundles = createLocalDuckDbBundles();

    expect(Object.keys(bundles)).toEqual(['mvp']);
    expect(bundles.mvp.mainModule).toContain('duckdb-mvp.wasm');
    expect(bundles.mvp.mainWorker).toContain('duckdb-browser-mvp.worker.js');
    expect(JSON.stringify(bundles)).not.toMatch(/duckdb-(eh|coi)|coi\.pthread/);
  });
});
