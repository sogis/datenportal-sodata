const YEAR_MIN = 1800;
const YEAR_MAX = 2200;
const YEAR_VALUE_PATTERN = /^\d{4}$/;
const DATE_VALUE_PATTERN = /^\d{4}-\d{2}-\d{2}/;
const YEAR_LIKE_RATIO = 0.7;
const DATE_LIKE_RATIO = 0.7;

const YEAR_LIKE_NAMES = new Set(['jahr', 'year', 'periode']);
const DATE_LIKE_NAMES = new Set([
  'datum',
  'date',
  'stand',
  'stichtag',
  'gueltig_ab',
  'gueltig_bis',
  'updated_at'
]);

export function recordColumnValues(
  rows: Array<Record<string, unknown>>,
  name: string,
  limit = 50
): unknown[] {
  const values: unknown[] = [];
  for (const row of rows) {
    const value = row[name];
    if (value === null || value === undefined) {
      continue;
    }
    values.push(value);
    if (values.length >= limit) {
      break;
    }
  }
  return values;
}

export function positionalColumnValues(
  rows: ReadonlyArray<ReadonlyArray<unknown>>,
  index: number,
  limit = 50
): unknown[] {
  const values: unknown[] = [];
  for (const row of rows) {
    const value = row[index];
    if (value === null || value === undefined) {
      continue;
    }
    values.push(value);
    if (values.length >= limit) {
      break;
    }
  }
  return values;
}

export function snapshotColumnValues(snapshot: {
  columns: ReadonlyArray<{name: string}>;
  rows: ReadonlyArray<ReadonlyArray<unknown>>;
}): Map<string, unknown[]> {
  const valuesByColumn = new Map<string, unknown[]>();
  snapshot.columns.forEach((column, index) => {
    valuesByColumn.set(column.name, positionalColumnValues(snapshot.rows, index));
  });
  return valuesByColumn;
}

export function isYearLikeValues(values: readonly unknown[]): boolean {
  if (values.length === 0) {
    return false;
  }
  const yearValues = values.filter(isYearValue);
  return yearValues.length / values.length >= YEAR_LIKE_RATIO;
}

export function isDateLikeValues(values: readonly unknown[]): boolean {
  if (values.length === 0) {
    return false;
  }
  const dateValues = values.filter(isDateValue);
  return dateValues.length / values.length >= DATE_LIKE_RATIO;
}

export function isYearLikeName(name: string): boolean {
  return YEAR_LIKE_NAMES.has(normalizeName(name));
}

export function isDateLikeName(name: string): boolean {
  return DATE_LIKE_NAMES.has(normalizeName(name));
}

function normalizeName(name: string): string {
  return name
    .trim()
    .toLowerCase()
    .replaceAll('ü', 'ue')
    .replaceAll('ä', 'ae')
    .replaceAll('ö', 'oe');
}

function isYearValue(value: unknown): boolean {
  if (typeof value === 'number') {
    return Number.isInteger(value) && value >= YEAR_MIN && value <= YEAR_MAX;
  }
  if (typeof value === 'bigint') {
    return value >= 1800n && value <= 2200n;
  }
  if (typeof value === 'string' && YEAR_VALUE_PATTERN.test(value.trim())) {
    const numeric = Number(value);
    return numeric >= YEAR_MIN && numeric <= YEAR_MAX;
  }
  return false;
}

function isDateValue(value: unknown): boolean {
  if (value instanceof Date) {
    return !Number.isNaN(value.getTime());
  }
  if (typeof value !== 'string') {
    return false;
  }
  return DATE_VALUE_PATTERN.test(value.trim()) && !Number.isNaN(Date.parse(value));
}
