import {formatResultCell} from './arrowResult';

export const CSV_DELIMITER = ';';
const CSV_LINE_ENDING = '\r\n';

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
  const url = URL.createObjectURL(blob);
  const link = documentRef.createElement('a');
  link.href = url;
  link.download = sanitizeCsvFilename(filename);
  link.style.display = 'none';
  documentRef.body.append(link);
  link.click();
  link.remove();
  URL.revokeObjectURL(url);
}

export function resultCsvFilename(datasetId: string): string {
  return sanitizeCsvFilename(`datenportal-${datasetId}-result.csv`);
}

export function sanitizeCsvFilename(filename: string): string {
  const sanitized = filename
    .normalize('NFKD')
    .replace(/[^\w.-]+/g, '-')
    .replace(/-+/g, '-')
    .replace(/^-|-$/g, '');
  return sanitized.endsWith('.csv') ? sanitized : `${sanitized || 'datenportal-result'}.csv`;
}

function escapeCsvValue(value: string): string {
  if (/[;"\r\n]/.test(value)) {
    return `"${value.replace(/"/g, '""')}"`;
  }
  return value;
}

