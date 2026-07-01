# Datenportal Erkunden SQLRooms MVP Agent Spec

Version: 1.0
Status: Draft, not implemented
Target artifact: `/datasets/{datasetId}/explore` as a per-data-topic browser-based SQL and chart exploration workspace
Primary audience: LLM coding agents implementing the MVP in the existing Datenportal web application

## 1. Purpose

Build the first production-quality MVP of the Datenportal page **Erkunden**.

The original product idea is not a generic playground and not a dashboard builder. The page must be available **per Datenthema** and should feel like a small, honest, useful, slightly nerdy data laboratory:

- Users open one data topic.
- All relevant Parquet files of that topic are registered as DuckDB tables/views in the browser.
- Users can inspect tables, schemas, generated profiling queries, query results, and simple charts.
- Power users can write SQL immediately.
- Non-power users can start with generated recipes.
- All execution runs locally in the browser with DuckDB-Wasm.

The MVP must already include charting. Without charting, the feature feels like a half-finished SQL textbox. With charting, the page becomes an actual exploration surface.

## 2. Product Positioning

Use this wording in docs and UI decisions:

> Erkunden ist ein lokales SQL-Labor pro Datenthema. Die Abfragen laufen im Browser mit DuckDB-Wasm direkt auf den Parquet-Dateien.

Do not position it as:

- a BI platform
- a Jupyter replacement
- a notebook
- a server-side query API
- an editing tool
- an AI-first feature

## 3. Non-Negotiable UX Principles

1. The page must be useful for a power user within 10 seconds.
2. The page must not hide SQL. SQL is a feature, not an implementation detail.
3. Generated recipes are entry points, not fake domain expertise.
4. Charts are generated from SQL results. SQL remains the reproducible source of truth.
5. The page must explain that execution is local in the browser, but only briefly.
6. It must degrade gracefully: if DuckDB-Wasm, CORS, Range Requests, or Parquet loading fails, show a clear error and keep the normal dataset detail page usable.
7. No user accounts, no stored server-side sessions, no mutation of source data.

## 4. Intended MVP Scope

### 4.1 In Scope

- Route `/datasets/{datasetId}/explore`
- Spring Boot/JTE host page integrated into the existing Datenportal layout
- Separate React/Vite/SQLRooms island for the interactive explorer
- JSON context object generated server-side per data topic
- DuckDB-Wasm registration of one or more Parquet files
- Schema/table browser
- Generated SQL recipes
- SQL editor
- Query execution
- Query cancel or timeout where technically available
- Result table
- Result CSV export
- Automatic chart suggestion from SQL result
- Chart configuration for bar, line, scatter, and histogram
- Local query history in browser storage
- Static "Weiterverwenden" code snippets for DuckDB CLI, Python, and R
- Tests at backend, frontend, and browser-integration level
- Ongoing documentation and phase tracking

### 4.2 Out of Scope for MVP

- WebR execution
- AI assistant in production
- server-side SQL execution
- user-uploaded data
- saving notebooks
- collaboration
- auth-protected datasets
- arbitrary S3 browser
- geospatial map viewer
- Vega-Lite spec editor
- Mosaic crossfilter dashboards
- Kepler/deck.gl maps
- editing source data

## 5. Technical Context and Assumptions

Adapt names to the actual repository when necessary, but preserve the architecture.

Expected backend:

- Java 25 or current project Java version
- Spring Boot 4.x or current project Spring Boot version
- Gradle
- JTE templates
- Existing package root likely `ch.so.agi.datenportal`
- Existing public dataset/detail pages
- Existing metadata model with distributions/download URLs
- Existing design system / static CSS / SO web components

Expected frontend:

- Current app is mostly server-rendered.
- The new page may use a single rich JavaScript island.
- Use Vite + React + TypeScript for the island.
- Use SQLRooms packages for DuckDB-Wasm, SQL editor, data table, schema/tree, and charting where practical.

Reference SQLRooms packages:

- `@sqlrooms/duckdb`: DuckDB-Wasm wrapper, `useSql`, file loading, schema utilities
- `@sqlrooms/sql-editor`: SQL editor UI/state, query execution helpers
- `@sqlrooms/data-table`: result table components with Arrow support
- `@sqlrooms/recharts`: Recharts integration and SQLRooms-friendly chart wrappers
- `@sqlrooms/schema-tree`: schema explorer
- Optional later: `@sqlrooms/vega`, `@sqlrooms/mosaic`, `@sqlrooms/ai`

Official docs to consult during implementation:

- https://sqlrooms.org/api/duckdb/
- https://sqlrooms.org/api/sql-editor/
- https://sqlrooms.org/api/data-table/
- https://sqlrooms.org/api/recharts/
- https://sqlrooms.org/api/vega/
- https://sqlrooms.org/api/mosaic/
- https://duckdb.org/docs/stable/data/parquet/overview
- https://duckdb.org/docs/stable/core_extensions/httpfs/https

## 6. Architecture Overview

```text
Spring Boot / JTE
  |
  |-- GET /datasets/{datasetId}/explore
  |     - validates dataset id
  |     - loads metadata/distributions
  |     - builds ExploreContextDto
  |     - renders normal portal layout
  |     - embeds JSON context
  |     - loads static React island assets
  |
  |-- GET /datasets/{datasetId}/explore/context.json
        - same ExploreContextDto as JSON
        - useful for tests, debugging, and future static embedding

React / SQLRooms island
  |
  |-- reads embedded context or fetches context.json
  |-- initializes DuckDB-Wasm
  |-- registers Parquet files as safe local views
  |-- renders:
        - dataset/table catalog
        - recipe list
        - SQL editor
        - result table
        - chart suggestion and chart controls
        - code snippets
```

## 7. UX/UI Specification

### 7.1 Desktop Layout

Use restrained Swiss public-sector UI. No giant hero, no marketing section, no decorative blobs, no heavy shadows. Cards may be used for repeated items or framed tools only. Radius 6-8px.

ASCII art target:

```text
+--------------------------------------------------------------------------------+
| Kanton Solothurn     Datenportal                                      Hilfe     |
+--------------------------------------------------------------------------------+
| Daten > Gemeindegrenzen > Erkunden                                             |
|                                                                                |
| Gemeindegrenzen erkunden                                                       |
| Läuft lokal im Browser mit DuckDB-Wasm direkt auf den Parquet-Dateien.          |
|                                                                                |
| [Vorschau] [SQL-Labor] [Diagramm] [Code]                                        |
+----------------------+--------------------------------------+------------------+
| Datenthema           | SQL-Labor                            | Tabellen         |
|----------------------|--------------------------------------|------------------|
| Format: Parquet      | Beispielabfragen                     | gemeinden        |
| Dateien: 2           | [Vorschau] [Anzahl] [Nullwerte]      | bezirke          |
| Aktualisiert: ...    | [Kategorie] [Numerik] [Zeitreihe]    | historisierung   |
| Ausführung: lokal    |                                      |                  |
|                      | SQL                                  | Attribute        |
| Hinweise             | +----------------------------------+ | bfs_nr INTEGER   |
| - max. Resultate     | | select bezirk, count(*) ...      | | name VARCHAR    |
| - keine Serverquery  | +----------------------------------+ | flaeche DOUBLE   |
|                      | [Ausführen] [Abbrechen] [Kopieren]  | stand DATE       |
|                      |                                      |                  |
|                      | Ergebnis                             |                  |
|                      | +----------------------------------+ |                  |
|                      | | bezirk      anzahl               | |                  |
|                      | | Thal        50                   | |                  |
|                      | +----------------------------------+ |                  |
|                      |                                      |                  |
|                      | Visualisierung                       |                  |
|                      | Chart: [Balken v] X: [bezirk] Y: [anzahl]             |
|                      | +----------------------------------+                    |
|                      | | Thal          █████████████ 50    |                    |
|                      | | Dorneck       ████████ 33         |                    |
|                      | +----------------------------------+                    |
+----------------------+--------------------------------------+------------------+
```

