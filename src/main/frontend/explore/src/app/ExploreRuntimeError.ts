export interface ExploreRuntimeError {
  summary: string;
  detail?: string;
}

export type ExploreQueryErrorKind = 'source-unavailable' | 'cors' | 'range' | 'http' | 'parquet' | 'duckdb' | 'sql';

export interface ExploreQueryError {
  kind: ExploreQueryErrorKind;
  summary: string;
  detail?: string;
}

const SOURCE_UNAVAILABLE_SUMMARY =
  'Quelldatei nicht erreichbar. Die zugrunde liegende Datendatei konnte momentan nicht geladen werden. Bitte versuchen Sie es später erneut.';
const QUERY_UNAVAILABLE_SUMMARY = 'Die Abfrage konnte momentan nicht ausgeführt werden.';

export function classifyExploreRuntimeError(error: unknown): ExploreRuntimeError {
  const detail = toErrorMessage(error);
  const normalized = detail.toLowerCase();

  if (normalized.includes('catalog')) {
    return {
      summary: 'DuckDB-Catalog konnte im Browser nicht geladen werden.',
      detail
    };
  }

  if (normalized.includes('cors') || normalized.includes('cross-origin')) {
    return {
      summary: 'Parquet-Datei konnte wegen CORS nicht im Browser geladen werden.',
      detail
    };
  }

  if (normalized.includes('range') || normalized.includes('accept-ranges') || normalized.includes('status code 416')) {
    return {
      summary: 'Parquet-Datei konnte nicht mit Byte-Range-Requests geladen werden.',
      detail
    };
  }

  if (
    normalized.includes('404') ||
    normalized.includes('403') ||
    normalized.includes('not found') ||
    normalized.includes('no files found') ||
    normalized.includes('io error') ||
    normalized.includes('failed to fetch') ||
    normalized.includes('network') ||
    normalized.includes('http')
  ) {
    return {
      summary: 'Parquet-Datei konnte über HTTP nicht geladen werden.',
      detail
    };
  }

  if (
    normalized.includes('parquet') ||
    normalized.includes('read_parquet') ||
    normalized.includes('duckdb_extension')
  ) {
    return {
      summary: 'Parquet-Datei konnte von DuckDB-Wasm nicht geöffnet werden.',
      detail
    };
  }

  if (normalized.includes('wasm') || normalized.includes('webassembly') || normalized.includes('worker')) {
    return {
      summary: 'DuckDB-Wasm konnte in diesem Browser nicht gestartet werden.',
      detail
    };
  }

  return {
    summary: detail || 'Erkunden konnte im Browser nicht vorbereitet werden.',
    detail: detail || undefined
  };
}

export function classifyExploreQueryError(error: unknown): ExploreQueryError {
  const detail = toErrorMessage(error);
  const normalized = detail.toLowerCase();

  if (normalized.includes('cors') || normalized.includes('cross-origin')) {
    return sourceQueryError('cors', detail);
  }

  if (normalized.includes('range') || normalized.includes('accept-ranges') || normalized.includes('status code 416')) {
    return sourceQueryError('range', detail);
  }

  if (
    normalized.includes('_setthrow') ||
    normalized.includes('_setthrew') ||
    normalized.includes('setthrow is not defined') ||
    normalized.includes('setthrew is not defined') ||
    normalized.includes('connection refused') ||
    normalized.includes('could not connect') ||
    normalized.includes('could not establish connection') ||
    normalized.includes('failed to fetch') ||
    normalized.includes('networkerror') ||
    normalized.includes('network error') ||
    normalized.includes('no files found') ||
    normalized.includes('file not found')
  ) {
    return sourceQueryError('source-unavailable', detail);
  }

  if (
    normalized.includes('404') ||
    normalized.includes('403') ||
    normalized.includes('io error') ||
    normalized.includes('http')
  ) {
    return sourceQueryError('http', detail);
  }

  if (
    normalized.includes('parquet') ||
    normalized.includes('read_parquet') ||
    normalized.includes('duckdb_extension')
  ) {
    return sourceQueryError('parquet', detail);
  }

  if (normalized.includes('wasm') || normalized.includes('webassembly') || normalized.includes('worker')) {
    return {
      kind: 'duckdb',
      summary: QUERY_UNAVAILABLE_SUMMARY,
      detail
    };
  }

  return {
    kind: 'sql',
    summary: detail || QUERY_UNAVAILABLE_SUMMARY,
    detail: detail || undefined
  };
}

export function isSourceQueryError(error: ExploreQueryError): boolean {
  return ['source-unavailable', 'cors', 'range', 'http', 'parquet'].includes(error.kind);
}

function sourceQueryError(kind: ExploreQueryErrorKind, detail: string): ExploreQueryError {
  return {
    kind,
    summary: SOURCE_UNAVAILABLE_SUMMARY,
    detail
  };
}

function toErrorMessage(error: unknown): string {
  if (error instanceof Error) {
    return error.message;
  }
  return String(error);
}
