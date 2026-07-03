import {useCallback, useEffect, useMemo, useRef, useState} from 'react';
import {makeQualifiedTableName, type DataTable, type DuckDbConnector, type QueryHandle} from '@sqlrooms/duckdb';
import type {Table} from 'apache-arrow';
import {Panel, PanelGroup, PanelResizeHandle} from 'react-resizable-panels';
import type {ExploreContextDto, ExploreRecipeDto, ExploreTableDto} from '../app/ExploreContext';
import {ChartPanel} from '../charts/ChartPanel';
import {hasResultLimitApplied, normalizeSqlForExecution, queryTimeoutMessage} from '../duckdb/querySafety';
import {ResultPanel} from '../results/ResultPanel';
import {idleQueryResult, type QueryResultState} from '../results/queryResultTypes';
import {successfulQueryResult} from '../results/arrowResult';
import {exportQueryResult, type ResultExportFormat} from '../results/ResultExport';
import {copyTextToClipboard} from './clipboard';
import {SqlEditorField} from './SqlEditorField';
import {SqlToolbar} from './SqlToolbar';

const DEFAULT_ROW_LIMIT = 1000;
const ROW_LIMIT_OPTIONS = [100, 1000, 10000] as const;
type ResultView = 'table' | 'chart';

export function SqlLaboratory({
  context,
  connector,
  ready,
  onResultChange
}: {
  context: ExploreContextDto;
  connector?: DuckDbConnector;
  ready: boolean;
  onResultChange?: (result: QueryResultState) => void;
}) {
  const initialRecipe = useMemo(() => context.recipes[0], [context.recipes]);
  const initialSql = useMemo(() => initialRecipe?.sql ?? buildInitialSql(context.tables[0]), [
    context.tables,
    initialRecipe
  ]);
  const rowLimitOptions = useMemo(() => {
    const options = ROW_LIMIT_OPTIONS.filter((limit) => limit <= context.execution.maxResultRows);
    return options.length > 0 ? options : [context.execution.maxResultRows];
  }, [context.execution.maxResultRows]);
  const [sql, setSql] = useState(initialSql);
  const [selectedRecipeId, setSelectedRecipeId] = useState<string | undefined>(initialRecipe?.id);
  const [result, setResult] = useState<QueryResultState>(idleQueryResult);
  const [resultView, setResultView] = useState<ResultView>('table');
  const [copied, setCopied] = useState(false);
  const [rowLimit, setRowLimit] = useState(Math.min(DEFAULT_ROW_LIMIT, context.execution.maxResultRows));
  const [exportingFormat, setExportingFormat] = useState<ResultExportFormat | null>(null);
  const [exportError, setExportError] = useState<string | null>(null);
  const tableSchemas = useMemo(() => context.tables.map(toSqlRoomsDataTable), [context.tables]);
  const getLatestSchemas = useCallback(() => ({tableSchemas}), [tableSchemas]);
  const selectedRecipe = useMemo(
    () => context.recipes.find((recipe) => recipe.id === selectedRecipeId),
    [context.recipes, selectedRecipeId]
  );
  const activeQuery = useRef<QueryHandle<Table> | null>(null);

  useEffect(() => {
    setSql(initialSql);
    setSelectedRecipeId(initialRecipe?.id);
    setResult(idleQueryResult);
    setResultView('table');
  }, [initialRecipe?.id, initialSql]);

  useEffect(() => {
    if (!rowLimitOptions.includes(rowLimit as (typeof rowLimitOptions)[number])) {
      setRowLimit(rowLimitOptions[0] ?? context.execution.maxResultRows);
    }
  }, [context.execution.maxResultRows, rowLimit, rowLimitOptions]);

  useEffect(() => {
    onResultChange?.(result);
  }, [onResultChange, result]);

  function changeSql(value: string) {
    setSql(value);
  }

  function selectRecipe(recipeId: string) {
    const recipe = context.recipes.find((item) => item.id === recipeId);
    if (!recipe) {
      return;
    }
    setSelectedRecipeId(recipe.id);
    setSql(recipe.sql);
    setExportError(null);
  }

  async function runSql(sourceSql = sql, recipeForExecution = selectedRecipe) {
    if (!connector || !ready) {
      setResult(errorResult(sourceSql, 'DuckDB ist noch nicht bereit.'));
      return;
    }

    let executedSql: string;
    try {
      executedSql = normalizeSqlForExecution(sourceSql, rowLimit);
    } catch (error) {
      setResult(errorResult(sourceSql, toErrorMessage(error)));
      return;
    }

    const startedAt = performance.now();
    const timeoutController = new AbortController();
    const timeoutId = window.setTimeout(() => timeoutController.abort(), context.execution.queryTimeoutMs);
    const maxRowsApplied = hasResultLimitApplied(sourceSql, executedSql);
    const runningResult: QueryResultState = {
      status: 'running',
      sourceSql,
      executedSql,
      columns: [],
      rows: [],
      rowCount: 0,
      maxRowsApplied
    };
    setExportError(null);
    setResult(runningResult);

    try {
      const handle = connector.query(executedSql, {signal: timeoutController.signal});
      activeQuery.current = handle;
      const table = await handle;
      const durationMs = performance.now() - startedAt;
      const successResult: QueryResultState = {
        ...successfulQueryResult({
          sourceSql,
          executedSql,
          table,
          durationMs,
          maxRowsApplied
        }),
        preferredChart: preferredChartForSql(sourceSql, recipeForExecution)
      };
      setResult(successResult);
    } catch (error) {
      const status = timeoutController.signal.aborted ? 'timeout' : activeQuery.current?.signal.aborted ? 'cancelled' : 'error';
      setResult({
        status,
        sourceSql,
        executedSql,
        columns: [],
        rows: [],
        rowCount: 0,
        durationMs: performance.now() - startedAt,
        error: status === 'timeout' ? queryTimeoutMessage(context.execution.queryTimeoutMs) : status === 'error' ? toErrorMessage(error) : undefined
      });
    } finally {
      window.clearTimeout(timeoutId);
      activeQuery.current = null;
    }
  }

  async function cancelQuery() {
    await activeQuery.current?.cancel();
  }

  async function copySql() {
    await copyTextToClipboard(sql);
    setCopied(true);
    window.setTimeout(() => setCopied(false), 1800);
  }

  async function exportResult(format: ResultExportFormat) {
    if (result.status !== 'success' || result.rows.length === 0) {
      return;
    }
    setExportingFormat(format);
    setExportError(null);
    try {
      await exportQueryResult(result, format, context.datasetId, connector);
    } catch (error) {
      console.error('Explore result export failed', error);
      setExportError(`Export konnte nicht erstellt werden: ${toErrorMessage(error)}`);
    } finally {
      setExportingFormat(null);
    }
  }

  const running = result.status === 'running';
  const canExport = result.status === 'success' && result.rows.length > 0;
  const chartsEnabled = context.featureFlags.charts;

  return (
    <PanelGroup
      autoSaveId={`datenportal.explore.${context.datasetId}.sql.v2`}
      className="dp-explore-lab dp-explore-resizable-group dp-explore-resizable-group--vertical"
      direction="vertical"
    >
      <Panel
        className="dp-explore-query-pane"
        defaultSize={43}
        id="query"
        maxSize={80}
        minSize={20}
        order={1}
        tagName="section"
        aria-label="SQL Editor"
      >
        <div className="dp-explore-query-pane__header">
          <div className="dp-explore-query-pane__toolbar-row">
            <SqlToolbar
              leading={
                context.recipes.length > 0 ? (
                  <RecipePicker
                    recipes={context.recipes}
                    selectedRecipeId={selectedRecipeId}
                    disabled={running}
                    onSelect={selectRecipe}
                  />
                ) : undefined
              }
              running={running}
              canRun={ready && sql.trim().length > 0}
              canCancel={running}
              canExport={canExport}
              exportingFormat={exportingFormat}
              onRun={() => void runSql()}
              onCancel={() => void cancelQuery()}
              onCopy={() => void copySql()}
              onExport={(format) => void exportResult(format)}
              copied={copied}
            />
          </div>
          {exportError && (
            <p className="dp-explore-export-error" role="alert">
              {exportError}
            </p>
          )}
        </div>
        <SqlEditorField
          value={sql}
          onChange={changeSql}
          onRun={() => void runSql()}
          disabled={running}
          tableSchemas={tableSchemas}
          getLatestSchemas={getLatestSchemas}
        />
      </Panel>

      <ExploreResizeHandle label="SQL-Editor und Resultattabelle Grösse anpassen" />

      <Panel
        className={chartsEnabled ? 'dp-explore-result-pane' : 'dp-explore-result-pane dp-explore-result-pane--table-only'}
        defaultSize={57}
        id="result"
        minSize={20}
        order={2}
        tagName="section"
        aria-label="SQL Resultat"
      >
        {chartsEnabled && (
          <ResultViewToggle view={resultView} onChange={setResultView} />
        )}
        <div className="dp-explore-result-pane__body">
          {chartsEnabled && resultView === 'chart' ? (
            <ChartPanel result={result} preferred={result.preferredChart} />
          ) : (
            <ResultPanel
              result={result}
              rowLimit={rowLimit}
              rowLimitOptions={rowLimitOptions}
              onRowLimitChange={setRowLimit}
            />
          )}
        </div>
      </Panel>
    </PanelGroup>
  );
}

