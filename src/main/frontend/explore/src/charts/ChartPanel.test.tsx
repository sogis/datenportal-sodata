import {render, screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {describe, expect, it} from 'vitest';
import {ChartPanel} from './ChartPanel';
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
    expect(document.querySelector('[data-chart-type="bar"]')).toBeInTheDocument();
  });

  it('changes chart type, axis fields and row limit', async () => {
    const user = userEvent.setup();
    render(<ChartPanel result={successResult(['x', 'y', 'gruppe'], [
      {x: 1, y: 2, gruppe: 'A'},
      {x: 2, y: 3, gruppe: 'B'}
    ])} />);

    await user.selectOptions(screen.getByDisplayValue('Balken'), 'scatter');
    expect(document.querySelector('[data-chart-type="scatter"]')).toBeInTheDocument();

    await user.selectOptions(screen.getByLabelText('Zeilen'), '50');
    expect(screen.getByDisplayValue('50')).toBeInTheDocument();
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
