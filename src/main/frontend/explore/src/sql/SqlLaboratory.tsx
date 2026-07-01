import {useEffect, useMemo, useRef, useState} from 'react';
import type {DuckDbConnector, QueryHandle} from '@sqlrooms/duckdb';
import type {Table} from 'apache-arrow';
import type {ExploreContextDto, ExploreRecipeDto} from '../app/ExploreContext';
import {hasResultLimitApplied, normalizeSqlForExecution, queryTimeoutMessage} from '../duckdb/querySafety';
import {RecipeList} from '../recipes/RecipeList';
import {ChartPanel} from '../charts/ChartPanel';
import {ResultPanel} from '../results/ResultPanel';
import {idleQueryResult, type QueryResultState} from '../results/queryResultTypes';
import {successfulQueryResult} from '../results/arrowResult';
import {copyTextToClipboard} from './clipboard';
import {SqlEditorField} from './SqlEditorField';
import {SqlToolbar} from './SqlToolbar';

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
  const [selectedRecipeId, setSelectedRecipeId] = useState<string | undefined>(initialRecipe?.id);
  const [sql, setSql] = useState(initialRecipe?.sql ?? '');
  const [modified, setModified] = useState(false);
  const [result, setResult] = useState<QueryResultState>(idleQueryResult);
  const [copied, setCopied] = useState(false);
  const activeQuery = useRef<QueryHandle<Table> | null>(null);
  const selectedRecipe = context.recipes.find((recipe) => recipe.id === selectedRecipeId);

  useEffect(() => {
    setSelectedRecipeId(initialRecipe?.id);
    setSql(initialRecipe?.sql ?? '');
    setModified(false);
    setResult(idleQueryResult);
  }, [initialRecipe]);

  useEffect(() => {
    onResultChange?.(result);
  }, [onResultChange, result]);

  function selectRecipe(recipe: ExploreRecipeDto) {
    setSelectedRecipeId(recipe.id);
    setSql(recipe.sql);
    setModified(false);
  }

  function changeSql(value: string) {
    setSql(value);
    setModified(selectedRecipe?.sql.trim() !== value.trim());
  }

  async function runRecipe(recipe: ExploreRecipeDto) {
    selectRecipe(recipe);
    await runSql(recipe.sql, recipe, false);
  }

  async function runSql(sourceSql = sql, recipeForExecution = selectedRecipe, modifiedForExecution = modified) {
    if (!connector || !ready) {
      setResult(errorResult(sourceSql, 'DuckDB ist noch nicht bereit.'));
      return;
    }

    let executedSql: string;
    try {
      executedSql = normalizeSqlForExecution(sourceSql, context.execution.maxResultRows);
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
    setResult(runningResult);

    try {
      const handle = connector.query(executedSql, {signal: timeoutController.signal});
      activeQuery.current = handle;
      const table = await handle;
      setResult({
        ...successfulQueryResult({
          sourceSql,
          executedSql,
          table,
          durationMs: performance.now() - startedAt,
          maxRowsApplied
        }),
        preferredChart: preferredChartForSql(sourceSql, recipeForExecution, modifiedForExecution)
      });
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

  const running = result.status === 'running';
  return (
    <div className="dp-explore-lab">
      <RecipeList
        recipes={context.recipes}
        tables={context.tables}
        selectedRecipeId={selectedRecipeId}
        onSelect={selectRecipe}
        onRun={(recipe) => void runRecipe(recipe)}
      />

      <section className="dp-explore-lab__editor" aria-labelledby="explore-sql-title">
        <div className="dp-explore-lab__heading">
          <h4 id="explore-sql-title">SQL</h4>
          {selectedRecipe && (
            <p>
              {selectedRecipe.title}
              {modified ? ' · geändert' : ''}
            </p>
          )}
        </div>
        <SqlEditorField
          value={sql}
          onChange={changeSql}
          onRun={() => void runSql()}
          disabled={running}
          connector={connector}
          tableNames={context.tables.map((table) => table.name)}
        />
        <SqlToolbar
          running={running}
          canRun={ready && sql.trim().length > 0}
          canCancel={running}
          onRun={() => void runSql()}
          onCancel={() => void cancelQuery()}
          onCopy={() => void copySql()}
          copied={copied}
        />
      </section>

      <section className="dp-explore-lab__result" aria-labelledby="explore-result-title">
        <h4 id="explore-result-title">Ergebnis</h4>
        <ResultPanel result={result} maxRows={context.execution.maxResultRows} datasetId={context.datasetId} />
      </section>

      <section className="dp-explore-lab__chart" aria-labelledby="explore-chart-title">
        <h4 id="explore-chart-title">Visualisierung</h4>
        <ChartPanel result={result} preferred={result.preferredChart} />
      </section>
    </div>
  );
}

function preferredChartForSql(sourceSql: string, recipe: ExploreRecipeDto | undefined, modified: boolean) {
  if (modified || !recipe?.preferredChart) {
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
