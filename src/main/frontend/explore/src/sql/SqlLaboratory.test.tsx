import {render, screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {tableFromArrays} from 'apache-arrow';
import {beforeEach, describe, expect, it, vi} from 'vitest';
import type {DuckDbConnector} from '@sqlrooms/duckdb';
import {SqlLaboratory} from './SqlLaboratory';
import {sampleExploreContext} from '../test/sampleExploreContext';

const contextWithRecipes = {
  ...sampleExploreContext,
  recipes: [
    ...sampleExploreContext.recipes,
    {
      id: 'ch_so_bauinventar-count',
      title: 'Anzahl Datensätze',
      description: 'Zählt alle Zeilen.',
      tableId: 'ch_so_bauinventar',
      category: 'profile' as const,
      sql: 'select count(*) as anzahl from ch_so_bauinventar;'
    }
  ]
};

describe('SqlLaboratory', () => {
  const query = vi.fn();
  const connector = {query} as unknown as DuckDbConnector;

  beforeEach(() => {
    query.mockReset().mockResolvedValue(tableFromArrays({
      gemeindename: ['Solothurn', 'Olten'],
      anzahl: [1, 1]
    }));
  });

  it('selects recipes and marks edited SQL as modified', async () => {
    const user = userEvent.setup();
    render(<SqlLaboratory context={contextWithRecipes} connector={connector} ready />);

    await user.click(screen.getByRole('button', {name: /Anzahl Datensätze/}));

    expect(screen.getByRole('button', {name: /Anzahl Datensätze/})).toHaveAttribute('aria-pressed', 'true');
    expect(screen.getByLabelText('SQL bearbeiten')).toHaveValue('select count(*) as anzahl from ch_so_bauinventar;');

    await user.type(screen.getByLabelText('SQL bearbeiten'), ' -- angepasst');

    expect(screen.getByText('Anzahl Datensätze · geändert')).toBeInTheDocument();
  });

  it('runs SQL through the guard and renders result rows', async () => {
    const user = userEvent.setup();
    render(<SqlLaboratory context={contextWithRecipes} connector={connector} ready />);

    await user.click(screen.getByRole('button', {name: /Anzahl Datensätze/}));
    await user.click(screen.getByRole('button', {name: 'Ausführen'}));

    expect(await screen.findByLabelText('SQL Ergebnis')).toBeInTheDocument();
    expect(screen.getByText('Solothurn')).toBeInTheDocument();
    expect(screen.getByText('Olten')).toBeInTheDocument();
    expect(screen.getByText("Maximal 10'000 Zeilen angezeigt")).toBeInTheDocument();
  });

  it('shows query guard errors for blocked SQL', async () => {
    const user = userEvent.setup();
    render(<SqlLaboratory context={contextWithRecipes} connector={connector} ready />);

    await user.clear(screen.getByLabelText('SQL bearbeiten'));
    await user.type(screen.getByLabelText('SQL bearbeiten'), 'drop table ch_so_bauinventar');
    await user.click(screen.getByRole('button', {name: 'Ausführen'}));

    expect(screen.getByText('Diese Abfrage ist im lokalen SQL-Labor nicht erlaubt.')).toBeInTheDocument();
    expect(query).not.toHaveBeenCalled();
  });

  it('copies SQL with visible feedback', async () => {
    const user = userEvent.setup();
    render(<SqlLaboratory context={contextWithRecipes} connector={connector} ready />);

    await user.click(screen.getByRole('button', {name: 'SQL kopieren'}));

    expect(await screen.findByRole('button', {name: 'SQL kopiert'})).toBeInTheDocument();
  });
});
