import {CartesianGrid, ChartContainer, ChartTooltip, Scatter, ScatterChart, XAxis, YAxis} from '@sqlrooms/recharts';
import {finiteChartNumber, seriesChartConfig, type ChartSeries} from './chartSeries';

interface ScatterPoint {axisX: number; axisY: number; seriesName: string; fill: string}

export function buildScatterRows(rows: Array<Record<string, unknown>>, x: string, item: ChartSeries): ScatterPoint[] {
  return rows.flatMap((row) => {
    const axisX = finiteChartNumber(row[x]);
    const axisY = finiteChartNumber(row[item.name]);
    return axisX === null || axisY === null ? [] : [{axisX, axisY, seriesName: item.name, fill: item.color}];
  });
}

export function ScatterResultChart({rows, x, series}: {
  rows: Array<Record<string, unknown>>; x: string; series: ChartSeries[];
}) {
  return <ChartContainer className="dp-explore-chart__canvas" config={seriesChartConfig(series)}>
    <ScatterChart accessibilityLayer margin={{top: 12, right: 12, bottom: 8, left: 8}}>
      <CartesianGrid stroke="var(--dp-color-border)" />
      <XAxis dataKey="axisX" name={x} type="number" tickLine={false} axisLine={false} />
      <YAxis dataKey="axisY" name="Wert" type="number" tickLine={false} axisLine={false} width={52} />
      <ChartTooltip content={({active, payload}) => {
        const point = payload?.[0]?.payload as ScatterPoint | undefined;
        return active && point ? <div className="dp-explore-series-tooltip">
          <span className="dp-explore-chart__legend-swatch" style={{backgroundColor: point.fill}} />
          <strong>{point.seriesName}</strong>
          <span>{x}: {point.axisX.toLocaleString('de-CH')}</span>
          <span>{point.seriesName}: {point.axisY.toLocaleString('de-CH')}</span>
        </div> : null;
      }} />
      {series.map((item) => <Scatter key={item.key} name={item.name}
        data={buildScatterRows(rows, x, item)} fill={item.color} isAnimationActive={false} />)}
    </ScatterChart>
  </ChartContainer>;
}
