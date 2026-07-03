# Erkunden Progress

Status: Phase tracking for `datenportal-erkunden-sqlrooms-mvp-agent-spec.md`

## Phase Status

| Phase | Status | Notes |
|---|---|---|
| 0. Repository orientation and documentation scaffold | DONE | Documentation scaffold created; baseline tests recorded. |
| 1. Backend context and route | DONE | Backend context, JSON endpoint, JTE host page and backend tests implemented. |
| 2. Frontend island bootstrap | DONE | React/Vite island embedded in JTE and built through Gradle/npm. |
| 3. DuckDB-Wasm Parquet registration | DONE | DuckDB-Wasm starts locally, Parquet views register, same-origin preview fixture passes. |
| 4. SQL laboratory and generated recipes | DONE | SQL-Labor, generated recipe execution, guarded/limited queries, result table and CSV export implemented. |
| 5. Charting V1 with Recharts | DONE | Automatic chart inference and Recharts panel from SQL results implemented. |
| 6. Code snippets and local query history | DONE | Static DuckDB/Python/R snippets and per-dataset local history implemented. |
| 7. UX hardening and browser checks | DONE | Loading/error states, accessibility, mobile robustness and browser checks documented. |
| 8. Future hooks for AI/WebR/Vega/Mosaic | DONE | Disabled AI/WebR/Vega/Mosaic/geospatial flags, hidden extension slot, docs and dependency guard implemented. |
| SQL-Labor redesign | DONE | Full-width compact SQL workbench with schema cards, Monaco editor, red run button and compact result table. |
| SQL-Labor UI-Nachschliff | DONE | Editable Monaco editor, resizable panels, `Geladen` status, compact toolbar and removed legacy headers/link. |
| SQL-Labor UI-Nachschliff 2 | DONE | Stable Monaco layout, row-limit selector, CSV/XLSX/Parquet result export and cleaned splitter/schema/result visuals. |
| SQL-Labor Feinschliff 3 | DONE | Unified borders, DuckDB-native XLSX/Parquet exports, local Excel extension mirror and SQLRooms schema autocomplete wiring. |
| SQL-Labor Autocomplete-Fix | DONE | Removed the local duplicate completion provider, bundled Monaco Suggest locally and wired SQLRooms tableSchemas/getLatestSchemas. |

## SQL-Labor Feinschliff 3 Entry

Date: 2026-07-02

Scope:

- Normalized workbench structural borders to the SQLRooms-like `#e2e8f0` color and made resize handles render one continuous thin line.
- Reworked the export splitbutton so the wrapper owns the outer border and only one internal divider separates the CSV button from the chevron.
- Replaced JS XLSX/Parquet generation with DuckDB-Wasm `COPY` exports against the successful `executedSql`; CSV remains the client serializer for Semicolon and CRLF behavior.
- Mirrored the signed official `excel.duckdb_extension.wasm` next to the existing Parquet extension under `/explore-extensions/v1.4.3/wasm_mvp/`.
- Removed direct `parquet-wasm` and `write-excel-file` dependencies from the Explore package.
- Wired Monaco autocomplete through SQLRooms `connector`, `tableSchemas` and memoized `getLatestSchemas`; the local duplicate table/column fallback provider has since been removed so the built-in SQLRooms provider owns suggestions.
- Autocomplete-Fix follow-up: the editor now uses SQLRooms `tableSchemas`/`getLatestSchemas` without a custom provider and bundles Monaco's Suggest contribution locally. The DuckDB connector is not passed into `SqlMonacoEditor` while `@sqlrooms/sql-editor@0.28.0` dynamic function metadata needs CSP-blocked `unsafe-eval`.
- Extended frontend and Playwright coverage for DuckDB-native exports, splitbutton borders, unified border colors and schema autocomplete wiring.

Test evidence:

| Command | Result |
|---|---|
| `npm --prefix src/main/frontend/explore test` | PASS, `Test Files 15 passed (15)`, `Tests 70 passed (70)` |
| `npm --prefix src/main/frontend/explore run typecheck` | PASS, `tsc --noEmit` without errors |
| `npm --prefix src/main/frontend/explore run build` | PASS, Vite built Explore assets; expected large DuckDB-Wasm chunk warning remains |
| `./gradlew test --tests 'ch.so.agi.datenportal.explore.*'` | PASS, `BUILD SUCCESSFUL in 21s` |
| `./gradlew playwrightTest --tests 'ch.so.agi.datenportal.explore.*'` | PASS, `BUILD SUCCESSFUL in 24s`; includes same-origin Parquet, local Monaco, CSV/XLSX/Parquet export, border-color checks, resizers and mobile overflow |
| `./gradlew clean check` | PASS, `BUILD SUCCESSFUL in 51s`; included Vitest, typecheck, Vite build, backend tests and Playwright |

## SQL-Labor UI-Nachschliff 2 Entry

Date: 2026-07-02

Scope:

