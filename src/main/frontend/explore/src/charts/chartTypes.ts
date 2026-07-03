import type {ExploreChartConfigDto} from '../app/ExploreContext';

export type ResultColumnTypeCategory = 'string' | 'number' | 'date' | 'boolean' | 'unknown';
export type ResultChartType = ExploreChartConfigDto['type'];

export interface ResultColumn {
  name: string;
  typeCategory: ResultColumnTypeCategory;
}

export interface ChartSuggestion {
  type: ResultChartType;
  x?: string;
  y?: string;
  title?: string;
  reason: string;
  confidence: number;
  warning?: string;
}

export interface ChartRenderConfig {
  type: ResultChartType;
  x?: string;
  y?: string;
  title?: string;
}

export const DEFAULT_CHART_ROW_LIMIT = 500;
export const CHART_ROW_LIMIT_OPTIONS = [50, 100, 500, 1000] as const;
export const LARGE_SERIES_WARNING_THRESHOLD = 500;
export const LARGE_SEGMENT_WARNING_THRESHOLD = 12;

export const chartTypeLabels: Record<ResultChartType, string> = {
  bar: 'Balken',
  line: 'Linie',
  scatter: 'Punkte',
  histogram: 'Histogramm',
  pie: 'Pie',
  donut: 'Donut'
};

export function chartTypeRequiresX(type: ResultChartType): boolean {
  return type !== 'histogram';
}

export function chartTypeRequiresY(type: ResultChartType): boolean {
  return type !== 'histogram';
}
