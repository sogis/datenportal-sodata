import type {QueryResultState} from './queryResultTypes';
import {exportRowsToCsv, resultCsvFilename} from './ResultExport';
import {formatResultCell} from './arrowResult';

export function ResultPanel({
  result,
  maxRows,
  datasetId
}: {
  result: QueryResultState;
  maxRows: number;
  datasetId: string;
}) {
  if (result.status === 'idle') {
    return <p className="dp-explore-muted">Noch keine Abfrage ausgeführt.</p>;
  }

  if (result.status === 'running') {
    return <p className="dp-explore-muted">Abfrage läuft.</p>;
  }

  if (result.status === 'cancelled') {
    return <p className="dp-explore-muted">Abfrage abgebrochen.</p>;
  }

  if (result.status === 'timeout') {
    return <p className="dp-explore-runtime-error">Abfrage nach dem Zeitlimit abgebrochen.</p>;
  }

  if (result.status === 'error') {
    return <p className="dp-explore-runtime-error">{result.error ?? 'Die Abfrage konnte nicht ausgeführt werden.'}</p>;
  }

  return (
    <div className="dp-explore-result" aria-label="SQL Ergebnis">
      <div className="dp-explore-result__summary">
        <span>{result.rowCount === 1 ? '1 Zeile' : `${formatSwissNumber(result.rowCount)} Zeilen`}</span>
        {typeof result.durationMs === 'number' && <span>{formatDuration(result.durationMs)}</span>}
        {result.maxRowsApplied && <span>Maximal {formatSwissNumber(maxRows)} Zeilen angezeigt</span>}
        <span>CSV exportiert nur das aktuelle Resultat</span>
      </div>
      <div className="dp-explore-result__actions">
        <button
          type="button"
          className="dp-explore-button"
          disabled={result.rows.length === 0}
          onClick={() => exportRowsToCsv(result.rows, result.columns, resultCsvFilename(datasetId))}
        >
          Resultat als CSV
        </button>
      </div>
      <ResultTable result={result} />
    </div>
  );
}

function ResultTable({result}: {result: QueryResultState}) {
  if (result.columns.length === 0) {
    return <p className="dp-explore-muted">Die Abfrage hat keine Tabellenspalten zurückgegeben.</p>;
  }

  return (
    <div className="dp-explore-preview__scroll">
      <table>
        <thead>
          <tr>
            {result.columns.map((column) => (
              <th key={column} scope="col">{column}</th>
            ))}
          </tr>
        </thead>
        <tbody>
          {result.rows.map((row, rowIndex) => (
            <tr key={rowIndex}>
              {result.columns.map((column) => (
                <td key={column}>{formatResultCell(row[column])}</td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function formatDuration(durationMs: number): string {
  if (durationMs < 1000) {
    return `${Math.max(1, Math.round(durationMs))} ms`;
  }
  return `${(durationMs / 1000).toFixed(1)} s`;
}

function formatSwissNumber(value: number): string {
  return new Intl.NumberFormat('de-CH').format(value);
}

