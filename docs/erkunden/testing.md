# Erkunden Tests

Status: SQL-Labor result chart tests

Dieses Dokument sammelt die Teststrategie fuer die Erkunden-Phasen und die Phase-0-Baseline des bestehenden Projekts.

## Bestehende Testlandschaft

- JUnit/Jupiter und Spring Boot Test Support fuer Backend-, Domain-, Parser-, Search-, MVC- und Konfigurationstests.
- Playwright ist als Java/JUnit-Testpfad integriert.
- `test` schliesst Tests mit Tag `playwright` aus.
- `check` haengt von `playwrightTest` ab und installiert Chromium bei Bedarf ueber `installPlaywrightChromium`.
- Seit Phase 2 gibt es ein separates npm/Vite/React/Vitest-Frontend-Paket unter `src/main/frontend/explore`.
- `check` haengt zusaetzlich von `npmTestExplore` und `npmTypecheckExplore` ab.
- Seit Phase 3 prueft Playwright DuckDB-Wasm mit einer same-origin Parquet-Fixture.
- Seit Phase 4 prueft Playwright SQL-Ausfuehrung, Resultattabelle und CSV-Download mit derselben Fixture.
- Seit Phase 5 existieren Unit-Tests fuer Diagramm-Inferenz und Diagrammkomponenten; nach dem SQL-Labor-Redesign pruefen Vitest und Playwright Diagramme als lokale Resultatansicht statt als alten Haupt-Tab.
- Seit Phase 6 existieren Tests fuer statische Codebeispiele und lokale Query-Historie; im SQL-Labor-Redesign bleiben diese Bereiche aus der primaeren UI entfernt.
- Seit Phase 7 pruefen Vitest und Playwright zusaetzlich Status-/Fehlerzustaende, fehlende Parquet-Dateien, Browser-Konsole und page-level Mobile-Overflow.
- Seit Phase 8 pruefen Backend-, Frontend- und npm-Guard-Tests deaktivierte Zukunftsflags und verhindern direkte AI/WebR/Vega/Mosaic/Karten-Abhaengigkeiten.
- Seit dem SQL-Labor-Redesign pruefen MVC, Vitest und Playwright die vollflaechige Explore-Layoutvariante, Schema-Karten, Start-SQL gegen registrierte Views, roten Run-Button, lokale Monaco-Assets, Typ-Badges in Resultat-Headern, fehlende alte Tabs und lokale Tabellen-Scrollflaechen.
- Seit dem SQL-Labor UI-Nachschliff pruefen Vitest und Playwright zusaetzlich den Schema-Status `Tabelle geladen` statt `Registriert`, entfernte `Abfrage 1`-/`SQL`-/`Resultat`-Header, den Play-Icon-Run-Button, den stabilen `✓ SQL kopiert`-Button, sichtbares/editierbares Monaco-SQL und pointer-bedienbare Resizer-Handles.
- Seit dem Status-Overlay-Nachschliff pruefen Vitest und Playwright, dass Lade- und Fehlerzustaende als zentriertes Overlay erscheinen, Ladezustaende eine Progressbar besitzen, Fehlerzustaende keine Progressbar anzeigen, der globale `Bereit`-Badge im Erfolgsfall nicht gerendert wird und die Workbench keine Topbar-Hoehe mehr reserviert, aber den oberen Border direkt am Container behaelt.
- Seit dem zweiten UI-Nachschliff pruefen Vitest und Playwright zusaetzlich Start-SQL ohne sichtbares `limit`, versionierte Panel-Speicher-IDs, Row-Limit-Ausfuehrung, Export-Splitbutton fuer CSV/XLSX/Parquet, fehlenden Footer-CSV-Button und sticky Zeilennummern.
- Seit dem Resultat-Scrollbar-Nachschliff pruefen Vitest und Playwright zusaetzlich die fokussierbare Resultattabellen-Scrollregion, echte lokale horizontale/vertikale Overflow-Situationen und sichtbare Custom-Scrollbar-Pixel bei Hover, Klick und Tastaturfokus.
- Seit der Diagramm-Wiederaufnahme pruefen Vitest und Playwright die kompakte Beispielabfrage-Auswahl, den `Tabelle`/`Diagramm`-Umschalter, Recharts-Balken/Punkte/Linien/Histogramm sowie Pie/Donut mit Segmentfarben und `Farben neu`.

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
- Der Playwright-Test erwartet, dass DuckDB die Parquet-Datei registriert, die Tabelle als `Tabelle geladen` markiert, kein globales `Bereit` rendert und Preview-Zeilen mit `Solothurn` und `Olten` zeigt.

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

