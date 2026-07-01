import type {Table} from 'apache-arrow';

export type QueryStatus = 'idle' | 'running' | 'success' | 'error' | 'cancelled' | 'timeout';

export interface QueryResultState {
  status: QueryStatus;
  sourceSql: string;
  executedSql?: string;
  columns: string[];
  rows: Array<Record<string, unknown>>;
  rowCount: number;
  durationMs?: number;
  maxRowsApplied?: boolean;
  arrowTable?: Table;
  error?: string;
}

export const idleQueryResult: QueryResultState = {
  status: 'idle',
  sourceSql: '',
  columns: [],
  rows: [],
  rowCount: 0
};

