# Erkunden Tests

Status: SQL- und R-Labor tests

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
- Seit der Code-Quality-Remediation pruefen Backend und Frontend den produktiven V4-Kontext; entfernte Beispiel-, History- und generische Flag-Pfade werden nicht mehr getestet.
- Seit Phase 7 pruefen Vitest und Playwright zusaetzlich Status-/Fehlerzustaende, fehlende Parquet-Dateien, Browser-Konsole und page-level Mobile-Overflow. Nicht erreichbare Quelldateien werden im Query-Pfad als neutraler Resultatbereich-Alert geprueft, nicht als globaler Runtime-Overlay-Fehler.
- Seit dem SQL-Labor-Redesign pruefen MVC, Vitest und Playwright die vollflaechige Explore-Layoutvariante, Schema-Karten, Start-SQL gegen registrierte Views, roten Run-Button, lokale Monaco-Assets, Typ-Badges in Resultat-Headern, fehlende alte Tabs und lokale Tabellen-Scrollflaechen.
- Seit dem SQL-Labor UI-Nachschliff pruefen Vitest und Playwright zusaetzlich den Schema-Status `Tabelle geladen` statt `Registriert`, entfernte `Abfrage 1`-/`SQL`-/`Resultat`-Header, den Play-Icon-Run-Button, den stabilen `✓ SQL kopiert`-Button, sichtbares/editierbares Monaco-SQL und pointer-bedienbare Resizer-Handles.
- Seit dem Status-Overlay-Nachschliff pruefen Vitest und Playwright, dass Lade- und Runtime-Fehlerzustaende als zentriertes Overlay erscheinen, Ladezustaende eine Progressbar besitzen, Runtime-Fehlerzustaende keine Progressbar anzeigen, der globale `Bereit`-Badge im Erfolgsfall nicht gerendert wird und die Workbench keine Topbar-Hoehe mehr reserviert, aber den oberen Border direkt am Container behaelt.
- Seit dem zweiten UI-Nachschliff pruefen Vitest und Playwright zusaetzlich Start-SQL ohne sichtbares `limit`, versionierte Panel-Speicher-IDs, Row-Limit-Ausfuehrung, Export-Splitbutton fuer CSV/XLSX/Parquet, fehlenden Footer-CSV-Button und sticky Zeilennummern.
- Seit der Exportsteuerungs-Verschiebung pruefen Vitest und Playwright, dass der Tabellen-Splitbutton im Ergebnis-Header statt im SQL-Toolbar-Bereich sitzt, im Diagrammmodus nur der PNG-Button erscheint und dieser bei einem nicht renderbaren Diagramm deaktiviert bleibt. Der PNG-Unit-Test prueft Dateiname, Titel-/Legendeninhalt, das Entfernen markierter UI-Elemente und die DOM-Aufräumung bei Erfolg und Fehler.
- Seit dem Resultat-Scrollbar-Nachschliff pruefen Vitest und Playwright zusaetzlich die fokussierbare Resultattabellen-Scrollregion, echte lokale horizontale/vertikale Overflow-Situationen und sichtbare Custom-Scrollbar-Pixel bei Hover, Klick und Tastaturfokus.
- Seit der Diagramm-Wiederaufnahme pruefen Vitest und Playwright die kompakte Beispielabfrage-Auswahl, den `Tabelle`/`Diagramm`-Umschalter, Recharts-Balken/Punkte/Linien/Histogramm, die erlaubten Zusatzfarben ohne Rot, mehrfarbige Balken sowie Pie/Donut mit Segmentfarben und `Farben neu`.
- Seit der Serienausgaben-Erweiterung pruefen MVC- und Playwright-Tests, dass Open-Data-Ausgaben einen aktiven Explore-Link zeigen, aktuelle und historische Ausgaben eigene Explore-Kontexte liefern, falsche Dataset-/Serienrouten 404 bleiben und das SQL-Labor auf einer Ausgabe echte Parquet-Daten laden und abfragen kann.
- Seit der V4-Umstellung pruefen Backend- und Frontend-Tests den Explore-Kontext
  Version 4 mit `rLaboratory`, direkten Chart-/WebR-Booleans und statischen
  Asset-/Cache-Regeln fuer `/webr/**` und `/webr-packages/**`.
