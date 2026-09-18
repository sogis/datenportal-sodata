import {
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  ChartContainer,
  ChartTooltip,
  ChartTooltipContent,
  XAxis,
  YAxis
} from '@sqlrooms/recharts';
import {stablePaletteColor} from './chartColors';

import {buildSeriesRows, seriesChartConfig, type ChartSeries} from './chartSeries';

export function BarResultChart({
  rows,
  x,
  series,
  colorSeed
}: {
  rows: Array<Record<string, unknown>>;
  x: string;
  series: ChartSeries[];
  colorSeed?: string;
}) {
  return (
    <ChartContainer className="dp-explore-chart__canvas" config={seriesChartConfig(series)}>
      <BarChart data={buildSeriesRows(rows, x, series)} accessibilityLayer margin={{top: 12, right: 12, bottom: 8, left: 8}}>
        <CartesianGrid vertical={false} stroke="var(--dp-color-border)" />
        <XAxis dataKey="axisX" tickLine={false} axisLine={false} minTickGap={rows.length <= 30 ? 0 : 16} />
        <YAxis tickLine={false} axisLine={false} width={52} />
        <ChartTooltip content={<ChartTooltipContent />} />
        {series.map((item) => <Bar key={item.key} dataKey={item.key} name={item.name} fill={item.color} radius={4} isAnimationActive={false}>
          {series.length === 1 && colorSeed && rows.map((_row, index) => (
            <Cell key={index} fill={stablePaletteColor(index, colorSeed)} />
          ))}
        </Bar>)}
      </BarChart>
    </ChartContainer>
  );
}