## Phase 4 am 2026-07-01

Phase 4 ergaenzt:

- Frontend-Unit-Tests fuer erweiterte Query-Guards, Timeout-Text und Limit-Erkennung.
- Frontend-Unit-Tests fuer CSV-Serialisierung, Dateinamen und Object-URL-Cleanup.
- React-Komponententests fuer Rezeptauswahl, SQL-Aenderungen, Ausfuehrung, Guard-Fehler, Copy-Feedback und Resultatanzeige.
- Java-Playwright-Test fuer SQL-Labor: Rezept auswaehlen, ausfuehren, Resultat sehen und CSV-Download starten.

Ausgefuehrte Befehle:

```bash
npm --prefix src/main/frontend/explore test
```

Ergebnis: PASS, `Test Files 7 passed (7)`, `Tests 28 passed (28)`, Dauer `1.43s`.

```bash
npm --prefix src/main/frontend/explore run typecheck
```

Ergebnis: PASS, `tsc --noEmit` ohne Fehler.

```bash
npm --prefix src/main/frontend/explore run build
```

Ergebnis: PASS, Vite baute Explore- und DuckDB-Wasm-Assets unter `/explore/assets/`, `built in 1.07s`. Vite meldet weiterhin die erwartete Warnung zu grossen DuckDB-Wasm-Chunks.

```bash
./gradlew test --tests 'ch.so.agi.datenportal.explore.*'
```

Ergebnis: PASS, `BUILD SUCCESSFUL in 5s`.

```bash
./gradlew playwrightTest --tests 'ch.so.agi.datenportal.explore.*'
```

Ergebnis: PASS, `BUILD SUCCESSFUL in 12s`; umfasst DuckDB-Wasm-Parquet-Registrierung, Preview, Rezeptausfuehrung und CSV-Download.

```bash
./gradlew clean check
```

Ergebnis: PASS, `BUILD SUCCESSFUL in 23s`. Der Lauf fuehrte Vitest, Typecheck, Vite-Build, Backend-Tests und Playwright aus.

## Phase 5 am 2026-07-01

Phase 5 ergaenzt:

- Frontend-Unit-Tests fuer `chartInference`: Balken, Linie, Punktdiagramm, Histogramm, bevorzugte Rezeptdiagramme, Fallbacks, Histogramm-Bins und Warnungen fuer grosse Resultate.
- React-Komponententests fuer `ChartPanel`: leerer Zustand, Balkensteuerung, Typ-/Achsen-/Zeilenlimit-Wechsel, Warnzustand und Histogramm.
- Erweiterte SQL-Labor- und App-Komponententests fuer Diagramm-Anzeige aus erfolgreichem SQL-Resultat.
- Java-Playwright-Smoke-Tests fuer gruppierte Rezeptausfuehrung mit Balkendiagramm und mobilem Viewport ohne horizontales Clipping.

Ausgefuehrte Befehle:

```bash
npm --prefix src/main/frontend/explore test
```

Ergebnis: PASS, `Test Files 9 passed (9)`, `Tests 42 passed (42)`, Dauer `2.11s`.

```bash
npm --prefix src/main/frontend/explore run typecheck
```

Ergebnis: PASS, `tsc --noEmit` ohne Fehler.

```bash
npm --prefix src/main/frontend/explore run build
```

Ergebnis: PASS, Vite baute Explore- und DuckDB-Wasm-Assets unter `/explore/assets/`, `built in 851ms`. Vite meldet weiterhin die erwartete Warnung zu grossen DuckDB-Wasm-Chunks.

```bash
./gradlew playwrightTest --tests 'ch.so.agi.datenportal.explore.*'
```

Ergebnis: PASS, `BUILD SUCCESSFUL in 18s`; umfasst DuckDB-Wasm-Parquet-Registrierung, Preview, Rezeptausfuehrung, CSV-Download, Diagramm-Rendering und mobilen Chart-Smoke-Test.

