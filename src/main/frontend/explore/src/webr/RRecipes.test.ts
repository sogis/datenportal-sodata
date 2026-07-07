import {describe, expect, it} from 'vitest';
import {buildRRecipes} from './RRecipes';
import type {SqlResultSnapshot} from '../results/sqlResultSnapshot';

describe('buildRRecipes', () => {
  it('builds generic recipes plus numeric/category/date plot recipes when compatible columns exist', () => {
    const recipes = buildRRecipes(snapshot([
      ['jahr', 'INTEGER', 'integer', ['year']],
      ['messstelle', 'VARCHAR', 'character', ['category']],
      ['nitrat_mg_l', 'DOUBLE', 'numeric', ['measure']]
    ]));

    expect(recipes.map((recipe) => recipe.id)).toEqual([
      'data-overview',
      'structure',
      'missing',
      'numeric-summary',
      'histogram',
      'boxplot-category',
      'trend'
    ]);
    expect(recipes.find((recipe) => recipe.id === 'data-overview')?.title).toBe('Datenüberblick');
    expect(recipes.find((recipe) => recipe.id === 'histogram')?.title).toBe('Histogramm «nitrat_mg_l»');
    expect(recipes.find((recipe) => recipe.id === 'boxplot-category')?.title).toBe('Boxplot «nitrat_mg_l» nach «messstelle»');
    expect(recipes.find((recipe) => recipe.id === 'trend')?.title).toBe('Trend «nitrat_mg_l» nach «jahr»');
    expect(recipes.find((recipe) => recipe.id === 'histogram')?.code).toContain('ggplot');
    expect(recipes.find((recipe) => recipe.id === 'histogram')?.code).toContain('plot_limit <- 10000');
    expect(recipes.find((recipe) => recipe.id === 'boxplot-category')?.code).toContain('coord_flip');
  });

  it('prefers measure columns over year, id and code columns for plot recipes', () => {
    const recipes = buildRRecipes(snapshot([
      ['jahr', 'INTEGER', 'integer', ['year']],
      ['messstelle_code', 'VARCHAR', 'character', ['category']],
      ['parameter', 'VARCHAR', 'character', ['category']],
      ['messwert', 'DOUBLE', 'numeric', ['measure']]
    ]));

    expect(recipes.find((recipe) => recipe.id === 'histogram')?.title).toBe('Histogramm «messwert»');
    expect(recipes.find((recipe) => recipe.id === 'boxplot-category')?.title).toBe('Boxplot «messwert» nach «parameter»');
    expect(recipes.find((recipe) => recipe.id === 'trend')?.title).toBe('Trend «messwert» nach «jahr»');
  });

  it('builds a standalone R example when no SQL result is available', () => {
    const recipes = buildRRecipes(undefined);

    expect(recipes).toEqual([{
      id: 'r-example',
      title: 'R-Beispiel',
      code: 'werte <- 1:5\ndata.frame(wert = werte, quadrat = werte^2)'
    }]);
  });

  it('omits plot recipes when no compatible columns exist', () => {
    const recipes = buildRRecipes(snapshot([
      ['name', 'VARCHAR', 'character', ['label']],
      ['status', 'VARCHAR', 'character', ['category']]
    ]));

    expect(recipes.map((recipe) => recipe.id)).toEqual(['data-overview', 'structure', 'missing']);
  });
});

function snapshot(columns: Array<[string, string, SqlResultSnapshot['columns'][number]['rType'], SqlResultSnapshot['columns'][number]['roles']]>): SqlResultSnapshot {
  return {
    sourceSql: 'select * from daten',
    rowCount: 10,
    rows: [],
    columns: columns.map(([name, duckdbType, rType, roles]) => ({
      name,
      duckdbType,
      rType,
      roles,
      nullable: true
    }))
  };
}