### 7.2 Mobile Layout

Mobile is allowed to be reduced. It must not pretend to be a full IDE.

```text
+----------------------------------+
| Datenportal                 Menü |
+----------------------------------+
| Daten > ... > Erkunden           |
| Gemeindegrenzen erkunden         |
| [Vorschau] [SQL] [Chart] [Code]  |
|                                  |
| Beispielabfrage                  |
| [Nach Bezirk gruppieren      v]  |
|                                  |
| SQL                              |
| +------------------------------+ |
| | select bezirk, count(*) ...  | |
| +------------------------------+ |
| [Ausführen]                      |
|                                  |
| Ergebnis                         |
| bezirk       anzahl              |
| Thal         50                  |
| Dorneck      33                  |
|                                  |
| Diagramm                         |
| Thal      █████████              |
| Dorneck   █████                  |
|                                  |
| [Schema anzeigen]                |
+----------------------------------+
```

### 7.3 Required UI States

Implement these visibly:

- Initializing DuckDB-Wasm
- Registering Parquet files
- Ready
- Running query
- Query completed
- Query failed
- Query cancelled or timed out
- No Parquet distribution available
- CORS/HTTP loading failed
- Result too large for chart suggestion
- No chart suggestion available

### 7.4 UI Text Guidelines

German UI text. Keep it short.

Preferred phrases:

- `Lokal im Browser`
- `DuckDB-Wasm`
- `Parquet-Datei direkt abgefragt`
- `SQL kopieren`
- `Resultat als CSV`
- `Abfrage abbrechen`
- `Diagramm aus Resultat`
- `Maximal 10'000 Zeilen angezeigt`

Avoid:

- `KI-gestützt` in MVP
- `Dashboard`
- `Notebook`
- `Beta` unless the whole product policy says so
- long explanatory paragraphs

## 8. Data Contract

Backend must provide this structure. Add fields only when needed; keep backwards compatibility once introduced.

### 8.1 TypeScript Shape

```ts
export interface ExploreContextDto {
  version: 1;
  datasetId: string;
  title: string;
  description?: string;
  canonicalUrl: string;
  updatedAt?: string;
  license?: string;
  execution: ExploreExecutionDto;
  tables: ExploreTableDto[];
  recipes: ExploreRecipeDto[];
  codeSnippets: ExploreCodeSnippetDto[];
  featureFlags: ExploreFeatureFlagsDto;
}

export interface ExploreExecutionDto {
  engine: 'duckdb-wasm';
  mode: 'browser-local';
  maxPreviewRows: number;
  maxResultRows: number;
  queryTimeoutMs: number;
}

export interface ExploreTableDto {
  id: string;
  name: string;
  title: string;
  description?: string;
  parquetUrl: string;
  sizeBytes?: number;
  rowCountEstimate?: number;
  primary: boolean;
  columns: ExploreColumnDto[];
}

export interface ExploreColumnDto {
  name: string;
  type: string;
  nullable?: boolean;
  required?: boolean;
  description?: string;
  example?: string;
  roles: ExploreColumnRole[];
}

export type ExploreColumnRole =
  | 'identifier'
  | 'label'
  | 'category'
  | 'measure'
  | 'date'
  | 'year'
  | 'geometry'
  | 'municipality'
  | 'unknown';

export interface ExploreRecipeDto {
  id: string;
  title: string;
  description: string;
  tableId: string;
  category: 'preview' | 'profile' | 'quality' | 'category' | 'numeric' | 'time' | 'custom';
  sql: string;
  preferredChart?: ExploreChartConfigDto;
}

export interface ExploreChartConfigDto {
  type: 'bar' | 'line' | 'scatter' | 'histogram';
  x?: string;
  y?: string;
  color?: string;
  title?: string;
}

export interface ExploreCodeSnippetDto {
  id: string;
  title: string;
  language: 'sql' | 'python' | 'r' | 'bash';
  code: string;
}

export interface ExploreFeatureFlagsDto {
  charts: boolean;
  localHistory: boolean;
  aiAssistant: boolean;
  webR: boolean;
  vega: boolean;
  mosaic: boolean;
}
```

### 8.2 Example JSON

```json
{
  "version": 1,
  "datasetId": "ch.so.gemeindegrenzen",
  "title": "Gemeindegrenzen",
  "description": "Politische Gemeinden des Kantons Solothurn.",
  "canonicalUrl": "/datasets/ch.so.gemeindegrenzen",
  "updatedAt": "2026-06-30",
  "execution": {
    "engine": "duckdb-wasm",
    "mode": "browser-local",
    "maxPreviewRows": 100,
    "maxResultRows": 10000,
    "queryTimeoutMs": 30000
  },
  "tables": [
    {
      "id": "gemeinden",
      "name": "gemeinden",
      "title": "Gemeinden",
      "parquetUrl": "https://daten.so.ch/downloads/ch.so.gemeindegrenzen.parquet",
      "sizeBytes": 2300000,
      "primary": true,
      "columns": [
        {
          "name": "bfs_nr",
          "type": "INTEGER",
          "required": true,
          "description": "BFS-Nummer der Gemeinde.",
          "example": "2611",
          "roles": ["identifier", "municipality"]
        },
        {
          "name": "bezirk",
          "type": "VARCHAR",
          "description": "Bezirk.",
          "example": "Thal",
          "roles": ["category"]
        },
        {
          "name": "flaeche_ha",
          "type": "DOUBLE",
          "description": "Fläche in Hektaren.",
          "example": "1201.45",
          "roles": ["measure"]
        }
      ]
    }
  ],
  "recipes": [
    {
      "id": "gemeinden-count",
      "title": "Anzahl Datensätze",
      "description": "Zählt alle Zeilen der Tabelle.",
      "tableId": "gemeinden",
      "category": "profile",
      "sql": "select count(*) as anzahl from gemeinden;"
    },
    {
      "id": "gemeinden-bezirk-count",
      "title": "Nach Bezirk gruppieren",
      "description": "Zählt Gemeinden pro Bezirk.",
      "tableId": "gemeinden",
      "category": "category",
      "sql": "select bezirk, count(*) as anzahl from gemeinden group by bezirk order by anzahl desc;",
      "preferredChart": {
        "type": "bar",
        "x": "bezirk",
        "y": "anzahl",
        "title": "Anzahl Gemeinden nach Bezirk"
      }
    }
  ],
  "codeSnippets": [],
  "featureFlags": {
    "charts": true,
    "localHistory": true,
    "aiAssistant": false,
    "webR": false,
    "vega": false,
    "mosaic": false
  }
}
```

## 9. Backend Implementation Specification

