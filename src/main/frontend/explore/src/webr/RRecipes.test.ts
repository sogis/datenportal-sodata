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
      'start',
      'structure',
      'missing',
      'numeric-summary',
      'histogram',
      'boxplot-category',
      'trend'
    ]);
    expect(recipes.find((recipe) => recipe.id === 'histogram')?.code).toContain('ggplot');
    expect(recipes.find((recipe) => recipe.id === 'boxplot-category')?.code).toContain('coord_flip');
  });

  it('omits plot recipes when no compatible columns exist', () => {
    const recipes = buildRRecipes(snapshot([
      ['name', 'VARCHAR', 'character', ['label']],
      ['status', 'VARCHAR', 'character', ['category']]
    ]));

    expect(recipes.map((recipe) => recipe.id)).toEqual(['start', 'structure', 'missing']);
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
