import type {ExploreChartConfigDto} from '../app/ExploreContext';
import {
  LARGE_SERIES_WARNING_THRESHOLD,
  type ChartSuggestion,
  type ResultChartType,
  type ResultColumn
} from './chartTypes';

const integerYearPattern = /^\d{4}$/;

export function inferResultColumns(
  columns: string[],
  rows: Array<Record<string, unknown>>
): ResultColumn[] {
  return columns.map((name) => ({
    name,
    typeCategory: inferColumnType(name, rows)
  }));
}

export function inferChartSuggestion(
  columns: ResultColumn[],
  rows: Array<Record<string, unknown>>,
  preferred?: ExploreChartConfigDto
): ChartSuggestion | null {
  if (columns.length === 0 || rows.length === 0) {
    return null;
  }

  const validPreferred = validatePreferredChart(columns, preferred);
  if (validPreferred) {
    return withLargeSeriesWarning({
      type: validPreferred.type,
      x: validPreferred.x,
      y: validPreferred.y,
      title: validPreferred.title,
      reason: 'Vorgabe der Beispielabfrage',
      confidence: 1
    }, rows.length);
  }

  const numericColumns = columns.filter((column) => column.typeCategory === 'number');
  const dateColumns = columns.filter((column) => isDateLikeResultColumn(column, rows));
  const stringColumns = columns.filter((column) => column.typeCategory === 'string');

  const dateX = dateColumns[0];
  const dateY = numericColumns.find((column) => column.name !== dateX?.name);
  if (dateX && dateY) {
    return withLargeSeriesWarning({
      type: 'line',
      x: dateX.name,
      y: dateY.name,
      reason: 'Zeit- oder Jahrspalte mit Zahlenwert',
      confidence: 0.85
    }, rows.length);
  }

  const categoryX = stringColumns[0];
  const categoryY = numericColumns[0];
  if (categoryX && categoryY) {
    return withLargeSeriesWarning({
      type: 'bar',
      x: categoryX.name,
      y: categoryY.name,
      reason: 'Kategorie mit Zahlenwert',
      confidence: 0.8
    }, rows.length);
  }

  if (numericColumns.length >= 2) {
    return {
      type: 'scatter',
      x: numericColumns[0].name,
      y: numericColumns[1].name,
      reason: 'Zwei Zahlenwerte',
      confidence: 0.65
    };
  }

  if (numericColumns.length === 1) {
    return {
      type: 'histogram',
      x: numericColumns[0].name,
      reason: 'Ein Zahlenwert',
      confidence: 0.55
    };
  }

  return null;
}

export function isYearLikeColumn(name: string, rows: Array<Record<string, unknown>>): boolean {
  const normalizedName = normalizeName(name);
  if (normalizedName === 'jahr' || normalizedName === 'year' || normalizedName === 'periode') {
    return true;
  }

  const values = rows
    .map((row) => row[name])
    .filter((value) => value !== null && value !== undefined)
    .slice(0, 50);
  if (values.length === 0) {
    return false;
  }

  const yearValues = values.filter((value) => {
    if (typeof value === 'number' && Number.isInteger(value)) {
      return value >= 1800 && value <= 2200;
    }
    if (typeof value === 'bigint') {
      return value >= 1800n && value <= 2200n;
    }
    if (typeof value === 'string' && integerYearPattern.test(value.trim())) {
      const numeric = Number(value);
      return numeric >= 1800 && numeric <= 2200;
    }
    return false;
  });

  return yearValues.length / values.length >= 0.7;
}

export function isDateLikeColumn(name: string, rows: Array<Record<string, unknown>>): boolean {
  const normalizedName = normalizeName(name);
  if (['datum', 'date', 'stand', 'stichtag', 'gueltig_ab', 'gueltig_bis', 'updated_at'].includes(normalizedName)) {
    return true;
  }

  const values = rows
    .map((row) => row[name])
    .filter((value) => value !== null && value !== undefined)
    .slice(0, 50);
  if (values.length === 0) {
    return false;
  }

  const dateValues = values.filter((value) => {
    if (value instanceof Date) {
      return !Number.isNaN(value.getTime());
    }
    if (typeof value !== 'string') {
      return false;
    }
    return /^\d{4}-\d{2}-\d{2}/.test(value.trim()) && !Number.isNaN(Date.parse(value));
  });

  return dateValues.length / values.length >= 0.7;
}