```bash
./gradlew clean check
```

Ergebnis: PASS, `BUILD SUCCESSFUL in 40s`; fuehrte Vitest, Typecheck, Vite-Build, Backend-Tests und Playwright aus.

## Phase 6 am 2026-07-01

Phase 6 ergaenzt:

- Backend-Unit-Tests fuer `ExploreCodeSnippetService`: DuckDB/Python/R-Snippets, primaere Tabelle, Fallback-Tabelle, URL-Escaping und leere Tabellen.
- Frontend-Unit-Tests fuer `QueryHistory`: dataset-spezifische Keys, maximal 20 Eintraege, newest first, clear, defekte Storage-Daten und keine gespeicherten Resultatzeilen.
- React-Komponententests fuer `CodeSnippetsPanel`: Tabs, leeren Zustand und Copy-Feedback.
- Erweiterte SQL-Labor-Tests fuer erfolgreiche History-Speicherung, Laden einer History-Abfrage, Loeschen und deaktiviertes `localHistory`-Flag.
- Java-Playwright-Test fuer erfolgreiche Query-Historie und statische Codebeispiele im Tab `Code`.

Ausgefuehrte Befehle:

```bash
npm --prefix src/main/frontend/explore test
```

Ergebnis: PASS, `Test Files 11 passed (11)`, `Tests 55 passed (55)`, Dauer `3.36s`.

```bash
npm --prefix src/main/frontend/explore run typecheck
```

Ergebnis: PASS, `tsc --noEmit` ohne Fehler.

```bash
npm --prefix src/main/frontend/explore run build
```

Ergebnis: PASS, Vite baute Explore- und DuckDB-Wasm-Assets unter `/explore/assets/`, `built in 1.35s`. Vite meldet weiterhin die erwartete Warnung zu grossen DuckDB-Wasm-Chunks.

```bash
./gradlew test --tests 'ch.so.agi.datenportal.explore.*'
```

Ergebnis: PASS, `BUILD SUCCESSFUL in 11s`.

```bash
./gradlew playwrightTest --tests 'ch.so.agi.datenportal.explore.*'
```

Ergebnis: PASS, `BUILD SUCCESSFUL in 20s`; umfasst DuckDB-Wasm-Parquet-Registrierung, Preview, Rezeptausfuehrung, CSV-Download, Diagramm-Rendering, lokale Query-Historie und statische Codebeispiele.

```bash
./gradlew clean check
```

Ergebnis: PASS, `BUILD SUCCESSFUL in 41s`; fuehrte Vitest, Typecheck, Vite-Build, Backend-Tests und Playwright aus.

## Phase 7 am 2026-07-01

Phase 7 ergaenzt:

- Frontend-Unit-Tests fuer Runtime-Fehlerklassifizierung: CORS, Range, HTTP/IO, DuckDB-Wasm.
- React-Komponententests fuer Haupt-Tab-Tastaturbedienung und ARIA-Status-/Alert-Regionen.
- React-Komponententests fuer Resultatzustaende: idle, running, cancelled, timeout und query error.
- Java-Playwright-Tests fuer Browser-Konsole ohne Fehler im Erfolgsfall, Tastatur-Navigation, fehlende Parquet-Datei mit lesbarer Fehlerbox und mobilen Overflow bei 320, 390 und 768 Pixel Breite.
- Externer Parquet-Host-Check per `curl`.

Ausgefuehrte Befehle:

```bash
npm --prefix src/main/frontend/explore test
```

Ergebnis: PASS, `Test Files 13 passed (13)`, `Tests 63 passed (63)`, Dauer `4.52s`.

```bash
npm --prefix src/main/frontend/explore run typecheck
```

Ergebnis: PASS, `tsc --noEmit` ohne Fehler.

```bash
npm --prefix src/main/frontend/explore run build
```

Ergebnis: PASS, Vite baute Explore- und DuckDB-Wasm-Assets unter `/explore/assets/`, `built in 1.58s`. Vite meldet weiterhin die erwartete Warnung zu grossen DuckDB-Wasm-Chunks.

```bash
./gradlew test --tests 'ch.so.agi.datenportal.explore.*'
```

