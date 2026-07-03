import type {DuckDbConnector} from '@sqlrooms/duckdb';
import type {Table} from 'apache-arrow';
import type {ExploreColumnDto, ExploreTableDto} from '../app/ExploreContext';
import type {RegisteredTable} from './registerParquetTables';

export interface RuntimeColumn {
  name: string;
  type: string;
}

export type RuntimeSchemaErrorHandler = (table: ExploreTableDto, error: unknown) => void;

export async function loadRuntimeSchemas(
  db: DuckDbConnector,
  registrations: RegisteredTable[],
  onError?: RuntimeSchemaErrorHandler
): Promise<ExploreTableDto[]> {
  const tables: ExploreTableDto[] = [];

  for (const registration of registrations) {
    if (registration.status !== 'registered') {
      tables.push(withoutRuntimeColumns(registration.table));
      continue;
    }

    try {
      tables.push(await loadRuntimeSchema(db, registration.table));
    } catch (error) {
      onError?.(registration.table, error);
      tables.push(withoutRuntimeColumns(registration.table));
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
    columns: mergeRuntimeColumns(table.columns, runtimeColumns)
  };
}

export function buildDescribeTableSql(table: ExploreTableDto): string {
  return `DESCRIBE ${quoteIdentifier(table.name)};`;
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

function findFieldIndex(fieldNames: string[], expectedName: string): number {
  return fieldNames.findIndex((name) => name.toLowerCase() === expectedName);
}

function normalizeCell(value: unknown): string {
  return value === null || value === undefined ? '' : String(value).trim();
}

function quoteIdentifier(identifier: string): string {
  return `"${identifier.replace(/"/g, '""')}"`;
}

function withoutRuntimeColumns(table: ExploreTableDto): ExploreTableDto {
  return {
    ...table,
    columns: []
  };
}
