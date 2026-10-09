import {MULTI_CHART_COLOR_PALETTE} from '../charts/chartColors';
export interface MapLegendItem {label: string; color: string}
export function mapColorScale(values: unknown[], enabled: boolean) {
  const missing = '#888888';
  if (!enabled) return {color: (_: unknown) => '#104E8B', legend: [] as MapLegendItem[]};
  const present = values.filter((v) => v !== null && v !== undefined);
  const numberValue = (v: unknown) => typeof v === 'number' ? v
    : typeof v === 'bigint' && Number.isSafeInteger(Number(v)) ? Number(v) : NaN;
  const numeric = present.length > 0 && present.every((v) => Number.isFinite(numberValue(v)));
  const legend: MapLegendItem[] = [];
  let color: (v: unknown) => string;
  if (numeric) {
    const min = Math.min(...present.map(numberValue)), max = Math.max(...present.map(numberValue));
    const gradient = (t: number) => `rgb(${Math.round(222 - 206 * t)}, ${Math.round(235 - 157 * t)}, ${Math.round(247 - 108 * t)})`;
    color = (v) => Number.isFinite(numberValue(v)) ? gradient(max === min ? 0.6 : (numberValue(v) - min) / (max - min)) : missing;
    [min, ...(max === min ? [] : [(min + max) / 2, max])].forEach((v) => legend.push({label: new Intl.NumberFormat('de-CH').format(v), color: color(v)}));
  } else {
    const categories = [...new Set(present.map(String))].sort((a, b) => a.localeCompare(b, 'de-CH'));
    categories.forEach((v, i) => legend.push({label: v, color: MULTI_CHART_COLOR_PALETTE[i % MULTI_CHART_COLOR_PALETTE.length]}));
    const lookup = new Map(legend.map((item) => [item.label, item.color]));
    color = (v) => v == null ? missing : lookup.get(String(v)) ?? missing;
  }
  if (present.length !== values.length) legend.push({label: 'Keine Angabe', color: missing});
  return {color, legend};
}
