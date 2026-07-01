import type {Table} from 'apache-arrow';
import type {QueryResultState} from './queryResultTypes';

export function arrowTableToRows(table: Table): {columns: string[]; rows: Array<Record<string, unknown>>} {
  const columns = table.schema.fields.map((field) => field.name);
  const rows: Array<Record<string, unknown>> = [];
  for (let rowIndex = 0; rowIndex < table.numRows; rowIndex++) {
    const row: Record<string, unknown> = {};
    columns.forEach((column, columnIndex) => {
      row[column] = table.getChildAt(columnIndex)?.get(rowIndex) ?? null;
    });
    rows.push(row);
  }
  return {columns, rows};
}

export function successfulQueryResult(args: {
  sourceSql: string;
  executedSql: string;
  table: Table;
  durationMs: number;
  maxRowsApplied: boolean;
}): QueryResultState {
  const {columns, rows} = arrowTableToRows(args.table);
  return {
    status: 'success',
    sourceSql: args.sourceSql,
    executedSql: args.executedSql,
    columns,
    rows,
    rowCount: args.table.numRows,
    durationMs: args.durationMs,
    maxRowsApplied: args.maxRowsApplied,
    arrowTable: args.table
  };
}

export function formatResultCell(value: unknown): string {
  if (value === null || value === undefined) {
    return '';
  }
  if (value instanceof Date) {
    return value.toISOString();
  }
  if (typeof value === 'bigint') {
    return value.toString();
  }
  if (value instanceof Uint8Array) {
    return `[${value.byteLength} Bytes]`;
  }
  return String(value);
}

