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

const chartConfig = {
  value: {
    label: 'Wert',
    color: 'var(--dp-color-action)'
  }
};

export function BarResultChart({
  rows,
  x,
  y,
  title,
  color,
  colorSeed
}: {
  rows: Array<Record<string, unknown>>;
  x: string;
  y: string;
  title?: string;
  color: string;
  colorSeed?: string;
}) {
  return (
    <ChartContainer className="dp-explore-chart__canvas" config={chartConfig}>
      <BarChart data={rows} accessibilityLayer margin={{top: 12, right: 12, bottom: 8, left: 8}}>
        <CartesianGrid vertical={false} stroke="var(--dp-color-border)" />
        <XAxis dataKey={x} tickLine={false} axisLine={false} minTickGap={rows.length <= 30 ? 0 : 16} />
        <YAxis tickLine={false} axisLine={false} width={52} />
        <ChartTooltip content={<ChartTooltipContent />} />
        <Bar dataKey={y} name={title ?? y} fill={color} radius={4} isAnimationActive={false}>
          {colorSeed && rows.map((_row, index) => (
            <Cell key={index} fill={stablePaletteColor(index, colorSeed)} />
          ))}
        </Bar>
      </BarChart>
    </ChartContainer>
  );
}