- Fixed the Monaco layout path with versioned resizable-panel storage, `ResizeObserver` and explicit editor height constraints.
- Kept the visible start SQL against the registered DuckDB-Wasm view free of an explicit preview `limit`.
- Added a Row-Limit-Combobox with `100`, `1'000` and `10'000` rows; the selected value is passed into the existing query guard.
- Moved SQL actions to the left toolbar area and added a right-aligned export splitbutton for the current query result.
- Added current-result exports for CSV, XLSX and Parquet; CSV keeps semicolon and CRLF behavior.
- Removed the result footer CSV button, added compact mono footer text, refined splitters, status badge, schema rows and sticky row index layering.
- Updated docs and tests for the new compact workbench behavior.

Test evidence:

| Command | Result |
|---|---|
| `npm --prefix src/main/frontend/explore test` | PASS, `Test Files 15 passed (15)`, `Tests 67 passed (67)` |
| `npm --prefix src/main/frontend/explore run typecheck` | PASS, `tsc --noEmit` without errors |
| `npm --prefix src/main/frontend/explore run build` | PASS, Vite built Explore assets; expected large DuckDB-Wasm chunk warning remains |
| `./gradlew test --tests 'ch.so.agi.datenportal.explore.*'` | PASS, `BUILD SUCCESSFUL in 23s` |
| `./gradlew playwrightTest --tests 'ch.so.agi.datenportal.explore.*'` | PASS, `BUILD SUCCESSFUL in 23s`; includes Monaco editability, old panel-size guard, row-limit and splitbutton export checks |
| `./gradlew clean check` | PASS, `BUILD SUCCESSFUL in 50s`; included Vitest, typecheck, Vite build, backend tests and Playwright |

## SQL-Labor UI-Nachschliff Entry

Date: 2026-07-02

Scope:

- Kept header and breadcrumb, but removed the visible Explore dataset backlink from the workbench topbar.
- Replaced the `Registriert` schema status with `Geladen`; the badge title and accessible label explain that the Parquet file is loaded as a local DuckDB-Wasm view in the browser.
- Removed visible `Abfrage 1`, `SQL` and `Resultat` chrome from the primary lab.
- Moved the SQL actions into the compact header row and added an inline Bootstrap `bi-play-fill` icon to `Ausfuehren`.
- Made `SQL kopieren` a stable-width secondary red button with `SQL kopiert` feedback.
- Added `react-resizable-panels@3.0.6` for desktop schema/lab and editor/result resizing with per-dataset `localStorage` persistence.
- Kept mobile stacked and non-resizable to avoid page-level horizontal overflow.

Test evidence:

| Command | Result |
|---|---|
| `npm --prefix src/main/frontend/explore test` | PASS, `Test Files 15 passed (15)`, `Tests 62 passed (62)` |
| `npm --prefix src/main/frontend/explore run typecheck` | PASS, `tsc --noEmit` without errors |
| `npm --prefix src/main/frontend/explore run build` | PASS, Vite built Explore assets; expected large DuckDB-Wasm chunk warning remains |
| `./gradlew test --tests 'ch.so.agi.datenportal.explore.*'` | PASS, `BUILD SUCCESSFUL in 20s` |
| `./gradlew playwrightTest --tests 'ch.so.agi.datenportal.explore.*'` | PASS, `BUILD SUCCESSFUL in 18s`; includes Monaco editability and resizer checks |
| `./gradlew clean check` | PASS, `BUILD SUCCESSFUL in 41s`; included Vitest, typecheck, Vite build, backend tests and Playwright |

## Phase 0 Entry

Date: 2026-07-01

Branch: `main`

Current HEAD at orientation time: `c7c28a62cc3f`

Scope:

- Read the SQLRooms Erkunden MVP specification.
- Read repository instructions, skills, README files, build files, existing docs and source patterns.
- Created `docs/erkunden/` scaffold.
- Recorded repository findings and baseline tests.
- Updated phase tracking in the Erkunden MVP specification.
- No product code implemented.

Repository findings:

- Package root is `ch.so.agi.datenportal`.
- The current application is server-rendered Spring Boot/JTE/HTMX with vendored static assets.
- Existing dataset detail route is `/datasets/{identifier}`.
- Future Explore route will be `/datasets/{datasetId}/explore`.
- Existing static assets are served from `src/main/resources/static` with explicit cache rules.
- No `package.json`, Vite config, TypeScript config or frontend lockfile exists yet.

Naming decision:

- Code and routes use English `explore`.
- UI text uses German `Erkunden`.
- No `/erkunden` alias is planned for the MVP unless explicitly requested later.

Baseline test evidence before Phase-0 documentation edits:

| Command | Result |
|---|---|
| `./gradlew test` | PASS, `BUILD SUCCESSFUL in 5s` |
| `./gradlew check` | PASS, `BUILD SUCCESSFUL in 15s` |
| `./gradlew clean check` | PASS, `BUILD SUCCESSFUL in 16s` |

Final verification after Phase-0 documentation edits:

