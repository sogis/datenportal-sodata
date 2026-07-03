import {useEffect, useMemo, useState} from 'react';
import type {ExploreChartConfigDto} from '../app/ExploreContext';
import type {QueryResultState} from '../results/queryResultTypes';
import {BarResultChart} from './BarResultChart';
import {buildHistogramBins, inferChartSuggestion, inferResultColumns, isCategoryColumn, isNumericColumn} from './chartInference';
import {
  CHART_ROW_LIMIT_OPTIONS,
  DEFAULT_CHART_ROW_LIMIT,
  LARGE_SEGMENT_WARNING_THRESHOLD,
  chartTypeLabels,
  chartTypeRequiresX,
  chartTypeRequiresY,
  type ResultChartType,
  type ResultColumn
} from './chartTypes';
import {HistogramResultChart} from './HistogramResultChart';
import {LineResultChart} from './LineResultChart';
import {buildPieRows, hasPieValues, PieResultChart} from './PieResultChart';
import {ScatterResultChart} from './ScatterResultChart';

export function ChartPanel({
  result,
  preferred
}: {
  result: QueryResultState;
  preferred?: ExploreChartConfigDto;
}) {
  if (result.status !== 'success') {
    return <p className="dp-explore-muted">Noch kein SQL-Resultat für ein Diagramm verfügbar.</p>;
  }

  return <SuccessfulChartPanel rows={result.rows} columns={result.columns} preferred={preferred} />;
}

function SuccessfulChartPanel({
  rows,
  columns,
  preferred
}: {
  rows: Array<Record<string, unknown>>;
  columns: string[];
  preferred?: ExploreChartConfigDto;
}) {
  const resultColumns = useMemo(() => inferResultColumns(columns, rows), [columns, rows]);
  const suggestion = useMemo(() => inferChartSuggestion(resultColumns, rows, preferred), [preferred, resultColumns, rows]);
  const [type, setType] = useState<ResultChartType>(suggestion?.type ?? 'bar');
  const [x, setX] = useState<string>(suggestion?.x ?? '');
  const [y, setY] = useState<string>(suggestion?.y ?? '');
  const [rowLimit, setRowLimit] = useState<number>(DEFAULT_CHART_ROW_LIMIT);
  const stableColorSeed = useMemo(() => buildStableColorSeed(columns, rows), [columns, rows]);
  const [colorSeedVersion, setColorSeedVersion] = useState(0);

  useEffect(() => {
    setType(suggestion?.type ?? 'bar');
    setX(suggestion?.x ?? '');
    setY(suggestion?.y ?? '');
    setRowLimit(DEFAULT_CHART_ROW_LIMIT);
    setColorSeedVersion(0);
  }, [stableColorSeed, suggestion?.type, suggestion?.x, suggestion?.y]);

  if (!suggestion) {
    return (
      <div className="dp-explore-chart" aria-label="Diagramm aus Resultat">
        <p className="dp-explore-muted">Kein Diagrammvorschlag für dieses Resultat verfügbar.</p>
      </div>
    );
  }

  const compatibleXColumns = xColumns(type, resultColumns, rows);
  const compatibleYColumns = yColumns(type, resultColumns);
  const safeX = selectSafeColumn(x, compatibleXColumns);
  const safeY = selectSafeColumn(y, compatibleYColumns);
  const limitedRows = rows.slice(0, rowLimit);
  const canRender = canRenderChart(type, safeX, safeY, limitedRows);
  const colorSeed = `${stableColorSeed}:${colorSeedVersion}`;
  const pieSegmentCount = isPieChartType(type) ? buildPieRows(limitedRows, safeX, safeY, colorSeed).length : 0;
  const segmentWarning = canRender && isPieChartType(type) && pieSegmentCount > LARGE_SEGMENT_WARNING_THRESHOLD
    ? 'Viele Segmente. Fuer Pie- und Donut-Diagramme eignet sich ein staerker aggregiertes SQL-Resultat.'
    : undefined;

  return (
    <div className="dp-explore-chart" aria-label="Diagramm aus Resultat">
      <div className="dp-explore-chart__header">
        <div>
          <h4>Diagramm aus Resultat</h4>
          <p>{suggestion.reason}</p>
        </div>
        {isPieChartType(type) && (
          <button
            type="button"
            className="dp-explore-button dp-explore-button--secondary dp-explore-chart__palette-button"
            onClick={() => setColorSeedVersion((version) => version + 1)}
          >
            Farben neu
          </button>
        )}
      </div>

      <div className="dp-explore-chart__controls" aria-label="Diagrammsteuerung">
        <label>
          <span>Typ</span>
          <select value={type} onChange={(event) => setType(event.target.value as ResultChartType)}>
            {Object.entries(chartTypeLabels).map(([value, label]) => (
              <option key={value} value={value}>{label}</option>
            ))}
          </select>
        </label>
        {chartTypeRequiresX(type) && (
          <label>
            <span>{isPieChartType(type) ? 'Kategorie' : 'X'}</span>
            <select value={safeX} onChange={(event) => setX(event.target.value)}>
              {compatibleXColumns.map((column) => (
                <option key={column.name} value={column.name}>{column.name}</option>
              ))}
            </select>
          </label>
        )}
        {chartTypeRequiresY(type) && (
          <label>
            <span>{isPieChartType(type) ? 'Wert' : 'Y'}</span>
            <select value={safeY} onChange={(event) => setY(event.target.value)}>
              {compatibleYColumns.map((column) => (
                <option key={column.name} value={column.name}>{column.name}</option>
              ))}
            </select>
          </label>
        )}
        {type === 'histogram' && (
          <label>
            <span>Spalte</span>
            <select value={safeX} onChange={(event) => setX(event.target.value)}>
              {compatibleXColumns.map((column) => (
                <option key={column.name} value={column.name}>{column.name}</option>
              ))}
            </select>
          </label>
        )}
        <label>
          <span>Zeilen</span>
          <select value={rowLimit} onChange={(event) => setRowLimit(Number(event.target.value))}>
            {CHART_ROW_LIMIT_OPTIONS.map((value) => (
              <option key={value} value={value}>{formatSwissNumber(value)}</option>
            ))}
          </select>
        </label>
      </div>

      {suggestion.warning && <p className="dp-explore-chart__warning">{suggestion.warning}</p>}
      {segmentWarning && <p className="dp-explore-chart__warning">{segmentWarning}</p>}
      {rows.length > rowLimit && (
        <p className="dp-explore-muted">Diagramm zeigt {formatSwissNumber(rowLimit)} von {formatSwissNumber(rows.length)} Resultatzeilen.</p>
      )}

      {canRender ? (
        <div className="dp-explore-chart__figure" data-chart-type={type}>
          {renderChart(type, limitedRows, safeX, safeY, suggestion.title, colorSeed)}
        </div>
      ) : (
        <p className="dp-explore-muted">Die gewählten Spalten passen nicht zu diesem Diagrammtyp.</p>
      )}
    </div>
  );
}