Create backend code under the existing package root. If the actual package differs, use the existing convention.

Suggested package:

```text
ch.so.agi.datenportal.explore
```

### 9.1 Controller

Class: `ExplorePageController`

Responsibilities:

- Serve the JTE host page.
- Serve the JSON context endpoint.
- Never construct SQL directly except via services.

Methods:

```java
@GetMapping("/datasets/{datasetId}/explore")
public String explorePage(@PathVariable String datasetId, Model model)
```

Behavior:

- Validate `datasetId` using existing dataset lookup service.
- Call `ExploreContextService.buildContext(datasetId)`.
- Add `ExplorePageVm` to model.
- Return JTE template path, for example `datasets/explore`.
- If no dataset exists, return existing 404 behavior.
- If dataset exists but has no Parquet distribution, render page with a clear "not available" state.

```java
@GetMapping(
  value = "/datasets/{datasetId}/explore/context.json",
  produces = MediaType.APPLICATION_JSON_VALUE
)
public ResponseEntity<ExploreContextDto> exploreContext(@PathVariable String datasetId)
```

Behavior:

- Same context generation as page.
- Add cache headers consistent with metadata freshness.
- Return 404 if dataset is missing.

### 9.2 View Model

Record: `ExplorePageVm`

Fields:

```java
public record ExplorePageVm(
    String datasetId,
    String title,
    String breadcrumbLabel,
    String canonicalDatasetUrl,
    String contextJson,
    boolean available,
    String unavailableReason,
    ExploreAssetLinks assetLinks
) {}
```

Record: `ExploreAssetLinks`

```java
public record ExploreAssetLinks(
    String scriptSrc,
    String styleSrc
) {}
```

### 9.3 Context Service

Class: `ExploreContextService`

Constructor dependencies:

- existing dataset catalog/metadata service
- `ExploreTableService`
- `ExploreRecipeService`
- `ExploreCodeSnippetService`
- `ExploreProperties`
- Jackson `ObjectMapper` if needed

Methods:

```java
public ExploreContextDto buildContext(String datasetId)
```

Behavior:

- Load dataset metadata.
- Build all Parquet-backed tables for the data topic.
- Generate recipes.
- Generate code snippets.
- Attach feature flags and execution limits.
- Ensure table names are unique and safe.

```java
public String buildContextJson(String datasetId)
```

Behavior:

- Serialize context safely for embedding in HTML.
- Escape `</script>` and other problematic sequences if embedding inline.
- Prefer a `script type="application/json"` element with text content rather than direct JS assignment.

### 9.4 Table Service

Class: `ExploreTableService`

Methods:

```java
public List<ExploreTableDto> buildTables(Dataset dataset)
```

Behavior:

- Find Parquet distributions belonging to this data topic.
- Each Parquet distribution becomes one `ExploreTableDto`.
- If there are multiple Parquet files for one logical table, either:
  - treat each as a separate table for MVP, or
  - create one table with multiple URLs only if SQLRooms/DuckDB registration supports it cleanly.
- Use existing metadata for title/description if available.
- Use `ExploreSqlNameSanitizer` for table IDs and SQL names.

```java
private ExploreTableDto buildTable(Dataset dataset, Distribution distribution)
```

Behavior:

- Determine `id`, `name`, `title`, `parquetUrl`, `sizeBytes`, `columns`.

```java
private List<ExploreColumnDto> buildColumns(Distribution distribution)
```

Behavior:

- Prefer metadata/structure summary/known schema over runtime inference.
- Include comments/descriptions from the data catalog model when available.
- If no schema metadata exists, return an empty list and let the frontend obtain runtime schema with DuckDB.

### 9.5 Column Role Detector

Class: `ExploreColumnRoleDetector`

Methods:

```java
public Set<ExploreColumnRole> detectRoles(ExploreColumnSource column)
```

Behavior:

- Combine type, name, metadata, and optional value stats if available.
- Return at least `unknown` if no role matches.

```java
boolean isIdentifier(String name, String type)
```

Rules:

- true for names matching `id`, `.*_id`, `uuid`, `oid`, `t_id`, `basket`, `bfs_nr`, `egid`, `ewid`
- false for plain count/amount measures

```java
boolean isLabel(String name, String type)
```

Rules:

- true for text names like `name`, `titel`, `title`, `bezeichnung`, `gemeindename`

```java
boolean isMunicipality(String name)
```

Rules:

- true for `bfs_nr`, `bfsnr`, `gemeinde`, `gemeindename`, `gemeinde_name`, `municipality`

```java
boolean isDateLike(String name, String type)
```

Rules:

- true for SQL date/timestamp types
- true for names `datum`, `date`, `stand`, `stichtag`, `gueltig_ab`, `gueltig_bis`, `updated_at`

```java
boolean isYearLike(String name, String type)
```

Rules:

- true for `jahr`, `year`, `periode`, `berichtsjahr`
- integer type preferred

```java
boolean isMeasure(String name, String type)
```

Rules:

- numeric type and not identifier/year-like

```java
boolean isGeometry(String name, String type)
```

Rules:

- true for `geom`, `geometry`, `wkb_geometry`, `the_geom`, `wkt`

### 9.6 SQL Name Sanitizer

Class: `ExploreSqlNameSanitizer`

Methods:

```java
public String toSafeTableName(String rawName)
```

Behavior:

- Lowercase.
- Normalize umlauts if existing project has a convention, otherwise ASCII fold.
- Replace non `[a-z0-9_]` with `_`.
- Collapse repeated `_`.
- Trim leading/trailing `_`.
- Prefix with `t_` if the name starts with a digit.
- Avoid reserved keywords by suffixing `_table`.

```java
public String quoteIdentifier(String identifier)
```

Behavior:

- Return a double-quoted SQL identifier.
- Escape embedded quotes.
- Use for generated SQL when column names are not guaranteed simple.

```java
public void assertSafeTableName(String tableName)
```

Behavior:

- Throw `IllegalArgumentException` if not matching `[a-z_][a-z0-9_]*`.

### 9.7 Recipe Service

Class: `ExploreRecipeService`

Methods:

```java
public List<ExploreRecipeDto> generateRecipes(List<ExploreTableDto> tables)
```

Behavior:

- Generate recipes for each table.
- Always generate preview, count, describe.
- Generate null profile when columns are known.
- Generate category distribution for up to 3 category columns.
- Generate numeric summary for up to 3 measure columns.
- Generate time series for up to 2 date/year columns.
- Sort recipes by usefulness: preview, count, quality, category, numeric, time.

```java
ExploreRecipeDto previewRecipe(ExploreTableDto table)
```

SQL:

```sql
select *
from <table>
limit 100;
```

```java
ExploreRecipeDto countRecipe(ExploreTableDto table)
```

SQL:

```sql
select count(*) as anzahl
from <table>;
```

```java
ExploreRecipeDto describeRecipe(ExploreTableDto table)
```

SQL:

```sql
describe <table>;
```

```java
Optional<ExploreRecipeDto> nullProfileRecipe(ExploreTableDto table)
```

SQL pattern:

```sql
select
  count(*) as zeilen,
  count(*) filter (where <col1> is null) as <col1>_fehlt,
  count(*) filter (where <col2> is null) as <col2>_fehlt
from <table>;
```

Limit to a reasonable number of columns, for example 8, to keep the result readable.