| Command | Result |
|---|---|
| `./gradlew clean check` | PASS, `BUILD SUCCESSFUL in 17s`; final rerun PASS, `BUILD SUCCESSFUL in 15s` |

Frontend test evidence:

- No separate frontend test command exists in Phase 0.
- Existing Playwright checks run through Gradle `playwrightTest` and are included in `check`.

Known limitations:

- SQLRooms, DuckDB-Wasm and React/Vite package compatibility has not yet been checked.
- CORS and Range Request behavior against real Parquet URLs has not yet been tested.
- CSP changes for Wasm/Worker/module assets are not yet known.

## Phase 1 Entry

Date: 2026-07-01

Branch: `main`

Scope:

- Added `/datasets/{datasetId}/explore` as a server-rendered portal page.
- Added `/datasets/{datasetId}/explore/context.json` as the backend context endpoint.
- Added `ch.so.agi.datenportal.explore` DTOs, services, properties, SQL name sanitizer, column role detector, generated recipes and static code snippets.
- Added a JTE host template with embedded JSON context and a server-side placeholder for the Phase-2 frontend island.
- Added unavailable handling for datasets without Parquet distributions.
- Updated phase tracking in the Erkunden MVP specification.

Implementation notes:

- Only normal `DatasetEntry` identifiers are accepted for the explore route.
- Parquet tables are derived from `DistributionFormat.PARQUET`.
- Column metadata comes from `CatalogEntryMetadata.attributes()` when available.
- The JSON endpoint uses the project-local JSON writer because Jackson is not part of the current application compile classpath.
- No React, Vite, SQLRooms, DuckDB-Wasm, frontend package or module asset was introduced in Phase 1.

Test evidence:

| Command | Result |
|---|---|
| `./gradlew test --tests 'ch.so.agi.datenportal.explore.*'` | PASS, `BUILD SUCCESSFUL in 2s` |
| `./gradlew test` | PASS, `BUILD SUCCESSFUL in 5s` |
| `./gradlew clean check` | PASS, `BUILD SUCCESSFUL in 17s` |

Known limitations:

- The Explore page currently shows only a server-rendered placeholder; the interactive island starts in Phase 2.
- CORS and Range Request behavior for real Parquet files remains untested until DuckDB-Wasm registration work begins.
- Charting, query history and runtime SQL execution are represented only as context flags and generated metadata.

## Phase 2 Entry

Date: 2026-07-01

Branch: `main`

Current HEAD before Phase-2 edits: `6ada81b71846`

Scope:

- Added isolated React/Vite/TypeScript frontend package under `src/main/frontend/explore`.
- Added npm lockfile and Gradle tasks for `npm ci`, frontend build, Vitest and typecheck.
- Integrated Vite build output into Spring static resources at `/explore/assets/explore.js` and `/explore/assets/explore.css`.
- Changed the Explore JTE host page to load the built island assets via `ExploreAssetLinks`.
- Added Zod validation for the embedded `ExploreContextDto`.
- Rendered Phase-2 static island UI with dataset title, table count, tabs and `DuckDB wird vorbereitet`.
- Added frontend unit/component tests, MVC asset-link/cache tests and a Java Playwright smoke test.
- Updated phase tracking in the Erkunden MVP specification.

Implementation notes:

- DuckDB-Wasm is not initialized in Phase 2.
- Parquet tables are not registered in Phase 2.
- SQLRooms core dependencies are installed for future phases, but not imported into the Phase-2 bundle.
- `@sqlrooms/ui` remains deferred because it introduces Tailwind peer dependencies.
- CSP remains unchanged; Wasm, worker and external Parquet loading policies will be handled in Phase 3.

Test evidence:

| Command | Result |
|---|---|
| `npm --prefix src/main/frontend/explore test` | PASS, `Test Files 2 passed (2)`, `Tests 7 passed (7)`, duration `808ms` |
| `npm --prefix src/main/frontend/explore run typecheck` | PASS, `tsc --noEmit` without errors |
| `npm --prefix src/main/frontend/explore run build` | PASS, Vite built `explore.css` and `explore.js`, `built in 141ms` |
| `./gradlew test --tests 'ch.so.agi.datenportal.explore.*'` | PASS, `BUILD SUCCESSFUL in 19s` |
| `./gradlew clean check` | PASS, `BUILD SUCCESSFUL in 21s`; included `npmTestExplore`, `npmTypecheckExplore`, `npmBuildExplore`, backend tests and `playwrightTest` |

Dependency notes:

- `npm install` / `npm ci` reports peer warnings from SQLRooms transitive packages with React 19, especially `react-virtual`, `react-dnd-multi-backend`, `react-dom@18.3.1` nested under `react-mosaic-component`, and `react-dnd-preview`.
- npm reports deprecated transitive packages `uuid@9.0.1` and `recharts@2.15.4`.
- npm audit currently reports 10 findings: `4 low`, `6 moderate`.

Known limitations:

- The island is a bootstrap skeleton only.
- No DuckDB-Wasm runtime, worker, Wasm binary, CORS or Range Request path is exercised yet.
- No SQL editor, query execution, result table, chart inference, chart rendering, CSV export or local history is implemented yet.

