# Erkunden Progress

Status: Phase tracking for `datenportal-erkunden-sqlrooms-mvp-agent-spec.md`

## Phase Status

| Phase | Status | Notes |
|---|---|---|
| 0. Repository orientation and documentation scaffold | DONE | Documentation scaffold created; baseline tests recorded. |
| 1. Backend context and route | DONE | Backend context, JSON endpoint, JTE host page and backend tests implemented. |
| 2. Frontend island bootstrap | TODO | No existing frontend package tooling found. |
| 3. DuckDB-Wasm Parquet registration | TODO | CORS and Range Request checks pending. |
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
