import {describe, expect, it} from 'vitest';
import {classifyExploreQueryError, classifyExploreRuntimeError, isSourceQueryError} from './ExploreRuntimeError';

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

describe('classifyExploreQueryError', () => {
  const neutralSummary =
    'Quelldatei nicht erreichbar. Die zugrunde liegende Datendatei konnte momentan nicht geladen werden. Bitte versuchen Sie es später erneut.';

  it('classifies missing source files with the neutral query message', () => {
    const error = classifyExploreQueryError('IO Error: No files found that match the pattern "/explore-fixtures/missing.parquet"');

    expect(error).toMatchObject({
      kind: 'source-unavailable',
      summary: neutralSummary,
      detail: 'IO Error: No files found that match the pattern "/explore-fixtures/missing.parquet"'
    });
    expect(isSourceQueryError(error)).toBe(true);
  });

  it('classifies browser fetch and HTTP errors as source query errors', () => {
    expect(classifyExploreQueryError('Failed to fetch')).toMatchObject({
      kind: 'source-unavailable',
      summary: neutralSummary
    });
    expect(classifyExploreQueryError('_setThrew is not defined')).toMatchObject({
      kind: 'source-unavailable',
      summary: neutralSummary
    });
    expect(classifyExploreQueryError('HTTP status code 404 while fetching file')).toMatchObject({
      kind: 'http',
      summary: neutralSummary
    });
  });

  it('classifies CORS and Range request errors without exposing them as the main message', () => {
    expect(classifyExploreQueryError(new Error('CORS blocked by policy'))).toMatchObject({
      kind: 'cors',
      summary: neutralSummary,
      detail: 'CORS blocked by policy'
    });
    expect(classifyExploreQueryError('Server does not support Accept-Ranges')).toMatchObject({
      kind: 'range',
      summary: neutralSummary
    });
  });

  it('keeps generic SQL errors specific', () => {
    expect(classifyExploreQueryError('Binder Error: Referenced column "foo" not found')).toMatchObject({
      kind: 'sql',
      summary: 'Binder Error: Referenced column "foo" not found'
    });
  });
});
