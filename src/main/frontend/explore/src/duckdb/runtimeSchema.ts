import type {DuckDbConnector} from '@sqlrooms/duckdb';
import type {Table} from 'apache-arrow';
import type {ExploreColumnDto, ExploreTableDto} from '../app/ExploreContext';
import type {RegisteredTable} from './registerParquetTables';

export interface RuntimeColumn {
  name: string;
  type: string;
}

export type RuntimeSchemaReadStage = 'schema' | 'rowCount';

export type RuntimeSchemaErrorHandler = (
  table: ExploreTableDto,
  error: unknown,
  stage: RuntimeSchemaReadStage
) => void;

export async function loadRuntimeSchemas(
  db: DuckDbConnector,
  registrations: RegisteredTable[],
  onError?: RuntimeSchemaErrorHandler
): Promise<ExploreTableDto[]> {
  const tables: ExploreTableDto[] = [];

  for (const registration of registrations) {
    if (registration.status !== 'registered') {
      tables.push(withoutRuntimeMetadata(registration.table));
      continue;
    }

    try {
      const tableWithRuntimeSchema = await loadRuntimeSchema(db, registration.table);
      let rowCountEstimate: number | undefined;
      try {
        rowCountEstimate = await loadRuntimeRowCount(db, registration.table);
      } catch (error) {
        onError?.(registration.table, error, 'rowCount');
      }
      tables.push({
        ...tableWithRuntimeSchema,
        rowCountEstimate
      });
    } catch (error) {
      onError?.(registration.table, error, 'schema');
      tables.push(withoutRuntimeMetadata(registration.table));
    }
  }

  return tables;
}

export async function loadRuntimeSchema(
  db: DuckDbConnector,
  table: ExploreTableDto
): Promise<ExploreTableDto> {
  const describeResult = await db.query(buildDescribeTableSql(table));
  const runtimeColumns = parseDescribeColumns(describeResult as Table);
  return {
    ...table,
    columns: mergeRuntimeColumns(table.columns, runtimeColumns),
    rowCountEstimate: undefined
  };
}

export function buildDescribeTableSql(table: ExploreTableDto): string {
  return `DESCRIBE ${quoteIdentifier(table.name)};`;
}

export async function loadRuntimeRowCount(
  db: DuckDbConnector,
  table: ExploreTableDto
): Promise<number | undefined> {
  const rowCountResult = await db.query(buildCountRowsSql(table));
  return parseRuntimeRowCount(rowCountResult as Table);
}

export function buildCountRowsSql(table: ExploreTableDto): string {
  return `SELECT count(*) AS row_count FROM ${quoteIdentifier(table.name)};`;
}

export function parseDescribeColumns(table: Table): RuntimeColumn[] {
  const fieldNames = table.schema.fields.map((field) => field.name);
  const nameIndex = findFieldIndex(fieldNames, 'column_name');
  const typeIndex = findFieldIndex(fieldNames, 'column_type');

  if (nameIndex < 0 || typeIndex < 0) {
    throw new Error('DuckDB DESCRIBE result does not contain column_name and column_type.');
  }

  const columns: RuntimeColumn[] = [];
  const nameVector = table.getChildAt(nameIndex);
  const typeVector = table.getChildAt(typeIndex);

  for (let rowIndex = 0; rowIndex < table.numRows; rowIndex++) {
    const name = normalizeCell(nameVector?.get(rowIndex));
    if (!name) {
      continue;
    }
    columns.push({
      name,
      type: normalizeCell(typeVector?.get(rowIndex)) || 'UNKNOWN'
    });
  }

  return columns;
}

export function mergeRuntimeColumns(
  catalogColumns: ExploreColumnDto[],
  runtimeColumns: RuntimeColumn[]
): ExploreColumnDto[] {
  const catalogByName = new Map(catalogColumns.map((column) => [column.name, column]));

  return runtimeColumns.map((runtimeColumn) => {
    const catalogColumn = catalogByName.get(runtimeColumn.name);
    return {
      name: runtimeColumn.name,
      type: runtimeColumn.type,
      nullable: catalogColumn?.nullable,
      required: catalogColumn?.required,
      description: catalogColumn?.description,
      example: catalogColumn?.example,
      roles: catalogColumn?.roles?.length ? catalogColumn.roles : ['unknown']
    };
  });
}

export function parseRuntimeRowCount(table: Table): number | undefined {
  if (table.numRows < 1) {
    return undefined;
  }

  const fieldNames = table.schema.fields.map((field) => field.name);
  const rowCountIndex = findRowCountFieldIndex(fieldNames);
  if (rowCountIndex < 0) {
    throw new Error('DuckDB row count result does not contain row_count.');
  }

  const rowCountValue = table.getChildAt(rowCountIndex)?.get(0);
  const rowCount = normalizeRowCount(rowCountValue);
  if (rowCount === undefined) {
    throw new Error('DuckDB row_count value is not numeric.');
  }
  return rowCount;
}

function findFieldIndex(fieldNames: string[], expectedName: string): number {
  return fieldNames.findIndex((name) => name.toLowerCase() === expectedName);
}

function findRowCountFieldIndex(fieldNames: string[]): number {
  const namedIndex = findFieldIndex(fieldNames, 'row_count');
  if (namedIndex >= 0) {
    return namedIndex;
  }
  return fieldNames.length === 1 ? 0 : -1;
}

function normalizeCell(value: unknown): string {
  return value === null || value === undefined ? '' : String(value).trim();
}

function quoteIdentifier(identifier: string): string {
  return `"${identifier.replace(/"/g, '""')}"`;
}

function normalizeRowCount(value: unknown): number | undefined {
  if (typeof value === 'number') {
    return Number.isFinite(value) ? Math.trunc(value) : undefined;
  }
  if (typeof value === 'bigint') {
    return Number(value);
  }
  if (Array.isArray(value) && value.length > 0) {
    return normalizeRowCount(value[0]);
  }
  if (ArrayBuffer.isView(value)) {
    const arrayLike = value as unknown as {length?: number; [index: number]: unknown};
    if (typeof arrayLike.length === 'number' && arrayLike.length > 0) {
      return normalizeRowCount(arrayLike[0]);
    }
  }
  if (typeof value === 'string') {
    const parsed = Number(value.trim());
    return Number.isFinite(parsed) ? Math.trunc(parsed) : undefined;
  }
  if (value && typeof value === 'object') {
    const primitive = value.valueOf();
    if (primitive !== value) {
      return normalizeRowCount(primitive);
    }
    const text = value.toString();
    if (text && text !== '[object Object]') {
      return normalizeRowCount(text);
    }
  }
  return undefined;
}

function withoutRuntimeMetadata(table: ExploreTableDto): ExploreTableDto {
  return {
    ...table,
    columns: [],
    rowCountEstimate: undefined
  };
}
