export interface QueryHistoryItem {
  id: string;
  sql: string;
  executedAt: string;
  recipeTitle?: string;
  rowCount?: number;
  durationMs?: number;
}

const historyKeyPrefix = 'datenportal.explore.history.';
const maxHistoryItems = 20;

export function loadQueryHistory(datasetId: string): QueryHistoryItem[] {
  try {
    const storage = localStorageOrUndefined();
    if (!storage) {
      return [];
    }
    const raw = storage.getItem(historyKey(datasetId));
    if (!raw) {
      return [];
    }
    const parsed: unknown = JSON.parse(raw);
    if (!Array.isArray(parsed)) {
      return [];
    }
    return parsed.map(toHistoryItem).filter((item): item is QueryHistoryItem => item !== null).slice(0, maxHistoryItems);
  } catch {
    return [];
  }
}

export function saveQueryHistory(datasetId: string, item: QueryHistoryItem): void {
  try {
    const storage = localStorageOrUndefined();
    if (!storage || !item.sql.trim()) {
      return;
    }
    const next = [sanitizeHistoryItem(item), ...loadQueryHistory(datasetId)].slice(0, maxHistoryItems);
    storage.setItem(historyKey(datasetId), JSON.stringify(next));
  } catch {
    // Local history is a convenience feature. Storage failures must not break SQL execution.
  }
}

export function clearQueryHistory(datasetId: string): void {
  try {
    localStorageOrUndefined()?.removeItem(historyKey(datasetId));
  } catch {
    // Ignore storage failures.
  }
}

function historyKey(datasetId: string): string {
  return `${historyKeyPrefix}${datasetId}`;
}

function localStorageOrUndefined(): Storage | undefined {
  try {
    return globalThis.localStorage;
  } catch {
    return undefined;
  }
}

function toHistoryItem(value: unknown): QueryHistoryItem | null {
  if (!value || typeof value !== 'object') {
    return null;
  }
  const candidate = value as Record<string, unknown>;
  if (typeof candidate.id !== 'string' || typeof candidate.sql !== 'string' || typeof candidate.executedAt !== 'string') {
    return null;
  }
  return sanitizeHistoryItem({
    id: candidate.id,
    sql: candidate.sql,
    executedAt: candidate.executedAt,
    recipeTitle: typeof candidate.recipeTitle === 'string' ? candidate.recipeTitle : undefined,
    rowCount: typeof candidate.rowCount === 'number' ? candidate.rowCount : undefined,
    durationMs: typeof candidate.durationMs === 'number' ? candidate.durationMs : undefined
  });
}

function sanitizeHistoryItem(item: QueryHistoryItem): QueryHistoryItem {
  return {
    id: item.id,
    sql: item.sql,
    executedAt: item.executedAt,
    recipeTitle: item.recipeTitle,
    rowCount: item.rowCount,
    durationMs: item.durationMs
  };
}
