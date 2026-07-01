export interface ExploreRuntimeError {
  summary: string;
  detail?: string;
}

export function classifyExploreRuntimeError(error: unknown): ExploreRuntimeError {
  const detail = toErrorMessage(error);
  const normalized = detail.toLowerCase();

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

function toErrorMessage(error: unknown): string {
  if (error instanceof Error) {
    return error.message;
  }
  return String(error);
}
