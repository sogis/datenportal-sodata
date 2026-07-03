import * as ScrollArea from '@radix-ui/react-scroll-area';
import type {Field, Table} from 'apache-arrow';
import {useRef} from 'react';
import type {QueryResultState} from './queryResultTypes';
import {formatResultCell} from './arrowResult';

export function ResultPanel({
  result,
  rowLimit,
  rowLimitOptions,
  onRowLimitChange
}: {
  result: QueryResultState;
  rowLimit: number;
  rowLimitOptions: readonly number[];
  onRowLimitChange: (rowLimit: number) => void;
}) {
  if (result.status === 'idle') {
    return <p className="dp-explore-result-state dp-explore-muted" role="status">Noch keine Abfrage ausgeführt.</p>;
  }

  if (result.status === 'running') {
    return <p className="dp-explore-result-state dp-explore-muted" role="status" aria-live="polite">Abfrage läuft.</p>;
  }

  if (result.status === 'cancelled') {
    return <p className="dp-explore-result-state dp-explore-muted" role="status">Abfrage abgebrochen.</p>;
  }

  if (result.status === 'timeout') {
    return <p className="dp-explore-runtime-error" role="alert">Abfrage nach dem Zeitlimit abgebrochen.</p>;
  }

  if (result.status === 'error') {
    return <p className="dp-explore-runtime-error" role="alert">{result.error ?? 'Die Abfrage konnte nicht ausgeführt werden.'}</p>;
  }

  return (
    <div className="dp-explore-result" aria-label="SQL Ergebnis">
      <ResultTable result={result} />
      <div className="dp-explore-result__footer">
        <div className="dp-explore-result__summary">
          <span>{result.rowCount === 1 ? '1 row' : `${formatSwissNumber(result.rowCount)} rows`}</span>
          {typeof result.durationMs === 'number' && <span>{formatDuration(result.durationMs)}</span>}
        </div>
        <select
          className="dp-explore-result-limit"
          aria-label="Anzahl zurückgelieferter Resultatzeilen"
          value={rowLimit}
          onChange={(event) => onRowLimitChange(Number(event.target.value))}
        >
          {rowLimitOptions.map((option) => (
            <option value={option} key={option}>Limit results to {formatSwissNumber(option)} rows</option>
          ))}
        </select>
      </div>
    </div>
  );
}

function ResultTable({result}: {result: QueryResultState}) {
  const viewportRef = useRef<HTMLDivElement | null>(null);

  if (result.columns.length === 0) {
    return <p className="dp-explore-result-state dp-explore-muted">Die Abfrage hat keine Tabellenspalten zurückgegeben.</p>;
  }

  const columnTypes = columnTypesByName(result.arrowTable);

  return (
    <ScrollArea.Root className="dp-explore-result-table__scroll" type="always">
      <ScrollArea.Viewport
        className="dp-explore-result-table__viewport"
        role="region"
        aria-label="SQL Ergebnistabelle"
        tabIndex={0}
        ref={viewportRef}
        onPointerDown={() => viewportRef.current?.focus({preventScroll: true})}
      >
        <table className="dp-explore-result-table">
          <thead>
            <tr>
              <th className="dp-explore-result-table__index" scope="col" aria-label="Zeilennummer" />
              {result.columns.map((column) => (
                <th key={column} scope="col">
                  <span className="dp-explore-result-table__column">
                    <span>{column}</span>
                    <span className="dp-explore-result-table__type">{columnTypes.get(column) ?? 'Value'}</span>
                  </span>
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {result.rows.map((row, rowIndex) => (
              <tr key={rowIndex}>
                <th className="dp-explore-result-table__index" scope="row">{rowIndex + 1}</th>
                {result.columns.map((column) => (
                  <td key={column} className={row[column] === null || row[column] === undefined ? 'is-null' : undefined}>
                    {formatDisplayCell(row[column])}
                  </td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      </ScrollArea.Viewport>
      <ScrollArea.Scrollbar className="dp-explore-result-table__scrollbar" orientation="vertical">
        <ScrollArea.Thumb className="dp-explore-result-table__thumb" />
      </ScrollArea.Scrollbar>
      <ScrollArea.Scrollbar className="dp-explore-result-table__scrollbar" orientation="horizontal">
        <ScrollArea.Thumb className="dp-explore-result-table__thumb" />
      </ScrollArea.Scrollbar>
      <ScrollArea.Corner className="dp-explore-result-table__corner" />
    </ScrollArea.Root>
  );
}

function columnTypesByName(table: Table | undefined): Map<string, string> {
  const types = new Map<string, string>();
  if (!table) {
    return types;
  }
  table.schema.fields.forEach((field: Field) => {
    types.set(field.name, formatArrowType(field.type.toString()));
  });
  return types;
}

function formatArrowType(type: string): string {
  const normalized = type.replace(/<.*>/, '').replace(/\[\]/g, '').trim();
  if (!normalized) {
    return 'Value';
  }
  if (/^int/i.test(normalized)) {
    return normalized.replace(/^int/i, 'Int');
  }
  if (/^utf8$/i.test(normalized)) {
    return 'Utf8';
  }
  if (/^float/i.test(normalized)) {
    return normalized.replace(/^float/i, 'Float');
  }
  return normalized.length > 14 ? normalized.slice(0, 14) : normalized;
}

function formatDisplayCell(value: unknown): string {
  if (value === null || value === undefined) {
    return 'NULL';
  }
  return formatResultCell(value);
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