- Vitest prueft DuckDB/Arrow-nahes Type-Mapping, Snapshot-Erzeugung aus SQL-Resultaten, R-Rezeptgenerierung, WebR-Runtime-Ladephasen mit gemocktem `webr`, R-Panel-UI, Limit-Warnungen, Exportbuttons und das Package-Mirror-Script inklusive Dependency-Closure und Lockdatei.
- Seit dem R-Labor UI-Nachschliff prueft Vitest zusaetzlich, dass R-Exportbuttons in den Outputbereichen statt in der oberen Toolbar sitzen, Paneltitel fuer Konsole/Plot nicht sichtbar gerendert werden, der Resultat-Export erst nach tabellarischem R-Resultat aktiv wird und die Dataframe-Kennzahlen `Anzahl Zeilen`/`Anzahl Spalten` heissen.
- Seit dem zweiten R-Labor UI-Nachschliff prueft Vitest, dass R ohne SQL-Result startet, Konsole und Plot einen eigenen Resize-Handle haben, die Rezepttitel Schweizer Anfuehrungszeichen verwenden und die Plot-Heuristik Messwerte statt Jahre/Codes priorisiert.
- Der echte WebR-Browser-Smoke ist opt-in: `./gradlew playwrightTest -Ddatenportal.playwright.webr=true`. Der normale `playwrightTest`-Task ueberspringt ihn, weil WebR die Laufzeit deutlich verlaengert. Bei Aenderungen an CSP, WebR-Runtime, Paketmirror oder R-Transfer muss der gezielte Test `ExploreIslandParquetPlaywrightTest.rLaboratoryLoadsWebRFromSameOriginAndReceivesSqlResult` zusaetzlich ausgefuehrt werden.

## Code-Quality Remediation Phase 1 am 2026-08-11

Die erste Remediation-Phase prueft die Explore-Lebenszyklen und die
Testdeterministik:

- `ExploreApp` verwendet Generationen und AbortController; DuckDB wird nach
  einem abgebrochenen Initialisierungsversuch genau einmal zerstört.
- `attachCatalogDatabase` propagiert das AbortSignal an `fetch()` und wandelt
  kontrollierte Fetch-Abbrüche weiterhin in Registrierungsfehler um.
- `executeDuckDbQuery` wartet nach `cancelSent()` auf das Ende der ursprünglichen
  Query-Promise; `SqlLaboratory` verhindert parallele Queries und löscht nur den
  jeweils eigenen aktiven Handle.
- `WebRRuntime` teilt Initialisierungen, schließt Runtime-Instanzen bei Init- und
  Paket-Timeouts, erlaubt kontrollierte Retries nach Fehlern und publiziert nach
  `close()` keine weiteren Fortschritte.
- `RPanel` schützt Initialisierung und Transfers über Operations-IDs; der
  Unmount leert Bridge/Runtime und schließt WebR.
- Monaco erhält `data-autocomplete-ready="true"` erst nach Mount und vorhandenen
  aktuellen Schemas. Der Playwright-Helfer wartet darauf und öffnet Completion
  genau einmal.

Testprotokoll:

| Command | Result |
|---|---|
| `npm --prefix src/main/frontend/explore test -- --run src/app/ExploreApp.test.tsx src/duckdb/attachCatalogDatabase.test.ts src/duckdb/executeDuckDbQuery.test.ts src/sql/SqlLaboratory.test.tsx src/webr/RPanel.test.tsx src/webr/WebRRuntime.test.ts` | PASS, 6 files, 45 tests |
| `npm --prefix src/main/frontend/explore test -- --run` | PASS, 23 files, 124 tests |
| `npm --prefix src/main/frontend/explore run typecheck` | PASS |
| `npm --prefix src/main/frontend/explore run build` | PASS; existing large DuckDB-Wasm chunk warning remains |
| `git diff --check` | PASS |
| `./gradlew clean check` | PASS twice, first `1m 7s`, second `57s` |
| `./gradlew playwrightTest --tests 'ch.so.agi.datenportal.explore.ExploreIslandParquetPlaywrightTest.rLaboratoryLoadsWebRFromSameOriginAndReceivesSqlResult' -Ddatenportal.playwright.webr=true` | PASS, real WebR browser smoke, `9s` |

Die Remediation-Phasen werden separat committed; die Commit-Zuordnung steht in
`docs/phase-status.md`.

## Code-Quality Remediation Phase 2 am 2026-08-11

Die zweite Remediation-Phase prueft die Java-Suche und ihre Fehlergrenzen:

- `CatalogSearchIndex` liefert die vollstaendige Lucene-Treffermenge und eine
  separate `documentCount()`-Diagnose; die alte Ergebnisgrenze und `isEmpty()`
  sind aus der Index-API entfernt.
- Geschlossene oder fehlerhafte Lucene-Indizes werfen
  `CatalogSearchException`; die Suchschicht faengt diese Fehler nicht als
  leere Resultate ab.
