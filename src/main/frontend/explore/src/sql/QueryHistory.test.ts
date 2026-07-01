import {beforeEach, describe, expect, it} from 'vitest';
import {clearQueryHistory, loadQueryHistory, saveQueryHistory, type QueryHistoryItem} from './QueryHistory';

describe('QueryHistory', () => {
  beforeEach(() => {
    window.localStorage.clear();
  });

  it('stores history newest first and keeps the last 20 queries', () => {
    for (let index = 0; index < 25; index++) {
      saveQueryHistory('dataset-a', item(index));
    }

    const history = loadQueryHistory('dataset-a');

    expect(history).toHaveLength(20);
    expect(history[0]?.sql).toBe('select 24');
    expect(history[19]?.sql).toBe('select 5');
  });

  it('uses dataset-specific keys', () => {
    saveQueryHistory('dataset-a', item(1));
    saveQueryHistory('dataset-b', item(2));

    expect(loadQueryHistory('dataset-a')).toHaveLength(1);
    expect(loadQueryHistory('dataset-a')[0]?.sql).toBe('select 1');
    expect(loadQueryHistory('dataset-b')[0]?.sql).toBe('select 2');
  });

  it('clears history for one dataset only', () => {
    saveQueryHistory('dataset-a', item(1));
    saveQueryHistory('dataset-b', item(2));

    clearQueryHistory('dataset-a');

    expect(loadQueryHistory('dataset-a')).toEqual([]);
    expect(loadQueryHistory('dataset-b')).toHaveLength(1);
  });

  it('ignores malformed stored history', () => {
    window.localStorage.setItem('datenportal.explore.history.dataset-a', '{not-json');

    expect(loadQueryHistory('dataset-a')).toEqual([]);
  });

  it('does not persist result rows', () => {
    saveQueryHistory('dataset-a', {
      ...item(1),
      rows: [{secret: 'not stored'}]
    } as QueryHistoryItem & {rows: Array<Record<string, string>>});

    const raw = window.localStorage.getItem('datenportal.explore.history.dataset-a');

    expect(raw).not.toContain('rows');
    expect(raw).not.toContain('not stored');
  });
});

function item(index: number): QueryHistoryItem {
  return {
    id: `query-${index}`,
    sql: `select ${index}`,
    executedAt: `2026-07-01T08:${String(index).padStart(2, '0')}:00.000Z`,
    recipeTitle: 'Rezept',
    rowCount: index,
    durationMs: index * 10
  };
}
