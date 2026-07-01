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
    window.localStorage.clear();
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
    expect(screen.getByText(/Maximal 10.?000 Zeilen angezeigt/)).toBeInTheDocument();
    expect(screen.getByLabelText('Diagramm aus Resultat')).toBeInTheDocument();
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

  it('saves successful queries to local history without result rows', async () => {
    const user = userEvent.setup();
    render(<SqlLaboratory context={contextWithRecipes} connector={connector} ready />);

    await user.click(screen.getByRole('button', {name: /Anzahl Datensätze/}));
    await user.click(screen.getByRole('button', {name: 'Ausführen'}));

    expect(await screen.findByLabelText('SQL Ergebnis')).toBeInTheDocument();
    expect(screen.getByRole('button', {name: /select count\(\*\) as anzahl from ch_so_bauinventar/})).toBeInTheDocument();
    const raw = window.localStorage.getItem('datenportal.explore.history.ch.so.bauinventar');
    expect(raw).toContain('select count(*) as anzahl from ch_so_bauinventar;');
    expect(raw).not.toContain('Solothurn');
    expect(raw).not.toContain('rows');
  });

  it('loads a local history item into the editor', async () => {
    const user = userEvent.setup();
    window.localStorage.setItem('datenportal.explore.history.ch.so.bauinventar', JSON.stringify([
      {
        id: 'manual-query',
        sql: 'select gemeindename from ch_so_bauinventar limit 5;',
        executedAt: '2026-07-01T08:00:00.000Z',
        rowCount: 5,
        durationMs: 12
      }
    ]));

    render(<SqlLaboratory context={contextWithRecipes} connector={connector} ready />);
    await user.click(screen.getByRole('button', {name: /select gemeindename from ch_so_bauinventar limit 5/}));

    expect(screen.getByLabelText('SQL bearbeiten')).toHaveValue('select gemeindename from ch_so_bauinventar limit 5;');
  });

  it('clears local history for the current dataset', async () => {
    const user = userEvent.setup();
    window.localStorage.setItem('datenportal.explore.history.ch.so.bauinventar', JSON.stringify([
      {
        id: 'manual-query',
        sql: 'select 1;',
        executedAt: '2026-07-01T08:00:00.000Z'
      }
    ]));

    render(<SqlLaboratory context={contextWithRecipes} connector={connector} ready />);
    await user.click(screen.getByRole('button', {name: 'Historie löschen'}));

    expect(window.localStorage.getItem('datenportal.explore.history.ch.so.bauinventar')).toBeNull();
    expect(screen.getByText('Noch keine lokalen Abfragen für dieses Datenthema.')).toBeInTheDocument();
  });

  it('hides and skips history storage when local history is disabled', async () => {
    const user = userEvent.setup();
    render(<SqlLaboratory
      context={{
        ...contextWithRecipes,
        featureFlags: {...contextWithRecipes.featureFlags, localHistory: false}
      }}
      connector={connector}
      ready
    />);

    expect(screen.queryByText('Lokale Historie')).not.toBeInTheDocument();
    await user.click(screen.getByRole('button', {name: /Anzahl Datensätze/}));
    await user.click(screen.getByRole('button', {name: 'Ausführen'}));

    expect(await screen.findByLabelText('SQL Ergebnis')).toBeInTheDocument();
    expect(window.localStorage.getItem('datenportal.explore.history.ch.so.bauinventar')).toBeNull();
  });
});
