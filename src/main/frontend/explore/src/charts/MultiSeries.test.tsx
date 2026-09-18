import {render, screen, within} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {describe, expect, it} from 'vitest';
import {ChartPanel} from './ChartPanel';
import {buildChartSeries, buildSeriesRows} from './chartSeries';
import {buildScatterRows} from './ScatterResultChart';
import type {QueryResultState} from '../results/queryResultTypes';

const result: QueryResultState = {status: 'success', sourceSql: '', columns: ['jahr', 'gemeinde', 'a.b', 'c d', 'e'],
  rows: [{jahr: 2020, gemeinde: 'A', 'a.b': 2, 'c d': 3, e: 4},
    {jahr: 2021, gemeinde: 'B', 'a.b': null, 'c d': 5, e: 6}], rowCount: 2};
const trigger = () => screen.getByRole('button', {name: 'Y (Zahl)'});
const colors = () => Array.from(screen.getByRole('list', {name: 'Datenreihen Legende'}).children)
  .map((li) => ({name: li.textContent, color: (li.firstElementChild as HTMLElement).style.backgroundColor}));

describe('multiple Y attributes', () => {
  it('keeps selections, assigns stable colors, and respects manual color overrides', async () => {
    const user = userEvent.setup();
    render(<ChartPanel result={result} />);
    await user.click(trigger());
    expect(screen.getByLabelText('a.b')).toBeDisabled();
    await user.click(screen.getByLabelText('c d'));
    expect(screen.getByRole('dialog')).toBeInTheDocument();
    expect(trigger()).toHaveTextContent('a.b+1');
    expect(screen.getByLabelText('Farbe')).toHaveValue('multi');
    const before = colors();
    await user.click(screen.getByLabelText('e'));
    expect(colors().slice(0, 2)).toEqual(before);
    await user.click(screen.getByLabelText('a.b'));
    expect(colors()[0]).toEqual(before[1]);
    await user.keyboard('{Escape}');
    expect(trigger()).toHaveFocus();
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
    await user.selectOptions(screen.getByLabelText('Farbe'), 'gold');
    expect(new Set(colors().map((item) => item.color)).size).toBe(1);
    await user.selectOptions(screen.getByLabelText('Typ'), 'pie');
    expect(screen.getByLabelText('Wert')).toHaveValue('c d');
    await user.selectOptions(screen.getByLabelText('Wert'), 'e');
    await user.selectOptions(screen.getByLabelText('Typ'), 'line');
    expect(trigger()).toHaveTextContent('c d+1');
    expect(screen.getByLabelText('Farbe')).toHaveValue('multi');
    await user.selectOptions(screen.getByLabelText('Typ'), 'histogram');
    await user.selectOptions(screen.getByLabelText('Typ'), 'scatter');
    expect(screen.getAllByTestId('chart-scatter')).toHaveLength(2);
    await user.selectOptions(screen.getByLabelText('Typ'), 'bar');
    expect(screen.getAllByTestId('chart-bar')).toHaveLength(2);
    expect(screen.queryByTestId('chart-cell')).not.toBeInTheDocument();
    const palette = colors();
    await user.click(screen.getByRole('button', {name: 'Farben neu'}));
    expect(colors()).not.toEqual(palette);
  });

  it('supports keyboard navigation, closes outside, and resets for a new result', async () => {
    const user = userEvent.setup();
    const {rerender} = render(<ChartPanel result={result} />);
    await user.click(trigger());
    await user.keyboard('{End} ');
    expect(screen.getByLabelText('e')).toBeChecked();
    await user.tab();
    expect(screen.getByLabelText('Farbe')).toHaveFocus();
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
    await user.click(trigger());
    await user.click(screen.getByRole('heading'));
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
    rerender(<ChartPanel result={{...result, rows: [...result.rows]}} />);
    expect(trigger()).toHaveTextContent('a.b');
    expect(screen.queryByRole('list', {name: 'Datenreihen Legende'})).not.toBeInTheDocument();
    expect(screen.getByLabelText('Farbe')).toHaveValue('dodgerBlue4');
  });

  it('renders one line per attribute and retains multicolor when reduced to one', async () => {
    const user = userEvent.setup();
    render(<ChartPanel result={result} />);
    await user.click(trigger());
    await user.click(screen.getByLabelText('c d'));
    expect(screen.getAllByTestId('chart-line')).toHaveLength(2);
    expect(within(screen.getByRole('list', {name: 'Datenreihen Legende'})).getByText('a.b')).toBeInTheDocument();
    await user.click(screen.getByLabelText('a.b'));
    expect(screen.getAllByTestId('chart-line')).toHaveLength(1);
    expect(screen.getByLabelText('Farbe')).toHaveValue('multi');
    expect(screen.getByLabelText('c d')).toBeDisabled();
  });

  it('allocates distinct colors for widely separated columns and rotates the palette', () => {
    const columns = Array.from({length: 25}, (_, index) => `column${index}`);
    const chosen = [columns[0], columns[12], columns[24]];
    const series = buildChartSeries(chosen, columns, '#104E8B', 'seed', 0, chosen);
    expect(new Set(series.map((item) => item.color)).size).toBe(3);
    expect(buildChartSeries(chosen.slice(1), columns, '#104E8B', 'seed', 0, chosen))
      .toEqual(series.slice(1));
    expect(buildChartSeries(chosen, columns, '#104E8B', 'seed', 1, chosen)).not.toEqual(series);
  });

  it('uses safe keys and preserves missing values independently for each series', () => {
    const series = buildChartSeries(['a.b', 'c d'], result.columns, '#104E8B');
    const rows = buildSeriesRows([{jahr: 2020, 'a.b': null, 'c d': 0},
      {jahr: 2021, 'a.b': Infinity, 'c d': 4n},
      {jahr: 2022, 'a.b': '', 'c d': '5'}], 'jahr', series);
    expect(rows).toEqual([{axisX: 2020, series2: null, series3: 0},
      {axisX: 2021, series2: null, series3: 4}, {axisX: 2022, series2: null, series3: 5}]);
    expect(buildScatterRows(result.rows, 'jahr', series[0])).toEqual([
      {axisX: 2020, axisY: 2, seriesName: 'a.b', fill: '#104E8B'}]);
  });
});
