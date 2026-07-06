import type {SqlResultSnapshot} from '../results/sqlResultSnapshot';
import type {WebRLike} from './WebRRuntime';

export type RConsoleEntryType = 'prompt' | 'stdout' | 'stderr' | 'message' | 'warning' | 'error' | 'result';

export interface RConsoleEntry {
  type: RConsoleEntryType;
  text: string;
}

export interface RDataFrameColumnInfo {
  name: string;
  duckdbType: string;
  rType: string;
  nullable: boolean;
  roles: string[];
  warning?: string;
}

export interface RDataFrameInfo {
  name: string;
  sourceSql: string;
  rowCount: number;
  columnCount: number;
  columns: RDataFrameColumnInfo[];
  warnings: string[];
  transferredAt: string;
  limitedFrom?: number;
}

export interface RRunResult {
  entries: RConsoleEntry[];
  images: ImageBitmap[];
  hasTabularResult: boolean;
}

export class WebRBridge {
  private readonly webR: WebRLike;

  constructor(webR: WebRLike) {
    this.webR = webR;
  }

  async loadDataFrame(snapshot: SqlResultSnapshot, dataFrameName: string, limitedFrom?: number): Promise<RDataFrameInfo> {
    if (!/^[A-Za-z.][A-Za-z0-9._]*$/.test(dataFrameName)) {
      throw new Error(`Ungültiger R-Dataframe-Name: ${dataFrameName}`);
    }

    const payloadPath = `/tmp/datenportal-sql-result-${Date.now()}.json`;
    const payload = {
      dataFrameName,
      columns: snapshot.columns,
      rows: snapshot.rows,
      rowCount: snapshot.rowCount,
      sourceSql: snapshot.sourceSql,
      executedSql: snapshot.executedSql,
      schemaJson: JSON.stringify({
        sourceSql: snapshot.sourceSql,
        executedSql: snapshot.executedSql,
        columns: snapshot.columns
      })
    };
    await this.webR.FS.writeFile(payloadPath, new TextEncoder().encode(JSON.stringify(payload)));
    try {
      await this.webR.evalRVoid(dataFrameTransferScript(payloadPath, dataFrameName), {captureStreams: true});
    } finally {
      await this.webR.FS.unlink(payloadPath).catch(() => undefined);
    }

    const warnings = snapshot.columns.map((column) => column.warning).filter((warning): warning is string => Boolean(warning));
    return {
      name: dataFrameName,
      sourceSql: snapshot.sourceSql,
      rowCount: snapshot.rowCount,
      columnCount: snapshot.columns.length,
      columns: snapshot.columns.map((column) => ({
        name: column.name,
        duckdbType: column.duckdbType,
        rType: column.rType,
        nullable: column.nullable,
        roles: column.roles,
        warning: column.warning
      })),
      warnings,
      transferredAt: new Date().toISOString(),
      limitedFrom
    };
  }

  async runR(code: string, plotWidth: number, plotHeight: number): Promise<RRunResult> {
    const shelter = await new this.webR.Shelter();
    const entries: RConsoleEntry[] = [{type: 'prompt', text: promptText(code)}];
    try {
      const captured = await shelter.captureR(wrapUserCode(code), {
        captureStreams: true,
        captureConditions: true,
        captureGraphics: {width: plotWidth, height: plotHeight, bg: 'white', capture: true},
        throwJsException: false,
        withAutoprint: true
      });
      entries.push(...captured.output.map(toConsoleEntry));
      const hasTabularResult = await this.hasTabularResult();
      return {entries, images: captured.images ?? [], hasTabularResult};
    } catch (error) {
      entries.push({type: 'error', text: toErrorMessage(error)});
      return {entries, images: [], hasTabularResult: await this.hasTabularResult()};
    } finally {
      await shelter.purge().catch(() => undefined);
    }
  }

  async exportCurrentTableCsv(dataFrameName: string): Promise<string> {
    const path = `/tmp/datenportal-r-result-${Date.now()}.csv`;
    await this.webR.evalRVoid(exportTableScript(path, dataFrameName), {captureStreams: true});
    try {
      return new TextDecoder('utf-8').decode(await this.webR.FS.readFile(path));
    } finally {
      await this.webR.FS.unlink(path).catch(() => undefined);
    }
  }

  private async hasTabularResult(): Promise<boolean> {
    return this.webR.evalRBoolean(
      'exists("datenportal_last_result", envir = .GlobalEnv) && (is.data.frame(datenportal_last_result) || is.matrix(datenportal_last_result))'
    ).catch(() => false);
  }
}

