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

const chartConfig = {
  value: {
    label: 'Wert',
    color: 'var(--dp-color-action)'
  }
};

export function LineResultChart({
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
      <LineChart data={rows} accessibilityLayer margin={{top: 12, right: 12, bottom: 8, left: 8}}>
        <CartesianGrid vertical={false} stroke="var(--dp-color-border)" />
        <XAxis dataKey={x} tickLine={false} axisLine={false} minTickGap={16} />
        <YAxis tickLine={false} axisLine={false} width={52} />
        <ChartTooltip content={<ChartTooltipContent />} />
        <Line
          type="monotone"
          dataKey={y}
          name={title ?? y}
          stroke="var(--color-value)"
          strokeWidth={2}
          dot={{r: 2}}
          isAnimationActive={false}
        />
      </LineChart>
    </ChartContainer>
  );
}