- Java-Filter werden erst nach der vollstaendigen Textsuche angewendet. Ein
  Test mit 501 Treffern stellt sicher, dass der Treffer an Position 500 nicht
  verloren geht.
- Die Health-Anzeige vergleicht sichtbare und indizierte Dokumentzahl und
  wird bei Abweichung oder `documentCount()`-Fehler `DOWN`.
- Vollseiten-Suchfehler liefern eine verstaendliche 503-Fehlerseite;
  HTMX-Anfragen erhalten zusaetzlich `HX-Refresh: true`.
- `CatalogService.currentSnapshot()` und ausschließlich geschriebene
  Lucene-Felder wurden entfernt.

Testprotokoll:

| Command | Result |
|---|---|
| `./gradlew test --tests 'ch.so.agi.datenportal.search.*' --tests 'ch.so.agi.datenportal.catalog.service.CatalogServiceTest' --tests 'ch.so.agi.datenportal.catalog.service.CatalogReloadServiceTest' --tests 'ch.so.agi.datenportal.catalog.service.CatalogSnapshotLoaderTest' --tests 'ch.so.agi.datenportal.admin.actuator.CatalogSearchIndexHealthIndicatorTest' --tests 'ch.so.agi.datenportal.web.CatalogSearchErrorMvcTest' --tests 'ch.so.agi.datenportal.admin.actuator.CatalogActuatorMvcTest' --tests 'ch.so.agi.datenportal.DatenportalApplicationTests'` | PASS, 41 Tests |
| `git diff --check` | PASS |
| `./gradlew clean check` | PASS, `BUILD SUCCESSFUL in 55s`; Vitest 124, TypeScript, Vite, Backend-Tests und Playwright |

Die Remediation-Phasen werden separat committed; die Commit-Zuordnung steht in
`docs/phase-status.md`.

## Code-Quality Remediation Phase 5 am 2026-08-11

Die fünfte Remediation-Phase reduziert die serverseitige UI auf tatsächlich
gerenderte Modelle:

- Dataset- und Issue-Detailseiten verwenden gemeinsam `EntryDetailPageVm` und
  `pages/entryDetail.jte`; `MetadataLineVm` trägt einzelne Text-, HTTP- oder
  Mail-Zeilen.
- Result Controls, Serienlisten, Related-Issues und Filtergruppen verwenden
  direkte Listen-/Boolean-Felder ohne Wrapper-Enums oder Controls-ViewModels.
- Starter-Rezepte, ihre `#`-Links, JTE-Komponente, Styles und vier PNG-Assets
  sind entfernt. Der produktive SQL-Rezeptpfad bleibt bestehen.
- Der Package-Umfang `web/view` liegt bei exakt 34 Java-Dateien.

Testprotokoll:

| Command | Result |
|---|---|
| `./gradlew test` | PASS, 271 Tests |
| `./gradlew playwrightTest --tests 'ch.so.agi.datenportal.web.CatalogFiltersPlaywrightTest'` | PASS, 24 Tests |
| `git diff --check` | PASS |
| `./gradlew clean check` | PASS, `BUILD SUCCESSFUL in 57s`; Vitest 124, TypeScript, Vite, Backend-Tests und Playwright |

Die Remediation-Phasen werden separat committed; die Commit-Zuordnung steht in
`docs/phase-status.md`.

## Code-Quality Remediation Phase 6 am 2026-08-11

Die sechste Remediation-Phase bindet fertige XTF- und DuckDB-Artefakte an
denselben unveränderlichen Runtime-Snapshot:

- Startup und Reload laden beide Quellen genau einmal vor Parse und Indexbau.
- `CatalogSnapshot` speichert beide Artefakte; `CatalogService` aktiviert
  sie gemeinsam mit dem Lucene-Index.
- DuckDB wird ausschließlich technisch auf Mindestgröße und den `DUCK`-
  Marker an Byteposition 8 bis 11 geprüft.
- Artifact-GETs lesen ausschließlich aus dem Snapshot und liefern ETag,
  Content-Length, 304/409 und die spezifizierten Cache-Header.
- Explore erhält die DuckDB-URL aus dem Hash desselben Snapshots.

Testprotokoll:

| Command | Result |
|---|---|
| `./gradlew test --tests 'ch.so.agi.datenportal.catalog.service.*' --tests 'ch.so.agi.datenportal.web.CatalogArtifactControllerMvcTest' --tests 'ch.so.agi.datenportal.explore.ExploreContextServiceTest' --tests 'ch.so.agi.datenportal.explore.ExplorePageControllerMvcTest' --tests 'ch.so.agi.datenportal.admin.actuator.CatalogSnapshotHealthIndicatorTest'` | PASS |
| `git diff --check` | PASS |
| `./gradlew clean check` | PASS, `BUILD SUCCESSFUL in 1m 46s`; Vitest 124, TypeScript, Vite, Backend-Tests und Playwright |

