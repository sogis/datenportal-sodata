import {describe, expect, it} from 'vitest';
import type {ExploreChartConfigDto} from '../app/ExploreContext';
import {buildHistogramBins, inferChartSuggestion, isDateLikeColumn, isYearLikeColumn} from './chartInference';
import type {ResultColumn} from './chartTypes';

describe('chart inference', () => {
  it('suggests bar for string and number columns', () => {
    const suggestion = inferChartSuggestion(
      columns([
        ['gemeinde', 'string'],
        ['anzahl', 'number']
      ]),
      [
        {gemeinde: 'Solothurn', anzahl: 1},
        {gemeinde: 'Olten', anzahl: 2}
      ]
    );

    expect(suggestion).toMatchObject({type: 'bar', x: 'gemeinde', y: 'anzahl'});
  });

  it('suggests line for year and number columns', () => {
    const suggestion = inferChartSuggestion(
      columns([
        ['jahr', 'number'],
        ['anzahl', 'number']
      ]),
      [
        {jahr: 2024, anzahl: 1},
        {jahr: 2025, anzahl: 2}
      ]
    );

    expect(suggestion).toMatchObject({type: 'line', x: 'jahr', y: 'anzahl'});
  });

  it('suggests line for date and number columns', () => {
    const suggestion = inferChartSuggestion(
      columns([
        ['stand', 'date'],
        ['anzahl', 'number']
      ]),
      [
        {stand: '2026-01-01', anzahl: 1},
        {stand: '2026-02-01', anzahl: 2}
      ]
    );

    expect(suggestion).toMatchObject({type: 'line', x: 'stand', y: 'anzahl'});
    expect(isDateLikeColumn('stand', [{stand: '2026-01-01'}])).toBe(true);
  });

  it('suggests scatter for two numeric columns', () => {
    const suggestion = inferChartSuggestion(
      columns([
        ['x', 'number'],
        ['y', 'number']
      ]),
      [
        {x: 1, y: 10},
        {x: 2, y: 20}
      ]
    );

    expect(suggestion).toMatchObject({type: 'scatter', x: 'x', y: 'y'});
  });

  it('suggests histogram for one numeric column', () => {
    const suggestion = inferChartSuggestion(columns([['wert', 'number']]), [{wert: 1}, {wert: 2}]);

    expect(suggestion).toMatchObject({type: 'histogram', x: 'wert'});
  });

  it('returns null when no useful chart is possible', () => {
    const suggestion = inferChartSuggestion(columns([['name', 'string']]), [{name: 'Solothurn'}]);

    expect(suggestion).toBeNull();
  });

  it('uses a valid preferred chart and falls back from an invalid preferred chart', () => {
    const resultColumns = columns([
      ['gemeinde', 'string'],
      ['anzahl', 'number']
    ]);
    const preferred: ExploreChartConfigDto = {type: 'bar', x: 'gemeinde', y: 'anzahl', title: 'Nach Gemeinde'};
    const invalid: ExploreChartConfigDto = {type: 'bar', x: 'missing', y: 'anzahl'};

    expect(inferChartSuggestion(resultColumns, [{gemeinde: 'Olten', anzahl: 2}], preferred))
      .toMatchObject({type: 'bar', x: 'gemeinde', y: 'anzahl', title: 'Nach Gemeinde', confidence: 1});
    expect(inferChartSuggestion(resultColumns, [{gemeinde: 'Olten', anzahl: 2}], invalid))
      .toMatchObject({type: 'bar', x: 'gemeinde', y: 'anzahl'});
  });

  it('builds histogram bins and flags large bar or line results', () => {
    const bins = buildHistogramBins([{wert: 1}, {wert: 2}, {wert: 3}, {wert: 4}], 'wert', 2);
    const rows = Array.from({length: 501}, (_, index) => ({gemeinde: `G${index}`, anzahl: index}));

    expect(bins).toHaveLength(2);
    expect(bins.reduce((sum, bin) => sum + bin.count, 0)).toBe(4);
    expect(inferChartSuggestion(columns([['gemeinde', 'string'], ['anzahl', 'number']]), rows)?.warning)
      .toContain('Viele Datenpunkte');
  });

  it('detects year-like columns from names and values', () => {
    expect(isYearLikeColumn('periode', [{periode: 2024}])).toBe(true);
    expect(isYearLikeColumn('wert', [{wert: 2024}, {wert: 2025}, {wert: 10}])).toBe(false);
  });
});

function columns(values: Array<[string, ResultColumn['typeCategory']]>): ResultColumn[] {
  return values.map(([name, typeCategory]) => ({name, typeCategory}));
}
