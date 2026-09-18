import {hashString, MULTI_CHART_COLOR_PALETTE} from './chartColors';

export interface ChartSeries {
  name: string;
  key: string;
  color: string;
}

export function buildChartSeries(
  names: string[], knownNames: string[], color: string,
  seed?: string, version = 0, colorNames = knownNames
): ChartSeries[] {
  return names.map((name) => {
    const index = knownNames.indexOf(name);
    return {
      name,
      key: `series${index}`,
      color: seed === undefined ? color : MULTI_CHART_COLOR_PALETTE[
        (hashString(seed) + version + colorNames.indexOf(name)) % MULTI_CHART_COLOR_PALETTE.length
      ]
    };
  });
}

export function finiteChartNumber(value: unknown): number | null {
  if (typeof value !== 'number' && typeof value !== 'bigint' && typeof value !== 'string') return null;
  if (typeof value === 'string' && value.trim() === '') return null;
  const number = Number(value);
  return Number.isFinite(number) ? number : null;
}

export function buildSeriesRows(rows: Array<Record<string, unknown>>, x: string, series: ChartSeries[]) {
  return rows.map((row) => Object.fromEntries([
    ['axisX', row[x]],
    ...series.map((item) => [item.key, finiteChartNumber(row[item.name])])
  ]));
}

export function seriesChartConfig(series: ChartSeries[]) {
  return Object.fromEntries(series.map((item) => [item.key, {label: item.name, color: item.color}]));
}