## Phase 3 Entry

Date: 2026-07-01

Branch: `main`

Scope:

- Added browser-local DuckDB-Wasm startup through SQLRooms room-store and DuckDB slice APIs.
- Bundled DuckDB-Wasm worker/wasm assets locally with Vite `?url`; no jsDelivr runtime bundles are used.
- Added Phase-3 Parquet view registration for every backend-provided table using `read_parquet('<absolute-url>')`.
- Added per-table registration state and a default preview query for the primary table.
- Added compact table catalog and preview result table in the existing Explore React island.
- Added Phase-3 query-safety helper coverage for single read-only statements and result limits.
- Updated CSP for Wasm, blob workers and `https://data.so.ch` Parquet fetches.
- Added same-origin DuckDB-Wasm Parquet extension mirror under `/explore-extensions/v1.4.3/wasm_mvp/`.
- Added same-origin Parquet Playwright fixture under `/explore-fixtures/`.
- Charting, SQL editor wiring, result export and local history remain deferred.

Implementation notes:

- The SQLRooms connector uses `createWasmDuckDbConnector`, `createDuckDbSlice`, `createBaseRoomSlice` and `createRoomStore`.
- DuckDB-Wasm is pinned to the MVP bundle for Phase 3. Chromium Headless selected the EH bundle automatically when offered, but that path failed in this environment with `RuntimeError: function signature mismatch`.
- DuckDB-Wasm loads Parquet through a loadable extension. The official signed `parquet.duckdb_extension.wasm` was mirrored same-origin and DuckDB is initialized with `custom_extension_repository = '<origin>/explore-extensions'`.
- The installed SQLRooms query helper package uses extensionless ESM internals that Vitest could not import directly. Phase 3 therefore keeps local equivalent query guard logic for `splitSqlStatements`/limit wrapping behavior and tests it explicitly.

Test evidence:

| Command | Result |
|---|---|
| `npm --prefix src/main/frontend/explore test` | PASS, `Test Files 4 passed (4)`, `Tests 17 passed (17)`, duration `1.30s` |
| `npm --prefix src/main/frontend/explore run typecheck` | PASS, `tsc --noEmit` without errors |
| `npm --prefix src/main/frontend/explore run build` | PASS, Vite built Explore bundle plus DuckDB worker/wasm assets under `/explore/assets/`, `built in 990ms` |
| `./gradlew test --tests 'ch.so.agi.datenportal.explore.*'` | PASS, `BUILD SUCCESSFUL in 3s` |
| `./gradlew playwrightTest --tests 'ch.so.agi.datenportal.explore.*'` | PASS, `BUILD SUCCESSFUL in 7s`; includes same-origin Parquet registration and preview rows |
| `./gradlew clean check` | PASS, `BUILD SUCCESSFUL in 23s`; included Vitest, typecheck, Vite build, backend tests and Playwright |

Known limitations:

- External `https://data.so.ch` Parquet smoke remains manual because DNS resolution for `data.so.ch` failed from the implementation/planning environment.
- Production Parquet URLs still require browser-visible CORS and byte Range support.
- The mirrored Parquet extension is tied to DuckDB-Wasm `v1.4.3/wasm_mvp`; upgrading `@duckdb/duckdb-wasm` requires refreshing the extension path and binary.
- Large DuckDB-Wasm assets are expected in Phase 3; code-splitting is deferred until broader UX hardening unless load time becomes a measured problem.

## Phase 4 Entry

Date: 2026-07-01

Branch: `main`

Scope:

- Added SQL-Labor UI with generated recipe list, SQL editor, toolbar, execution status, result table and CSV export.
- Wired recipe selection and manual SQL execution to the existing Phase-3 DuckDB-Wasm connector.
- Kept all execution browser-local; no backend SQL route or server-side SQL execution was added.
- Extended query guards for single read-only statements, blocked mutation/system commands, result-limit detection and timeout text.
- Updated the server-rendered no-JS/loading fallback copy for the now-live SQL laboratory.
- Added frontend unit/component tests and extended the same-origin Parquet Playwright test to run a generated recipe and verify CSV download.
- Updated Phase 4 tracking in the Erkunden MVP specification.

Implementation notes:

- Production uses `SqlMonacoEditor` from installed `@sqlrooms/sql-editor@0.28.0`; Vitest mocks the editor because that package has extensionless ESM internals that the test runner cannot resolve directly.
- Result rendering uses a portal-styled accessible HTML table instead of `DataTableArrowPaginated` for Phase 4. This keeps the result view consistent with the existing preview table and avoids pulling SQLRooms UI styling into the Datenportal surface.
- CSV export uses semicolon delimiters, CRLF line endings, RFC-style quote escaping, no UTF-8 BOM, and filenames like `datenportal-<datasetId>-result.csv`.
- Client-side query guards are documented as UX protection, not a security boundary.

Test evidence:

