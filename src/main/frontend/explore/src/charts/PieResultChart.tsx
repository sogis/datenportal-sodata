import type {CSSProperties} from 'react';
import {
  Cell,
  ChartContainer,
  ChartTooltip,
  ChartTooltipContent,
  Pie,
  PieChart
} from '@sqlrooms/recharts';

const chartConfig = {
  value: {
    label: 'Wert',
    color: 'var(--dp-color-action)'
  }
};

interface PieChartRow {
  __dpKey: string;
  __dpLabel: string;
  __dpValue: number;
  __dpFill: string;
}

export function PieResultChart({
  rows,
  x,
  y,
  title,
  variant,
  colorSeed
}: {
  rows: Array<Record<string, unknown>>;
  x: string;
  y: string;
  title?: string;
  variant: 'pie' | 'donut';
  colorSeed: string;
}) {
  const chartRows = buildPieRows(rows, x, y, colorSeed);
  const innerRadius = variant === 'donut' ? '56%' : 0;

  return (
    <div className="dp-explore-chart__pie-layout">
      <ChartContainer className="dp-explore-chart__canvas dp-explore-chart__canvas--pie" config={chartConfig}>
        <PieChart margin={{top: 12, right: 12, bottom: 12, left: 12}}>
          <ChartTooltip content={<ChartTooltipContent nameKey="__dpLabel" />} />
          <Pie
            data={chartRows}
            dataKey="__dpValue"
            nameKey="__dpLabel"
            name={title ?? y}
            innerRadius={innerRadius}
            outerRadius="82%"
            paddingAngle={variant === 'donut' ? 1 : 0}
            isAnimationActive={false}
          >
            {chartRows.map((row) => (
              <Cell key={row.__dpKey} fill={row.__dpFill} />
            ))}
          </Pie>
        </PieChart>
      </ChartContainer>

      <ul className="dp-explore-chart__legend" aria-label={`${variant === 'donut' ? 'Donut' : 'Pie'} Legende`}>
        {chartRows.map((row) => (
          <li key={row.__dpKey}>
            <span
              className="dp-explore-chart__legend-swatch"
              data-testid="chart-segment-color"
              style={{backgroundColor: row.__dpFill} as CSSProperties}
              aria-hidden="true"
            />
            <span className="dp-explore-chart__legend-label" title={row.__dpLabel}>{row.__dpLabel}</span>
            <span className="dp-explore-chart__legend-value">{formatSwissNumber(row.__dpValue)}</span>
          </li>
        ))}
      </ul>
    </div>
  );
}

export function buildPieRows(
  rows: Array<Record<string, unknown>>,
  x: string,
  y: string,
  colorSeed: string
): PieChartRow[] {
  return rows
    .map((row, index) => {
      const value = toPositiveNumber(row[y]);
      if (value === null) {
        return null;
      }
      const label = formatSegmentLabel(row[x], index);
      return {
        __dpKey: `${index}:${label}`,
        __dpLabel: label,
        __dpValue: value,
        __dpFill: segmentColor(label, index, colorSeed)
      };
    })
    .filter((row): row is PieChartRow => row !== null);
}

export function hasPieValues(rows: Array<Record<string, unknown>>, y: string): boolean {
  return rows.some((row) => toPositiveNumber(row[y]) !== null);
}

function toPositiveNumber(value: unknown): number | null {
  if (typeof value === 'number' && Number.isFinite(value) && value > 0) {
    return value;
  }
  if (typeof value === 'bigint' && value > 0n) {
    return Number(value);
  }
  if (typeof value === 'string' && value.trim() !== '') {
    const numeric = Number(value);
    return Number.isFinite(numeric) && numeric > 0 ? numeric : null;
  }
  return null;
}

function formatSegmentLabel(value: unknown, index: number): string {
  if (value === null || value === undefined || value === '') {
    return `Segment ${index + 1}`;
  }
  if (value instanceof Date) {
    return value.toISOString().slice(0, 10);
  }
  return String(value);
}

function segmentColor(label: string, index: number, colorSeed: string): string {
  const offset = hashString(colorSeed) % 360;
  const jitter = hashString(`${colorSeed}:${label}`) % 43;
  const hue = (offset + index * 137 + jitter) % 360;
  return `hsl(${hue}, 64%, 47%)`;
}

function hashString(value: string): number {
  let hash = 2166136261;
  for (let index = 0; index < value.length; index++) {
    hash ^= value.charCodeAt(index);
    hash = Math.imul(hash, 16777619);
  }
  return hash >>> 0;
}

function formatSwissNumber(value: number): string {
  return new Intl.NumberFormat('de-CH', {maximumFractionDigits: 2}).format(value);
}