```java
List<ExploreRecipeDto> categoryRecipes(ExploreTableDto table)
```

SQL pattern:

```sql
select <category_col>, count(*) as anzahl
from <table>
where <category_col> is not null
group by <category_col>
order by anzahl desc
limit 50;
```

Preferred chart:

```json
{ "type": "bar", "x": "<category_col>", "y": "anzahl" }
```

```java
List<ExploreRecipeDto> numericRecipes(ExploreTableDto table)
```

SQL pattern:

```sql
select
  min(<measure_col>) as minimum,
  avg(<measure_col>) as durchschnitt,
  max(<measure_col>) as maximum
from <table>
where <measure_col> is not null;
```

```java
List<ExploreRecipeDto> timeRecipes(ExploreTableDto table)
```

SQL pattern:

```sql
select <time_col>, count(*) as anzahl
from <table>
where <time_col> is not null
group by <time_col>
order by <time_col>;
```

Preferred chart:

```json
{ "type": "line", "x": "<time_col>", "y": "anzahl" }
```

### 9.8 Code Snippet Service

Class: `ExploreCodeSnippetService`

Methods:

```java
public List<ExploreCodeSnippetDto> generateSnippets(ExploreContextSource source, List<ExploreTableDto> tables)
```

Generate at least:

- DuckDB CLI SQL
- Python with DuckDB
- R with `duckdb` or `arrow` wording, depending on project preference

DuckDB example:

```sql
install httpfs;
load httpfs;

select *
from read_parquet('https://daten.so.ch/.../file.parquet')
limit 100;
```

Python example:

```python
import duckdb

url = "https://daten.so.ch/.../file.parquet"

con = duckdb.connect()
df = con.sql(f"""
    select *
    from read_parquet('{url}')
    limit 100
""").df()

print(df)
```

R example:

```r
library(duckdb)

url <- "https://daten.so.ch/.../file.parquet"

con <- dbConnect(duckdb::duckdb())
daten <- dbGetQuery(con, sprintf("
  select *
  from read_parquet('%s')
  limit 100
", url))

print(daten)
```

### 9.9 Properties

Class: `ExploreProperties`

Configuration prefix:

```properties
datenportal.explore.enabled=true
datenportal.explore.max-preview-rows=100
datenportal.explore.max-result-rows=10000
datenportal.explore.query-timeout-ms=30000
datenportal.explore.charts-enabled=true
datenportal.explore.local-history-enabled=true
datenportal.explore.ai-enabled=false
datenportal.explore.webr-enabled=false
datenportal.explore.vega-enabled=false
datenportal.explore.mosaic-enabled=false
```

## 10. Frontend Implementation Specification

Create a frontend island. Exact directory can follow project conventions, but this structure is recommended:

```text
src/main/frontend/explore/
  package.json
  vite.config.ts
  tsconfig.json
  src/
    main.tsx
    app/ExploreApp.tsx
    app/ExploreContext.ts
    app/ExploreContextLoader.ts
    app/ExploreErrorBoundary.tsx
    duckdb/createExploreRoomStore.ts
    duckdb/registerParquetTables.ts
    duckdb/querySafety.ts
    recipes/RecipeList.tsx
    recipes/recipeTypes.ts
    sql/SqlLaboratory.tsx
    sql/SqlToolbar.tsx
    sql/QueryHistory.ts
    results/ResultPanel.tsx
    results/ResultExport.ts
    charts/ChartPanel.tsx
    charts/chartInference.ts
    charts/chartTypes.ts
    charts/BarResultChart.tsx
    charts/LineResultChart.tsx
    charts/ScatterResultChart.tsx
    charts/HistogramResultChart.tsx
    schema/TableCatalog.tsx
    schema/AttributePanel.tsx
    code/CodeSnippetsPanel.tsx
    styles/explore.css
```

### 10.1 Frontend Dependencies

Install current compatible versions after checking SQLRooms docs:

```json
{
  "dependencies": {
    "@sqlrooms/duckdb": "...",
    "@sqlrooms/sql-editor": "...",
    "@sqlrooms/data-table": "...",
    "@sqlrooms/recharts": "...",
    "@sqlrooms/schema-tree": "...",
    "@sqlrooms/ui": "...",
    "@sqlrooms/room-shell": "...",
    "@sqlrooms/room-store": "...",
    "react": "...",
    "react-dom": "...",
    "zod": "..."
  },
  "devDependencies": {
    "@testing-library/react": "...",
    "@testing-library/user-event": "...",
    "typescript": "...",
    "vite": "...",
    "vitest": "...",
    "playwright": "..."
  }
}
```

If the project already has frontend tooling, integrate with it instead of creating a second style.

### 10.2 Entry Point

File: `src/main/frontend/explore/src/main.tsx`

Responsibilities:

- Find root element `#datenportal-explore-root`.
- Load context from embedded JSON element `#datenportal-explore-context`.
- Render `ExploreApp`.
- Fail gracefully if context cannot be parsed.

Method/function:

```ts
function bootstrapExploreApp(): void
```

Behavior:

- Parse JSON from `<script type="application/json" id="datenportal-explore-context">`.
- Validate minimally with Zod or TypeScript guards.
- Render React root.

### 10.3 ExploreApp

File: `ExploreApp.tsx`

Component:

```tsx
export function ExploreApp({context}: {context: ExploreContextDto}) { ... }
```

Responsibilities:

- Provide context to child components.
- Initialize DuckDB room/store.
- Render top-level tabs: `Vorschau`, `SQL-Labor`, `Diagramm`, `Code`.
- Show global state: initializing, ready, error.
- Keep selected table, selected recipe, active SQL, last result, chart config.

State model:

```ts
interface ExploreUiState {
  activeTab: 'preview' | 'sql' | 'chart' | 'code';
  selectedTableId: string;
  selectedRecipeId?: string;
  sql: string;
  running: boolean;
  lastQuery?: ExecutedQuery;
  chartConfig?: ChartConfig;
}
```

### 10.4 DuckDB Store and Registration

File: `createExploreRoomStore.ts`

Function:

```ts
export function createExploreRoomStore(context: ExploreContextDto): ExploreRoomStore
```

Responsibilities:

- Compose SQLRooms DuckDB slice and any needed SQL editor slice.
- Configure DuckDB-Wasm connector.
- Keep state local to the island.

File: `registerParquetTables.ts`

Functions:

```ts
export async function registerParquetTables(
  db: DuckDbConnector,
  tables: ExploreTableDto[]
): Promise<RegisteredTable[]>
```

Behavior:

- For each table, create a DuckDB view:

```sql
create or replace view <safe_table_name> as
select *
from read_parquet('<url>');
```

- Use safe table names from backend.
- Do not allow raw user-provided table names.
- Collect per-table success/error state.

```ts
export function buildCreateViewSql(table: ExploreTableDto): string
```

Behavior:

- Use strict escaping for SQL string literal URL.
- Use already-safe table name.

### 10.5 Query Safety

File: `querySafety.ts`

Functions:

```ts
export function isReadOnlyQuery(sql: string): boolean
```

MVP behavior:

- Allow `select`, `with`, `describe`, `show`, `pragma table_info` if needed.
- Disallow obvious mutation and system commands:
  - `insert`
  - `update`
  - `delete`
  - `drop`
  - `alter`
  - `create table`
  - `copy ... to`
  - `attach`
  - `install`
  - `load`
  - `call`
  - `set`
