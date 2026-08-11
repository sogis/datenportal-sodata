import type {DuckDBBundles} from '@duckdb/duckdb-wasm';
import duckdbMvpWorker from '@duckdb/duckdb-wasm/dist/duckdb-browser-mvp.worker.js?url';
import duckdbMvpWasm from '@duckdb/duckdb-wasm/dist/duckdb-mvp.wasm?url';

export function createLocalDuckDbBundles(): DuckDBBundles {
  return {
    mvp: {
      mainModule: duckdbMvpWasm,
      mainWorker: duckdbMvpWorker
    }
  };
}
