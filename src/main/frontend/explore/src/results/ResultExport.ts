import {escapeVal, isWasmDuckDbConnector, type DuckDbConnector} from '@sqlrooms/duckdb';
import {formatResultCell} from './arrowResult';
import type {QueryResultState} from './queryResultTypes';

export const CSV_DELIMITER = ';';
const CSV_LINE_ENDING = '\r\n';
export const ROW_EXPORT_FORMATS = ['csv', 'xlsx', 'parquet'] as const;
export type ResultExportFormat = typeof ROW_EXPORT_FORMATS[number];

const XLSX_CONTENT_TYPE = 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet';
const PARQUET_CONTENT_TYPE = 'application/vnd.apache.parquet';
const EXPORT_SHEET_NAME = 'Daten';

export function rowsToCsv(columns: string[], rows: Array<Record<string, unknown>>): string {
  const header = columns.map(escapeCsvValue).join(CSV_DELIMITER);
  const body = rows.map((row) => columns.map((column) => escapeCsvValue(formatResultCell(row[column]))).join(CSV_DELIMITER));
  return [header, ...body].join(CSV_LINE_ENDING);
}

export function exportRowsToCsv(
  rows: Array<Record<string, unknown>>,
  columns: string[],
  filename: string,
  documentRef: Document = document
): void {
  const csv = rowsToCsv(columns, rows);
  const blob = new Blob([csv], {type: 'text/csv;charset=utf-8'});
  downloadBlob(blob, sanitizeCsvFilename(filename), documentRef);
}

export async function exportQueryResult(
  result: QueryResultState,
  format: ResultExportFormat,
  datasetId: string,
  connector?: DuckDbConnector,
  documentRef: Document = document
): Promise<void> {
  if (result.status !== 'success') {
    return;
  }

  const filename = resultExportFilename(datasetId, format);
  if (format === 'csv') {
    exportRowsToCsv(result.rows, result.columns, filename, documentRef);
    return;
  }
  if (!result.executedSql) {
    throw new Error('Für diesen Export fehlt die ausgeführte SQL-Abfrage.');
  }
  await exportQueryResultWithDuckDbCopy(result.executedSql, format, filename, connector, documentRef);
}

export async function exportQueryResultWithDuckDbCopy(
  executedSql: string,
  format: Exclude<ResultExportFormat, 'csv'>,
  filename: string,
  connector: DuckDbConnector | undefined,
  documentRef: Document = document
): Promise<void> {
  if (!connector || !isWasmDuckDbConnector(connector)) {
    throw new Error('XLSX- und Parquet-Export brauchen die lokale DuckDB-Wasm-Verbindung.');
  }

  const exportFilename = sanitizeResultFilename(filename, format);
  const duckDbFilename = temporaryDuckDbFilename(format);
  const db = connector.getDb();
  const connection = connector.getConnection();

  try {
    await connection.query(`install ${duckDbExtensionForFormat(format)};`);
    await connection.query(`load ${duckDbExtensionForFormat(format)};`);
    await connection.query(copySqlForResult(executedSql, duckDbFilename, format));
    await db.flushFiles();
    const bytes = await db.copyFileToBuffer(duckDbFilename);
    downloadBlob(new Blob([uint8ArrayToArrayBuffer(bytes)], {type: contentTypeForFormat(format)}), exportFilename, documentRef);
  } finally {
    await db.dropFile(duckDbFilename).catch(() => undefined);
  }
}

export function copySqlForResult(
  executedSql: string,
  duckDbFilename: string,
  format: Exclude<ResultExportFormat, 'csv'>
): string {
  const selectSql = stripTrailingSemicolon(executedSql);
  if (format === 'xlsx') {
    return `copy (
${selectSql}
) to ${escapeVal(duckDbFilename)} with (format xlsx, header true, sheet ${escapeVal(EXPORT_SHEET_NAME)});`;
  }
  return `copy (
${selectSql}
) to ${escapeVal(duckDbFilename)} with (format parquet);`;
}

export function downloadBlob(blob: Blob, filename: string, documentRef: Document = document): void {
  const url = URL.createObjectURL(blob);
  const link = documentRef.createElement('a');
  link.href = url;
  link.download = filename;
  link.style.display = 'none';
  documentRef.body.append(link);
  link.click();
  link.remove();
  URL.revokeObjectURL(url);
}

export function resultCsvFilename(datasetId: string): string {
  return resultExportFilename(datasetId, 'csv');
}

export function resultExportFilename(datasetId: string, format: ResultExportFormat): string {
  return sanitizeResultFilename(`datenportal-${datasetId}-result.${format}`, format);
}

export function sanitizeCsvFilename(filename: string): string {
  return sanitizeResultFilename(filename, 'csv');
}

export function sanitizeResultFilename(filename: string, extension: ResultExportFormat): string {
  const sanitized = filename
    .normalize('NFKD')
    .replace(/[^\w.-]+/g, '-')
    .replace(/-+/g, '-')
    .replace(/^-|-$/g, '');
  const fallback = `datenportal-result.${extension}`;
  if (!sanitized) {
    return fallback;
  }
  const withoutKnownExtension = sanitized.replace(/\.(csv|xlsx|parquet)$/i, '');
  return `${withoutKnownExtension}.${extension}`;
}

function escapeCsvValue(value: string): string {
  if (/[;"\r\n]/.test(value)) {
    return `"${value.replace(/"/g, '""')}"`;
  }
  return value;
}

function duckDbExtensionForFormat(format: Exclude<ResultExportFormat, 'csv'>): 'excel' | 'parquet' {
  return format === 'xlsx' ? 'excel' : 'parquet';
}

function contentTypeForFormat(format: Exclude<ResultExportFormat, 'csv'>): string {
  return format === 'xlsx' ? XLSX_CONTENT_TYPE : PARQUET_CONTENT_TYPE;
}

function temporaryDuckDbFilename(format: Exclude<ResultExportFormat, 'csv'>): string {
  const randomSuffix = Math.random().toString(36).slice(2);
  return `datenportal-result-${Date.now()}-${randomSuffix}.${format}`;
}

function stripTrailingSemicolon(sql: string): string {
  return sql.trim().replace(/;+$/, '').trim();
}

function uint8ArrayToArrayBuffer(bytes: Uint8Array): ArrayBuffer {
  const buffer = new ArrayBuffer(bytes.byteLength);
  new Uint8Array(buffer).set(bytes);
  return buffer;
}