- This is a client-side guard only. Do not overclaim security. It is mostly UX protection.

```ts
export function applyResultLimit(sql: string, maxRows: number): string
```

Behavior:

- If query already has a top-level `limit`, preserve it.
- Else wrap:

```sql
select *
from (
  <user_sql_without_trailing_semicolon>
) as q
limit <maxRows>
```

Use SQLRooms/DuckDB helpers if available, for example `makeLimitQuery` and statement splitting utilities.

```ts
export function normalizeSqlForExecution(sql: string, maxRows: number): string
```

Behavior:

- Trim.
- Ensure single statement where possible.
- Apply result limit for `select`/`with`.
- Return normalized SQL.

### 10.6 SQL Laboratory

File: `SqlLaboratory.tsx`

Component:

```tsx
export function SqlLaboratory(props: {
  context: ExploreContextDto;
  selectedRecipe?: ExploreRecipeDto;
  onResultChange: (result: QueryResultState) => void;
}) { ... }
```

Responsibilities:

- Render recipe list, SQL editor, toolbar, result panel, chart panel.
- Support `Ctrl/Cmd + Enter` to execute.
- Support copy SQL.
- Support cancel if SQLRooms query handle supports it.

Required user interactions:

- Clicking recipe loads SQL into editor.
- Double-clicking recipe or pressing "Ausführen" runs it.
- Editing SQL marks current recipe as modified.
- Result table and chart update after successful execution.

### 10.7 Recipe List

File: `RecipeList.tsx`

Component:

```tsx
export function RecipeList(props: {
  recipes: ExploreRecipeDto[];
  selectedRecipeId?: string;
  onSelect: (recipe: ExploreRecipeDto) => void;
  onRun: (recipe: ExploreRecipeDto) => void;
}) { ... }
```

Behavior:

- Group recipes by table and category.
- Show icons only if existing icon set supports them.
- Highlight selected recipe.
- Show short description on hover/focus or below title.

### 10.8 Results

File: `ResultPanel.tsx`

Component:

```tsx
export function ResultPanel(props: {
  result: QueryResultState;
  maxRows: number;
}) { ... }
```

Responsibilities:

- Render `QueryDataTable` or `DataTableArrowPaginated`.
- Show query duration.
- Show row count if available.
- Show "max rows applied" note.
- Show export button.

File: `ResultExport.ts`

Functions:

```ts
export function exportRowsToCsv(rows: Record<string, unknown>[], filename: string): void
```

Behavior:

- Export current result rows only.
- Use semicolon or comma according to project convention; document choice in UI.
- Include UTF-8 BOM only if existing portal convention requires it.

### 10.9 Chart Inference

File: `chartInference.ts`

Types:

```ts
export interface ResultColumn {
  name: string;
  typeCategory: 'string' | 'number' | 'date' | 'boolean' | 'unknown';
}

export interface ChartSuggestion {
  type: 'bar' | 'line' | 'scatter' | 'histogram';
  x?: string;
  y?: string;
  reason: string;
  confidence: number;
}
```

Functions:

```ts
export function inferChartSuggestion(
  columns: ResultColumn[],
  rows: Record<string, unknown>[],
  preferred?: ExploreChartConfigDto
): ChartSuggestion | null
```

Rules:

1. If `preferred` exists and columns are present, use it.
2. If one string/category column and one numeric column: bar chart.
3. If one date/year-like column and one numeric column: line chart.
4. If two numeric columns: scatter chart.
5. If one numeric column only: histogram.
6. If more than 500 rows for bar/line, warn and suggest aggregating first.
7. Return `null` if no useful chart is possible.

```ts
export function isYearLikeColumn(name: string, rows: Record<string, unknown>[]): boolean
```

Rules:

- Name `jahr`, `year`, `periode`.
- Integer values mostly between 1800 and 2200.

```ts
export function isDateLikeColumn(name: string, rows: Record<string, unknown>[]): boolean
```

Rules:

- Type is date/timestamp if known.
- Or sample values parse as ISO dates.

```ts
export function buildHistogramBins(
  rows: Record<string, unknown>[],
  column: string,
  binCount: number
): Array<{bin: string; count: number}>
```

### 10.10 Chart Components

Use `@sqlrooms/recharts` for V1.

File: `ChartPanel.tsx`

Component:

```tsx
export function ChartPanel(props: {
  rows: Record<string, unknown>[];
  columns: ResultColumn[];
  preferred?: ExploreChartConfigDto;
}) { ... }
```

Responsibilities:

- Infer initial chart.
- Render chart controls:
  - chart type
  - x column
  - y column
  - sort direction for bar
  - row limit for chart
- Render empty state if no chart possible.
- Render warning if too many points.

Component files:

```tsx
export function BarResultChart(props: {
  rows: Record<string, unknown>[];
  x: string;
  y: string;
  title?: string;
}) { ... }
```

```tsx
export function LineResultChart(props: {
  rows: Record<string, unknown>[];
  x: string;
  y: string;
  title?: string;
}) { ... }
```

```tsx
export function ScatterResultChart(props: {
  rows: Record<string, unknown>[];
  x: string;
  y: string;
  title?: string;
}) { ... }
```

```tsx
export function HistogramResultChart(props: {
  rows: Record<string, unknown>[];
  column: string;
  title?: string;
}) { ... }
```

Styling:

- Use one primary red series color.
- Use neutral grid lines.
- Disable excessive animation by default.
- Ensure charts are readable in 320px width.

### 10.11 Table Catalog and Attribute Panel

File: `TableCatalog.tsx`

Component:

```tsx
export function TableCatalog(props: {
  tables: ExploreTableDto[];
  selectedTableId: string;
  onSelectTable: (tableId: string) => void;
}) { ... }
```

File: `AttributePanel.tsx`

Component:

```tsx
export function AttributePanel(props: {
  table: ExploreTableDto;
}) { ... }
```

Behavior:

- Show column name, type, roles, description, example.
- Provide "copy column name".
- Keep layout compact.

### 10.12 Code Snippets

File: `CodeSnippetsPanel.tsx`

Component:

```tsx
export function CodeSnippetsPanel(props: {
  snippets: ExploreCodeSnippetDto[];
}) { ... }
```

Behavior:

- Tabs or segmented control for DuckDB, Python, R.
- Copy button per snippet.
- Show static code only in MVP.
- No WebR execution yet.

### 10.13 Local Query History

File: `QueryHistory.ts`

Functions:

```ts
export function loadQueryHistory(datasetId: string): QueryHistoryItem[]
export function saveQueryHistory(datasetId: string, item: QueryHistoryItem): void
export function clearQueryHistory(datasetId: string): void
```

Storage:

- `localStorage` is sufficient for MVP.
- Key prefix: `datenportal.explore.history.<datasetId>`.
- Keep last 20 queries.
- Do not store result rows.

## 11. JTE Template Specification

Suggested template:

```text
src/main/jte/datasets/explore.jte
```

Required elements:

```html
<div id="datenportal-explore-root"></div>

<script
  id="datenportal-explore-context"
  type="application/json">
  ${unsafeOrEscapedContextJsonAccordingToProjectConvention}
</script>

<script type="module" src="${vm.assetLinks().scriptSrc()}"></script>
```

