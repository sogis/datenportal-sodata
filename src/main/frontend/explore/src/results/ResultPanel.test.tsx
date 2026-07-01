import {render, screen} from '@testing-library/react';
import {describe, expect, it} from 'vitest';
import {ResultPanel} from './ResultPanel';
import type {QueryResultState} from './queryResultTypes';

describe('ResultPanel', () => {
  it('renders idle, running and cancelled states as status text', () => {
    const {rerender} = render(<ResultPanel result={state('idle')} maxRows={10000} datasetId="fixture" />);
    expect(screen.getByRole('status')).toHaveTextContent('Noch keine Abfrage ausgeführt.');

    rerender(<ResultPanel result={state('running')} maxRows={10000} datasetId="fixture" />);
    expect(screen.getByRole('status')).toHaveTextContent('Abfrage läuft.');

    rerender(<ResultPanel result={state('cancelled')} maxRows={10000} datasetId="fixture" />);
    expect(screen.getByRole('status')).toHaveTextContent('Abfrage abgebrochen.');
  });

  it('renders timeout and query failures as alerts', () => {
    const {rerender} = render(<ResultPanel result={state('timeout')} maxRows={10000} datasetId="fixture" />);
    expect(screen.getByRole('alert')).toHaveTextContent('Abfrage nach dem Zeitlimit abgebrochen.');

    rerender(<ResultPanel result={{...state('error'), error: 'Spalte nicht gefunden'}} maxRows={10000} datasetId="fixture" />);
    expect(screen.getByRole('alert')).toHaveTextContent('Spalte nicht gefunden');
  });
});

function state(status: QueryResultState['status']): QueryResultState {
  return {
    status,
    sourceSql: '',
    columns: [],
    rows: [],
    rowCount: 0
  };
}
