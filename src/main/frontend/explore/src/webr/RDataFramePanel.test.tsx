import {render, screen, within} from '@testing-library/react';
import {describe, expect, it} from 'vitest';
import type {SqlResultSnapshot} from '../results/sqlResultSnapshot';
import {sampleExploreContext} from '../test/sampleExploreContext';
import {RDataFramePanel} from './RDataFramePanel';

describe('RDataFramePanel', () => {
  it('renders R type badges before column names with schema-explorer color classes', () => {
    render(<RDataFramePanel snapshot={snapshot()} laboratory={sampleExploreContext.rLaboratory} />);

    expect(screen.getByRole('heading', {name: 'Datengrundlage'})).toBeInTheDocument();
    expect(screen.queryByRole('heading', {name: 'Data Frame'})).not.toBeInTheDocument();
    expect(screen.getByText('Data Frame', {selector: 'dt'})).toBeInTheDocument();
    expect(screen.getByText('Anzahl Zeilen')).toBeInTheDocument();
    expect(screen.getByText('Anzahl Spalten')).toBeInTheDocument();
    expect(screen.getByRole('heading', {name: 'Spalten'})).toBeInTheDocument();

    const items = within(screen.getByRole('list')).getAllByRole('listitem');

    expect(items).toHaveLength(6);
    expectColumn(items[0], 'integer', 'jahr', 'numeric');
    expectColumn(items[1], 'character', 'gemeinde', 'text');
    expectColumn(items[2], 'numeric', 'messwert', 'numeric');
    expectColumn(items[3], 'logical', 'aktiv', 'boolean');
    expectColumn(items[4], 'Date', 'datum', 'other');
    expectColumn(items[5], 'POSIXct', 'zeitpunkt', 'other');
  });
});

function expectColumn(item: HTMLElement, rType: string, name: string, typeClass: string): void {
  expect(item.children[0]).toHaveTextContent(rType);
  expect(item.children[0]).toHaveClass('dp-r-dataframe-panel__type');
  expect(item.children[0]).toHaveClass(`dp-r-dataframe-panel__type--${typeClass}`);
  expect(item.children[1]).toHaveTextContent(name);
}

function snapshot(): SqlResultSnapshot {
  return {
    sourceSql: 'select * from daten',
    executedSql: 'select * from daten limit 1000',
    rowCount: 1,
    columns: [
      {name: 'jahr', duckdbType: 'INTEGER', nullable: false, roles: ['year'], rType: 'integer'},
      {name: 'gemeinde', duckdbType: 'VARCHAR', nullable: true, roles: ['category'], rType: 'character'},
      {name: 'messwert', duckdbType: 'DOUBLE', nullable: true, roles: ['measure'], rType: 'numeric'},
      {name: 'aktiv', duckdbType: 'BOOLEAN', nullable: true, roles: ['unknown'], rType: 'logical'},
      {name: 'datum', duckdbType: 'DATE', nullable: true, roles: ['date'], rType: 'Date'},
      {name: 'zeitpunkt', duckdbType: 'TIMESTAMP', nullable: true, roles: ['date'], rType: 'POSIXct'}
    ],
    rows: [[2026, 'Solothurn', 12.5, true, '2026-07-07', '2026-07-07T18:00:00.000Z']]
  };
}
