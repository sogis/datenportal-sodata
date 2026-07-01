import type {DuckDBBundles} from '@duckdb/duckdb-wasm';
import duckdbMvpWorker from '@duckdb/duckdb-wasm/dist/duckdb-browser-mvp.worker.js?url';
import duckdbEhWorker from '@duckdb/duckdb-wasm/dist/duckdb-browser-eh.worker.js?url';
import duckdbCoiWorker from '@duckdb/duckdb-wasm/dist/duckdb-browser-coi.worker.js?url';
import duckdbCoiPthreadWorker from '@duckdb/duckdb-wasm/dist/duckdb-browser-coi.pthread.worker.js?url';
import duckdbMvpWasm from '@duckdb/duckdb-wasm/dist/duckdb-mvp.wasm?url';
import duckdbEhWasm from '@duckdb/duckdb-wasm/dist/duckdb-eh.wasm?url';
import duckdbCoiWasm from '@duckdb/duckdb-wasm/dist/duckdb-coi.wasm?url';

export function createLocalDuckDbBundles(): DuckDBBundles {
  return {
    mvp: {
      mainModule: duckdbMvpWasm,
      mainWorker: duckdbMvpWorker
    }
  };
}

export const bundledDuckDbAssetUrls = {
  eh: {
    mainModule: duckdbEhWasm,
    mainWorker: duckdbEhWorker
  },
  coi: {
    mainModule: duckdbCoiWasm,
    mainWorker: duckdbCoiWorker,
    pthreadWorker: duckdbCoiPthreadWorker
  }
};
