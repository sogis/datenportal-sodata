import {render, screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {describe, expect, it} from 'vitest';
import {ChartPanel} from './ChartPanel';
import {CHART_SINGLE_COLOR_OPTIONS, MULTI_CHART_COLOR_OPTION, MULTI_CHART_COLOR_PALETTE} from './chartColors';
import type {QueryResultState} from '../results/queryResultTypes';

describe('ChartPanel', () => {
  it('renders an empty state before a successful result exists', () => {
    render(<ChartPanel result={{status: 'idle', sourceSql: '', columns: [], rows: [], rowCount: 0}} />);

    expect(screen.getByText('Noch kein SQL-Resultat für ein Diagramm verfügbar.')).toBeInTheDocument();
  });

  it('renders bar controls for grouped category results', () => {
    render(<ChartPanel result={successResult(['gemeinde', 'anzahl'], [
      {gemeinde: 'Solothurn', anzahl: 1n},
      {gemeinde: 'Olten', anzahl: 2n}
    ])} />);

    expect(screen.getByLabelText('Diagramm aus Resultat')).toBeInTheDocument();
    expect(screen.getByRole('heading', {name: 'Diagramm aus Resultat'})).toBeInTheDocument();
    expect(screen.getByLabelText('Diagrammsteuerung')).toBeInTheDocument();
    expect(screen.getByDisplayValue('Balken')).toBeInTheDocument();
    expect(screen.getByDisplayValue('gemeinde')).toBeInTheDocument();
    expect(screen.getByDisplayValue('anzahl')).toBeInTheDocument();
    expect(screen.getByDisplayValue('Dunkelblau')).toBeInTheDocument();
    expect(screen.getByTestId('chart-bar')).toHaveAttribute('data-fill', '#104E8B');
    expect(document.querySelector('[data-chart-type="bar"]')).toBeInTheDocument();
  });

  it('reports only a renderable chart as the PNG export target', () => {
    let exportTarget: HTMLElement | null | undefined;
    render(
      <ChartPanel
        result={successResult(['gemeinde', 'anzahl'], [
          {gemeinde: 'Solothurn', anzahl: 1},
          {gemeinde: 'Olten', anzahl: 2}
        ])}
        onExportTargetChange={(element) => {
          exportTarget = element;
        }}
      />
    );

    expect(exportTarget).toBeInstanceOf(HTMLElement);
    expect(exportTarget?.querySelector('h4')).toHaveTextContent('Diagramm aus Resultat');
    expect(exportTarget?.querySelector('[data-chart-type="bar"]')).toBeInTheDocument();
    expect(exportTarget?.querySelector('[data-export-ignore]')).toBeInTheDocument();
    expect(exportTarget?.querySelector('.dp-explore-chart__controls')).toHaveAttribute('data-export-ignore', 'true');
  });

  it('does not report a target when no chart can be rendered', () => {
    let exportTarget: HTMLElement | null | undefined;
    render(
      <ChartPanel
        result={successResult(['gemeinde'], [{gemeinde: 'Solothurn'}, {gemeinde: 'Olten'}])}
        onExportTargetChange={(element) => {
          exportTarget = element;
        }}
      />
    );

    expect(exportTarget).toBeNull();
  });

  it('offers only the configured additional colors as single colors', async () => {
    const user = userEvent.setup();
    render(<ChartPanel result={successResult(['gemeinde', 'anzahl'], [
      {gemeinde: 'Solothurn', anzahl: 3},
      {gemeinde: 'Olten', anzahl: 2},
      {gemeinde: 'Grenchen', anzahl: 1}
    ])} />);

    const colorSelect = screen.getByLabelText('Farbe') as HTMLSelectElement;
    const labels = Array.from(colorSelect.options).map((option) => option.text);

    expect(labels).toEqual([
      ...CHART_SINGLE_COLOR_OPTIONS.map((option) => option.label),
      MULTI_CHART_COLOR_OPTION.label
    ]);
    expect(labels.join(' ')).not.toMatch(/rot|red/i);

    await user.selectOptions(colorSelect, 'deepSkyBlue2');
    expect(screen.getByTestId('chart-bar')).toHaveAttribute('data-fill', '#00B2EE');

    await user.selectOptions(colorSelect, MULTI_CHART_COLOR_OPTION.value);
    const barColors = screen.getAllByTestId('chart-cell')
      .map((cell) => cell.getAttribute('data-fill'))
      .filter((color): color is string => color !== null);
    expect(new Set(barColors).size).toBeGreaterThan(1);
    barColors.forEach((color) => expect(MULTI_CHART_COLOR_PALETTE).toContain(color));
    expect(barColors).not.toContain('#000000');
  });

  it('uses an allowed preferred chart color', () => {
    render(<ChartPanel
      result={successResult(['gemeinde', 'anzahl'], [
        {gemeinde: 'Solothurn', anzahl: 1},
        {gemeinde: 'Olten', anzahl: 2}
      ])}
      preferred={{type: 'bar', x: 'gemeinde', y: 'anzahl', color: '#E1D700'}}
    />);

    expect(screen.getByDisplayValue('Gold')).toBeInTheDocument();
    expect(screen.getByTestId('chart-bar')).toHaveAttribute('data-fill', '#E1D700');
  });

  it('accepts legacy preferred chart color labels', () => {
    render(<ChartPanel
      result={successResult(['gemeinde', 'anzahl'], [
        {gemeinde: 'Solothurn', anzahl: 1},
        {gemeinde: 'Olten', anzahl: 2}
      ])}
      preferred={{type: 'bar', x: 'gemeinde', y: 'anzahl', color: 'DeepSkyBlue 2'}}
    />);

    expect(screen.getByDisplayValue('Hellblau')).toBeInTheDocument();
    expect(screen.getByTestId('chart-bar')).toHaveAttribute('data-fill', '#00B2EE');
  });

  it('changes chart type, axis fields and row limit', async () => {
    const user = userEvent.setup();
    render(<ChartPanel result={successResult(['x', 'y', 'gruppe'], [
      {x: 1, y: 2, gruppe: 'A'},
      {x: 2, y: 3, gruppe: 'B'}
    ])} />);

    await user.selectOptions(screen.getByDisplayValue('Balken'), 'scatter');
    expect(document.querySelector('[data-chart-type="scatter"]')).toBeInTheDocument();
    expect(screen.getByLabelText('X (Zahl)')).toBeInTheDocument();
    expect(screen.getByLabelText('Y (Zahl)')).toBeInTheDocument();

    await user.selectOptions(screen.getByDisplayValue('Punkte'), 'line');
    expect(document.querySelector('[data-chart-type="line"]')).toBeInTheDocument();
    expect(screen.getByLabelText('X (Zeit/Zahl)')).toBeInTheDocument();

    await user.selectOptions(screen.getByLabelText('Zeilen'), '50');
    expect(screen.getByDisplayValue('50')).toBeInTheDocument();
  });

  it('renders pie and donut charts with renewable segment colors', async () => {
    const user = userEvent.setup();
    render(<ChartPanel result={successResult(['gemeinde', 'anzahl'], [
      {gemeinde: 'Solothurn', anzahl: 3},
      {gemeinde: 'Olten', anzahl: 2},
      {gemeinde: 'Grenchen', anzahl: 1}
    ])} />);

    await user.selectOptions(screen.getByLabelText('Farbe'), MULTI_CHART_COLOR_OPTION.value);
    await user.selectOptions(screen.getByLabelText('Typ'), 'pie');

    expect(document.querySelector('[data-chart-type="pie"]')).toBeInTheDocument();
    expect(screen.getByLabelText('Pie Legende')).toBeInTheDocument();
    const initialColors = screen.getAllByTestId('chart-segment-color')
      .map((swatch) => swatch.getAttribute('style'));
    expect(new Set(initialColors).size).toBeGreaterThan(1);

    await user.click(screen.getByRole('button', {name: 'Farben neu'}));
    const renewedColors = screen.getAllByTestId('chart-segment-color')
      .map((swatch) => swatch.getAttribute('style'));
    expect(renewedColors).not.toEqual(initialColors);

    await user.selectOptions(screen.getByLabelText('Typ'), 'donut');
    expect(document.querySelector('[data-chart-type="donut"]')).toBeInTheDocument();
    expect(screen.getByLabelText('Donut Legende')).toBeInTheDocument();
  });

  it('warns when pie and donut charts have many segments', async () => {
    const user = userEvent.setup();
    const rows = Array.from({length: 13}, (_, index) => ({gemeinde: `G${index}`, anzahl: index + 1}));

    render(<ChartPanel result={successResult(['gemeinde', 'anzahl'], rows)} />);

    await user.selectOptions(screen.getByLabelText('Typ'), 'pie');

    expect(screen.getByText(/Viele Segmente/)).toBeInTheDocument();
  });

  it('shows a warning for large bar results', () => {
    const rows = Array.from({length: 501}, (_, index) => ({gemeinde: `G${index}`, anzahl: index}));

    render(<ChartPanel result={successResult(['gemeinde', 'anzahl'], rows)} />);

    expect(screen.getByText(/Viele Datenpunkte/)).toBeInTheDocument();
    expect(screen.getByText("Diagramm zeigt 500 von 501 Resultatzeilen.")).toBeInTheDocument();
  });

  it('renders a histogram for one numeric column', () => {
    render(<ChartPanel result={successResult(['wert'], [{wert: 1}, {wert: 2}, {wert: 3}])} />);

    expect(screen.getByDisplayValue('Histogramm')).toBeInTheDocument();
    expect(screen.getByDisplayValue('wert')).toBeInTheDocument();
    expect(document.querySelector('[data-chart-type="histogram"]')).toBeInTheDocument();
  });
});

function successResult(columns: string[], rows: Array<Record<string, unknown>>): QueryResultState {
  return {
    status: 'success',
    sourceSql: 'select * from q',
    executedSql: 'select * from q',
    columns,
    rows,
    rowCount: rows.length,
    maxRowsApplied: false
  };
}
