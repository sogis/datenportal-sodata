import {
  Bar,
  BarChart,
  CartesianGrid,
  ChartContainer,
  ChartTooltip,
  ChartTooltipContent,
  XAxis,
  YAxis
} from '@sqlrooms/recharts';
import {buildHistogramBins} from './chartInference';

const chartConfig = {
  count: {
    label: 'Anzahl',
    color: 'var(--dp-color-action)'
  }
};

export function HistogramResultChart({
  rows,
  column,
  title
}: {
  rows: Array<Record<string, unknown>>;
  column: string;
  title?: string;
}) {
  const bins = buildHistogramBins(rows, column, 12);
  return (
    <ChartContainer className="dp-explore-chart__canvas" config={chartConfig}>
      <BarChart data={bins} accessibilityLayer margin={{top: 12, right: 12, bottom: 8, left: 8}}>
        <CartesianGrid vertical={false} stroke="var(--dp-color-border)" />
        <XAxis dataKey="bin" tickLine={false} axisLine={false} minTickGap={16} />
        <YAxis tickLine={false} axisLine={false} width={52} />
        <ChartTooltip content={<ChartTooltipContent />} />
        <Bar dataKey="count" name={title ?? 'Anzahl'} fill="var(--color-count)" radius={4} isAnimationActive={false} />
      </BarChart>
    </ChartContainer>
  );
}