If CSS is emitted separately:

```html
<link rel="stylesheet" href="${vm.assetLinks().styleSrc()}">
```

Server-rendered fallback:

- If JS disabled: show message and links to downloads/code snippets if possible.
- If no Parquet available: show "Erkunden ist für dieses Datenthema noch nicht verfügbar, weil keine Parquet-Datei publiziert ist."

## 12. Testing Strategy

Every phase must add or update tests. Do not leave tests for the end.

### 12.1 Backend Unit Tests

Test class: `ExploreSqlNameSanitizerTest`

Cases:

- `Gemeindegrenzen` -> `gemeindegrenzen`
- `2024 Statistik` -> `t_2024_statistik`
- `select` -> `select_table`
- repeated separators collapse
- invalid names rejected by `assertSafeTableName`

Test class: `ExploreColumnRoleDetectorTest`

Cases:

- `bfs_nr INTEGER` -> identifier, municipality
- `gemeindename VARCHAR` -> label, municipality
- `flaeche_ha DOUBLE` -> measure
- `jahr INTEGER` -> year, not measure
- `geom GEOMETRY` -> geometry

Test class: `ExploreRecipeServiceTest`

Cases:

- preview/count/describe always generated
- category recipe generated for category column
- numeric recipe generated for measure column
- time recipe generated for year/date column
- generated SQL uses safe identifiers

Test class: `ExploreContextServiceTest`

Cases:

- context contains tables, recipes, snippets, flags
- dataset without Parquet marks unavailable or returns empty tables according to final design
- JSON serialization does not break embedded script

### 12.2 Backend Web Tests

Test class: `ExplorePageControllerTest`

Cases:

- `GET /datasets/{id}/explore` returns 200 for existing dataset
- contains root element and JSON script tag
- missing dataset returns 404
- `GET /datasets/{id}/explore/context.json` returns expected JSON shape
- dataset without Parquet returns readable unavailable state

### 12.3 Frontend Unit Tests

Use Vitest.

Test file: `querySafety.test.ts`

Cases:

- allows `select * from gemeinden`
- allows `with q as (...) select * from q`
- blocks `drop table`
- blocks `insert`
- blocks `copy ... to`
- applies limit when missing
- preserves existing top-level limit

Test file: `chartInference.test.ts`

Cases:

- string + number -> bar
- year + number -> line
- date + number -> line
- number + number -> scatter
- one number -> histogram
- no useful columns -> null
- preferred chart wins if valid

Test file: `QueryHistory.test.ts`

Cases:

- stores max 20
- newest first
- dataset-specific keys
- clear removes history

### 12.4 Frontend Component Tests

Use React Testing Library.

Cases:

- `RecipeList` selects recipe.
- `AttributePanel` shows roles/descriptions/examples.
- `ChartPanel` renders empty state when no suggestion.
- `ChartPanel` renders bar controls for category result.
- `CodeSnippetsPanel` copies snippets if clipboard mock available.

### 12.5 Browser Integration Tests

Use Playwright if the project already has it or introduce it in the frontend island.

Minimum tests:

- Explore page loads.
- DuckDB initialization state appears and transitions.
- Recipe click puts SQL into editor.
- Running a stubbed or small local Parquet query shows result table.
- Chart appears for grouped result.
- Mobile viewport does not overlap or clip key controls.

If real Parquet over HTTP is brittle in CI:

- Use a tiny static Parquet fixture served by the test server.
- Keep one manual smoke test against real object storage documented.

## 13. Documentation Requirements

The coding agent must keep documentation current during every phase.

Create or update:

```text
docs/erkunden/README.md
docs/erkunden/progress.md
docs/erkunden/architecture.md
docs/erkunden/testing.md
docs/erkunden/troubleshooting.md
```

Minimum contents:

- `README.md`: what Erkunden is, user-facing behavior, known limits
- `progress.md`: phase status, date, commit/branch if available, test evidence
- `architecture.md`: backend/frontend boundaries, data contract, asset build
- `testing.md`: how to run backend/frontend/e2e tests
- `troubleshooting.md`: CORS, Range Requests, Safari/Wasm, large files, query errors

After every phase:

- Update phase tracking in this spec if the project workflow allows it.
- Always update `docs/erkunden/progress.md`.
- Include test command and result.
- Document known limitations honestly.

## 14. Phase Tracking

The implementation agent must update this table as work progresses.

Status values:

- `TODO`
- `IN_PROGRESS`
- `DONE`
- `BLOCKED`

| Phase | Status | Artifact | Test Evidence |
|---|---|---|---|
| 0. Repository orientation and documentation scaffold | DONE | `docs/erkunden/` scaffold, implementation notes | `./gradlew test`, `./gradlew check`, `./gradlew clean check` passed |
| 1. Backend context and route | DONE | `/datasets/{id}/explore`, `context.json`, backend DTOs/services, JTE host page | `./gradlew test` passed; `./gradlew clean check` passed |
| 2. Frontend island bootstrap | TODO | React/Vite app embedded in JTE | frontend unit test, page smoke test |
| 3. DuckDB-Wasm Parquet registration | TODO | tables registered as views | integration test with tiny Parquet |
| 4. SQL laboratory and generated recipes | TODO | recipes, editor, result table | unit/component/e2e tests |
| 5. Charting V1 with Recharts | TODO | automatic chart suggestion and chart panel | chart inference/component/e2e tests |
| 6. Code snippets and local query history | TODO | DuckDB/Python/R snippets, history | unit/component tests |
| 7. UX hardening and browser checks | TODO | loading/error/mobile states | Playwright + manual smoke notes |
| 8. Future hooks for AI/WebR/Vega/Mosaic | TODO | feature flags, docs, disabled UI slots | tests for disabled flags |

## 15. Phase 0: Repository Orientation and Documentation Scaffold

### Goal

Understand the existing project conventions before changing code. Create the documentation scaffold.

### Tasks

1. Read repository instructions:
   - `AGENTS.md`
   - `README.md`
   - existing docs
   - build files
   - existing controller/template/test patterns
2. Identify package root and existing dataset detail route.
3. Identify frontend asset build strategy.
4. Create `docs/erkunden/` files listed above.
5. Add a short initial entry to `docs/erkunden/progress.md`.
6. Record baseline test commands and whether they pass.

### Deliverable

- Documentation scaffold.
- No product behavior yet.

### Tests

- Run existing backend tests.
- Run existing frontend tests if present.
- Record results in `docs/erkunden/progress.md`.

### Agent Prompt for Phase 0

You are implementing Phase 0 of `datenportal-erkunden-sqlrooms-mvp-agent-spec.md`. Read that specification first and keep it open as the source of truth. Also read all repository-local agent instructions, skills, AGENTS files, README files, build files, and existing architectural docs before editing. Use relevant available skills and project conventions. Do not implement product code yet. Create the `docs/erkunden/` scaffold, document repository findings, record baseline test commands and results, and update the phase tracking/progress documentation.

## 16. Phase 1: Backend Context and Route

### Goal

Expose a server-rendered Explore page and a JSON context endpoint.

### Tasks