| Command | Result |
|---|---|
| `npm --prefix src/main/frontend/explore test` | PASS, `Test Files 7 passed (7)`, `Tests 28 passed (28)`, duration `1.43s` |
| `npm --prefix src/main/frontend/explore run typecheck` | PASS, `tsc --noEmit` without errors |
| `npm --prefix src/main/frontend/explore run build` | PASS, Vite built Explore and DuckDB-Wasm assets under `/explore/assets/`, `built in 1.07s` |
| `./gradlew test --tests 'ch.so.agi.datenportal.explore.*'` | PASS, `BUILD SUCCESSFUL in 5s` |
| `./gradlew playwrightTest --tests 'ch.so.agi.datenportal.explore.*'` | PASS, `BUILD SUCCESSFUL in 12s`; includes recipe execution and CSV download |
| `./gradlew clean check` | PASS, `BUILD SUCCESSFUL in 23s`; included Vitest, typecheck, Vite build, backend tests and Playwright |

Known limitations:

- Charting remains deferred to Phase 5; the `Diagramm` tab still states that charts come from SQL results later.
- Code snippets and local query history remain deferred to Phase 6.
- Query cancellation depends on the SQLRooms/DuckDB-Wasm query handle; the UI exposes cancellation while a query is running, but long-running browser behavior still needs broader Phase-7 hardening.
- The browser fixture covers same-origin Parquet; external `https://data.so.ch` CORS/Range behavior remains a manual/operational smoke test.

## Phase 5 Entry

Date: 2026-07-01

Branch: `main`

Scope:

- Added automatic chart inference for SQL result rows.
- Added a compact `ChartPanel` with chart type, axis and row-limit controls.
- Added Balken, Linie, Punkte and Histogramm chart components using `@sqlrooms/recharts@0.28.0`.
- Wired charts into the SQL-Labor and the top-level `Diagramm` tab through the existing `QueryResultState`.
- Used recipe `preferredChart` only for unchanged recipe SQL; edited/manual SQL falls back to result inference.
- Kept charting frontend-only; no backend SQL execution, dashboard builder, Vega, Mosaic, AI or WebR work was added.
- Added chart inference, component and browser smoke tests, including a mobile viewport check.
- Updated Phase 5 tracking in the Erkunden MVP specification.

Implementation notes:

- `@sqlrooms/recharts@0.28.0` typechecks and builds with the current React 19/Vite stack. No fallback to direct `recharts` imports was needed.
- Vitest mocks `@sqlrooms/recharts` because the package has extensionless internal ESM imports that Vitest cannot resolve directly in this project setup. Typecheck, Vite build and Playwright use the real package.
- DuckDB `count(*)` returns BigInt values in the browser. Chart rows are normalized to plain JavaScript values before Recharts rendering; SQL result rows remain unchanged for the result table and CSV export.
- Bar and line suggestions warn when the result has more than 500 rows and render only the selected chart row limit.

Test evidence:

| Command | Result |
|---|---|
| `npm --prefix src/main/frontend/explore test` | PASS, `Test Files 9 passed (9)`, `Tests 42 passed (42)`, duration `2.11s` |
| `npm --prefix src/main/frontend/explore run typecheck` | PASS, `tsc --noEmit` without errors |
| `npm --prefix src/main/frontend/explore run build` | PASS, Vite built Explore and DuckDB-Wasm assets under `/explore/assets/`, `built in 851ms` |
| `./gradlew playwrightTest --tests 'ch.so.agi.datenportal.explore.*'` | PASS, `BUILD SUCCESSFUL in 18s`; includes chart rendering and mobile viewport smoke checks |
| `./gradlew clean check` | PASS, `BUILD SUCCESSFUL in 40s`; included Vitest, typecheck, Vite build, backend tests and Playwright |

Known limitations:

- Code snippets and local query history remain deferred to Phase 6.
- Broader UX hardening, manual browser matrix checks and real external `https://data.so.ch` Parquet smoke tests remain deferred to Phase 7.
- Vite still reports expected large DuckDB-Wasm bundle warnings.

## Phase 6 Entry

Date: 2026-07-01

Branch: `main`

Scope:

- Added copyable static code snippets in the Explore `Code` tab for DuckDB CLI, Python and R.
- Hardened backend snippet generation to prefer the primary Parquet table and fall back to the first table.
- Added browser-local, per-dataset query history for successful SQL executions.
- Kept history behind `featureFlags.localHistory`; disabled history does not read or write `localStorage`.
- Stored only SQL and small execution metadata in history; result rows are not persisted.
- Added clear-history and load-from-history actions in the SQL laboratory.
- Kept WebR execution, server-side SQL execution, saved views and new heavy dependencies out of scope.
- Updated Phase 6 tracking in the Erkunden MVP specification.

Implementation notes:

- Local history key format is `datenportal.explore.history.<datasetId>`.
- The history stores at most 20 entries, newest first.
- `localStorage` read/write and JSON errors are ignored so SQL execution remains usable.
- Static R snippets remain copy-only and do not load WebR.

