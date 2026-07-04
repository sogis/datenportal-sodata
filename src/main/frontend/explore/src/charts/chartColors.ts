export const MULTI_CHART_COLOR_VALUE = 'multi';

export const CHART_SINGLE_COLOR_OPTIONS = [
  {value: 'dodgerBlue4', label: 'Dunkelblau', color: '#104E8B'},
  {value: 'deepSkyBlue2', label: 'Hellblau', color: '#00B2EE'},
  {value: 'darkOrange1', label: 'Orange', color: '#E18C00'},
  {value: 'gold', label: 'Gold', color: '#E1D700'},
  {value: 'forestGreen', label: 'Dunkelgrün', color: '#228B22'},
  {value: 'yellowGreen', label: 'Hellgrün', color: '#9ACD32'}
] as const;

const LEGACY_CHART_COLOR_LABELS: Record<ChartSingleColorValue, string[]> = {
  dodgerBlue4: ['DodgerBlue4'],
  deepSkyBlue2: ['DeepSkyBlue 2'],
  darkOrange1: ['DarkOrange 1'],
  gold: [],
  forestGreen: ['ForestGreen'],
  yellowGreen: ['YellowGreen']
};

export const MULTI_CHART_COLOR_OPTION = {
  value: MULTI_CHART_COLOR_VALUE,
  label: 'Mehrfarbig'
} as const;

export const MULTI_CHART_COLOR_PALETTE = [
  ...CHART_SINGLE_COLOR_OPTIONS.map((option) => option.color),
  '#006D77',
  '#4682B4',
  '#2E8B57',
  '#DAA520',
  '#5F9EA0',
  '#7CB342'
] as const;

export type ChartSingleColorValue = (typeof CHART_SINGLE_COLOR_OPTIONS)[number]['value'];
export type ChartColorValue = ChartSingleColorValue | typeof MULTI_CHART_COLOR_VALUE;

export const DEFAULT_CHART_COLOR: ChartSingleColorValue = 'dodgerBlue4';

export function normalizeChartColor(value: string | undefined | null): ChartColorValue {
  const normalized = normalizeColorToken(value);
  if (!normalized) {
    return DEFAULT_CHART_COLOR;
  }
  if (normalized === MULTI_CHART_COLOR_VALUE || normalized === normalizeColorToken(MULTI_CHART_COLOR_OPTION.label)) {
    return MULTI_CHART_COLOR_VALUE;
  }

  const singleOption = CHART_SINGLE_COLOR_OPTIONS.find((option) =>
    [option.value, option.label, option.color, ...LEGACY_CHART_COLOR_LABELS[option.value]]
      .map(normalizeColorToken)
      .includes(normalized)
  );
  return singleOption?.value ?? DEFAULT_CHART_COLOR;
}

export function chartColorHex(value: ChartColorValue): string {
  return CHART_SINGLE_COLOR_OPTIONS.find((option) => option.value === value)?.color
    ?? CHART_SINGLE_COLOR_OPTIONS[0].color;
}

export function stablePaletteColor(index: number, colorSeed: string): string {
  const paletteOffset = hashString(colorSeed) % MULTI_CHART_COLOR_PALETTE.length;
  return MULTI_CHART_COLOR_PALETTE[(paletteOffset + index) % MULTI_CHART_COLOR_PALETTE.length];
}

export function hashString(value: string): number {
  let hash = 2166136261;
  for (let index = 0; index < value.length; index++) {
    hash ^= value.charCodeAt(index);
    hash = Math.imul(hash, 16777619);
  }
  return hash >>> 0;
}

function normalizeColorToken(value: string | undefined | null): string {
  return value?.trim().toLowerCase().replaceAll(/\s+/g, '').replace(/^#/, '') ?? '';
}
