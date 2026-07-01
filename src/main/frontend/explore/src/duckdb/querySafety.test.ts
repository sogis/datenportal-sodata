import {describe, expect, it} from 'vitest';
import {applyResultLimit, isReadOnlyQuery, normalizeSqlForExecution} from './querySafety';

describe('querySafety', () => {
  it('allows read-only query forms', () => {
    expect(isReadOnlyQuery('select * from gemeinden')).toBe(true);
    expect(isReadOnlyQuery('with q as (select 1) select * from q')).toBe(true);
    expect(isReadOnlyQuery('describe gemeinden')).toBe(true);
    expect(isReadOnlyQuery('show tables')).toBe(true);
    expect(isReadOnlyQuery('pragma table_info(gemeinden)')).toBe(true);
  });

  it('blocks mutation and system commands', () => {
    expect(isReadOnlyQuery('drop table gemeinden')).toBe(false);
    expect(isReadOnlyQuery('insert into gemeinden values (1)')).toBe(false);
    expect(isReadOnlyQuery("copy gemeinden to 'out.csv'")).toBe(false);
    expect(isReadOnlyQuery('install httpfs')).toBe(false);
    expect(isReadOnlyQuery('load httpfs')).toBe(false);
  });

  it('requires a single statement', () => {
    expect(isReadOnlyQuery('select 1; select 2;')).toBe(false);
    expect(() => normalizeSqlForExecution('select 1; select 2;', 100)).toThrow('genau eine SQL-Anweisung');
  });

  it('applies a row limit when missing', () => {
    const limited = applyResultLimit('select * from gemeinden', 100);

    expect(limited.toLowerCase()).toContain('limit 100');
    expect(limited.toLowerCase()).toContain('from gemeinden');
  });

  it('preserves an existing top-level limit', () => {
    expect(applyResultLimit('select * from gemeinden limit 10;', 100)).toBe('select * from gemeinden limit 10');
  });

  it('does not treat nested limits as top-level limits', () => {
    const limited = applyResultLimit('select * from (select * from gemeinden limit 10) q', 100);

    expect(limited.toLowerCase()).toContain('limit 100');
  });
});
