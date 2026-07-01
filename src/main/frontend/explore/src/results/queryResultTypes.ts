import type {Table} from 'apache-arrow';
import type {ExploreChartConfigDto} from '../app/ExploreContext';

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
  preferredChart?: ExploreChartConfigDto;
  error?: string;
}

export const idleQueryResult: QueryResultState = {
  status: 'idle',
  sourceSql: '',
  columns: [],
  rows: [],
  rowCount: 0
};
