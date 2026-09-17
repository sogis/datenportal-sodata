import {useCallback, useEffect, useMemo, useRef, useState} from 'react';
import {makeQualifiedTableName, type DataTable, type DuckDbConnector, type QueryHandle} from '@sqlrooms/duckdb';
import type {Table} from 'apache-arrow';
import {Panel, PanelGroup, PanelResizeHandle} from 'react-resizable-panels';
import type {ExploreCatalogDatabaseDto, ExploreContextDto, ExploreRecipeDto, ExploreTableDto} from '../app/ExploreContext';
import {classifyExploreQueryError} from '../app/ExploreRuntimeError';
import {ChartPanel} from '../charts/ChartPanel';
import {executeDuckDbQuery} from '../duckdb/executeDuckDbQuery';
import {hasResultLimitApplied, normalizeSqlForExecution, queryTimeoutMessage} from '../duckdb/querySafety';
import {ResultPanel} from '../results/ResultPanel';
import {idleQueryResult, type QueryResultState} from '../results/queryResultTypes';
import {successfulQueryResult} from '../results/arrowResult';
import {exportChartAsPng} from '../results/ChartExport';
import {exportQueryResult, type ResultExportFormat} from '../results/ResultExport';
import {ResultExportControl} from '../results/ResultExportControl';
import {sqlResultSnapshotFromQueryResult, type SqlResultSnapshot} from '../results/sqlResultSnapshot';
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
  onResultChange,
  onTransferToR
}: {
  context: ExploreContextDto;
  connector?: DuckDbConnector;
  ready: boolean;
  onResultChange?: (result: QueryResultState) => void;
  onTransferToR?: (snapshot: SqlResultSnapshot) => void;
}) {
  const initialRecipe = useMemo(() => context.recipes[0], [context.recipes]);
  const initialSql = useMemo(() => initialRecipe?.sql ?? buildInitialSql(context.tables[0], context.catalogDatabase), [
    context.catalogDatabase,
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
  const [exportingChart, setExportingChart] = useState(false);
  const [exportError, setExportError] = useState<string | null>(null);
  const [chartExportTarget, setChartExportTarget] = useState<HTMLElement | null>(null);
  const tableSchemas = useMemo(
    () => context.tables.map((table) => toSqlRoomsDataTable(table, context.catalogDatabase)),
    [context.catalogDatabase, context.tables]
  );
  const getLatestSchemas = useCallback(() => ({tableSchemas}), [tableSchemas]);
  const selectedRecipe = useMemo(
    () => context.recipes.find((recipe) => recipe.id === selectedRecipeId),
    [context.recipes, selectedRecipeId]
  );
  const activeQuery = useRef<QueryHandle<Table> | null>(null);
  const mountedRef = useRef(true);

  useEffect(() => {
    return () => {
      mountedRef.current = false;
      void activeQuery.current?.cancel();
    };
  }, []);

  useEffect(() => {
    setSql(initialSql);
    setSelectedRecipeId(initialRecipe?.id);
    setResult(idleQueryResult);
    setResultView('table');
    setChartExportTarget(null);
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
    if (activeQuery.current) {
      return;
    }
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
    setChartExportTarget(null);
    setResult(runningResult);

    let handle: QueryHandle<Table> | undefined;
    try {
      handle = executeDuckDbQuery(connector, executedSql, {signal: timeoutController.signal});
      activeQuery.current = handle;
      const table = await handle;
      if (!mountedRef.current) {
        return;
      }
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
      if (!mountedRef.current) {
        return;
      }
      const status = timeoutController.signal.aborted ? 'timeout' : handle?.signal?.aborted ? 'cancelled' : 'error';
      const queryError = status === 'error' ? classifyExploreQueryError(error) : undefined;
      setResult({
        status,
        sourceSql,
        executedSql,
        columns: [],
        rows: [],
        rowCount: 0,
        durationMs: performance.now() - startedAt,
        error: status === 'timeout' ? queryTimeoutMessage(context.execution.queryTimeoutMs) : queryError?.summary,
        errorKind: queryError?.kind,
        errorDetail: queryError?.detail
      });
    } finally {
      window.clearTimeout(timeoutId);
      if (activeQuery.current === handle) {
        activeQuery.current = null;
      }
    }
  }

  async function cancelQuery() {
    const handle = activeQuery.current;
    if (!handle) {
      return;
    }
    await handle.cancel();
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

  async function exportChart() {
    if (result.status !== 'success' || result.rows.length === 0 || !chartExportTarget) {
      return;
    }
    setExportingChart(true);
    setExportError(null);
    try {
      await exportChartAsPng(chartExportTarget, context.datasetId);
    } catch (error) {
      console.error('Explore chart export failed', error);
      setExportError(`Export konnte nicht erstellt werden: ${toErrorMessage(error)}`);
    } finally {
      setExportingChart(false);
    }
  }

  function transferToR() {
    if (result.status !== 'success') {
      return;
    }
    try {
      onTransferToR?.(sqlResultSnapshotFromQueryResult(result, context.tables));
    } catch (error) {
      setExportError(`Resultat konnte nicht nach R übernommen werden: ${toErrorMessage(error)}`);
    }
  }

  const running = result.status === 'running';
  const canExport = result.status === 'success' && result.rows.length > 0;
  const canTransferToR = context.webREnabled && result.status === 'success' && result.rows.length > 0;
  const chartsEnabled = context.chartsEnabled;

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
              canTransferToR={canTransferToR}
              onRun={() => void runSql()}
              onCancel={() => void cancelQuery()}
              onCopy={() => void copySql()}
              onTransferToR={onTransferToR ? transferToR : undefined}
              copied={copied}
            />
          </div>
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
        <div className="dp-explore-result-pane__header">
          <div className="dp-explore-result-pane__header-row">
            <div className="dp-explore-result-pane__actions">
              {chartsEnabled && resultView === 'chart' ? (
                <button
                  type="button"
                  className="dp-explore-button dp-explore-button--secondary"
                  disabled={!canExport || !chartExportTarget || exportingChart || Boolean(exportingFormat)}
                  aria-label="Diagramm als PNG herunterladen"
                  onClick={() => void exportChart()}
                >
                  PNG
                </button>
              ) : (
                <ResultExportControl
                  canExport={canExport}
                  exportingFormat={exportingFormat}
                  onExport={(format) => void exportResult(format)}
                />
              )}
            </div>
            {chartsEnabled && <ResultViewToggle view={resultView} onChange={setResultView} />}
          </div>
          {exportError && (
            <p className="dp-explore-export-error" role="alert">
              {exportError}
            </p>
          )}
        </div>
        <div className="dp-explore-result-pane__body">
          {chartsEnabled && resultView === 'chart' ? (
            <ChartPanel
              result={result}
              preferred={result.preferredChart}
              onExportTargetChange={setChartExportTarget}
            />
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
  );
}

function buildInitialSql(table: ExploreTableDto | undefined, catalogDatabase: ExploreCatalogDatabaseDto): string {
  if (!table) {
    return '';
  }
  return `SELECT *
FROM ${catalogDatabase.schema}.${table.name};`;
}

function toSqlRoomsDataTable(table: ExploreTableDto, catalogDatabase: ExploreCatalogDatabaseDto): DataTable {
  const qualifiedName = makeQualifiedTableName({
    database: catalogDatabase.database,
    schema: catalogDatabase.schema,
    table: table.name
  });
  return {
    table: qualifiedName,
    isView: true,
    database: catalogDatabase.database,
    schema: qualifiedName.schema ?? catalogDatabase.schema,
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
