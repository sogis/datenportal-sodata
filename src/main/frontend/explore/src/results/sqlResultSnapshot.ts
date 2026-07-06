import type {ExploreColumnDto, ExploreColumnRole, ExploreTableDto} from '../app/ExploreContext';
import type {QueryResultState} from './queryResultTypes';
import {mapDuckDbColumnToR, normalizeDuckDbValueForR} from '../webr/DuckDbToWebRTypeMapper';

export interface SqlResultColumn {
  name: string;
  duckdbType: string;
  nullable: boolean;
  roles: ExploreColumnRole[];
  rType: ReturnType<typeof mapDuckDbColumnToR>['rType'];
  warning?: string;
}

export interface SqlResultSnapshot {
  sourceSql: string;
  executedSql?: string;
  rowCount: number;
  columns: SqlResultColumn[];
  rows: Array<Array<string | number | boolean | null>>;
  maxRowsApplied?: boolean;
}

export function sqlResultSnapshotFromQueryResult(result: QueryResultState, tables: ExploreTableDto[]): SqlResultSnapshot {
  if (result.status !== 'success') {
    throw new Error('Nur erfolgreiche SQL-Resultate können ins R-Labor übernommen werden.');
  }

  const contextColumns = contextColumnIndex(tables);
  const arrowFields = result.arrowTable?.schema.fields ?? [];
  const columns = result.columns.map((name, index) => {
    const contextColumn = contextColumns.get(name.toLowerCase());
    const duckdbType = contextColumn?.type ?? arrowFields[index]?.type?.toString?.() ?? 'VARCHAR';
    const nullable = contextColumn?.nullable ?? (contextColumn?.required === true ? false : arrowFields[index]?.nullable ?? true);
    const roles = contextColumn?.roles ?? inferRoles(name, duckdbType);
    const mapping = mapDuckDbColumnToR({name, duckdbType, nullable, roles});
    return {
      name,
      duckdbType,
      nullable,
      roles,
      rType: mapping.rType,
      warning: mapping.warning
    };
  });

  const rows = result.rows.map((row) =>
    columns.map((column) =>
      normalizeDuckDbValueForR(row[column.name], {
        rType: column.rType,
        warning: column.warning
      })
    )
  );

  return {
    sourceSql: result.sourceSql,
    executedSql: result.executedSql,
    rowCount: result.rowCount,
    columns,
    rows,
    maxRowsApplied: result.maxRowsApplied
  };
}

export function limitSqlResultSnapshot(snapshot: SqlResultSnapshot, limit: number): SqlResultSnapshot {
  return {
    ...snapshot,
    rows: snapshot.rows.slice(0, limit),
    rowCount: Math.min(snapshot.rowCount, limit),
    maxRowsApplied: true
  };
}

function contextColumnIndex(tables: ExploreTableDto[]): Map<string, ExploreColumnDto> {
  const columns = new Map<string, ExploreColumnDto>();
  for (const table of tables) {
    for (const column of table.columns) {
      const key = column.name.toLowerCase();
      if (!columns.has(key)) {
        columns.set(key, column);
      }
    }
  }
  return columns;
}

function inferRoles(name: string, type: string): ExploreColumnRole[] {
  const normalizedName = name.toLowerCase();
  const normalizedType = type.toUpperCase();
  if (normalizedName === 'id' || normalizedName.endsWith('_id') || normalizedName.includes('identifier')) {
    return ['identifier'];
  }
  if (normalizedName.includes('geom') || normalizedType.includes('GEOMETRY')) {
    return ['geometry'];
  }
  if (normalizedName.includes('date') || normalizedName.includes('datum') || normalizedType.includes('DATE')) {
    return ['date'];
  }
  if (normalizedName.includes('year') || normalizedName.includes('jahr')) {
    return ['year'];
  }
  if (/(INT|DOUBLE|FLOAT|REAL|DECIMAL|NUMERIC)/i.test(normalizedType)) {
    return ['measure'];
  }
  return ['unknown'];
}