export function buildHistogramBins(
  rows: Array<Record<string, unknown>>,
  column: string,
  binCount: number
): Array<{bin: string; count: number}> {
  const values = rows
    .map((row) => toFiniteNumber(row[column]))
    .filter((value): value is number => typeof value === 'number');
  if (values.length === 0 || binCount <= 0) {
    return [];
  }

  const min = Math.min(...values);
  const max = Math.max(...values);
  if (min === max) {
    return [{bin: formatBinLabel(min, max), count: values.length}];
  }

  const width = (max - min) / binCount;
  const bins = Array.from({length: binCount}, (_, index) => {
    const lower = min + width * index;
    const upper = index === binCount - 1 ? max : min + width * (index + 1);
    return {bin: formatBinLabel(lower, upper), count: 0};
  });

  values.forEach((value) => {
    const index = Math.min(binCount - 1, Math.floor((value - min) / width));
    bins[index].count += 1;
  });

  return bins;
}

export function isNumericColumn(column: ResultColumn): boolean {
  return column.typeCategory === 'number';
}

export function isCategoryColumn(column: ResultColumn, rows: Array<Record<string, unknown>>): boolean {
  return column.typeCategory === 'string' || isDateLikeResultColumn(column, rows);
}

function inferColumnType(name: string, rows: Array<Record<string, unknown>>) {
  const values = rows
    .map((row) => row[name])
    .filter((value) => value !== null && value !== undefined)
    .slice(0, 50);

  if (values.length === 0) {
    return 'unknown' as const;
  }

  if (values.every((value) => typeof value === 'number' || typeof value === 'bigint')) {
    return 'number' as const;
  }
  if (values.every((value) => value instanceof Date)) {
    return 'date' as const;
  }
  if (values.every((value) => typeof value === 'boolean')) {
    return 'boolean' as const;
  }
  if (values.every((value) => typeof value === 'string')) {
    return isDateLikeColumn(name, rows) && !isYearLikeColumn(name, rows) ? 'date' as const : 'string' as const;
  }
  return 'unknown' as const;
}

function validatePreferredChart(
  columns: ResultColumn[],
  preferred?: ExploreChartConfigDto
): ExploreChartConfigDto | null {
  if (!preferred) {
    return null;
  }

  if (!isKnownChartType(preferred.type)) {
    return null;
  }

  const names = new Set(columns.map((column) => column.name));
  if (preferred.type === 'histogram') {
    const histogramColumn = preferred.x ?? preferred.y;
    return histogramColumn && names.has(histogramColumn)
      ? {...preferred, x: histogramColumn, y: undefined}
      : null;
  }

  if (!preferred.x || !preferred.y || !names.has(preferred.x) || !names.has(preferred.y)) {
    return null;
  }
  return preferred;
}

function isKnownChartType(type: string): type is ResultChartType {
  return type === 'bar' || type === 'line' || type === 'scatter' || type === 'histogram' || type === 'pie' || type === 'donut';
}

function isDateLikeResultColumn(column: ResultColumn, rows: Array<Record<string, unknown>>): boolean {
  return column.typeCategory === 'date' || isYearLikeColumn(column.name, rows) || isDateLikeColumn(column.name, rows);
}

function withLargeSeriesWarning(suggestion: ChartSuggestion, rowCount: number): ChartSuggestion {
  if ((suggestion.type === 'bar' || suggestion.type === 'line') && rowCount > LARGE_SERIES_WARNING_THRESHOLD) {
    return {
      ...suggestion,
      warning: 'Viele Datenpunkte. Aggregieren Sie das SQL-Resultat oder reduzieren Sie die Diagrammzeilen.'
    };
  }
  return suggestion;
}

function toFiniteNumber(value: unknown): number | null {
  if (typeof value === 'number' && Number.isFinite(value)) {
    return value;
  }
  if (typeof value === 'bigint') {
    return Number(value);
  }
  if (typeof value === 'string' && value.trim() !== '') {
    const numeric = Number(value);
    return Number.isFinite(numeric) ? numeric : null;
  }
  return null;
}

function formatBinLabel(lower: number, upper: number): string {
  const formatter = new Intl.NumberFormat('de-CH', {maximumFractionDigits: 2});
  if (lower === upper) {
    return formatter.format(lower);
  }
  return `${formatter.format(lower)}-${formatter.format(upper)}`;
}

function normalizeName(name: string): string {
  return name.trim().toLowerCase().replaceAll('ü', 'ue').replaceAll('ä', 'ae').replaceAll('ö', 'oe');
}
