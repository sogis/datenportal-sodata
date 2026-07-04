import {isWasmDuckDbConnector, type DuckDbConnector, type QueryHandle, type QueryOptions} from '@sqlrooms/duckdb';
import type {Table} from 'apache-arrow';

export function executeDuckDbQuery(
  connector: DuckDbConnector,
  sql: string,
  options?: QueryOptions
): QueryHandle<Table> {
  if (!isWasmDuckDbConnector(connector)) {
    return connector.query(sql, options);
  }

  const connection = connector.getConnection();
  const controller = new AbortController();
  const queryPromise = connection.query(sql);

  const abortPromise = new Promise<never>((_, reject) => {
    controller.signal.addEventListener('abort', () => reject(abortError()), {once: true});
    options?.signal?.addEventListener(
      'abort',
      () => {
        controller.abort();
        void connection.cancelSent().catch(() => undefined);
      },
      {once: true}
    );
    if (options?.signal?.aborted) {
      controller.abort();
    }
  });

  const result = Promise.race([queryPromise, abortPromise]);
  queryPromise.catch(() => undefined);

  return {
    result,
    signal: controller.signal,
    cancel: async () => {
      if (!controller.signal.aborted) {
        controller.abort();
      }
      await connection.cancelSent().catch(() => undefined);
    },
    then: result.then.bind(result),
    catch: result.catch.bind(result),
    finally: result.finally.bind(result)
  };
}

function abortError(): Error {
  const error = new Error('Query cancelled.');
  error.name = 'AbortError';
  return error;
}
