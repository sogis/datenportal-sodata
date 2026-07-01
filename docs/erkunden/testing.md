# Erkunden Tests

Status: Phase 3 DuckDB-Wasm Parquet tests

Dieses Dokument sammelt die Teststrategie fuer die Erkunden-Phasen und die Phase-0-Baseline des bestehenden Projekts.

## Bestehende Testlandschaft

- JUnit/Jupiter und Spring Boot Test Support fuer Backend-, Domain-, Parser-, Search-, MVC- und Konfigurationstests.
- Playwright ist als Java/JUnit-Testpfad integriert.
- `test` schliesst Tests mit Tag `playwright` aus.
- `check` haengt von `playwrightTest` ab und installiert Chromium bei Bedarf ueber `installPlaywrightChromium`.
- Seit Phase 2 gibt es ein separates npm/Vite/React/Vitest-Frontend-Paket unter `src/main/frontend/explore`.
- `check` haengt zusaetzlich von `npmTestExplore` und `npmTypecheckExplore` ab.
- Seit Phase 3 prueft Playwright DuckDB-Wasm mit einer same-origin Parquet-Fixture.

## Baseline am 2026-07-01

Die folgenden Befehle wurden vor Phase-0-Dokumentationsaenderungen ausgefuehrt:

```bash
./gradlew test
```

Ergebnis: `BUILD SUCCESSFUL in 5s`

```bash
./gradlew check
```

Ergebnis: `BUILD SUCCESSFUL in 15s`

```bash
./gradlew clean check
```

Ergebnis: `BUILD SUCCESSFUL in 16s`

Hinweis: `check` und `clean check` fuehrten den vorhandenen `playwrightTest`-Task aus. Ein separates Frontend-Testkommando existiert in Phase 0 noch nicht.

## Phase 1 am 2026-07-01

Phase 1 ergaenzt Backend-Unit- und MVC-Tests:

- Backend-Unit-Tests fuer SQL-Namen, Rollen, Rezepte und Kontextaufbau.
- MVC-Tests fuer `/datasets/{datasetId}/explore` und `/datasets/{datasetId}/explore/context.json`.
- MVC-Test fuer Datensaetze ohne Parquet-Distribution.

Ausgefuehrte Befehle:

```bash
./gradlew test --tests 'ch.so.agi.datenportal.explore.*'
```

Ergebnis: `BUILD SUCCESSFUL in 2s`

```bash
./gradlew test
```

Ergebnis: `BUILD SUCCESSFUL in 5s`

```bash
./gradlew clean check
```

Ergebnis: `BUILD SUCCESSFUL in 17s`

Hinweis: Der erste fokussierte Implementierungslauf schlug voruebergehend fehl, weil Jackson nicht im Application Compile Classpath verfuegbar ist. Phase 1 verwendet deshalb einen projektlokalen JSON Writer und der fokussierte Testlauf wurde danach erfolgreich wiederholt.

## Phase 2 am 2026-07-01

Phase 2 ergaenzt:

- Frontend-Unit-Tests fuer Kontext-Parsing, Fehlermeldungen und initiales Rendering.
- React-Komponententest fuer Tabs und Nicht-verfuegbar-Zustand.
- MVC-Tests fuer Asset-Links und `/explore/**`-Cache-Header.
- Java-Playwright-Smoke-Test fuer den gebootstrappten React-Island auf `/datasets/ch.so.bauinventar/explore`.

Ausgefuehrte Befehle:

```bash
npm --prefix src/main/frontend/explore test
```

Ergebnis: PASS, `Test Files 2 passed (2)`, `Tests 7 passed (7)`, Dauer `808ms`.

```bash
npm --prefix src/main/frontend/explore run typecheck
```

Ergebnis: PASS, `tsc --noEmit` ohne Fehler.

```bash
npm --prefix src/main/frontend/explore run build
```

Ergebnis: PASS, Vite baute `explore.css` und `explore.js`, `built in 141ms`.

```bash
./gradlew test --tests 'ch.so.agi.datenportal.explore.*'
```

Ergebnis: PASS, `BUILD SUCCESSFUL in 19s`.

```bash
./gradlew clean check
```

Ergebnis: PASS, `BUILD SUCCESSFUL in 21s`. Der Lauf fuehrte `npmTestExplore`, `npmTypecheckExplore`, `npmBuildExplore`, Backend-Tests und `playwrightTest` aus.

Hinweis: `npm ci` meldet SQLRooms-transitive Peer-Warnings mit React 19 und `npm audit` meldet 10 Findings (`4 low`, `6 moderate`). In Phase 2 wurden keine abweichenden Versionen oder Audit-Fixes eingespielt, damit die geplanten Paketversionen stabil bleiben.

## Phase 3 am 2026-07-01

Phase 3 ergaenzt:

- Frontend-Unit-Tests fuer `buildCreateViewSql`, Tabellenname-/URL-Validierung und per-table Registrierungsergebnisse.
- Frontend-Unit-Tests fuer den Phase-3 Query Guard: read-only Statements, blockierte Mutations-/Systembefehle, Single-Statement-Verhalten und Limit-Wrapping.
- React-Komponententests fuer DuckDB-Initialisierung, Registrierungsstatus, Fehleranzeige, Tabellenkatalog und Preview-Tabelle.
- MVC-/Header-Test fuer CSP `worker-src`, `wasm-unsafe-eval` und `connect-src`.
- Java-Playwright-Test mit `/explore-fixtures/ch.so.oev_haltestellen.parquet`.

Fixture-/Runtime-Hinweise:

- Die Browser-Fixture liegt unter `src/test/resources/static/explore-fixtures/ch.so.oev_haltestellen.parquet`.
- Die DuckDB-Wasm Parquet-Erweiterung wird same-origin unter `/explore-extensions/v1.4.3/wasm_mvp/parquet.duckdb_extension.wasm` ausgeliefert.
- Der Playwright-Test erwartet, dass DuckDB den Status `Bereit` erreicht, die Tabelle als `Registriert` markiert und Preview-Zeilen mit `Solothurn` und `Olten` rendert.

Ausgefuehrte Befehle:

```bash
npm --prefix src/main/frontend/explore test
```

Ergebnis: PASS, `Test Files 4 passed (4)`, `Tests 17 passed (17)`, Dauer `1.30s`.

```bash
npm --prefix src/main/frontend/explore run typecheck
```

Ergebnis: PASS, `tsc --noEmit` ohne Fehler.

```bash
npm --prefix src/main/frontend/explore run build
```

Ergebnis: PASS, Vite baute Explore- und DuckDB-Wasm-Assets unter `/explore/assets/`, `built in 990ms`.

```bash
./gradlew playwrightTest --tests 'ch.so.agi.datenportal.explore.*'
```

Ergebnis: PASS, `BUILD SUCCESSFUL in 7s`.

```bash
./gradlew clean check
```

Ergebnis: PASS, `BUILD SUCCESSFUL in 23s`. Der Lauf fuehrte Vitest, Typecheck, Vite-Build, Backend-Tests und Playwright aus.

Phase 4 und spaeter:

- Tests fuer SQL-Editor, Rezeptausfuehrung, Resultat-Export, Charting und Mobile-Layout.

## Standard-Verifikation

Fuer abgeschlossene Phasen gilt weiterhin:

```bash
./gradlew clean check
```

Frontend-Einzelchecks:

```bash
npm --prefix src/main/frontend/explore test
npm --prefix src/main/frontend/explore run typecheck
npm --prefix src/main/frontend/explore run build
```
