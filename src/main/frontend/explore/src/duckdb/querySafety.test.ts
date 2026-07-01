import {describe, expect, it} from 'vitest';
import {
  applyResultLimit,
  hasResultLimitApplied,
  isReadOnlyQuery,
  normalizeSqlForExecution,
  queryTimeoutMessage
} from './querySafety';

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
    expect(isReadOnlyQuery('update gemeinden set name = 1')).toBe(false);
    expect(isReadOnlyQuery('delete from gemeinden')).toBe(false);
    expect(isReadOnlyQuery('alter table gemeinden add column x int')).toBe(false);
    expect(isReadOnlyQuery('create table kopie as select * from gemeinden')).toBe(false);
    expect(isReadOnlyQuery("copy gemeinden to 'out.csv'")).toBe(false);
    expect(isReadOnlyQuery("attach 'file.db' as other")).toBe(false);
    expect(isReadOnlyQuery('install httpfs')).toBe(false);
    expect(isReadOnlyQuery('load httpfs')).toBe(false);
    expect(isReadOnlyQuery('call dbgen(sf=1)')).toBe(false);
    expect(isReadOnlyQuery('set memory_limit = 1GB')).toBe(false);
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

  it('normalizes statements and reports when a result limit was applied', () => {
    const executed = normalizeSqlForExecution('select * from gemeinden;', 25);

    expect(hasResultLimitApplied('select * from gemeinden;', executed)).toBe(true);
    expect(hasResultLimitApplied('select * from gemeinden limit 10;', 'select * from gemeinden limit 10')).toBe(false);
  });

  it('formats timeout messages', () => {
    expect(queryTimeoutMessage(30000)).toBe('Die Abfrage wurde nach 30 Sekunden abgebrochen.');
  });
});