1. Add `ExploreProperties`.
2. Add DTO/record classes:
   - `ExploreContextDto`
   - `ExploreExecutionDto`
   - `ExploreTableDto`
   - `ExploreColumnDto`
   - `ExploreRecipeDto`
   - `ExploreChartConfigDto`
   - `ExploreCodeSnippetDto`
   - `ExploreFeatureFlagsDto`
   - `ExplorePageVm`
   - `ExploreAssetLinks`
3. Add services:
   - `ExploreContextService`
   - `ExploreTableService`
   - `ExploreColumnRoleDetector`
   - `ExploreSqlNameSanitizer`
   - `ExploreRecipeService`
   - `ExploreCodeSnippetService`
4. Add `ExplorePageController`.
5. Add JTE host template with fallback text.
6. Add JSON endpoint.
7. Add tests listed in section 12.

### Deliverable

- `/datasets/{datasetId}/explore` renders an integrated portal page with root element and embedded context.
- `/datasets/{datasetId}/explore/context.json` returns valid context JSON.
- If no frontend island exists yet, page shows a fallback placeholder.

### Tests

- Backend unit tests for sanitizer, roles, recipes.
- Controller tests for page and JSON endpoint.

### Agent Prompt for Phase 1

You are implementing Phase 1 of `datenportal-erkunden-sqlrooms-mvp-agent-spec.md`. Read the full spec first, especially sections 8, 9, 11, 12, 13, and 14. Respect all repository instructions and available skills. Implement only the backend context, route, DTOs, services, JTE host template, and backend tests. Keep frontend behavior as a placeholder if Phase 2 is not done. Update `docs/erkunden/progress.md`, update phase tracking, and record exact test commands and results.

## 17. Phase 2: Frontend Island Bootstrap

### Goal

Embed a React/Vite/SQLRooms-capable island in the server-rendered page.

### Tasks

1. Create frontend island directory.
2. Add Vite, TypeScript, React build config.
3. Add asset build/copy integration with Gradle or existing frontend build.
4. Add `main.tsx`, `ExploreApp`, `ExploreContextLoader`, `ExploreErrorBoundary`.
5. Parse embedded JSON context.
6. Render static UI skeleton with tabs and dataset title.
7. Add frontend tests for context parsing and initial rendering.

### Deliverable

- Explore page loads the JS island.
- It shows dataset title, table count, tab bar, and "DuckDB wird vorbereitet" placeholder.

### Tests

- `npm test` or equivalent.
- Backend page test still passes.
- Optional Playwright smoke test for rendered island.

### Agent Prompt for Phase 2

You are implementing Phase 2 of `datenportal-erkunden-sqlrooms-mvp-agent-spec.md`. Read the full spec first, especially sections 7, 8, 10.1, 10.2, 10.3, 11, 12, 13, and 14. Respect project build conventions and available skills. Add the React/Vite island and integrate it into the existing Spring Boot/JTE asset pipeline. Do not implement DuckDB registration yet except for placeholders. Add tests, update docs, update progress, and record exact commands and results.

## 18. Phase 3: DuckDB-Wasm Parquet Registration

### Goal

Initialize DuckDB-Wasm in the browser and register Parquet files as queryable views.

### Tasks

1. Add SQLRooms DuckDB dependencies.
2. Implement `createExploreRoomStore`.
3. Implement `registerParquetTables`.
4. Implement registration status UI.
5. Show table catalog from backend context.
6. Run a default preview query for the primary table.
7. Add tiny Parquet fixture for tests if needed.

### Deliverable

- Opening the page registers all available Parquet tables.
- `select * from <primary_table> limit 100` can run.
- User sees clear per-table loading/success/error state.

### Tests

- Unit test for `buildCreateViewSql`.
- Integration/browser test against a tiny Parquet fixture.
- Manual smoke test against one real public Parquet file if CI cannot cover it.

### Agent Prompt for Phase 3

You are implementing Phase 3 of `datenportal-erkunden-sqlrooms-mvp-agent-spec.md`. Read the full spec first, especially sections 6, 8, 10.4, 10.5, 12, 13, and 14. Use SQLRooms/DuckDB APIs according to their current docs. Implement browser-local DuckDB-Wasm initialization and Parquet view registration. Do not add charting yet. Add automated tests and document CORS/Range Request findings in `docs/erkunden/troubleshooting.md` and `docs/erkunden/progress.md`.

## 19. Phase 4: SQL Laboratory and Generated Recipes

### Goal

Make the page useful as a per-topic SQL workspace.

### Tasks

1. Implement `RecipeList`.
2. Implement `SqlLaboratory`.
3. Implement `SqlToolbar`.
4. Implement `querySafety`.
5. Implement `ResultPanel`.
6. Implement result CSV export.
7. Wire recipe selection to editor.
8. Wire execution to result panel.
9. Add `Ctrl/Cmd + Enter`.
10. Add local visible query status and duration.

### Deliverable

- User can select generated recipes, edit SQL, run it, see results, copy SQL, and export current result rows.

### Tests

- Unit tests for query safety.
- Component tests for recipe/editor/result interactions.
- Browser test: select recipe -> run -> result table visible.

### Agent Prompt for Phase 4

You are implementing Phase 4 of `datenportal-erkunden-sqlrooms-mvp-agent-spec.md`. Read the full spec first, especially sections 7, 8, 9.7, 10.5, 10.6, 10.7, 10.8, 12, 13, and 14. Respect existing UI conventions and available skills. Build the SQL laboratory as a real power-user surface, not a toy. Ensure generated recipes work, queries are guarded and limited, results render, and CSV export works. Add tests, update documentation, and record test evidence.

## 20. Phase 5: Charting V1 with Recharts

### Goal

Add automatic charting from SQL results using `@sqlrooms/recharts`.

### Tasks

1. Add Recharts/SQLRooms chart dependencies.
2. Implement `chartTypes.ts`.
3. Implement `chartInference.ts`.
4. Implement chart components:
   - `BarResultChart`
   - `LineResultChart`
   - `ScatterResultChart`
   - `HistogramResultChart`
5. Implement `ChartPanel`.
6. Wire chart panel to current SQL result.
7. Use recipe `preferredChart` when present.
8. Add chart controls:
   - type
   - x
   - y
   - chart row limit
9. Add empty/warning states.

### Deliverable

- Running a grouped query immediately displays a useful chart.
- User can switch chart type and axis fields when compatible.

### Tests

- Unit tests for chart inference.
- Component tests for chart panel.
- Browser test: run category recipe -> bar chart visible.
- Mobile screenshot or Playwright viewport check.

### Agent Prompt for Phase 5

You are implementing Phase 5 of `datenportal-erkunden-sqlrooms-mvp-agent-spec.md`. Read the full spec first, especially sections 7, 10.9, 10.10, 12, 13, and 14. Use `@sqlrooms/recharts` for V1 charting unless current package compatibility makes that impossible; if impossible, document why and choose the smallest equivalent React charting fallback. Charts must be generated from SQL results and must not become a dashboard builder. Add inference tests, component tests, browser smoke tests, update docs, and record exact test results.

## 21. Phase 6: Code Snippets and Local Query History

### Goal

Make the Explore page a bridge to real local data use.

### Tasks

1. Implement `CodeSnippetsPanel`.
2. Generate and render DuckDB CLI, Python, and R snippets.
3. Implement copy buttons.
4. Implement `QueryHistory`.
5. Show last local queries for this dataset.
6. Add clear-history action.

