import type {CSSProperties, ComponentProps} from 'react';
import {ChartTooltipContent} from '@sqlrooms/recharts';
import {axisTooltipLabel, formatTooltipNumber} from './chartSeries';

/**
 * Tooltip content for line and bar charts.
 *
 * The library component uses the first series label as header whenever the x
 * value is not a string, and formats values with the browser locale. This
 * wrapper passes the real x value and Swiss number formatting. The row uses
 * portal CSS because the Tailwind utilities of the library are not shipped by
 * the portal, so name and value would otherwise stick together.
 */
export function SeriesTooltipContent(props: ComponentProps<typeof ChartTooltipContent>) {
  return <ChartTooltipContent
    {...props}
    labelFormatter={(_label, payload) => axisTooltipLabel(payload)}
    formatter={(value, name, item) => (
      <SeriesTooltipRow value={value} name={name} indicatorColor={item.payload?.fill ?? item.color} />
    )}
  />;
}

export function SeriesTooltipRow({
  value,
  name,
  indicatorColor
}: {
  value: unknown;
  name: string | number;
  indicatorColor?: string;
}) {
  return <>
    <div
      className="border-border shrink-0 rounded-[2px] bg-(--color-bg) h-2.5 w-2.5"
      style={{'--color-bg': indicatorColor, '--color-border': indicatorColor} as CSSProperties}
      data-testid="chart-series-swatch"
      aria-hidden="true"
    />
    <div className="dp-explore-series-tooltip__row">
      <span className="dp-explore-series-tooltip__name">{name}</span>
      {value !== null && value !== undefined && (
        <span className="dp-explore-series-tooltip__value">{formatTooltipNumber(value)}</span>
      )}
    </div>
  </>;
}
