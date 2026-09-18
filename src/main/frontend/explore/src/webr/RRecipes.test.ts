import {describe, expect, it} from 'vitest';
import {buildRRecipes, type RRecipe} from './RRecipes';
import type {SqlResultSnapshot} from '../results/sqlResultSnapshot';

type SnapshotColumnTuple = [
  string,
  string,
  SqlResultSnapshot['columns'][number]['rType'],
  SqlResultSnapshot['columns'][number]['roles']
];

describe('buildRRecipes', () => {
  it('builds generic recipes plus numeric, category and date plot recipes when compatible columns exist', () => {
    const recipes = buildRRecipes(snapshot([
      ['jahr', 'INTEGER', 'integer', ['year']],
      ['messstelle', 'VARCHAR', 'character', ['category']],
      ['nitrat_mg_l', 'DOUBLE', 'numeric', ['measure']]
    ], [
      [2020, 'A', 4.2],
      [2021, 'B', 5.1]
    ]));

    expect(idsOf(recipes)).toEqual([
      'data-overview',
      'structure',
      'missing',
      'numeric-summary',
      'histogram',
      'bar-category',
      'bar-measure-category',
      'boxplot-category',
      'trend'
    ]);
    expect(titleOf(recipes, 'data-overview')).toBe('Datenüberblick');
    expect(titleOf(recipes, 'histogram')).toBe('Histogramm «nitrat_mg_l»');
    expect(titleOf(recipes, 'bar-category')).toBe('Balkendiagramm «messstelle»');
    expect(titleOf(recipes, 'bar-measure-category')).toBe('Mittelwert «nitrat_mg_l» nach «messstelle»');
    expect(titleOf(recipes, 'boxplot-category')).toBe('Boxplot «nitrat_mg_l» nach «messstelle»');
    expect(titleOf(recipes, 'trend')).toBe('Trend «nitrat_mg_l» nach «jahr»');
    expect(codeOf(recipes, 'bar-category')).toContain('table(');
    expect(codeOf(recipes, 'bar-category')).toContain('geom_col(');
    expect(codeOf(recipes, 'bar-measure-category')).toContain('aggregate(');
    expect(codeOf(recipes, 'histogram')).toContain('plot_limit <- 10000');
    expect(codeOf(recipes, 'boxplot-category')).toContain('plot_limit <- 10000');
    expect(codeOf(recipes, 'boxplot-category')).toContain('coord_flip');
  });

  it('prefers measure columns over year, id and code columns for plot recipes', () => {
    const recipes = buildRRecipes(snapshot([
      ['jahr', 'INTEGER', 'integer', ['year']],
      ['messstelle_code', 'VARCHAR', 'character', ['category']],
      ['parameter', 'VARCHAR', 'character', ['category']],
      ['messwert', 'DOUBLE', 'numeric', ['measure']]
    ], [
      [2020, 'M1', 'Nitrat', 4.2]
    ]));

    expect(idsOf(recipes)).toEqual([
      'data-overview',
      'structure',
      'missing',
      'numeric-summary',
      'histogram',
      'bar-category',
      'bar-category-2',
      'bar-measure-category',
      'boxplot-category',
      'trend'
    ]);
    expect(titleOf(recipes, 'histogram')).toBe('Histogramm «messwert»');
    expect(titleOf(recipes, 'bar-category')).toBe('Balkendiagramm «parameter»');
    expect(titleOf(recipes, 'bar-category-2')).toBe('Balkendiagramm «messstelle_code»');
    expect(titleOf(recipes, 'boxplot-category')).toBe('Boxplot «messwert» nach «parameter»');
    expect(titleOf(recipes, 'trend')).toBe('Trend «messwert» nach «jahr»');
  });

  it('uses the SQL laboratory Dunkelblau for single series plots', () => {
    const recipes = buildRRecipes(snapshot([
      ['jahr', 'INTEGER', 'integer', ['year']],
      ['messstelle', 'VARCHAR', 'character', ['category']],
      ['messwert', 'DOUBLE', 'numeric', ['measure']]
    ], [
      [2020, 'A', 4.2],
      [2021, 'B', 5.1]
    ]));

    expect(codeOf(recipes, 'histogram')).toContain('fill = "#104E8B"');
    expect(codeOf(recipes, 'bar-category')).toContain('fill = "#104E8B"');
    expect(codeOf(recipes, 'bar-measure-category')).toContain('fill = "#104E8B"');
    expect(codeOf(recipes, 'boxplot-category')).toContain('fill = "#104E8B"');
    expect(codeOf(recipes, 'trend')).toContain('geom_line(color = "#104E8B")');
    expect(codeOf(recipes, 'trend')).toContain('geom_point(color = "#104E8B")');
    expect(recipes.every((recipe) => !recipe.code.includes('#d00009') && !recipe.code.includes('#24364b'))).toBe(true);
  });

  it('detects year values like Jahrgang and keeps them out of the measure recipes', () => {
    const recipes = buildRRecipes(snapshot([
      ['Jahrgang', 'BIGINT', 'integer', ['measure']],
      ['Auslaender', 'BIGINT', 'integer', ['measure']],
      ['Schweizer', 'BIGINT', 'integer', ['measure']]
    ], [
      [1920, 2, 60],
      [1921, 3, 58],
      [1922, 5, 55]
    ]));

    expect(idsOf(recipes)).toEqual([
      'data-overview',
      'structure',
      'missing',
      'numeric-summary',
      'histogram',
      'trend',
      'trend-multi',
      'scatter'
    ]);
    expect(titleOf(recipes, 'histogram')).toBe('Histogramm «Auslaender»');
    expect(titleOf(recipes, 'trend')).toBe('Trend «Auslaender» nach «Jahrgang»');
    expect(titleOf(recipes, 'trend-multi')).toBe('Trend «Auslaender» + «Schweizer» nach «Jahrgang»');
    expect(titleOf(recipes, 'scatter')).toBe('Streudiagramm «Auslaender» und «Schweizer»');
    expect(codeOf(recipes, 'trend-multi')).toContain('pivot_longer(');
    expect(codeOf(recipes, 'trend-multi')).toContain('scale_color_manual(values = c("#104E8B", "#00B2EE"))');
    expect(codeOf(recipes, 'scatter')).toContain('color = "#104E8B"');
    expect(codeOf(recipes, 'scatter')).toContain('cor(');
    expect(codeOf(recipes, 'scatter')).toContain('plot_limit <- 10000');
    expect(recipes.some((recipe) => recipe.title.includes('Jahrgang') && recipe.title.startsWith('Histogramm'))).toBe(false);
  });

  it('offers a count per time recipe when only a time column exists', () => {
    const recipes = buildRRecipes(snapshot([
      ['Jahrgang', 'BIGINT', 'integer', ['measure']]
    ], [
      [1920],
      [1921]
    ]));

    expect(idsOf(recipes)).toEqual([
      'data-overview',
      'structure',
      'missing',
      'numeric-summary',
      'time-count'
    ]);
    expect(titleOf(recipes, 'time-count')).toBe('Anzahl pro «Jahrgang»');
    expect(codeOf(recipes, 'time-count')).toContain('geom_line(color = "#104E8B")');
    expect(codeOf(recipes, 'time-count')).toContain('geom_point(color = "#104E8B")');
  });

  it('builds a standalone R example when no SQL result is available', () => {
    const recipes = buildRRecipes(undefined);

    expect(recipes).toEqual([{
      id: 'r-example',
      title: 'R-Beispiel',
      code: 'werte <- 1:5\ndata.frame(wert = werte, quadrat = werte^2)'
    }]);
  });

  it('omits plot recipes when only identifier columns exist', () => {
    const recipes = buildRRecipes(snapshot([
      ['objekt_id', 'BIGINT', 'character', ['identifier']]
    ], [
      ['9007199254740999']
    ]));

    expect(idsOf(recipes)).toEqual(['data-overview', 'structure', 'missing']);
  });
});

function snapshot(
  columns: SnapshotColumnTuple[],
  rows: Array<Array<string | number | boolean | null>> = []
): SqlResultSnapshot {
  return {
    sourceSql: 'select * from daten',
    rowCount: rows.length,
    rows,
    columns: columns.map(([name, duckdbType, rType, roles]) => ({
      name,
      duckdbType,
      rType,
      roles,
      nullable: true
    }))
  };
}

function idsOf(recipes: RRecipe[]): string[] {
  return recipes.map((recipe) => recipe.id);
}

function titleOf(recipes: RRecipe[], id: string): string {
  return recipeOf(recipes, id).title;
}

function codeOf(recipes: RRecipe[], id: string): string {
  return recipeOf(recipes, id).code;
}

function recipeOf(recipes: RRecipe[], id: string): RRecipe {
  const recipe = recipes.find((candidate) => candidate.id === id);
  if (!recipe) {
    throw new Error(`Recipe not found: ${id}`);
  }
  return recipe;
}