### Deliverable

- Users can copy reproducible code and see their last local queries.

### Tests

- Unit tests for query history.
- Component tests for snippets and copy actions.
- Backend tests for snippet generation.

### Agent Prompt for Phase 6

You are implementing Phase 6 of `datenportal-erkunden-sqlrooms-mvp-agent-spec.md`. Read the full spec first, especially sections 9.8, 10.12, 10.13, 12, 13, and 14. Implement static code snippets and local query history. Do not implement WebR execution yet. Add tests, document behavior, update progress, and record test results.

## 22. Phase 7: UX Hardening and Browser Checks

### Goal

Make the MVP robust enough to show to real users.

### Tasks

1. Polish desktop layout.
2. Polish mobile layout.
3. Add all required UI states.
4. Add accessible labels and keyboard behavior.
5. Verify no layout overlap/clipping at common widths.
6. Test Chrome, Firefox, Safari manually if available.
7. Document known browser limitations.
8. Verify CORS/Range Request behavior against real hosted Parquet.

### Deliverable

- MVP feels intentional and resilient.

### Tests

- Playwright desktop and mobile.
- Manual browser smoke notes in `docs/erkunden/progress.md`.

### Agent Prompt for Phase 7

You are implementing Phase 7 of `datenportal-erkunden-sqlrooms-mvp-agent-spec.md`. Read the full spec first, especially sections 3, 7, 12, 13, and 14. Focus on UX, error states, accessibility, mobile layout, and browser robustness. Do not add major new features. Add or update Playwright tests, document manual browser checks, update progress, and record exact results.

## 23. Phase 8: Future Hooks for AI/WebR/Vega/Mosaic

### Goal

Prepare clean extension points without implementing future features.

### Tasks

1. Ensure feature flags exist and default to false:
   - AI
   - WebR
   - Vega
   - Mosaic
2. Add disabled UI placeholders only where useful and not distracting.
3. Document future architecture.
4. Ensure disabled flags do not load heavy packages.
5. Add tests that disabled features remain hidden.

### Deliverable

- Future features have a documented path.
- MVP bundle is not bloated by disabled future dependencies.

### Tests

- Unit/component tests for feature flags.
- Bundle check if project has tooling.

### Agent Prompt for Phase 8

You are implementing Phase 8 of `datenportal-erkunden-sqlrooms-mvp-agent-spec.md`. Read the full spec first, especially sections 24, 25, 26, 27, 28, and 29. Prepare feature flags and documentation for future AI, WebR/r-stats, Vega, Mosaic, and geospatial exploration without implementing them or loading heavy disabled dependencies. Add tests for disabled flags, update docs, update progress, and record exact test results.

## 24. Future: AI Assistant

Later, add an AI assistant only after the SQL laboratory is stable.

Recommended direction:

- Use SQLRooms AI packages only behind a feature flag.
- Start internally, not public.
- Prefer "Bring your own key" or a server-side proxy with quotas.
- AI receives:
  - dataset title
  - description
  - table names
  - columns with types/descriptions/roles
  - generated recipes
  - at most 20 sample rows
- AI may propose SQL.
- User must click before SQL executes.
- Apply same query safety and limits as manual SQL.

Do not allow:

- mutation queries
- remote writes
- uncontrolled tool access
- hidden background execution
- uploading full data to a model provider

Future classes/components:

```text
ai/AiExplorePanel.tsx
ai/buildAiContext.ts
ai/aiQueryGuard.ts
ai/AiSqlSuggestion.tsx
```

## 25. Future: WebR / r-stats

WebR is attractive but should not be part of the first MVP.

Recommended V2/V3 path:

1. Keep static R snippets in MVP.
2. Add an optional "R ausführen" panel later.
3. Start with small in-memory result data from current SQL result, not full Parquet.
4. Add package availability checks.
5. Use precompiled WebR packages only.
6. Document memory/browser limitations.

Useful first WebR use cases:

- summary statistics
- histogram/boxplot from current result
- simple `lm()` examples
- reproducible mini-analysis
- generate R code from current SQL

Avoid initially:

- running R directly on huge remote Parquet files
- Shiny-in-browser
- custom package builds
- geospatial R stacks

Future components:

```text
webr/WebRPanel.tsx
webr/WebRRuntime.ts
webr/RResultBridge.ts
webr/RPlotOutput.tsx
```

## 26. Future: Vega-Lite

Use SQLRooms `@sqlrooms/vega` after Recharts V1 if users want more expressive charts.

Potential features:

- show Vega-Lite spec for current chart
- edit Vega-Lite JSON
- copy chart spec
- AI generates Vega-Lite spec
- export SVG/PNG

Keep Recharts as the default simple mode.

## 27. Future: Mosaic Crossfilter

Use SQLRooms `@sqlrooms/mosaic` later for interactive dashboards:

- linked histograms
- brush selection
- cross-filtering
- large local DuckDB-backed exploration

This is powerful but changes product expectations. Treat it as an advanced mode:

```text
Erkunden
  - SQL-Labor
  - Diagramm
  - Crossfilter-Labor (advanced)
```

## 28. Future: Geospatial Exploration

Do not make maps the first charting requirement. Add after the table/SQL/chart path works.

Possible path:

1. Detect geometry columns.
2. Show geometry metadata/profile:
   - geometry type
   - count
   - bounding box
   - CRS if known
3. Add simple map preview for small results.
4. Add deck.gl/Kepler only for advanced datasets.

Future components:

```text
geo/GeometryProfilePanel.tsx
geo/SimpleMapPreview.tsx
geo/GeoQueryRecipes.ts
```

## 29. Future: Shareable SQL URLs

After local history:

- encode current SQL in URL hash or compressed query parameter
- do not include result rows
- optionally include selected chart config
- warn users that URLs can become long

Suggested format:

```text
/datasets/ch.so.gemeindegrenzen/explore#sql=<encoded>&chart=<encoded>
```

## 30. Definition of Done for MVP

The MVP is done when:

- At least three real data topics can be explored.
- Each data topic has at least one Parquet table.
- DuckDB-Wasm registration works for real public URLs.
- Generated recipes are available for each table.
- SQL editor can run queries and show result tables.
- Grouped queries produce charts automatically.
- Users can copy SQL and export current result rows.
- Code snippets for DuckDB, Python, and R are available.
- Mobile layout is usable.
- No major browser console errors occur during normal use.
- Backend, frontend, and e2e tests pass.
- `docs/erkunden/` is complete and current.
- Phase tracking says `DONE` for phases 0-8 or documents explicitly deferred items.

## 31. Implementation Guardrails for LLM Agents

1. Before editing code, read this spec completely.
2. Read repository-local instructions and respect them.
3. Use existing patterns over invented abstractions.
4. Keep changes phase-scoped.
5. Do not add AI/WebR/Vega/Mosaic heavy dependencies before Phase 8, and even then only as disabled docs/flags unless explicitly requested.
6. Do not silently remove existing behavior.
7. Do not introduce server-side SQL execution.
8. Do not claim client-side query guards are real security controls.
9. Always run relevant tests.
10. Always update `docs/erkunden/progress.md`.
11. If a phase is blocked, mark it `BLOCKED`, document why, and propose the smallest next test.
12. If package APIs differ from this spec, prefer official current docs and document the deviation.