Test evidence:

| Command | Result |
|---|---|
| `npm --prefix src/main/frontend/explore test` | PASS, `Test Files 11 passed (11)`, `Tests 55 passed (55)`, duration `3.36s` |
| `npm --prefix src/main/frontend/explore run typecheck` | PASS, `tsc --noEmit` without errors |
| `npm --prefix src/main/frontend/explore run build` | PASS, Vite built Explore and DuckDB-Wasm assets under `/explore/assets/`, `built in 1.35s`; expected large DuckDB-Wasm chunk warning remains |
| `./gradlew test --tests 'ch.so.agi.datenportal.explore.*'` | PASS, `BUILD SUCCESSFUL in 11s` |
| `./gradlew playwrightTest --tests 'ch.so.agi.datenportal.explore.*'` | PASS, `BUILD SUCCESSFUL in 20s`; includes local history and static code snippets |
| `./gradlew clean check` | PASS, `BUILD SUCCESSFUL in 41s`; included Vitest, typecheck, Vite build, backend tests and Playwright |

Known limitations:

- Query history is browser-local only and is not synchronized across devices or sessions outside the same browser storage.
- Clearing browser storage removes local history.
- Broader UX hardening, manual browser matrix checks and real external `https://data.so.ch` Parquet smoke tests remain deferred to Phase 7.

## Phase 7 Entry

Date: 2026-07-01

Branch: `main`

Scope:

- Added accessible runtime status semantics for the Explore island (`role="status"`, alert state, `aria-busy`).
- Added keyboard navigation for the main Explore tablist with ArrowLeft/ArrowRight/Home/End.
- Added readable runtime error classification for browser-local DuckDB-Wasm, HTTP/CORS/Range/Parquet and IO loading failures.
- Kept the dataset detail page reachable through the existing breadcrumb/header navigation when the browser-local runtime fails.
- Hardened mobile CSS for the SQL toolbar, code tabs, chart controls and page-level horizontal overflow at common narrow widths.
- Added a broken same-origin Parquet fixture route in Playwright to test failure rendering without depending on external DNS/CORS.
- No backend SQL execution, persistence, AI/WebR/Vega/Mosaic feature or public DTO change was added.

Implementation notes:

- Runtime error classification is best-effort because DuckDB-Wasm error messages vary by browser and failure layer.
- The local automated browser path remains Chromium Playwright. Chrome, Firefox and Safari applications are installed locally, but this agent run did not perform a controllable GUI smoke test in those applications.
- The real hosted Parquet check against `https://data.so.ch/download/ch.so.oev_haltestellen.parquet` could not reach DNS from this environment.

Test evidence:

| Command | Result |
|---|---|
| `npm --prefix src/main/frontend/explore test` | PASS, `Test Files 13 passed (13)`, `Tests 63 passed (63)`, duration `4.52s` |
| `npm --prefix src/main/frontend/explore run typecheck` | PASS, `tsc --noEmit` without errors |
| `npm --prefix src/main/frontend/explore run build` | PASS, Vite built Explore and DuckDB-Wasm assets under `/explore/assets/`, `built in 1.58s`; expected large DuckDB-Wasm chunk warning remains |
| `./gradlew test --tests 'ch.so.agi.datenportal.explore.*'` | PASS, `BUILD SUCCESSFUL in 10s` |
| `./gradlew playwrightTest --tests 'ch.so.agi.datenportal.explore.*'` | PASS, `BUILD SUCCESSFUL in 24s`; includes same-origin Parquet registration, console-error check, keyboard tab navigation, broken-Parquet error state, SQL result/chart/code flows and mobile overflow checks at 320/390/768px |
| `./gradlew clean check` | PASS, `BUILD SUCCESSFUL in 50s`; included Vitest, typecheck, Vite build, backend tests and Playwright |
| `curl -I --max-time 10 https://data.so.ch/download/ch.so.oev_haltestellen.parquet` | FAIL from this environment, `curl: (6) Could not resolve host: data.so.ch` |

Known limitations:

- Real `data.so.ch` CORS, byte Range and Safari/Firefox runtime behavior still need an operator/manual smoke test from a network where `data.so.ch` resolves.
- Large DuckDB-Wasm bundle warnings remain expected for the MVP.

## Phase 8 Entry

Date: 2026-07-01

Branch: `main`

Scope:

- Added additive `geospatial` future flag beside the existing disabled AI, WebR, Vega and Mosaic flags.
- Added `datenportal.explore.geospatial-enabled=false` to the default configuration.
- Passed all future flags through backend properties, JSON serialization, frontend validation and frontend sample context.
- Added a quiet `FutureExtensionSlots` component that renders nothing while future flags are disabled.
- Added `npm run check:future-deps` to catch direct disabled future dependencies, source imports and built asset markers for AI, WebR, Vega, Mosaic and map runtimes.
- Documented future extension paths for AI, WebR/r-stats, Vega-Lite, Mosaic crossfilter, geospatial exploration and shareable SQL URLs.
- Updated Phase 8 tracking in the Erkunden MVP specification.

