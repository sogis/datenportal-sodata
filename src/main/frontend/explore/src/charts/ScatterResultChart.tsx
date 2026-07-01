import {
  CartesianGrid,
  ChartContainer,
  ChartTooltip,
  ChartTooltipContent,
  Scatter,
  ScatterChart,
  XAxis,
  YAxis
} from '@sqlrooms/recharts';

const chartConfig = {
  value: {
    label: 'Wert',
    color: 'var(--dp-color-action)'
  }
};

export function ScatterResultChart({
  rows,
  x,
  y,
  title
}: {
  rows: Array<Record<string, unknown>>;
  x: string;
  y: string;
  title?: string;
}) {
  return (
    <ChartContainer className="dp-explore-chart__canvas" config={chartConfig}>
      <ScatterChart data={rows} accessibilityLayer margin={{top: 12, right: 12, bottom: 8, left: 8}}>
        <CartesianGrid stroke="var(--dp-color-border)" />
        <XAxis dataKey={x} name={x} type="number" tickLine={false} axisLine={false} />
        <YAxis dataKey={y} name={y} type="number" tickLine={false} axisLine={false} width={52} />
        <ChartTooltip content={<ChartTooltipContent />} />
        <Scatter name={title ?? y} data={rows} fill="var(--color-value)" isAnimationActive={false} />
      </ScatterChart>
    </ChartContainer>
  );
}