Die fachliche Übereinstimmung von XTF und DuckDB bleibt Verantwortung der
externen Publishing-Pipeline; die Anwendung erzeugt oder transformiert die
DuckDB-Datei nicht.

## Code-Quality Remediation Phase 7 am 2026-08-11

Die siebte Remediation-Phase reduziert das Produktionsartefakt:

- `duckdbBundles.ts` liefert nur noch den MVP-Bundle.
- Vite und der WebR-Copy-Task legen keine Source-Maps in das Boot-JAR.
- `RPanel` und `RDataFramePanel` werden mit `React.lazy()` erst nach
  Aktivierung des R-Labors geladen.

JAR-Abnahme mit `./gradlew clean bootJar --no-daemon`:

- Ausgang: 228.979.643 Bytes.
- Ergebnis: 174.656.030 Bytes.
- Reduktion: 54.323.613 Bytes, rund 51,8 MiB; JAR rund 166,5 MiB.
- `jar tf` findet keine EH-/COI-Bundles, keine `coi.pthread`-Datei und
  keine `.map`; MVP- und R-Chunks sind vorhanden.

Testprotokoll:

| Command | Result |
|---|---|
| `npm test` | PASS, 23 Dateien, 125 Tests |
| `npm run typecheck` | PASS |
| `./gradlew playwrightTest --tests 'ch.so.agi.datenportal.explore.ExploreIslandParquetPlaywrightTest.rLaboratoryLoadsWebRFromSameOriginAndReceivesSqlResult' -Ddatenportal.playwright.webr=true` | PASS, real WebR browser smoke, `BUILD SUCCESSFUL in 14s` |
| `./gradlew clean bootJar --no-daemon` | PASS |
| `git diff --check` | PASS |
| `./gradlew clean check --no-daemon` | PASS, `BUILD SUCCESSFUL in 57s`; Vitest 125, TypeScript, Vite, Backend-Tests und Playwright |

Die alte Größenmessung und die JAR-Prüfung wurden vor dem vollständigen Gate
mit demselben `clean bootJar`-Task durchgeführt.

## Code-Quality Remediation Phase 3 am 2026-08-11

Die dritte Remediation-Phase prueft produktionssichere Konfiguration und
öffentliche Health-Ausgaben:

- `application.yml` enthält keine implizite XTF- oder DuckDB-Fixture, keine
  localhost-Downloadbasis und keinen JTE-Development-Mode. JTE nutzt im
  Produktionsmodus vorcompilierte Templates.
- `application-local.yml` aktiviert die lokalen Fixtures, localhost-Downloads
  und JTE-Development-Mode explizit; `application-test.yml` enthält die
  deterministischen Testwerte.
- `CatalogProperties` und `CatalogDuckDbProperties` verlangen Quellart und
  die jeweils passende Location. Ungültige HTTP-Schemas und fehlende Werte
  schlagen beim Binding früh fehl.
- `CatalogImportConfiguration` baut Quellen über direkte `switch`-Ausdrücke;
  Legacy-Fallbacks und automatische Quellarterkennung existieren nicht mehr.
- `/actuator/health` liefert öffentlich nur den Gesamtstatus. Interne Counts,
  Zeiten und Reload-Informationen bleiben Indicator-/Admin-intern.

Testprotokoll:

| Command | Result |
|---|---|
| `./gradlew test --tests 'ch.so.agi.datenportal.config.*' --tests 'ch.so.agi.datenportal.admin.actuator.*' --tests 'ch.so.agi.datenportal.DatenportalApplicationTests' --tests 'ch.so.agi.datenportal.web.StaticAssetCachingMvcTest'` | PASS |
| `./gradlew test --tests 'ch.so.agi.datenportal.config.ConfigurationStartupTest'` | PASS, fünf fail-fast Binding-Fälle |
| `git diff --check` | PASS |
| `./gradlew clean check` | PASS, `BUILD SUCCESSFUL in 55s`; Vitest 124, TypeScript, Vite, Backend-Tests und Playwright |

Die Remediation-Phasen werden separat committed; die Commit-Zuordnung steht in
`docs/phase-status.md`.

## Code-Quality Remediation Phase 4 am 2026-08-11

Die vierte Remediation-Phase synchronisiert den UI-Vertrag mit der fachlichen
Datenabbildung:

