import {render, screen, waitFor} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {tableFromArrays} from 'apache-arrow';
import {beforeEach, describe, expect, it, vi} from 'vitest';
import type {DuckDbConnector} from '@sqlrooms/duckdb';
import {SqlLaboratory} from './SqlLaboratory';
import {sampleExploreContext} from '../test/sampleExploreContext';
import {exportChartAsPng} from '../results/ChartExport';

vi.mock('../results/ChartExport', () => ({
  exportChartAsPng: vi.fn().mockResolvedValue(undefined)
}));

const contextWithRecipes = {
  ...sampleExploreContext,
  recipes: [
    {
      id: 'ch_so_bauinventar-preview',
      title: 'Vorschau',
      description: 'Zeigt die ersten Zeilen.',
      tableId: 'ch_so_bauinventar',
      category: 'preview' as const,
      sql: 'SELECT *\nFROM opendata.ch_so_bauinventar;'
    },
    {
      id: 'ch_so_bauinventar-gemeinde-count',
      title: 'Nach «gemeindename» gruppieren',
      description: 'Zählt Datensätze pro Gemeinde.',
      tableId: 'ch_so_bauinventar',
      category: 'category' as const,
      sql: 'SELECT gemeindename, count(*) AS anzahl\nFROM opendata.ch_so_bauinventar\nGROUP BY gemeindename;',
      preferredChart: {
        type: 'pie' as const,
        x: 'gemeindename',
        y: 'anzahl',
        title: 'Anzahl nach Gemeinde'
      }
    }
  ]
};

const neutralSourceError =
  'Quelldatei nicht erreichbar. Die zugrunde liegende Datendatei konnte momentan nicht geladen werden. Bitte versuchen Sie es später erneut.';