function RecipePicker({
  recipes,
  selectedRecipeId,
  disabled,
  onSelect
}: {
  recipes: ExploreRecipeDto[];
  selectedRecipeId?: string;
  disabled: boolean;
  onSelect: (recipeId: string) => void;
}) {
  if (recipes.length === 0) {
    return null;
  }

  const selectedRecipe = recipes.find((recipe) => recipe.id === selectedRecipeId) ?? recipes[0];

  return (
    <label className="dp-explore-recipe-picker" title={selectedRecipe.description}>
      <span>Beispiel</span>
      <select
        aria-label="Beispielabfrage auswählen"
        value={selectedRecipe.id}
        disabled={disabled}
        onChange={(event) => onSelect(event.target.value)}
      >
        {recipes.map((recipe) => (
          <option key={recipe.id} value={recipe.id}>{recipe.title}</option>
        ))}
      </select>
    </label>
  );
}

function ResultViewToggle({
  view,
  onChange
}: {
  view: ResultView;
  onChange: (view: ResultView) => void;
}) {
  return (
    <div className="dp-explore-result-pane__header">
      <div className="dp-explore-result-view-toggle" role="group" aria-label="Resultatansicht">
        <button
          type="button"
          className={view === 'table' ? 'is-active' : undefined}
          aria-pressed={view === 'table'}
          onClick={() => onChange('table')}
        >
          Tabelle
        </button>
        <button
          type="button"
          className={view === 'chart' ? 'is-active' : undefined}
          aria-pressed={view === 'chart'}
          onClick={() => onChange('chart')}
        >
          Diagramm
        </button>
      </div>
    </div>
  );
}

