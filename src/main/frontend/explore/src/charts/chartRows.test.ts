import {DateDay, DateMillisecond, Table, vectorFromArray} from 'apache-arrow';
import {describe, expect, it} from 'vitest';
import {arrowTableToRows} from '../results/arrowResult';
import {inferResultColumns} from './chartInference';
import {normalizeChartRows} from './chartRows';
import {axisTooltipLabel, buildChartSeries, buildSeriesRows} from './chartSeries';

describe('chart row normalization', () => {
  it.each([new DateDay(), new DateMillisecond()])('formats Arrow %s as UTC ISO dates without changing query rows', (type) => {
    const dates = ['2025-12-01', '1970-01-01', '1969-12-31', '2026-03-29'];
    const table = new Table({
      berichtsmonat: vectorFromArray([...dates.map((date) => new Date(`${date}T00:00:00Z`)), null], type),
      anzahl: vectorFromArray([53, 0, 10, 70, 1]),
      jahr: vectorFromArray([2025, 1970, 1969, 2026, 2026]),
      nummer: vectorFromArray([1764547200000, 0, -86400000, 1774742400000, 1])
    });
    const {columns, rows} = arrowTableToRows(table);
    const originals = rows.map((row) => ({...row}));
    const chartRows = normalizeChartRows(rows, inferResultColumns(columns, rows, table.schema));

    expect(chartRows.map((row) => row.berichtsmonat)).toEqual([...dates, null]);
    expect(chartRows.map((row) => row.anzahl)).toEqual([53, 0, 10, 70, 1]);
    expect(chartRows.map((row) => row.jahr)).toEqual(originals.map((row) => row.jahr));
    expect(chartRows.map((row) => row.nummer)).toEqual(originals.map((row) => row.nummer));
    expect(rows).toEqual(originals);
    expect(arrowTableToRows(table).rows).toEqual(originals);
    expect(chartRows[0]).not.toBe(rows[0]);

    const seriesRows = buildSeriesRows(chartRows, 'berichtsmonat', buildChartSeries(['anzahl'], columns, '#104E8B'));
    expect(seriesRows.map((row) => axisTooltipLabel([{payload: row}]))).toEqual([...dates, '']);
  });

  it('preserves schema-free Date/string handling and BigInt chart values', () => {
    const rows = [
      {datum: new Date('2025-12-01T00:00:00Z'), anzahl: 53n},
      {datum: '2026-01-01', anzahl: 0n},
      {datum: null, anzahl: null}
    ];
    const columns = [{name: 'datum', typeCategory: 'date' as const}, {name: 'anzahl', typeCategory: 'number' as const}];

    expect(normalizeChartRows(rows, columns)).toEqual([
      {datum: '2025-12-01', anzahl: 53}, {datum: '2026-01-01', anzahl: 0}, {datum: null, anzahl: null}
    ]);
    expect(rows[0].anzahl).toBe(53n);
  });

  it('keeps invalid numeric dates missing instead of throwing or displaying milliseconds', () => {
    expect(normalizeChartRows([{datum: Infinity}, {datum: NaN}, {datum: 1e20}], [{name: 'datum', typeCategory: 'date'}]))
      .toEqual([{datum: null}, {datum: null}, {datum: null}]);
  });
});