Ergebnis: PASS, `BUILD SUCCESSFUL in 10s`.

```bash
./gradlew playwrightTest --tests 'ch.so.agi.datenportal.explore.*'
```

Ergebnis: PASS, `BUILD SUCCESSFUL in 24s`; umfasst erfolgreiche DuckDB-Wasm-Parquet-Registrierung, Browser-Konsolencheck, Tastatur-Tabnavigation, fehlende Parquet-Datei, SQL-Resultat, Diagramm, Codebeispiele und mobile Overflow-Checks.

```bash
./gradlew clean check
```

Ergebnis: PASS, `BUILD SUCCESSFUL in 50s`; fuehrte Vitest, Typecheck, Vite-Build, Backend-Tests und Playwright aus.

```bash
curl -I --max-time 10 https://data.so.ch/download/ch.so.oev_haltestellen.parquet
```

Ergebnis: FAIL in der Agent-Umgebung, `curl: (6) Could not resolve host: data.so.ch`.

Phase 8 und spaeter:

- Manuelle Browsermatrix fuer echte Chrome-/Firefox-/Safari-Installationen und real erreichbares `data.so.ch` bleiben operative Smoke-Checks.

## Phase 8 am 2026-07-01

Phase 8 ergaenzt:

- Backend-Tests fuer deaktivierte Zukunftsflags `aiAssistant`, `webR`, `vega`, `mosaic` und `geospatial`.
- MVC-/JSON-Tests fuer die serialisierte `featureFlags`-Form.
- Frontend-Kontext-Parsing fuer `geospatial`.
- React-Komponententests, dass vorbereitete Erweiterungsslots bei deaktivierten Flags nicht sichtbar sind.
- `npm run check:future-deps` als Guard gegen direkte Zukunftspakete und Source-/Bundle-Imports fuer AI, WebR, Vega, Mosaic und Kartenframeworks.

Ausgefuehrte Befehle werden in `docs/erkunden/progress.md` mit exakten Ergebnissen dokumentiert.

## SQL-Labor Redesign am 2026-07-02

Das Redesign ersetzt die alte Taboberflaeche durch eine vollflaechige SQLRooms-nahe Workbench:

- MVC-Tests pruefen Header, Breadcrumb, Explore-spezifische Full-width-Klassen, eingebetteten Kontext, Asset-Links und fehlenden Footer.
- Frontend-Tests pruefen Schema-Karten, kompakte Workbench, Start-SQL gegen den registrierten View, roten `Ausfuehren`-Button, entfernte Preview-/Diagramm-/Code-Tabs, Fehlerzustaende, Resultattabellen mit Typ-Badges, Row-Limit, SQLRooms-`tableSchemas` fuer Autocomplete und CSV/XLSX/Parquet-Exporthelfer.
- Playwright prueft same-origin Parquet-Registrierung, lokale Monaco-Assets, initiale SQL-Ausfuehrung, CSV/XLSX/Parquet-Export aus dem Splitbutton, einheitliche Borderfarben, duennere Splitter, lesbare Broken-Parquet-Fehler, mobile lokale Tabellen-Scrollflaechen, Row-Limit-Wechsel und kein page-level Horizontal-Overflow.

## SQL-Labor Diagramme am 2026-07-03

Die Diagramm-Wiederaufnahme behaelt die vollflaechige Workbench und ersetzt keine alten Haupt-Tabs:

- Frontend-Tests pruefen, dass Beispielabfragen nur SQL laden, der Resultatumschalter zwischen Tabelle und Diagramm wechselt und `preferredChart` nur bei unveraendertem Rezept-SQL greift.
- Chart-Tests pruefen Balken, Punkte, Histogramm, Pie, Donut, Warnungen fuer viele Segmente und die erneuerbaren pseudo-zufaelligen Segmentfarben.
- Playwright prueft, dass `button[role='tab']` fuer `Diagramm` weiterhin fehlt, die lokale Diagrammansicht aber nach Query-Ausfuehrung Recharts rendert und Pie/Donut-Farben im Browser sichtbar sind.

Ausgefuehrte Befehle werden in `docs/erkunden/progress.md` mit exakten Ergebnissen dokumentiert.

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
