import {render, screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {tableFromArrays} from 'apache-arrow';
import {describe, expect, it, vi} from 'vitest';
import {ResultPanel} from './ResultPanel';
import type {QueryResultState} from './queryResultTypes';

describe('ResultPanel', () => {
  it('renders idle, running and cancelled states as status text', () => {
    const {rerender} = renderPanel(state('idle'));
    expect(screen.getByRole('status')).toHaveTextContent('Noch keine Abfrage ausgeführt.');

    rerender(panel(state('running')));
    expect(screen.getByRole('status')).toHaveTextContent('Abfrage läuft.');

    rerender(panel(state('cancelled')));
    expect(screen.getByRole('status')).toHaveTextContent('Abfrage abgebrochen.');
  });

  it('renders timeout and query failures as alerts', () => {
    const {rerender} = renderPanel(state('timeout'));
    expect(screen.getByRole('alert')).toHaveTextContent('Abfrage nach dem Zeitlimit abgebrochen.');

    rerender(panel({...state('error'), error: 'Spalte nicht gefunden'}));
    expect(screen.getByRole('alert', {name: 'Abfragefehler'})).toHaveTextContent('Spalte nicht gefunden');
  });

  it('renders technical error details below the main query message', () => {
    renderPanel({
      ...state('error'),
      errorKind: 'source-unavailable',
      error: 'Quelldatei nicht erreichbar. Die zugrunde liegende Datendatei konnte momentan nicht geladen werden. Bitte versuchen Sie es später erneut.',
      errorDetail: 'IO Error: No files found that match the pattern "/explore-fixtures/missing.parquet"'
    });

    expect(screen.getByRole('alert', {name: 'Abfragefehler'})).toHaveTextContent('Quelldatei nicht erreichbar.');
    expect(screen.getByText('Technische Details')).toBeInTheDocument();
    expect(screen.getByText(/missing\.parquet/)).toBeInTheDocument();
  });

  it('renders a compact result footer with row limit combobox and no standalone CSV button', async () => {
    const user = userEvent.setup();
    const onRowLimitChange = vi.fn();
    renderPanel(successState(), onRowLimitChange);

    expect(screen.getByText('2 rows')).toBeInTheDocument();
    expect(screen.getByText('17 ms')).toBeInTheDocument();
    expect(screen.queryByRole('button', {name: 'CSV'})).not.toBeInTheDocument();
    expect(screen.getByRole('region', {name: 'SQL Ergebnistabelle'})).toHaveAttribute('tabindex', '0');
    expect(document.querySelector('.dp-explore-result-table__scrollbar[data-orientation="vertical"]')).toBeInTheDocument();
    expect(document.querySelector('.dp-explore-result-table__scrollbar[data-orientation="horizontal"]')).toBeInTheDocument();

    const select = screen.getByLabelText('Anzahl zurückgelieferter Resultatzeilen');
    expect(select).toHaveValue('1000');
    await user.selectOptions(select, '100');
    expect(onRowLimitChange).toHaveBeenCalledWith(100);
  });
});

function renderPanel(result: QueryResultState, onRowLimitChange = vi.fn()) {
  return render(panel(result, onRowLimitChange));
}

function panel(result: QueryResultState, onRowLimitChange = vi.fn()) {
  return (
    <ResultPanel
      result={result}
      rowLimit={1000}
      rowLimitOptions={[100, 1000, 10000]}
      onRowLimitChange={onRowLimitChange}
    />
  );
}

function state(status: QueryResultState['status']): QueryResultState {
  return {
    status,
    sourceSql: '',
    columns: [],
    rows: [],
    rowCount: 0
  };
}

function successState(): QueryResultState {
  const arrowTable = tableFromArrays({
    name: ['Solothurn', 'Olten'],
    jahr: [2024, 2025]
  });
  return {
    status: 'success',
    sourceSql: 'select * from ch_so_bauinventar',
    executedSql: 'select * from ch_so_bauinventar limit 1000',
    columns: ['name', 'jahr'],
    rows: [
      {name: 'Solothurn', jahr: 2024},
      {name: 'Olten', jahr: 2025}
    ],
    rowCount: 2,
    durationMs: 17,
    maxRowsApplied: true,
    arrowTable
  };
}
