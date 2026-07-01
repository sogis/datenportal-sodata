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
| 7. UX hardening and browser checks | TODO | Not started. |
| 8. Future hooks for AI/WebR/Vega/Mosaic | TODO | Not started. |

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