Implementation notes:

- No AI, WebR, Vega, Mosaic, map or geospatial runtime dependency was added.
- No public MVP UI is visible for disabled future features.
- The existing transitive `react-mosaic-component` package comes from current SQLRooms Shell-/Editor dependencies and is not treated as enabled `@sqlrooms/mosaic`.

Test evidence:

| Command | Result |
|---|---|
| `npm --prefix src/main/frontend/explore test` | PASS, `Test Files 14 passed (14)`, `Tests 65 passed (65)`, duration `3.40s` |
| `npm --prefix src/main/frontend/explore run typecheck` | PASS, `tsc --noEmit` without errors |
| `npm --prefix src/main/frontend/explore run build` | PASS, Vite built Explore and DuckDB-Wasm assets under `/explore/assets/`, `built in 964ms`; expected large DuckDB-Wasm chunk warning remains |
| `npm --prefix src/main/frontend/explore run check:future-deps` | PASS, `Future dependency check passed: no disabled AI/WebR/Vega/Mosaic/geospatial packages are directly loaded.` |
| `./gradlew test --tests 'ch.so.agi.datenportal.explore.*'` | PASS, `BUILD SUCCESSFUL in 7s`; included focused Explore backend tests and frontend asset build |
| `./gradlew clean check` | PASS, `BUILD SUCCESSFUL in 52s`; included Vitest, TypeScript, Vite build, backend tests and Playwright |

Known limitations:

- Future flags are extension points only. Enabling one does not implement production AI, WebR, Vega, Mosaic or map behavior.
- Real `data.so.ch` CORS, byte Range and Safari/Firefox runtime behavior still need an operator/manual smoke test from a network where `data.so.ch` resolves.

## DuckDB-Wasm Asset Compression Entry

Date: 2026-07-02

Branch: `main`

Scope:

- Added generated Brotli and Gzip variants for Explore CSS/JS/Wasm assets and the mirrored DuckDB-Wasm Parquet extension.
- Added Spring `EncodedResourceResolver` handling for `/explore/**` and `/explore-extensions/**`, while preserving the existing cache headers and uncompressed fallback.
- Re-tested the DuckDB-Wasm EH runtime path in Chromium Playwright. The Explore Parquet ready state timed out when EH was offered, so runtime selection remains pinned to MVP.
- Kept EH assets available in the bundle output for inspection and compression checks, but did not offer EH or COI through `createLocalDuckDbBundles()`.

Implementation notes:

- `precompressStaticAssets` runs after `npmBuildExplore` and before `processResources`.
- The generated `.br`/`.gz` resources live under `build/generated-resources/precompressed-static/` and are not committed source assets.
- Brotli uses quality 9 to keep local Gradle builds fast while reducing `duckdb-eh.wasm` transfer size to about 5.95 MB and `duckdb-mvp.wasm` to about 6.73 MB.

Test evidence:

| Command | Result |
|---|---|
| `npm --prefix src/main/frontend/explore test` | PASS, `Test Files 15 passed (15)`, `Tests 66 passed (66)`, duration `1.66s` |
| `npm --prefix src/main/frontend/explore run typecheck` | PASS, `tsc --noEmit` without errors |
| `npm --prefix src/main/frontend/explore run build` | PASS, Vite built Explore and DuckDB-Wasm assets under `/explore/assets/`; expected large DuckDB-Wasm chunk warning remains |
| `./gradlew test --tests 'ch.so.agi.datenportal.web.StaticAssetCachingMvcTest'` | PASS, `BUILD SUCCESSFUL in 4s`; includes Brotli, Gzip and uncompressed fallback checks |
| `./gradlew playwrightTest --tests 'ch.so.agi.datenportal.explore.*'` with EH offered | FAIL, 6 Explore Parquet tests timed out waiting for `.dp-explore-status--ready`; EH was therefore not enabled for runtime selection |
| `./gradlew playwrightTest --tests 'ch.so.agi.datenportal.explore.*'` with MVP runtime | PASS, `BUILD SUCCESSFUL in 16s`; confirms existing Explore Parquet flows remain stable |
| `./gradlew clean check` | PASS, `BUILD SUCCESSFUL in 37s`; included Vitest, TypeScript, Vite build, precompression, backend tests and Playwright |

Known limitations:

- Runtime remains on DuckDB-Wasm MVP until the EH path can pass the Explore Parquet browser smoke.
- COI/threaded DuckDB-Wasm remains a separate architecture decision because it requires Cross-Origin-Isolation headers.

## Local Monaco Bundle Entry

Date: 2026-07-02

Branch: `main`

Scope:

- Configured SQLRooms Monaco before the Explore React bootstrap so `@monaco-editor/react` uses the locally bundled Monaco runtime.
- Added direct frontend dependencies on `@sqlrooms/monaco-editor` and `monaco-editor` to make the production loader contract explicit.
- Bundled the default Monaco editor worker via Vite, keeping worker delivery same-origin under `/explore/assets/`.
- Kept the CSP closed for external Monaco CDNs; a `cdn.jsdelivr.net` or `unpkg.com` Monaco request is now treated as a browser-smoke regression.
- Added a Playwright assertion for the SQL laboratory that waits for `.monaco-editor` and records unexpected external Monaco CDN requests.

