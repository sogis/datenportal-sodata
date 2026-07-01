# Erkunden Progress

Status: Phase tracking for `datenportal-erkunden-sqlrooms-mvp-agent-spec.md`

## Phase Status

| Phase | Status | Notes |
|---|---|---|
| 0. Repository orientation and documentation scaffold | DONE | Documentation scaffold created; baseline tests recorded. |
| 1. Backend context and route | DONE | Backend context, JSON endpoint, JTE host page and backend tests implemented. |
| 2. Frontend island bootstrap | DONE | React/Vite island embedded in JTE and built through Gradle/npm. |
| 3. DuckDB-Wasm Parquet registration | DONE | DuckDB-Wasm starts locally, Parquet views register, same-origin preview fixture passes. |
| 4. SQL laboratory and generated recipes | TODO | Not started. |
| 5. Charting V1 with Recharts | TODO | Not started. |
| 6. Code snippets and local query history | TODO | Not started. |
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
