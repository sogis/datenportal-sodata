import type {ChartSeries} from './chartSeries';

export function SeriesLegend({series}: {series: ChartSeries[]}) {
  if (series.length < 2) return null;
  return <ul className="dp-explore-chart__legend dp-explore-chart__legend--series" aria-label="Datenreihen Legende">
    {series.map((item) => <li key={item.key}>
      <span className="dp-explore-chart__legend-swatch" style={{backgroundColor: item.color}} aria-hidden="true" />
      <span>{item.name}</span>
    </li>)}
  </ul>;
}