function xColumns(type: ResultChartType, columns: ResultColumn[], rows: Array<Record<string, unknown>>): ResultColumn[] {
  if (type === 'scatter' || type === 'histogram') {
    return columns.filter(isNumericColumn);
  }
  return columns.filter((column) => isCategoryColumn(column, rows));
}

function yColumns(type: ResultChartType, columns: ResultColumn[]): ResultColumn[] {
  if (type === 'histogram') {
    return [];
  }
  return columns.filter(isNumericColumn);
}

function selectSafeColumn(selected: string, columns: ResultColumn[]): string {
  if (columns.some((column) => column.name === selected)) {
    return selected;
  }
  return columns[0]?.name ?? '';
}

function canRenderChart(type: ResultChartType, x: string, y: string, rows: Array<Record<string, unknown>>): boolean {
  if (rows.length === 0) {
    return false;
  }
  if (type === 'histogram') {
    return x.length > 0 && buildHistogramBins(rows, x, 12).length > 0;
  }
  if (isPieChartType(type)) {
    return x.length > 0 && y.length > 0 && hasPieValues(rows, y);
  }
  return x.length > 0 && y.length > 0;
}

function renderChart(
  type: ResultChartType,
  rows: Array<Record<string, unknown>>,
  x: string,
  y: string,
  title: string | undefined,
  colorSeed: string
) {
  const chartRows = rows.map(normalizeChartRow);
  switch (type) {
    case 'bar':
      return <BarResultChart rows={chartRows} x={x} y={y} title={title} />;
    case 'line':
      return <LineResultChart rows={chartRows} x={x} y={y} title={title} />;
    case 'scatter':
      return <ScatterResultChart rows={chartRows} x={x} y={y} title={title} />;
    case 'histogram':
      return <HistogramResultChart rows={chartRows} column={x} title={title} />;
    case 'pie':
      return <PieResultChart rows={chartRows} x={x} y={y} title={title} variant="pie" colorSeed={colorSeed} />;
    case 'donut':
      return <PieResultChart rows={chartRows} x={x} y={y} title={title} variant="donut" colorSeed={colorSeed} />;
  }
}

function normalizeChartRow(row: Record<string, unknown>): Record<string, unknown> {
  return Object.fromEntries(Object.entries(row).map(([key, value]) => [key, normalizeChartValue(value)]));
}

function normalizeChartValue(value: unknown): unknown {
  if (typeof value === 'bigint') {
    return Number(value);
  }
  if (value instanceof Date) {
    return value.toISOString().slice(0, 10);
  }
  if (value instanceof Uint8Array) {
    return `[${value.byteLength} Bytes]`;
  }
  return value;
}

function formatSwissNumber(value: number): string {
  return new Intl.NumberFormat('de-CH').format(value);
}

function isPieChartType(type: ResultChartType): boolean {
  return type === 'pie' || type === 'donut';
}

function buildStableColorSeed(columns: string[], rows: Array<Record<string, unknown>>): string {
  const sample = rows.slice(0, 40)
    .map((row) => columns.map((column) => normalizeSeedValue(row[column])).join('='))
    .join('|');
  return `${columns.join(',')}|${rows.length}|${hashString(sample)}`;
}

function normalizeSeedValue(value: unknown): string {
  if (value === null || value === undefined) {
    return 'null';
  }
  if (typeof value === 'bigint') {
    return value.toString();
  }
  if (value instanceof Date) {
    return value.toISOString();
  }
  if (value instanceof Uint8Array) {
    return `bytes:${value.byteLength}`;
  }
  return String(value);
}

function hashString(value: string): number {
  let hash = 2166136261;
  for (let index = 0; index < value.length; index++) {
    hash ^= value.charCodeAt(index);
    hash = Math.imul(hash, 16777619);
  }
  return hash >>> 0;
}
