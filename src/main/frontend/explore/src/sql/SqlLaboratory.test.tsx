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
    {
      id: 'ch_so_bauinventar-preview',
      title: 'Vorschau',
      description: 'Zeigt die ersten Zeilen.',
      tableId: 'ch_so_bauinventar',
      category: 'preview' as const,
      sql: 'select * from ch_so_bauinventar;'
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

  it('renders the initial registered-view query without secondary panels', () => {
    render(<SqlLaboratory context={contextWithRecipes} connector={connector} ready />);

    const editor = screen.getByLabelText('SQL bearbeiten');
    expect(editor).toHaveValue('select * from ch_so_bauinventar;');
    expect(editor).toHaveAttribute('data-has-connector', 'false');
    expect(editor).toHaveAttribute('data-table-schemas', 'ch_so_bauinventar');
    expect(editor).toHaveAttribute('data-table-columns', 'egid,gemeindename');
    expect(editor).toHaveAttribute('data-latest-schemas', 'ch_so_bauinventar');
    expect(editor).toHaveAttribute('data-custom-keywords', '');
    expect(screen.queryByRole('button', {name: 'Abfrage 1'})).not.toBeInTheDocument();
    expect(screen.queryByRole('heading', {name: 'SQL'})).not.toBeInTheDocument();
    expect(screen.queryByRole('heading', {name: 'Resultat'})).not.toBeInTheDocument();
    expect(screen.getByLabelText('SQL-Editor und Resultattabelle Grösse anpassen')).toBeInTheDocument();
    expect(screen.getByLabelText('SQL Aktionen').querySelector('.dp-explore-sql-toolbar__actions')).toBeInTheDocument();
    expect(screen.getByLabelText('SQL Aktionen').querySelector('.dp-explore-sql-toolbar__export')).toBeInTheDocument();
    expect(screen.queryByText('Beispielabfragen')).not.toBeInTheDocument();
    expect(screen.queryByText('Lokale Historie')).not.toBeInTheDocument();
    expect(screen.queryByLabelText('Diagramm aus Resultat')).not.toBeInTheDocument();
  });

  it('renders a red run button with the Bootstrap play icon', () => {
    render(<SqlLaboratory context={contextWithRecipes} connector={connector} ready />);

    const runButton = screen.getByRole('button', {name: 'Ausführen'});
    expect(runButton).toHaveClass('dp-explore-button--primary');
    expect(runButton.querySelector('svg.bi-play-fill')).toBeInTheDocument();
  });

  it('runs SQL through the guard and renders a compact result table', async () => {
    const user = userEvent.setup();
    render(<SqlLaboratory context={contextWithRecipes} connector={connector} ready />);

    await user.click(screen.getByRole('button', {name: 'Ausführen'}));

    expect(await screen.findByLabelText('SQL Ergebnis')).toBeInTheDocument();
    expect(screen.getByText('Solothurn')).toBeInTheDocument();
    expect(screen.getByText('Olten')).toBeInTheDocument();
    expect(screen.getByText('gemeindename')).toBeInTheDocument();
    expect(screen.getAllByText(/Dictionary|Float64|Value/).length).toBeGreaterThan(0);
    expect(screen.getByRole('button', {name: 'CSV'})).toBeEnabled();
    expect(query).toHaveBeenCalledWith(
      expect.stringContaining('limit 1000'),
      expect.objectContaining({signal: expect.any(AbortSignal)})
    );
  });

  it('uses the selected row limit in the query guard', async () => {
    const user = userEvent.setup();
    render(<SqlLaboratory context={contextWithRecipes} connector={connector} ready />);

    await user.click(screen.getByRole('button', {name: 'Ausführen'}));
    await screen.findByLabelText('SQL Ergebnis');
    await user.selectOptions(screen.getByLabelText('Anzahl zurückgelieferter Resultatzeilen'), '100');
    await user.click(screen.getByRole('button', {name: 'Ausführen'}));

    expect(query).toHaveBeenLastCalledWith(
      expect.stringMatching(/\blimit 100$/),
      expect.objectContaining({signal: expect.any(AbortSignal)})
    );
  });

  it('opens the result export split-button menu', async () => {
    const user = userEvent.setup();
    render(<SqlLaboratory context={contextWithRecipes} connector={connector} ready />);

    await user.click(screen.getByRole('button', {name: 'Ausführen'}));
    await screen.findByLabelText('SQL Ergebnis');
    await user.click(screen.getByRole('button', {name: 'Exportformat auswählen'}));

    expect(screen.getByRole('menu', {name: 'Exportformate'})).toBeInTheDocument();
    expect(screen.getAllByRole('menuitem').map((item) => item.textContent)).toEqual(['CSV', 'XLSX', 'Parquet']);
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

  it('copies SQL with visible compact feedback', async () => {
    const user = userEvent.setup();
    render(<SqlLaboratory context={contextWithRecipes} connector={connector} ready />);

    const copyButton = screen.getByRole('button', {name: 'SQL kopieren'});
    expect(copyButton).toHaveClass('dp-explore-button--copy');
    expect(copyButton).toHaveClass('dp-explore-button--secondary');
    await user.click(copyButton);

    expect(await screen.findByRole('button', {name: '✓ SQL kopiert'})).toHaveClass('dp-explore-button--copy');
  });

  it('does not save hidden local history while running queries', async () => {
    const user = userEvent.setup();
    render(<SqlLaboratory
      context={{
        ...contextWithRecipes,
        featureFlags: {...contextWithRecipes.featureFlags, localHistory: true}
      }}
      connector={connector}
      ready
    />);

    await user.click(screen.getByRole('button', {name: 'Ausführen'}));

    expect(await screen.findByLabelText('SQL Ergebnis')).toBeInTheDocument();
    expect(window.localStorage.getItem('datenportal.explore.history.ch.so.bauinventar')).toBeNull();
  });
});
