import {describe, expect, it} from 'vitest';
import {classifyExploreRuntimeError} from './ExploreRuntimeError';

describe('classifyExploreRuntimeError', () => {
  it('classifies CORS errors', () => {
    expect(classifyExploreRuntimeError(new Error('CORS blocked by policy'))).toMatchObject({
      summary: 'Parquet-Datei konnte wegen CORS nicht im Browser geladen werden.',
      detail: 'CORS blocked by policy'
    });
  });

  it('classifies range request errors', () => {
    expect(classifyExploreRuntimeError('Server does not support Accept-Ranges')).toMatchObject({
      summary: 'Parquet-Datei konnte nicht mit Byte-Range-Requests geladen werden.'
    });
  });

  it('classifies HTTP loading errors', () => {
    expect(classifyExploreRuntimeError('HTTP status code 404 while fetching file')).toMatchObject({
      summary: 'Parquet-Datei konnte über HTTP nicht geladen werden.'
    });
  });

  it('classifies browser IO errors as HTTP loading errors', () => {
    expect(classifyExploreRuntimeError('IO Error: No files found that match the pattern')).toMatchObject({
      summary: 'Parquet-Datei konnte über HTTP nicht geladen werden.'
    });
  });

  it('classifies DuckDB-Wasm startup errors', () => {
    expect(classifyExploreRuntimeError('WebAssembly worker failed')).toMatchObject({
      summary: 'DuckDB-Wasm konnte in diesem Browser nicht gestartet werden.'
    });
  });
});
