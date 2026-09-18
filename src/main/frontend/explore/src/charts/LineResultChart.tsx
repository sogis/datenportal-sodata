import {
  CartesianGrid,
  ChartContainer,
  ChartTooltip,
  ChartTooltipContent,
  Line,
  LineChart,
  XAxis,
  YAxis
} from '@sqlrooms/recharts';

import {buildSeriesRows, seriesChartConfig, type ChartSeries} from './chartSeries';

export function LineResultChart({
  rows,
  x,
  series
}: {
  rows: Array<Record<string, unknown>>;
  x: string;
  series: ChartSeries[];
}) {
  return (
    <ChartContainer className="dp-explore-chart__canvas" config={seriesChartConfig(series)}>
      <LineChart data={buildSeriesRows(rows, x, series)} accessibilityLayer margin={{top: 12, right: 12, bottom: 8, left: 8}}>
        <CartesianGrid vertical={false} stroke="var(--dp-color-border)" />
        <XAxis dataKey="axisX" tickLine={false} axisLine={false} minTickGap={16} />
        <YAxis tickLine={false} axisLine={false} width={52} />
        <ChartTooltip content={<ChartTooltipContent />} />
        {series.map((item) => <Line
          key={item.key}
          connectNulls={false}
          type="monotone"
          dataKey={item.key}
          name={item.name}
          stroke={item.color}
          strokeWidth={2}
          dot={{r: 2}}
          isAnimationActive={false}
        />)}
      </LineChart>
    </ChartContainer>
  );
}