Implementation notes:

- The hidden textarea remains a non-visual fallback for state and accessibility plumbing, but the production SQL editor is the local Monaco instance.
- No external `script-src` or `connect-src` allowance was added for Monaco.

Test evidence:

| Command | Result |
|---|---|
| `npm --prefix src/main/frontend/explore test` | PASS, `Test Files 15 passed (15)`, `Tests 66 passed (66)`, duration `2.44s` |
| `npm --prefix src/main/frontend/explore run typecheck` | PASS, `tsc --noEmit` without errors |
| `npm --prefix src/main/frontend/explore run build` | PASS, Vite built Explore, local Monaco worker assets and DuckDB-Wasm assets under `/explore/assets/`; expected large chunk warning remains |
| `./gradlew playwrightTest --tests 'ch.so.agi.datenportal.explore.*'` | PASS, `BUILD SUCCESSFUL in 17s`; SQL laboratory renders `.monaco-editor`, records no external Monaco CDN request and exports a CSV result |
| `./gradlew clean check` | PASS, `BUILD SUCCESSFUL in 41s`; included Vitest, TypeScript, Vite build, precompression, backend tests and Playwright |

Known limitations:

- Only the default Monaco editor worker is explicitly wired through `configureMonacoLoader`; Vite may still emit additional Monaco worker chunks from the ESM editor API.

## SQL-Labor Redesign Entry

Date: 2026-07-02

Branch: `main`

Scope:

- Replaced the previous tabbed Explore UI with a full-width SQL workbench directly below `so-header` and `so-breadcrumb`.
- Added an Explore-specific JTE layout variant that preserves header, breadcrumb, assets and skip link but removes the normal page container and footer.
- Added a compact left `DATA` panel with visual schema cards; removed Add-files, grey icon rail and tree-style schema explorer from the primary UI.
- Kept DuckDB-Wasm registration as registered Views and changed the runnable initial SQL to query the registered table name.
- Kept Monaco local, with JetBrains Mono at compact editor scale, and styled the workbench with local Explore CSS rather than `@sqlrooms/ui`.
- Rebuilt the result table with row index, sticky/light headers, type pills, local horizontal scroll, compact footer and CSV export.
- Removed charts, code snippets, preview tab and visible query history from the primary UI without deleting their code paths.
- Updated MVC, frontend and Playwright tests for the redesigned shell, schema cards, red run button, missing old tabs, local Monaco, broken Parquet errors and mobile overflow.
- Updated Erkunden documentation for the new primary UI.

Implementation notes:

- A first `./gradlew clean check` rerun failed because `StaticAssetCachingMvcTest` still looked for the old `.dp-explore-island` CSS marker. The assertion now checks `.dp-explore-workbench`.
- Playwright caught a real mobile layout issue where Monaco intercepted clicks on the wrapped `Ausfuehren` toolbar. The mobile grid rows now use auto sizing for the toolbar and a stable editor minimum height.
- The Vite build still reports expected large DuckDB-Wasm chunk warnings.

Test evidence:

| Command | Result |
|---|---|
| `npm --prefix src/main/frontend/explore test` | PASS, `Test Files 15 passed (15)`, `Tests 61 passed (61)`, duration `2.71s` |
| `npm --prefix src/main/frontend/explore run typecheck` | PASS, `tsc --noEmit` without errors |
| `npm --prefix src/main/frontend/explore run build` | PASS, Vite built Explore, Monaco and DuckDB-Wasm assets under `/explore/assets/`; expected large chunk warning remains |
| `./gradlew test --tests 'ch.so.agi.datenportal.explore.*'` | PASS, `BUILD SUCCESSFUL in 11s` |
| `./gradlew playwrightTest --tests 'ch.so.agi.datenportal.explore.*'` | PASS, `BUILD SUCCESSFUL in 24s`; includes same-origin Parquet, local Monaco, SQL result, CSV export, hidden charts and mobile overflow checks |
| `./gradlew test --tests 'ch.so.agi.datenportal.web.StaticAssetCachingMvcTest'` | PASS, `BUILD SUCCESSFUL in 3s`; confirms updated Explore CSS marker and encoded asset handling |
| `./gradlew clean check` | PASS, `BUILD SUCCESSFUL in 39s`; included Vitest, TypeScript, Vite build, precompression, backend tests and Playwright |

Known limitations:

- Diagramm-, Codebeispiel- und Query-Historie-Komponenten bleiben als spaetere Anschlussstellen im Code, sind aber in der aktuellen Labor-UI nicht sichtbar.
- Real `data.so.ch` CORS, byte Range and Safari/Firefox runtime behavior still need an operator/manual smoke test from a network where `data.so.ch` resolves.