function buildInitialSql(table: ExploreTableDto | undefined): string {
  if (!table) {
    return '';
  }
  return `SELECT *
FROM ${table.name};`;
}

function toSqlRoomsDataTable(table: ExploreTableDto): DataTable {
  const qualifiedName = makeQualifiedTableName({schema: 'main', table: table.name});
  return {
    table: qualifiedName,
    isView: true,
    schema: qualifiedName.schema ?? 'main',
    tableName: table.name,
    columns: table.columns.map((column) => ({name: column.name, type: column.type})),
    rowCount: table.rowCountEstimate,
    inputFileName: table.parquetUrl
  };
}

function preferredChartForSql(sourceSql: string, recipe: ExploreRecipeDto | undefined) {
  if (!recipe?.preferredChart) {
    return undefined;
  }
  return normalizeSql(sourceSql) === normalizeSql(recipe.sql) ? recipe.preferredChart : undefined;
}

function normalizeSql(sql: string): string {
  return sql.trim().replace(/;$/, '').trim();
}

function errorResult(sourceSql: string, error: string): QueryResultState {
  return {
    status: 'error',
    sourceSql,
    columns: [],
    rows: [],
    rowCount: 0,
    error
  };
}

function toErrorMessage(error: unknown): string {
  if (error instanceof Error) {
    return error.message;
  }
  return String(error);
}

function ExploreResizeHandle({label}: {label: string}) {
  return (
    <PanelResizeHandle
      aria-label={label}
      className="dp-explore-resize-handle dp-explore-resize-handle--horizontal"
      hitAreaMargins={{coarse: 12, fine: 8}}
    >
      <span className="dp-explore-resize-handle__knob" aria-hidden="true" />
    </PanelResizeHandle>
  );
}