describe('SqlLaboratory', () => {
  const query = vi.fn();
  const connector = {query} as unknown as DuckDbConnector;

  beforeEach(() => {
    window.localStorage.clear();
    query.mockReset().mockResolvedValue(tableFromArrays({
      gemeindename: ['Solothurn', 'Olten'],
      anzahl: [1, 1]
    }));
    vi.mocked(exportChartAsPng).mockClear();
  });

  it('renders the initial registered-view query without secondary panels', () => {
    render(<SqlLaboratory context={contextWithRecipes} connector={connector} ready />);

    const editor = screen.getByLabelText('SQL bearbeiten');
    expect(editor).toHaveValue('SELECT *\nFROM opendata.ch_so_bauinventar;');
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
    expect(screen.getByLabelText('SQL Aktionen').querySelector('.dp-explore-sql-toolbar__export')).not.toBeInTheDocument();
    expect(screen.getByLabelText('SQL Resultat').querySelector('.dp-explore-result-export')).toBeInTheDocument();
    expect(screen.getByRole('button', {name: 'CSV'})).toBeDisabled();
    expect(screen.getByLabelText('SQL Aktionen').querySelector('.dp-explore-sql-toolbar__leading')).toBeInTheDocument();
    expect(screen.getByLabelText('Beispielabfrage auswählen')).toHaveValue('ch_so_bauinventar-preview');
    expect(screen.getByRole('option', {name: 'Nach «gemeindename» gruppieren'})).toBeInTheDocument();
    expect(screen.getByRole('group', {name: 'Resultatansicht'})).toBeInTheDocument();
    expect(screen.getByRole('button', {name: 'Tabelle'})).toHaveAttribute('aria-pressed', 'true');
    expect(screen.getByRole('button', {name: 'Diagramm'})).toHaveAttribute('aria-pressed', 'false');
    expect(screen.queryByText('Beispielabfragen')).not.toBeInTheDocument();
    expect(screen.queryByText('Lokale Historie')).not.toBeInTheDocument();
    expect(screen.queryByLabelText('Diagramm aus Resultat')).not.toBeInTheDocument();
  });

  it('loads a compact recipe selection without executing it', async () => {
    const user = userEvent.setup();
    render(<SqlLaboratory context={contextWithRecipes} connector={connector} ready />);

    await user.selectOptions(screen.getByLabelText('Beispielabfrage auswählen'), 'ch_so_bauinventar-gemeinde-count');

    expect(screen.getByLabelText('SQL bearbeiten')).toHaveValue(
      'SELECT gemeindename, count(*) AS anzahl\nFROM opendata.ch_so_bauinventar\nGROUP BY gemeindename;'
    );
    expect(query).not.toHaveBeenCalled();
  });

  it('builds the fallback initial query with uppercase keywords and an unquoted table name', () => {
    render(<SqlLaboratory context={{...contextWithRecipes, recipes: []}} connector={connector} ready />);

    expect(screen.getByLabelText('SQL bearbeiten')).toHaveValue('SELECT *\nFROM opendata.ch_so_bauinventar;');
  });

  it('keeps the table export in the result header when charts are disabled', () => {
    render(<SqlLaboratory context={{...contextWithRecipes, chartsEnabled: false}} connector={connector} ready />);

    const resultPane = screen.getByLabelText('SQL Resultat');
    expect(resultPane.querySelector('.dp-explore-result-export')).toBeInTheDocument();
    expect(screen.queryByRole('group', {name: 'Resultatansicht'})).not.toBeInTheDocument();
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

  it('enables transfer to R only for successful result rows', async () => {
    const user = userEvent.setup();
    const onTransferToR = vi.fn();
    render(<SqlLaboratory context={contextWithRecipes} connector={connector} ready onTransferToR={onTransferToR} />);

    expect(screen.getByRole('button', {name: 'Nach R übernehmen'})).toBeDisabled();

    await user.click(screen.getByRole('button', {name: 'Ausführen'}));
    expect(await screen.findByLabelText('SQL Ergebnis')).toBeInTheDocument();
    await user.click(screen.getByRole('button', {name: 'Nach R übernehmen'}));

    expect(onTransferToR).toHaveBeenCalledWith(expect.objectContaining({
      rowCount: 2,
      columns: expect.arrayContaining([
        expect.objectContaining({name: 'gemeindename', rType: 'character'}),
        expect.objectContaining({name: 'anzahl', rType: 'numeric'})
      ])
    }));
  });

  it('shows a neutral source-file error and keeps the query controls usable', async () => {
    const user = userEvent.setup();
    query.mockRejectedValueOnce(new Error('IO Error: No files found that match the pattern "/explore-fixtures/missing.parquet"'));
    render(<SqlLaboratory context={contextWithRecipes} connector={connector} ready />);

    await user.click(screen.getByRole('button', {name: 'Ausführen'}));

    expect(await screen.findByRole('alert', {name: 'Abfragefehler'})).toHaveTextContent(neutralSourceError);
    expect(screen.getByText('Technische Details')).toBeInTheDocument();
    expect(screen.getByText(/missing\.parquet/)).toBeInTheDocument();
    expect(screen.getByRole('button', {name: 'Ausführen'})).toBeEnabled();
  });

  it('switches from table to chart for the current result without changing the query', async () => {
    const user = userEvent.setup();
    render(<SqlLaboratory context={contextWithRecipes} connector={connector} ready />);

    await user.click(screen.getByRole('button', {name: 'Ausführen'}));
    expect(await screen.findByLabelText('SQL Ergebnis')).toBeInTheDocument();
    expect(screen.queryByLabelText('Diagramm aus Resultat')).not.toBeInTheDocument();

    await user.click(screen.getByRole('button', {name: 'Diagramm'}));

    expect(screen.getByRole('button', {name: 'Diagramm'})).toHaveAttribute('aria-pressed', 'true');
    expect(await screen.findByLabelText('Diagramm aus Resultat')).toBeInTheDocument();
    expect(document.querySelector('[data-chart-type="bar"]')).toBeInTheDocument();
    expect(screen.getByRole('button', {name: 'Diagramm als PNG herunterladen'})).toBeEnabled();
    expect(screen.queryByRole('button', {name: 'CSV'})).not.toBeInTheDocument();
    await user.click(screen.getByRole('button', {name: 'Diagramm als PNG herunterladen'}));
    expect(exportChartAsPng).toHaveBeenCalledWith(expect.any(HTMLElement), contextWithRecipes.datasetId);
    expect(query).toHaveBeenCalledTimes(1);
  });

  it('disables PNG when the result cannot be rendered as a chart', async () => {
    const user = userEvent.setup();
    query.mockResolvedValueOnce(tableFromArrays({gemeindename: ['Solothurn', 'Olten']}));
    render(<SqlLaboratory context={contextWithRecipes} connector={connector} ready />);

    await user.click(screen.getByRole('button', {name: 'Ausführen'}));
    await screen.findByLabelText('SQL Ergebnis');
    await user.click(screen.getByRole('button', {name: 'Diagramm'}));

    expect(screen.getByRole('button', {name: 'Diagramm als PNG herunterladen'})).toBeDisabled();
  });

  it('uses a preferred chart only for unchanged selected recipe SQL', async () => {
    const user = userEvent.setup();
    render(<SqlLaboratory context={contextWithRecipes} connector={connector} ready />);

    await user.selectOptions(screen.getByLabelText('Beispielabfrage auswählen'), 'ch_so_bauinventar-gemeinde-count');
    await user.click(screen.getByRole('button', {name: 'Ausführen'}));
    await screen.findByLabelText('SQL Ergebnis');
    await user.click(screen.getByRole('button', {name: 'Diagramm'}));

    expect(await screen.findByLabelText('Diagramm aus Resultat')).toBeInTheDocument();
    expect(document.querySelector('[data-chart-type="pie"]')).toBeInTheDocument();
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

  it('shows export errors in the result header', async () => {
    const user = userEvent.setup();
    render(<SqlLaboratory context={contextWithRecipes} connector={connector} ready />);

    await user.click(screen.getByRole('button', {name: 'Ausführen'}));
    await screen.findByLabelText('SQL Ergebnis');
    await user.click(screen.getByRole('button', {name: 'Exportformat auswählen'}));
    await user.click(screen.getByRole('menuitem', {name: 'XLSX'}));

    const alert = await screen.findByRole('alert');
    expect(alert).toHaveTextContent('Export konnte nicht erstellt werden');
    expect(screen.getByLabelText('SQL Resultat').contains(alert)).toBe(true);
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

  it('does not start a second query and allows a new query after cancellation settles', async () => {
    const user = userEvent.setup();
    const first = deferredQuery();
    query.mockReturnValueOnce(first.handle).mockResolvedValueOnce(tableFromArrays({gemeindename: ['Olten']}));
    const {unmount} = render(<SqlLaboratory context={contextWithRecipes} connector={connector} ready />);

    await user.click(screen.getByRole('button', {name: 'Ausführen'}));
    expect(await screen.findByText('Abfrage läuft.')).toBeInTheDocument();
    await user.click(screen.getByRole('button', {name: 'Ausführen'}));
    expect(query).toHaveBeenCalledTimes(1);

    await user.click(screen.getByRole('button', {name: 'Abbrechen'}));
    await waitFor(() => expect(first.handle.cancel).toHaveBeenCalledTimes(1));
    await waitFor(() => expect(screen.getByRole('button', {name: 'Ausführen'})).toBeEnabled());

    await user.click(screen.getByRole('button', {name: 'Ausführen'}));
    expect(await screen.findByText('Olten')).toBeInTheDocument();
    expect(query).toHaveBeenCalledTimes(2);
    unmount();
  });

  it('cancels an active query when the laboratory unmounts', async () => {
    const first = deferredQuery();
    query.mockReturnValue(first.handle);
    const {unmount} = render(<SqlLaboratory context={contextWithRecipes} connector={connector} ready />);

    await userEvent.setup().click(screen.getByRole('button', {name: 'Ausführen'}));
    await screen.findByText('Abfrage läuft.');
    unmount();

    expect(first.handle.cancel).toHaveBeenCalledTimes(1);
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

});

function deferredQuery() {
  let resolveQuery!: (value: ReturnType<typeof tableFromArrays>) => void;
  let rejectQuery!: (error: Error) => void;
  const promise = new Promise<ReturnType<typeof tableFromArrays>>((resolve, reject) => {
    resolveQuery = resolve;
    rejectQuery = reject;
  });
  const controller = new AbortController();
  const cancel = vi.fn(async () => {
    controller.abort();
    rejectQuery(Object.assign(new Error('Query cancelled.'), {name: 'AbortError'}));
    await promise.catch(() => undefined);
  });
  const handle = {
    result: promise,
    signal: controller.signal,
    cancel,
    then: promise.then.bind(promise),
    catch: promise.catch.bind(promise),
    finally: promise.finally.bind(promise)
  };
  return {handle, resolveQuery};
}
