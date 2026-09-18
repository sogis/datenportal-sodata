import {useEffect, useMemo, useRef, useState} from 'react';
import type {ExploreChartConfigDto} from '../app/ExploreContext';
import type {QueryResultState} from '../results/queryResultTypes';
import {BarResultChart} from './BarResultChart';
import {
  buildHistogramBins,
  inferChartSuggestion,
  inferResultColumns,
  isCategoryColumn,
  isDateLikeResultColumn,
  isNumericColumn
} from './chartInference';
import {
  CHART_SINGLE_COLOR_OPTIONS,
  MULTI_CHART_COLOR_OPTION,
  MULTI_CHART_COLOR_VALUE,
  chartColorHex,
  hashString,
  normalizeChartColor,
  type ChartColorValue
} from './chartColors';
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
import {YColumnSelect} from './YColumnSelect';
import {SeriesLegend} from './SeriesLegend';
import {buildChartSeries, type ChartSeries} from './chartSeries';

export function ChartPanel({
  result,
  preferred,
  onExportTargetChange
}: {
  result: QueryResultState;
  preferred?: ExploreChartConfigDto;
  onExportTargetChange?: (element: HTMLElement | null) => void;
}) {
  if (result.status !== 'success') {
    return <p className="dp-explore-muted">Noch kein SQL-Resultat für ein Diagramm verfügbar.</p>;
  }

  return (
    <SuccessfulChartPanel
      rows={result.rows}
      columns={result.columns}
      preferred={preferred}
      onExportTargetChange={onExportTargetChange}
    />
  );
}

