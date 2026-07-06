import type {ExploreColumnRole} from '../app/ExploreContext';

export type RColumnType = 'integer' | 'numeric' | 'character' | 'logical' | 'Date' | 'POSIXct';

export interface DuckDbColumnForR {
  name: string;
  duckdbType: string;
  nullable: boolean;
  roles: ExploreColumnRole[];
}

export interface RTypeMapping {
  rType: RColumnType;
  warning?: string;
}

export function mapDuckDbColumnToR(column: DuckDbColumnForR): RTypeMapping {
  const normalizedType = normalizeDuckDbType(column.duckdbType);
  if (column.roles.includes('identifier')) {
    return {rType: 'character', warning: 'Identifier werden in R als character übernommen.'};
  }
  if (column.roles.includes('geometry') || isGeometryType(normalizedType)) {
    return {rType: 'character', warning: 'Geometrien werden in R V1 als character übernommen.'};
  }
  if (isWideIntegerType(normalizedType)) {
    return {rType: 'character', warning: '64-bit Integer werden verlustfrei als character übernommen.'};
  }
  if (isDecimalType(normalizedType)) {
    return {rType: 'character', warning: 'DECIMAL-Werte werden verlustfrei als character übernommen.'};
  }
  if (isBinaryType(normalizedType)) {
    return {rType: 'character', warning: 'Binärspalten werden als character-Beschreibung übernommen.'};
  }
  if (isBooleanType(normalizedType)) {
    return {rType: 'logical'};
  }
  if (isIntegerType(normalizedType)) {
    return {rType: 'integer'};
  }
  if (isNumericType(normalizedType)) {
    return {rType: 'numeric'};
  }
  if (isTimestampType(normalizedType)) {
    return {rType: 'POSIXct'};
  }
  if (isDateType(normalizedType) || column.roles.includes('date')) {
    return {rType: 'Date'};
  }
  return {rType: 'character'};
}

export function normalizeDuckDbValueForR(value: unknown, mapping: RTypeMapping): string | number | boolean | null {
  if (value === null || value === undefined) {
    return null;
  }
  if (value instanceof Date) {
    if (mapping.rType === 'Date') {
      return value.toISOString().slice(0, 10);
    }
    if (mapping.rType === 'POSIXct') {
      return value.toISOString();
    }
    return value.toISOString();
  }
  if (typeof value === 'bigint') {
    return value.toString();
  }
  if (value instanceof Uint8Array) {
    return `[${value.byteLength} Bytes]`;
  }
  if (typeof value === 'number') {
    if (!Number.isFinite(value)) {
      return null;
    }
    if (mapping.rType === 'integer') {
      return Math.trunc(value);
    }
    return mapping.rType === 'character' ? String(value) : value;
  }
  if (typeof value === 'boolean') {
    return mapping.rType === 'character' ? String(value) : value;
  }
  return String(value);
}

function normalizeDuckDbType(type: string): string {
  return type
    .replace(/\(.+\)/g, '')
    .replace(/<.+>/g, '')
    .replace(/\s+/g, ' ')
    .trim()
    .toUpperCase();
}

function isWideIntegerType(type: string): boolean {
  return ['BIGINT', 'INT64', 'LONG', 'UBIGINT', 'HUGEINT', 'UHUGEINT'].includes(type);
}

function isDecimalType(type: string): boolean {
  return ['DECIMAL', 'NUMERIC'].includes(type);
}

function isBinaryType(type: string): boolean {
  return ['BLOB', 'BYTEA', 'BINARY', 'VARBINARY', 'FIXED_SIZE_BINARY'].includes(type);
}

function isBooleanType(type: string): boolean {
  return ['BOOLEAN', 'BOOL'].includes(type);
}

function isIntegerType(type: string): boolean {
  return ['TINYINT', 'SMALLINT', 'INTEGER', 'INT', 'INT32', 'INT16', 'INT8', 'UTINYINT', 'USMALLINT'].includes(type);
}

function isNumericType(type: string): boolean {
  return ['DOUBLE', 'FLOAT', 'REAL', 'FLOAT4', 'FLOAT8', 'FLOAT32', 'FLOAT64'].includes(type);
}

function isDateType(type: string): boolean {
  return ['DATE', 'DATEDAY', 'DATEMILLISECOND'].includes(type);
}

function isTimestampType(type: string): boolean {
  return type.startsWith('TIMESTAMP') || type === 'TIME64' || type === 'TIME32';
}

function isGeometryType(type: string): boolean {
  return ['GEOMETRY', 'WKB', 'WKT'].includes(type);
}