- Karten zeigen `Struktur beschrieben` nur bei Attributen oder Datenmodell;
  Datensatz-/Datenreihe-Typen verwenden den gemeinsamen Info-/Ink-Badge-Stil.
- Serien-Root-Zeilen tragen ihr bestehendes Expand-Ziel als Datenattribut.
  Ein Zeilenklick löst nur den vorhandenen Plus-/Minus-Link aus; interaktive
  Nachfahren bleiben eigenständig.
- Qualitätskarten zeigen das Datenmodell als Text und verlinken den Report nur
  aus `qualitySummary.reportUrl`. Der sichtbare Name kommt aus dem URI-Pfad,
  mit `Validierungsreport` als Fallback.

Testprotokoll:

| Command | Result |
|---|---|
| `./gradlew test --tests 'ch.so.agi.datenportal.web.DetailPageVmFactoryTest' --tests 'ch.so.agi.datenportal.web.CatalogControllerMvcTest' --tests 'ch.so.agi.datenportal.web.CatalogDetailControllerMvcTest'` | PASS, 74 Tests |
| `./gradlew playwrightTest --tests 'ch.so.agi.datenportal.web.CatalogFiltersPlaywrightTest.seriesRootRowClickTogglesUsingDisclosureLinkAndIgnoresInteractiveChildren'` | PASS |
| `git diff --check` | PASS |
| `./gradlew clean check` | PASS, `BUILD SUCCESSFUL in 55s`; Vitest 124, TypeScript, Vite, Backend-Tests und Playwright |

Die Remediation-Phasen werden separat committed; die Commit-Zuordnung steht in
`docs/phase-status.md`.

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

- Frontend-Unit-Tests fuer DuckDB-Catalog-Attach, Schema-Explorer-Rendering, Tabellenname-/URL-Validierung und alte per-table Registrierungsergebnisse.
- Frontend-Unit-Tests fuer den Phase-3 Query Guard: read-only Statements, blockierte Mutations-/Systembefehle, Single-Statement-Verhalten und Limit-Wrapping.
- React-Komponententests fuer DuckDB-Initialisierung, Catalog-Attach, Fehleranzeige, Schema Explorer und Preview-Tabelle.
- MVC-/Header-Test fuer CSP `worker-src`, `wasm-unsafe-eval`, Explore-runtime-begrenztes `unsafe-eval` und `connect-src`.
- Java-Playwright-Test mit `/explore-fixtures/ch.so.oev_haltestellen.parquet`.

Fixture-/Runtime-Hinweise:

- Die Browser-Fixture liegt unter `src/test/resources/static/explore-fixtures/ch.so.oev_haltestellen.parquet`.
- Die Playwright-Suite nutzt `spec/fixtures/explore_fixture_catalog.duckdb`, eine kleine test-spezifische DuckDB-Datei mit `opendata.ch_so_oev_haltestellen`.
- Die DuckDB-Wasm Parquet-Erweiterung wird same-origin unter `/explore-extensions/v1.5.4/wasm_mvp/parquet.duckdb_extension.wasm` ausgeliefert.
- Der Playwright-Test erwartet, dass DuckDB den Catalog attached, das Schema `opendata` im Schema Explorer initial geschlossen bleibt, der aktuelle View nach manuellem Aufklappen markiert wird, direkt gegen die attached Catalog-View queried, kein globales `Bereit` rendert und Preview-Zeilen mit `Solothurn` und `Olten` zeigt.
- Die Schema-Explorer-Aktionsmenues werden in React- und Playwright-Tests nach Kopieraktionen, Outside-Click und Escape geschlossen.

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

- Der fruehere Beispiel- und History-Ausbau ist historisch dokumentiert, aber
  kein Bestandteil des aktuellen Testvertrags.

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

Ergebnis: PASS, `BUILD SUCCESSFUL in 20s`; umfasst DuckDB-Wasm-Parquet-Registrierung, Preview, Rezeptausfuehrung, CSV-Download und Diagramm-Rendering.

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

Ergebnis: PASS, `BUILD SUCCESSFUL in 24s`; umfasst erfolgreiche DuckDB-Wasm-Parquet-Registrierung, Browser-Konsolencheck, Tastatur-Tabnavigation, fehlende Parquet-Datei, SQL-Resultat, Diagramm, produktive SQL-Rezepte und mobile Overflow-Checks.

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

- Der fruehere Zukunftscode-Abschnitt wurde durch die Code-Quality-
  Remediation entfernt; der aktuelle Testvertrag prueft stattdessen Kontext V4
  und die weiterhin produktiven SQL-/R-Pfade.

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
