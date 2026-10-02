import type {ResultColumn} from './chartTypes';

/** Normalize only chart copies; Arrow DATE getters return epoch milliseconds. */
export function normalizeChartRows(rows: Array<Record<string, unknown>>, columns: ResultColumn[]): Array<Record<string, unknown>> {
  const dateColumns = new Set(columns.filter((column) => column.typeCategory === 'date').map((column) => column.name));
  return rows.map((row) => Object.fromEntries(Object.entries(row).map(([name, value]) =>
    [name, normalizeChartValue(value, dateColumns.has(name))]
  )));
}

function normalizeChartValue(value: unknown, dateColumn: boolean): unknown {
  if (value instanceof Date || (dateColumn && (typeof value === 'number' || typeof value === 'bigint'))) {
    const date = value instanceof Date ? value : new Date(Number(value));
    return Number.isNaN(date.getTime()) ? null : date.toISOString().slice(0, 10);
  }
  if (typeof value === 'bigint') {
    return Number(value);
  }
  if (value instanceof Uint8Array) {
    return `[${value.byteLength} Bytes]`;
  }
  return value;
}