function dataFrameTransferScript(payloadPath: string, dataFrameName: string): string {
  return `
payload <- jsonlite::fromJSON(${rString(payloadPath)}, simplifyVector = FALSE)
columns <- payload$columns
rows <- payload$rows

value_or_na <- function(value) {
  if (is.null(value)) {
    return(NA)
  }
  value
}

as_character_values <- function(values) {
  vapply(values, function(value) {
    if (is.null(value) || length(value) == 0 || is.na(value)) {
      NA_character_
    } else {
      as.character(value)
    }
  }, character(1), USE.NAMES = FALSE)
}

coerce_column <- function(index) {
  column <- columns[[index]]
  values <- lapply(rows, function(row) {
    if (length(row) < index) {
      return(NA)
    }
    value_or_na(row[[index]])
  })
  r_type <- column$rType
  if (identical(r_type, "integer")) {
    return(as.integer(unlist(values, use.names = FALSE)))
  }
  if (identical(r_type, "numeric")) {
    return(as.numeric(unlist(values, use.names = FALSE)))
  }
  if (identical(r_type, "logical")) {
    return(as.logical(unlist(values, use.names = FALSE)))
  }
  text_values <- as_character_values(values)
  if (identical(r_type, "Date")) {
    return(as.Date(text_values))
  }
  if (identical(r_type, "POSIXct")) {
    return(as.POSIXct(text_values, tz = "UTC", origin = "1970-01-01"))
  }
  text_values
}

data_columns <- lapply(seq_along(columns), coerce_column)
names(data_columns) <- vapply(columns, function(column) column$name, character(1))
${dataFrameName} <- as.data.frame(data_columns, stringsAsFactors = FALSE, optional = TRUE, check.names = FALSE)

field <- function(name) {
  vapply(columns, function(column) {
    value <- column[[name]]
    if (is.null(value) || length(value) == 0) NA_character_ else as.character(value)
  }, character(1), USE.NAMES = FALSE)
}
roles <- vapply(columns, function(column) {
  value <- column$roles
  if (is.null(value) || length(value) == 0) NA_character_ else paste(unlist(value, use.names = FALSE), collapse = ",")
}, character(1), USE.NAMES = FALSE)
daten_schema <- data.frame(
  name = field("name"),
  duckdb_type = field("duckdbType"),
  r_type = field("rType"),
  nullable = as.logical(field("nullable")),
  roles = roles,
  stringsAsFactors = FALSE,
  check.names = FALSE
)
attr(${dataFrameName}, "duckdb_schema_json") <- payload$schemaJson
assign("daten_schema", daten_schema, envir = .GlobalEnv)
assign(${rString(dataFrameName)}, ${dataFrameName}, envir = .GlobalEnv)
`;
}

function wrapUserCode(code: string): string {
  return `
datenportal_last_value <- local({
${code}
})
if (is.data.frame(datenportal_last_value) || is.matrix(datenportal_last_value)) {
  assign("datenportal_last_result", datenportal_last_value, envir = .GlobalEnv)
}
datenportal_last_value
`;
}

function exportTableScript(path: string, dataFrameName: string): string {
  return `
datenportal_export_target <- if (
  exists("datenportal_last_result", envir = .GlobalEnv) &&
  (is.data.frame(datenportal_last_result) || is.matrix(datenportal_last_result))
) {
  get("datenportal_last_result", envir = .GlobalEnv)
} else {
  get(${rString(dataFrameName)}, envir = .GlobalEnv)
}
utils::write.table(
  datenportal_export_target,
  file = ${rString(path)},
  sep = ";",
  dec = ".",
  quote = TRUE,
  row.names = FALSE,
  na = "",
  fileEncoding = "UTF-8"
)
`;
}

function promptText(code: string): string {
  return code.split(/\r?\n/).map((line, index) => `${index === 0 ? '>' : '+'} ${line}`).join('\n');
}

function toConsoleEntry(output: {type: string; data: unknown}): RConsoleEntry {
  const type = normalizeOutputType(output.type);
  return {
    type,
    text: typeof output.data === 'string' ? output.data : JSON.stringify(output.data)
  };
}

function normalizeOutputType(type: string): RConsoleEntryType {
  if (type === 'stdout' || type === 'stderr' || type === 'warning' || type === 'error' || type === 'message') {
    return type;
  }
  return 'result';
}

function rString(value: string): string {
  return JSON.stringify(value);
}

function toErrorMessage(error: unknown): string {
  return error instanceof Error ? error.message : String(error);
}