function SuccessfulChartPanel({
  rows,
  columns,
  preferred,
  onExportTargetChange
}: {
  rows: Array<Record<string, unknown>>;
  columns: string[];
  preferred?: ExploreChartConfigDto;
  onExportTargetChange?: (element: HTMLElement | null) => void;
}) {
  const resultColumns = useMemo(() => inferResultColumns(columns, rows), [columns, rows]);
  const suggestion = useMemo(() => inferChartSuggestion(resultColumns, rows, preferred), [preferred, resultColumns, rows]);
  const [type, setType] = useState<ResultChartType>(suggestion?.type ?? 'bar');
  const [x, setX] = useState<string>(suggestion?.x ?? '');
  const [y, setY] = useState<string>(suggestion?.y ?? '');
  const [ys, setYs] = useState<string[]>(suggestion?.y ? [suggestion.y] : []);
  const [colorNames, setColorNames] = useState<string[]>(suggestion?.y ? [suggestion.y] : []);
  const [color, setColor] = useState<ChartColorValue>(normalizeChartColor(suggestion?.color));
  const [rowLimit, setRowLimit] = useState<number>(DEFAULT_CHART_ROW_LIMIT);
  const stableColorSeed = useMemo(() => buildStableColorSeed(columns, rows), [columns, rows]);
  const [colorSeedVersion, setColorSeedVersion] = useState(0);
  const chartRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    setType(suggestion?.type ?? 'bar');
    setX(suggestion?.x ?? '');
    setY(suggestion?.y ?? '');
    setYs(suggestion?.y ? [suggestion.y] : []);
    setColorNames(suggestion?.y ? [suggestion.y] : []);
    setColor(normalizeChartColor(suggestion?.color));
    setRowLimit(DEFAULT_CHART_ROW_LIMIT);
    setColorSeedVersion(0);
  }, [rows, columns, suggestion?.color, suggestion?.type, suggestion?.x, suggestion?.y]);

  const compatibleXColumns = suggestion ? xColumns(type, resultColumns, rows) : [];
  const compatibleYColumns = suggestion ? yColumns(type, resultColumns) : [];
  const safeX = selectSafeColumn(x, compatibleXColumns);
  const validYs = ys.filter((name) => compatibleYColumns.some((column) => column.name === name));
  const safeYs = validYs.length ? validYs : compatibleYColumns.slice(0, 1).map((column) => column.name);
  const safeY = supportsSeries(type) ? safeYs[0] ?? '' : selectSafeColumn(y, compatibleYColumns);
  const changeType = (next: ResultChartType) => {
    if (isPieChartType(next) && !isPieChartType(type)) setY(ys[0] ?? '');
    if (supportsSeries(next) && !supportsSeries(type) && ys.length > 1) setColor(MULTI_CHART_COLOR_VALUE);
    setType(next);
  };
  const changeYs = (next: string[]) => {
    if (safeYs.length <= 1 && next.length > 1) setColor(MULTI_CHART_COLOR_VALUE);
    setYs(next);
    setColorNames((previous) => [...new Set([...previous, ...safeYs, ...next])]);
  };
  const limitedRows = rows.slice(0, rowLimit);
  const canRender = Boolean(suggestion) && canRenderChart(type, safeX, safeY, limitedRows);

  useEffect(() => {
    onExportTargetChange?.(canRender ? chartRef.current : null);
    return () => onExportTargetChange?.(null);
  }, [canRender, onExportTargetChange]);

  if (!suggestion) {
    return (
      <div className="dp-explore-chart" aria-label="Diagramm aus Resultat">
        <p className="dp-explore-muted">Kein Diagrammvorschlag für dieses Resultat verfügbar.</p>
      </div>
    );
  }

  const chartColor = chartColorHex(color);
  const isMultiColor = color === MULTI_CHART_COLOR_VALUE;
  const colorSeed = isMultiColor ? `${stableColorSeed}:${colorSeedVersion}` : undefined;
  const series = buildChartSeries(
    safeYs, columns, chartColor, isMultiColor ? stableColorSeed : undefined,
    colorSeedVersion, [...new Set([...colorNames, ...safeYs])]
  );
  const pieSegmentCount = isPieChartType(type) ? buildPieRows(limitedRows, safeX, safeY, chartColor, colorSeed).length : 0;
  const segmentWarning = canRender && isPieChartType(type) && pieSegmentCount > LARGE_SEGMENT_WARNING_THRESHOLD
    ? 'Viele Segmente. Fuer Pie- und Donut-Diagramme eignet sich ein staerker aggregiertes SQL-Resultat.'
    : undefined;

  return (
    <div ref={chartRef} className="dp-explore-chart" aria-label="Diagramm aus Resultat">
      <div className="dp-explore-chart__header">
        <div>
          <h4>Diagramm aus Resultat</h4>
          <p data-export-ignore="true">{suggestion.reason}</p>
        </div>
        {isMultiColor && (
          <button
            type="button"
            className="dp-explore-button dp-explore-button--secondary dp-explore-chart__palette-button"
            data-export-ignore="true"
            onClick={() => setColorSeedVersion((version) => version + 1)}
          >
            Farben neu
          </button>
        )}
      </div>

      <div className="dp-explore-chart__controls" aria-label="Diagrammsteuerung" data-export-ignore="true">
        <label>
          <span>Typ</span>
          <select value={type} onChange={(event) => changeType(event.target.value as ResultChartType)}>
            {Object.entries(chartTypeLabels).map(([value, label]) => (
              <option key={value} value={value}>{label}</option>
            ))}
          </select>
        </label>
        {chartTypeRequiresX(type) && (
          <label>
            <span>{xControlLabel(type)}</span>
            <select value={safeX} onChange={(event) => setX(event.target.value)}>
              {compatibleXColumns.map((column) => (
                <option key={column.name} value={column.name}>{column.name}</option>
              ))}
            </select>
          </label>
        )}
        {supportsSeries(type) && (
          <YColumnSelect
            columns={compatibleYColumns.map((column) => column.name)}
            selected={safeYs}
            onChange={changeYs}
          />
        )}
        {chartTypeRequiresY(type) && !supportsSeries(type) && (
          <label>
            <span>{isPieChartType(type) ? 'Wert' : 'Y (Zahl)'}</span>
            <select value={safeY} onChange={(event) => setY(event.target.value)}>
              {compatibleYColumns.map((column) => (
                <option key={column.name} value={column.name}>{column.name}</option>
              ))}
            </select>
          </label>
        )}
        {type === 'histogram' && (
          <label>
            <span>Spalte (Zahl)</span>
            <select value={safeX} onChange={(event) => setX(event.target.value)}>
              {compatibleXColumns.map((column) => (
                <option key={column.name} value={column.name}>{column.name}</option>
              ))}
            </select>
          </label>
        )}
        <label>
          <span>Farbe</span>
          <select value={color} onChange={(event) => setColor(normalizeChartColor(event.target.value))}>
            {CHART_SINGLE_COLOR_OPTIONS.map((option) => (
              <option key={option.value} value={option.value}>{option.label}</option>
            ))}
            <option value={MULTI_CHART_COLOR_OPTION.value}>{MULTI_CHART_COLOR_OPTION.label}</option>
          </select>
        </label>
        <label>
          <span>Zeilen</span>
          <select value={rowLimit} onChange={(event) => setRowLimit(Number(event.target.value))}>
            {CHART_ROW_LIMIT_OPTIONS.map((value) => (
              <option key={value} value={value}>{formatSwissNumber(value)}</option>
            ))}
          </select>
        </label>
      </div>

      {suggestion.warning && <p className="dp-explore-chart__warning" data-export-ignore="true">{suggestion.warning}</p>}
      {segmentWarning && <p className="dp-explore-chart__warning" data-export-ignore="true">{segmentWarning}</p>}
      {rows.length > rowLimit && (
        <p className="dp-explore-muted" data-export-ignore="true">
          Diagramm zeigt {formatSwissNumber(rowLimit)} von {formatSwissNumber(rows.length)} Resultatzeilen.
        </p>
      )}

      {canRender ? (
        <div className="dp-explore-chart__figure" data-chart-type={type}>
          {renderChart(type, limitedRows, safeX, safeY, suggestion.title, chartColor, colorSeed, series)}
          {supportsSeries(type) && <SeriesLegend series={series} />}
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
  if (type === 'line') {
    return columns.filter((column) => isNumericColumn(column) || isDateLikeResultColumn(column, rows));
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
  color: string,
  colorSeed: string
    | undefined,
  series: ChartSeries[]
) {
  const chartRows = rows.map(normalizeChartRow);
  switch (type) {
    case 'bar':
      return <BarResultChart rows={chartRows} x={x} series={series} colorSeed={colorSeed} />;
    case 'line':
      return <LineResultChart rows={chartRows} x={x} series={series} />;
    case 'scatter':
      return <ScatterResultChart rows={chartRows} x={x} series={series} />;
    case 'histogram':
      return <HistogramResultChart rows={chartRows} column={x} title={title} color={color} colorSeed={colorSeed} />;
    case 'pie':
      return <PieResultChart rows={chartRows} x={x} y={y} title={title} variant="pie" color={color} colorSeed={colorSeed} />;
    case 'donut':
      return <PieResultChart rows={chartRows} x={x} y={y} title={title} variant="donut" color={color} colorSeed={colorSeed} />;
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

function supportsSeries(type: ResultChartType): boolean {
  return type === 'bar' || type === 'line' || type === 'scatter';
}

function xControlLabel(type: ResultChartType): string {
  if (isPieChartType(type)) {
    return 'Kategorie';
  }
  if (type === 'bar') {
    return 'X (Kategorie)';
  }
  if (type === 'line') {
    return 'X (Zeit/Zahl)';
  }
  if (type === 'scatter') {
    return 'X (Zahl)';
  }
  return 'X';
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
