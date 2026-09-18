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
| 6. Productive SQL recipes and R laboratory | DONE | Generated SQL recipes and the productive WebR laboratory remain active. |
| 7. UX hardening and browser checks | DONE | Loading/error states, accessibility, mobile robustness and browser checks documented. |
| 8. Productive Explore context V4 | DONE | Prepared future UI, generic flags, manual JSON and local query history were removed; SQL recipes and R remain active. |
| SQL-Labor redesign | DONE | Full-width compact SQL workbench with schema cards, Monaco editor, red run button and compact result table. |
| SQL-Labor UI-Nachschliff | DONE | Editable Monaco editor, resizable panels, `Geladen` status, compact toolbar and removed legacy headers/link. |
| SQL-Labor UI-Nachschliff 2 | DONE | Stable Monaco layout, row-limit selector, CSV/XLSX/Parquet result export and cleaned splitter/schema/result visuals. |
| SQL-Labor Feinschliff 3 | DONE | Unified borders, DuckDB-native XLSX/Parquet exports, local Excel extension mirror and SQLRooms schema autocomplete wiring. |
| SQL-Labor Autocomplete-Fix | DONE | Removed the local duplicate completion provider, bundled Monaco Suggest locally and wired SQLRooms tableSchemas/getLatestSchemas. |
| SQL-Labor Status-Overlay | DONE | Removed the global ready badge, added centered loading/error overlay and renamed schema status to `Tabelle geladen`. |
| SQL-Labor Loading-Overlay Styling | DONE | Styled the loading overlay with dark backdrop, white shadowless card, red indeterminate progressbar and restored workbench top border. |
| SQL-Labor fuer Serienausgaben | DONE | Issue detail pages link to Explore; shared Explore controller methods resolve datasets and concrete series issues. |
| SQL-Labor Diagrammfarben | DONE | Charts use configured additional colors, no red option, stable multi-color palette and clearer axis labels. |
| DuckDB Catalog Direct Query | DONE | DuckDB-Wasm package upgraded, mirrored extensions moved to `v1.5.4`, catalog files regenerated with DuckDB 1.5.4 and query-side `memory.opendata` mirror removed. |
| SQL-Labor Query-Fehlerhandling | DONE | Nicht erreichbare Quelldateien zeigen eine neutrale Meldung im Resultatbereich und blockieren die Workbench nicht. |
| WebR-R-Labor | DONE | SQL-Resultate koennen als typisiertes `daten`-Dataframe ins R-Labor uebernommen werden; WebR runtime/packages werden same-origin ausgeliefert. |
| WebR CSP-Fix | DONE | Explore- und WebR-Runtime-Pfade erlauben `unsafe-eval` zusaetzlich zu `wasm-unsafe-eval`, normale Katalogseiten bleiben strenger. |
| R-Labor UI-Nachschliff | DONE | R-Ausgaben nutzen schlanke Trenner ohne sichtbare Paneltitel; Exporte sitzen in den Output-Kopfzeilen und werden erst bei echten Exportinhalten aktiv. |
| R-Labor UI-Nachschliff 2 | DONE | Konsole/Plot sind resizable, R startet ohne SQL-Result, und R-Rezepte priorisieren fachliche Messwertspalten mit Schweizer Anfuehrungszeichen. |
| Code-Quality Remediation Phase 1 | DONE | Explore-Lebenszyklen, Query-Cancel, WebR-Cleanup, Monaco-Ready-Zustand und deterministischer Autocomplete-Smoke sind abgesichert. |
| Code-Quality Remediation Phase 2 | DONE | Lucene-Suchfehler werden als 503 sichtbar, Java-Filter verarbeiten die vollständige Treffermenge und der Index meldet seine Dokumentzahl transparent. |
| Code-Quality Remediation Phase 3 | DONE | Produktionssichere Profile, explizite XTF-/DuckDB-Quellen, fail-fast Binding und öffentliche Health-Minimierung sind abgesichert. |
| Code-Quality Remediation Phase 4 | DONE | UI-Vertrag, Struktur-Badges, Serienzeilen-Interaktion und echte Qualitätsreport-Links sind abgesichert. |
| Code-Quality Remediation Phase 5 | DONE | ViewModels, Detailtemplates, Starter-Rezepte und weitere vorbereitete Server-UI sind konsolidiert. |
| Code-Quality Remediation Phase 6 | DONE | XTF, Lucene und DuckDB werden snapshotgebunden und atomar geladen, veröffentlicht und versioniert ausgeliefert. |
| Code-Quality Remediation Phase 7 | DONE | Nur MVP-DuckDB, produktive Chunks und WebR-Runtime ohne Source-Maps werden im Boot-JAR ausgeliefert. |
| R-Labor 64-bit-Typisierung | DONE | 64-bit Integer werden wertbasiert als `integer`/`numeric` uebernommen; `character` nur noch bei echten Praezisionsgrenzen. |
| R-Rezeptvorschlaege ausgebaut | DONE | R-Labor erkennt Jahre wertebasiert und bietet Balken-, Streu- und Mehrfachtrend-Rezepte analog zum SQL-Labor. |

## R-Rezeptvorschlaege Entry

Date: 2026-09-18

Goal:

- R-Rezepte aus dem SQL-Resultat wertebasiert erweitern, damit auch
  Jahrspalten wie `Jahrgang` Linien-/Trendrezepte erzeugen.
- Balken-, Streu- und Mehrfachtrend-Rezepte analog zu den SQL-Labor-
  Vorschlaegen anbieten; Zeitspalten nie als Messwert verwenden.
- Die Backend-Jahrerkennung fuer SQL-Rezepte um `jahrgang` und `*jahr`
  ergaenzen.

Changed files:

- `src/main/frontend/explore/src/analysis/resultColumnProfiles.ts` (neu):
  gemeinsame wertebasierte Jahr-/Datumspruefungen und Spaltenwert-Zugriff.
- `src/main/frontend/explore/src/charts/chartInference.ts`: nutzt die
  gemeinsamen Helfer statt eigener Wertpruefungen.
- `src/main/frontend/explore/src/webr/RRecipes.ts` und Test: neue Auswahl-
  und Rezeptlogik mit Kandidatenlimits; einfarbige Rezepte zeichnen in
  Dunkelblau `#104E8B`, der Mehrfachtrend nutzt die ersten Farben der
  `Mehrfarbig`-Palette aus `chartColors.ts`.
- `src/main/java/ch/so/agi/datenportal/explore/ExploreColumnRoleDetector.java`
  und Test: `jahrgang` und `*jahr` als `YEAR`.
- `docs/erkunden/architecture.md` und `docs/erkunden/testing.md`: Rollen- und
  Rezeptdokumentation aktualisiert.

Definition of Done:

- Jahrswerte 1800–2200 werden als Zeitachse erkannt und erzeugen Trend-,
  Mehrfachtrend- und ggf. Streurezepte; kein Histogramm ueber die Jahrspalte.
- Kategorien erzeugen Balkenrezepte; Zeit ohne Messwert erzeugt den
  Anzahl-Fallback.
- Einfarbige R-Plots nutzen Dunkelblau, Linien und Punkte eines Trends sind
  beide Dunkelblau; der Mehrfachtrend nutzt die `Mehrfarbig`-Palette.
- Vitest, Typecheck und der vollstaendige Gradle-Check sind erfolgreich.

## R-Labor 64-bit-Typisierung Entry

Date: 2026-09-18

Goal:

- 64-bit-Integer-Spalten aus dem SQL-Labor nur noch dann als `character`
  uebernehmen, wenn die tatsaechlichen Werte den exakten Wertebereich von R
  `integer` oder R `numeric` verlassen.
- Kleine Werte wie Jahrgaenge oder Anzahlwerte ohne manuelles `CAST` als
  `integer` beziehungsweise `numeric` in R verfuegbar machen und die
  pauschalen Typ-Hinweise im Datenbasis-Panel entfernen.
- Identifier-, DECIMAL-, Geometrie- und Binaerspalten bleiben unveraendert
  konservativ `character`.

Changed files:

- `src/main/frontend/explore/src/webr/DuckDbToWebRTypeMapper.ts` und Test:
  `mapDuckDbColumnToR` akzeptiert die tatsaechlichen Werte, 64-bit Integer
  werden anhand von int32- und double-exaktem Bereich auf `integer`, `numeric`
  oder `character` abgebildet; `normalizeDuckDbValueForR` uebertraegt BigInts
  fuer numerische R-Typen als Zahl.
- `src/main/frontend/explore/src/results/sqlResultSnapshot.ts` und Test:
  Der Snapshot uebergibt die Spaltenwerte an das Typ-Mapping.
- `docs/erkunden/architecture.md` und `docs/erkunden/testing.md`: Mapping-Regel
  und Testabdeckung dokumentiert.

Definition of Done:

- Alle nicht-leeren Werte einer 64-bit-Spalte im int32-Bereich ergeben
  `integer`, bis 2^53 − 1 `numeric`, darueber `character` mit Typ-Hinweis.
- Der bestehende Identifier-Fall `BIGINT` mit grossem Wert bleibt `character`.
- Vitest, Typecheck und der vollstaendige Gradle-Check sind erfolgreich.

## Code-Quality Remediation Phase 1 Entry

Date: 2026-08-11

Goal:

- DuckDB- und WebR-Runtimes bei Abbruch, Timeout, Kontextwechsel und Unmount
  kontrolliert beenden.
- Gemeinsame DuckDB-Verbindungen nie gleichzeitig von einer alten und einer
  neuen Query verwenden.
- React-States und WebR-Fortschrittsmeldungen veralteter Operationen nicht mehr
  publizieren.
- Monaco-Autocomplete über einen beobachtbaren Ready-Zustand deterministisch
  testen, ohne Retry-Schleife.

Changed files:

- `src/main/frontend/explore/src/app/ExploreApp.tsx` und zugehöriger Test:
  monotoner Initialisierungs-Generation, AbortController, verzögertes und
  idempotentes DuckDB-Destroy sowie Schutz später Schemaantworten.
- `src/main/frontend/explore/src/duckdb/attachCatalogDatabase.ts` und Test:
  optionales AbortSignal am Fetch.
- `src/main/frontend/explore/src/duckdb/executeDuckDbQuery.ts` und neuer Test:
  einmaliges `cancelSent()`, Warten auf das ursprüngliche Query-Ende und
  unterscheidbare Abort-/Normalfehler.
- `src/main/frontend/explore/src/sql/SqlLaboratory.tsx` und Test:
  kein paralleler Start, handle-identisches `finally`, Unmount-Cancel und
  sichtbare Cancel-/Timeout-Zustände.
- `src/main/frontend/explore/src/sql/SqlEditorField.tsx` und
  `src/test/java/ch/so/agi/datenportal/explore/ExploreIslandParquetPlaywrightTest.java`:
  fachliches `data-autocomplete-ready="true"` und einmaliges Öffnen der
  Monaco-Completion nach dem Ready-Zustand.
- `src/main/frontend/explore/src/webr/WebRRuntime.ts`, neuer Runtime-Test und
  RPanel-Test: geteilte Initialisierung, Retry nach Fehler, idempotentes Close,
  Timeout-Cleanup und keine späten Fortschrittsmeldungen.
- `src/main/frontend/explore/src/webr/RPanel.tsx` und Test:
  Operations-IDs, Unmount-Cleanup und Schutz alter Transfers.

No files were deleted. Deliberately unchanged are the catalog.duckdb creation
pipeline, data contents, backend publication behavior, and the later artifact
size and future-code scopes from Phases 6–8.

Definition of Done:

- Keine React-State-Updates oder WebR-Schritte aus veralteten Operationen nach
  Unmount oder Kontextwechsel.
- DuckDB `destroy()` und WebR `close()` sind in den beschriebenen Cleanup-Pfaden
  idempotent und kontrolliert.
- Eine aktive DuckDB-Query wird vor Abschluss von `cancel()` nicht verlassen;
  eine zweite Query wird verweigert.
- Autocomplete öffnet Completion genau einmal nach dem fachlichen Ready-Attribut.
- Alle fokussierten und vollständigen Tests sowie beide `clean check`-Läufe sind
  erfolgreich.

The implementation is committed separately per remediation phase; see
`docs/phase-status.md` for the authoritative commit map.

## Code-Quality Remediation Phase 1 verification

| Command | Result |
|---|---|
| `npm --prefix src/main/frontend/explore test -- --run src/app/ExploreApp.test.tsx src/duckdb/attachCatalogDatabase.test.ts src/duckdb/executeDuckDbQuery.test.ts src/sql/SqlLaboratory.test.tsx src/webr/RPanel.test.tsx src/webr/WebRRuntime.test.ts` | PASS, 6 files, 45 tests |
| `npm --prefix src/main/frontend/explore test -- --run` | PASS, 23 files, 124 tests |
| `npm --prefix src/main/frontend/explore run typecheck` | PASS, `tsc --noEmit` |
| `npm --prefix src/main/frontend/explore run build` | PASS, Vite build; existing large DuckDB-Wasm chunk warning remains |
| `git diff --check` | PASS |
| `./gradlew clean check` (first run) | PASS, `BUILD SUCCESSFUL in 1m 7s`; frontend 124 tests, typecheck, Vite, backend tests and normal Playwright included |
| `./gradlew playwrightTest --tests 'ch.so.agi.datenportal.explore.ExploreIslandParquetPlaywrightTest.rLaboratoryLoadsWebRFromSameOriginAndReceivesSqlResult' -Ddatenportal.playwright.webr=true` | PASS, real WebR browser smoke, `BUILD SUCCESSFUL in 9s` |
| `./gradlew clean check` (second run) | PASS, `BUILD SUCCESSFUL in 57s`; same complete gate |

## Code-Quality Remediation Phase 2 Entry

Date: 2026-08-11

Goal:

- Lucene-Fehler nicht mehr als künstliche Leermenge behandeln.
- Java-Filter auf die vollständige Lucene-Treffermenge anwenden, ohne eine
  versteckte Vorabgrenze von 500 Treffern.
- Snapshot- und Indexzugriffe ausschließlich unter dem bestehenden
  `withSnapshot()`-Read-Lock ausführen.
- Fehler für Vollseiten- und HTMX-Suchanfragen als verständlichen 503 sichtbar
  machen.

Changed files:

- `src/main/java/ch/so/agi/datenportal/search/CatalogSearchIndex.java`,
  `CatalogSearchException.java`, `EmptyCatalogSearchIndex.java` und
  `LuceneCatalogSearchIndex.java`: API auf `search`, `documentCount` und
  `close` reduziert; Lucene-Fehler werden gewrapped; geschlossene Indizes
  melden kontrolliert einen Fehler.
- `CatalogSearchService.java` und `SearchProperties.java`: vollständige
  Trefferverarbeitung vor den bestehenden Java-Filtern; die alte Suchgrenze
  wurde entfernt.
- `CatalogSearchIndexHealthIndicator.java`: Vergleich zwischen sichtbaren und
  indizierten Dokumenten mit `expectedDocuments`/`indexedDocuments`.
- `CatalogService.java`, `CatalogErrorControllerAdvice.java` und
  `ErrorPageVmFactory.java`: ungeschütztes `currentSnapshot()` entfernt und
  eine 503-Fehlerseite inklusive `HX-Refresh` ergänzt.
- `CatalogSearchFields.java` und `CatalogDocumentMapper.java`: ausschließlich
  von `buildQuery()` gelesene Indexfelder bleiben erhalten.
- Such-, Snapshot-, Reload-, Health-, MVC- und Starttests decken die neue API,
  mehr als 500 Treffer, geschlossene/fehlerhafte Indizes und HTMX ab.

No files were deleted. The catalog data, DuckDB artifact pipeline and the
later configuration, snapshot-publication and Explore-artifact scopes remain
unchanged.

Definition of Done:

- Keine alte Vorab-Suchgrenze mehr in produktivem Code, Konfiguration, Tests
  oder Maintainer-Dokumentation.
- Keine Suchfehlerbehandlung liefert eine künstliche Leermenge.
- `currentSnapshot()` existiert nicht mehr.
- Vollseiten- und HTMX-Suchfehler sind als 503 sichtbar.
- Die vollständigen fokussierten und globalen Qualitätsprüfungen sind grün.

The implementation is committed separately per remediation phase; see
`docs/phase-status.md` for the authoritative commit map.

## Code-Quality Remediation Phase 2 verification

| Command | Result |
|---|---|
| `./gradlew test --tests 'ch.so.agi.datenportal.search.*' --tests 'ch.so.agi.datenportal.catalog.service.CatalogServiceTest' --tests 'ch.so.agi.datenportal.catalog.service.CatalogReloadServiceTest' --tests 'ch.so.agi.datenportal.catalog.service.CatalogSnapshotLoaderTest' --tests 'ch.so.agi.datenportal.admin.actuator.CatalogSearchIndexHealthIndicatorTest' --tests 'ch.so.agi.datenportal.web.CatalogSearchErrorMvcTest' --tests 'ch.so.agi.datenportal.admin.actuator.CatalogActuatorMvcTest' --tests 'ch.so.agi.datenportal.DatenportalApplicationTests'` | PASS, 41 Tests |
| `git diff --check` | PASS |
| `./gradlew clean check` | PASS, `BUILD SUCCESSFUL in 57s`; Vitest 124, TypeScript, Vite, Backend-Tests und Playwright |

## Code-Quality Remediation Phase 3 Entry

Date: 2026-08-11

Goal:

- Die Basiskonfiguration ohne explizite Quelle fail-fast machen.
- Demo-Fixtures, localhost-Downloads und JTE-Development-Mode ausschließlich
  über das `local`- beziehungsweise `test`-Profil aktivieren.
- Öffentliche Actuator-Health-Antworten auf den Gesamtstatus begrenzen.

Changed files:

- `src/main/resources/application.yml`: produktionssichere technische
  Defaults, `gg.jte.development-mode: false`, vorcompilierte JTE-Templates,
  `show-details: never` und keine implizite Katalog-/DuckDB-Quelle.
- `src/main/resources/application-local.yml` und
  `src/main/resources/application-test.yml`: explizite 62-Einträge-XTF-,
  `catalog.duckdb`-, Download- und JTE-Profilwerte.
- `CatalogProperties`, `CatalogDuckDbProperties` und
  `CatalogImportConfiguration`: verpflichtende Quellart und passende
  Location, frühe HTTP-Schema-Prüfung und direkte `switch`-Erzeugung ohne
  Legacy-Fallbacks.
- `CatalogSnapshotHealthIndicator` und `CatalogActuatorMvcTest`: interne
  Betriebsdetails bleiben intern; öffentlich ist nur der Gesamtstatus sichtbar.
- Konfigurations-, Profil-, Health- und Indicator-Tests sowie README,
  Konfigurations-, Betriebs- und Architektur-Dokumentation aktualisiert.

No files were deleted. Die externe Erzeugung und das Laden des fertigen
DuckDB-Artefakts bleiben getrennt; die Katalogdaten selbst wurden nicht
verändert.

Definition of Done:

- Start ohne Produktionsquelle scheitert verständlich.
- Keine Fixture oder localhost-URL ist produktiver Fallback.
- `SPRING_PROFILES_ACTIVE=local ./gradlew bootRun` ist der lokale Einstieg.
- Öffentliche Health-Antwort enthält keine Komponenten, Details oder Pfade.
- Legacy-Quellkonfiguration ist entfernt und die vollständige Prüfung grün.

The implementation is committed separately per remediation phase; see
`docs/phase-status.md` for the authoritative commit map.

## Code-Quality Remediation Phase 3 verification

| Command | Result |
|---|---|
| `./gradlew test --tests 'ch.so.agi.datenportal.config.*' --tests 'ch.so.agi.datenportal.admin.actuator.*' --tests 'ch.so.agi.datenportal.DatenportalApplicationTests' --tests 'ch.so.agi.datenportal.web.StaticAssetCachingMvcTest'` | PASS |
| `./gradlew test --tests 'ch.so.agi.datenportal.config.ConfigurationStartupTest'` | PASS, fünf fail-fast Binding-Fälle |
| `git diff --check` | PASS |
| `./gradlew clean check` | PASS, `BUILD SUCCESSFUL in 55s`; Vitest 124, TypeScript, Vite, Backend-Tests und Playwright |

## Code-Quality Remediation Phase 4 Entry

Date: 2026-08-11

Goal:

- Karten-Badges aus `hasStructureInformation()` ableiten und die gemeinsame
  blaue Info-/Ink-Variante für `Datensatz` und `Datenreihe` dokumentieren.
- Serien-Root-Zeilen über das bestehende Plus-/Minus-Element per Zeilenklick
  bedienen, ohne Info-, Download- oder Formelemente abzufangen.
- Qualitäts-ViewModel und Qualitätskarte auf tatsächlich vorhandene
  Datenmodell- und `qualitySummary`-Informationen reduzieren.

Changed files:

- `ResultsVmFactory`, `entryRow.jte` und `catalog-filters.js`: fachliches
  Struktur-Mapping, `data-series-expand-href` und progressive Row-Click-
  Delegation ohne zusätzlichen Client-State.
- `QualityVm`, `DetailPageVmFactory` und `qualityCard.jte`: Modell als Text,
  reale Report-URL, Dateiname aus dem URI-Pfad und verständlicher Fallback;
  keine Platzhalter-Links `href="#"`.
- `AGENTS.md`, `datenportal-ui-contract`, `docs/ui-primitives.md` und
  `docs/ui-implementation-contract.md`: blauer Info-Badge-Vertrag und
  Row-Click-/Qualitätskarten-Regeln.
- MVC-, Factory- und Playwright-Tests für Badges, Report-URLs, Fallbacknamen,
  Zeilenklick sowie geschützte Info-/Download-Klicks.

No files were deleted in this phase. The implementation is committed
separately per remediation phase; see `docs/phase-status.md`.

Definition of Done:

- Karten setzen `structureDescribed` aus `hasStructureInformation()` und
  verwenden den dokumentierten Info-/Ink-Badge-Vertrag.
- Serienzeilen haben einen deklarativen Expand-Link; Row Click toggelt über
  denselben Link und ignoriert interaktive Nachfahren.
- Qualitätskarten zeigen nur reale Modell-/Reportdaten und keine `#`-Links.
- Die fokussierten Tests, `git diff --check` und `./gradlew clean check` sind
  grün.

## Code-Quality Remediation Phase 4 verification

| Command | Result |
|---|---|
| `./gradlew test --tests 'ch.so.agi.datenportal.web.DetailPageVmFactoryTest' --tests 'ch.so.agi.datenportal.web.CatalogControllerMvcTest' --tests 'ch.so.agi.datenportal.web.CatalogDetailControllerMvcTest'` | PASS, 74 Tests |
| `./gradlew playwrightTest --tests 'ch.so.agi.datenportal.web.CatalogFiltersPlaywrightTest.seriesRootRowClickTogglesUsingDisclosureLinkAndIgnoresInteractiveChildren'` | PASS |
| `git diff --check` | PASS |
| `./gradlew clean check` | PASS, `BUILD SUCCESSFUL in 55s`; Vitest 124, TypeScript, Vite, Backend-Tests und Playwright |

## Code-Quality Remediation Phase 5 verification

| Command | Result |
|---|---|
| `./gradlew test` | PASS, 271 Tests |
| `./gradlew playwrightTest --tests 'ch.so.agi.datenportal.web.CatalogFiltersPlaywrightTest'` | PASS, 24 Tests |
| `rg`-Prüfung auf entfernte ViewModels, Templates und Starter-Assets | PASS, keine Referenzen |
| `find src/main/java/ch/so/agi/datenportal/web/view -maxdepth 1 -name '*.java'` | PASS, exakt 34 Dateien |
| `git diff --check` | PASS |
| `./gradlew clean check` | PASS, `BUILD SUCCESSFUL in 57s`; Vitest 124, TypeScript, Vite, Backend-Tests und Playwright |

## Code-Quality Remediation Phase 6 Entry

Date: 2026-08-11

Goal:

- Fertige XTF- und DuckDB-Artefakte gemeinsam laden und als unveränderlichen
  Runtime-Stand mit Lucene atomar veröffentlichen.
- Artefakt-Requests ausschließlich aus dem aktiven Snapshot bedienen.
- Explore mit einer hash-versionierten DuckDB-URL an denselben Snapshot binden.
- Reload-Fehler und technische DuckDB-Prüfung kontrolliert dokumentieren.

Changed files:

- `CatalogSnapshot`, `CatalogSnapshotBuilder`, `CatalogSnapshotLoader`
  und `CatalogReloadService`: beide Quellen werden vor Parse/Index einmal
  geladen; der DuckDB-Header wird minimal geprüft; Snapshot-Wechsel bleiben
  atomar.
- `CatalogArtifactController`: Snapshot-only, InputStreamResource,
  Content-Length, SHA-256-ETag, 304, 409 sowie versioniertes Caching.
- `ExploreContextService` und `ExploreContextDto`: keine unversionierte
  Fallback-URL; Kontext und DuckDB stammen aus demselben Snapshot.
- Health-, Architektur-, Konfigurations- und Betriebsdokumentation sowie
  Loader-, Reload-, Artifact- und Explore-Tests.

No files were deleted. Die Anwendung erzeugt oder transformiert
`catalog.duckdb` nicht. Die fachliche Übereinstimmung von XTF und DuckDB
bleibt Verantwortung der externen Publishing-Pipeline.

Definition of Done:

- Keine Source wird pro Artifact-GET erneut gelesen.
- XTF, Lucene und DuckDB werden gemeinsam aktiviert; fehlerhafte Reloads
  lassen den vollständigen alten Stand aktiv.
- Explore verwendet `/catalog/catalog.duckdb?v=<sha256>`.
- ETag, 304, 409, Content-Length und Cache-Control sind getestet.
- DuckDB wird nur auf Mindestgröße und `DUCK`-Marker geprüft.
- `git diff --check` und `./gradlew clean check` sind grün.

## Code-Quality Remediation Phase 6 verification

| Command | Result |
|---|---|
| `./gradlew test --tests 'ch.so.agi.datenportal.catalog.service.*' --tests 'ch.so.agi.datenportal.web.CatalogArtifactControllerMvcTest' --tests 'ch.so.agi.datenportal.explore.ExploreContextServiceTest' --tests 'ch.so.agi.datenportal.explore.ExplorePageControllerMvcTest' --tests 'ch.so.agi.datenportal.admin.actuator.CatalogSnapshotHealthIndicatorTest'` | PASS |
| `git diff --check` | PASS |
| `./gradlew clean check` | PASS, `BUILD SUCCESSFUL in 1m 46s`; Vitest 124, TypeScript, Vite, Backend-Tests und Playwright |

## Code-Quality Remediation Phase 7 Entry

Date: 2026-08-11

Goal:

- EH-/COI-DuckDB-Bundles und produktive Source-Maps aus dem Boot-JAR entfernen.
- RPanel und RDataFramePanel erst nach Aktivierung des R-Labors laden.
- Die Abnahme direkt am erzeugten Boot-JAR durchführen.

Changed files:

- `duckdbBundles.ts` und Test: ausschließlich der `mvp`-Bundle, keine
  EH-/COI-URLs.
- `vite.config.ts` und `copyWebRRuntime`: Produktions-Source-Maps werden
  nicht erzeugt oder in das JAR kopiert.
- `ExploreApp.tsx` und Test: React.lazy/Suspense für beide R-Komponenten,
  Mount erst nach R-Tab-Aktivierung.

JAR-Abnahme mit demselben Task:

- Ausgangswert vor Phase 7: 228.979.643 Bytes.
- Ergebnis nach Phase 7: 174.656.030 Bytes.
- Reduktion: 54.323.613 Bytes, rund 51,8 MiB; Ergebnis rund 166,5 MiB.
- `jar tf` findet keine `duckdb-eh`, `duckdb-coi`, `coi.pthread`
  oder `.map`-Einträge; MVP und eigene R-Chunks sind vorhanden.

No files were deleted. Die R-Module bleiben produktiv und werden nur
nachgelagert geladen.

Definition of Done:

- Nur MVP-Wasm wird ausgeliefert.
- Keine produktiven Source-Maps sind im Boot-JAR.
- Ein eigener R-Chunk ist vorhanden; der initiale Explore-Chunk enthält keine
  R-Komponentenimplementierung.
- SQL- und R-Smokes, `git diff --check` und `./gradlew clean check` sind
  grün.

## Code-Quality Remediation Phase 7 verification

| Command | Result |
|---|---|
| `npm test` | PASS, 23 Dateien, 125 Tests |
| `npm run typecheck` | PASS |
| `./gradlew playwrightTest --tests 'ch.so.agi.datenportal.explore.ExploreIslandParquetPlaywrightTest.rLaboratoryLoadsWebRFromSameOriginAndReceivesSqlResult' -Ddatenportal.playwright.webr=true` | PASS, real WebR browser smoke, `BUILD SUCCESSFUL in 14s` |
| `./gradlew clean bootJar --no-daemon` | PASS |
| `jar tf build/libs/*.jar` Inhaltsprüfung | PASS, nur MVP-DuckDB, R-Chunks, keine EH/COI/Maps |
| `git diff --check` | PASS |
| `./gradlew clean check --no-daemon` | PASS, `BUILD SUCCESSFUL in 57s`; Vitest 125, TypeScript, Vite, Backend-Tests und Playwright |

## Code-Quality Remediation Phase 8 Entry

Date: 2026-08-11

Goal:

- Vorbereiteten Explore-Zukunftscode, generische Feature-Flags und lokale
  Query-History entfernen.
- Den gemeinsam ausgelieferten Explore-Kontext auf Version 4 mit direkten
  chartsEnabled-/webREnabled-Booleans vereinfachen.
- Jackson als Spring-verwalteten JSON-Serializer verwenden und den
  Produktionspfad für SQL-Rezepte und das R-Labor erhalten.

Changed files:

- Backend-Kontext: Snippet-/Flag-/manueller-JSON-Code entfernt,
  ExploreProperties reduziert, optionale DTO-Felder mit
  JsonInclude.NON_ABSENT versehen und Enumwerte mit JsonValue serialisiert.
- Frontend-Kontext: Zod akzeptiert ausschließlich V4; SQL und R lesen direkte
  Booleans.
- Entfernt wurden die vorbereiteten Zukunfts-, Codebeispiel- und
  History-Komponenten samt exklusiven Tests sowie der zugehörige
  Dependency-Guard.
- Dokumentation und Explore-MVC-/Unit-Tests beschreiben den produktiven
  V4-Vertrag.

Definition of Done:

- Kein ungenutzter Zukunfts-Slot, keine generische Zukunfts-Flag-Map und kein
  manueller JSON-Writer.
- V3 wird im Frontend-Schema kontrolliert abgelehnt.
- Quotes, Backslashes, Zeilenumbrüche, Unicode, Script-Sequenzen und leere
  Optionals sind getestet.
- Produktive SQL-Rezepte und das R-Labor bleiben vorhanden und getestet.

## Code-Quality Remediation Phase 8 verification

| Command | Result |
|---|---|
| ./gradlew test --tests 'ch.so.agi.datenportal.explore.*' --no-daemon | PASS |
| npm test | PASS, 20 Dateien, 114 Tests |
| npm run typecheck | PASS |
| rg gelöschte Explore-Typen/Flags/Komponenten in src und docs/erkunden | PASS, keine Referenzen |
| git diff --check | PASS |
| ./gradlew clean check --no-daemon | PASS, BUILD SUCCESSFUL in 54s; Vitest 114, TypeScript, Vite, Backend-Tests und Playwright |
| ./gradlew clean check -Ddatenportal.playwright.webr=true --no-daemon | PASS, BUILD SUCCESSFUL in 59s; WebR-aktivierter Gesamtcheck |
| ./gradlew playwrightTest --no-daemon --tests 'ch.so.agi.datenportal.explore.ExploreIslandParquetPlaywrightTest.rLaboratoryLoadsWebRFromSameOriginAndReceivesSqlResult' -Ddatenportal.playwright.webr=true | PASS, real WebR browser smoke, BUILD SUCCESSFUL in 13s |
| ./gradlew clean check --no-daemon (zweimal abschließend) | PASS, BUILD SUCCESSFUL in 55s und 55s; der fokussierte Autocomplete-Smoke lief nach einem transienten Timeout isoliert in 10s erfolgreich |
| ./gradlew clean bootJar --no-daemon | PASS, BUILD SUCCESSFUL in 15s; 174.644.557 Bytes, weiterhin unter 180 MiB und rund 54,3 MiB unter dem Phase-7-Ausgangswert |
| jar tf build/libs/*.jar | PASS, keine EH/COI/COI.pthread/.map-Einträge; MVP-DuckDB und eigene R-Chunks vorhanden |

## R-Labor UI-Nachschliff 2 Entry

Date: 2026-07-07

Scope:

- Added a resizable vertical handle between the R console and plot panes.
- Left-aligned the empty plot message with the rest of the R output messaging.
- Allowed the R-Labor tab to initialize WebR and run standalone R code without a prior SQL result transfer.
- Renamed the default recipe from `Start` to `Datenüberblick` when `daten` exists and to `R-Beispiel` without SQL data.
- Improved generated R plot recipes to prefer measure-like columns over years, IDs, numbers and codes; raw plot recipes use a 10'000-row plot limit.
- Rendered column names in recipe titles with Swiss quotes.

Test evidence:

| Command | Result |
|---|---|
| `npm --prefix src/main/frontend/explore test -- RPanel.test.tsx RRecipes.test.ts RDataFramePanel.test.tsx` | PASS, `Test Files 3 passed (3)`, `Tests 8 passed (8)` |
| `npm --prefix src/main/frontend/explore run typecheck` | PASS, `tsc --noEmit` without errors |
| `npm --prefix src/main/frontend/explore run test` | PASS, `Test Files 21 passed (21)`, `Tests 107 passed (107)` |
| `./gradlew clean check` | PASS, `BUILD SUCCESSFUL in 58s`; included Vitest, typecheck, Vite build, backend tests and Playwright |

## R-Labor UI-Nachschliff Entry

Date: 2026-07-07

Scope:

- Removed visible `Konsole` and `Plot` titles from the R output panes while keeping accessible output labels.
- Moved `Resultat exportieren` into the R console header and `Plot exportieren` into the plot header.
- Disabled R result export until a user R run produces a tabular result; the loaded SQL dataframe and placeholder text are no longer export-enabled.
- Removed broad grey R output gutters and double panel borders in favor of slim 1px separators matching the SQL-Labor visual model.
- Renamed Dataframe facts from `Zeilen`/`Spalten` to `Anzahl Zeilen`/`Anzahl Spalten`; the later columns list remains `Spalten`.

Test evidence:

| Command | Result |
|---|---|
| `npm --prefix src/main/frontend/explore test -- RPanel.test.tsx RDataFramePanel.test.tsx` | PASS, `Test Files 2 passed (2)`, `Tests 3 passed (3)` |
| `npm --prefix src/main/frontend/explore run typecheck` | PASS, `tsc --noEmit` without errors |
| `npm --prefix src/main/frontend/explore run test` | PASS, `Test Files 21 passed (21)`, `Tests 104 passed (104)` |
| `./gradlew clean check` | PASS, `BUILD SUCCESSFUL in 1m 4s`; included Vitest, typecheck, Vite build, backend tests and Playwright |

## WebR CSP-Fix Entry

Date: 2026-07-07

Scope:

- Fixed the browser CSP for WebR 0.6.0/Emscripten runtime startup by allowing `script-src 'self' 'wasm-unsafe-eval' 'unsafe-eval'` on Explore pages and same-origin Explore/WebR runtime asset paths only.
- Mirrored `PACKAGES.rds` next to `PACKAGES` and `PACKAGES.gz` so WebR/R does not log a same-origin package-index 404 before falling back.
- Kept normal catalog and detail pages on the stricter `script-src 'self' 'wasm-unsafe-eval'`.
- Updated MVC and configuration tests so the browser-visible headers cover both paths.
- Updated WebR troubleshooting/configuration docs to reflect the runtime requirement while keeping external WebR/CDN requests disallowed.

Test evidence:

| Command | Result |
|---|---|
| `./gradlew test --tests 'ch.so.agi.datenportal.config.SecurityHeadersConfigurationTest' --tests 'ch.so.agi.datenportal.web.StaticAssetCachingMvcTest' --tests 'ch.so.agi.datenportal.explore.ExplorePageControllerMvcTest'` | PASS, focused CSP/MVC/static-asset tests |
| `./gradlew mirrorWebRPackages` | PASS, mirrored 40 WebR packages including `PACKAGES.rds` |
| `./gradlew playwrightTest --tests 'ch.so.agi.datenportal.explore.ExploreIslandParquetPlaywrightTest.rLaboratoryLoadsWebRFromSameOriginAndReceivesSqlResult' -Ddatenportal.playwright.webr=true` | PASS, real WebR browser smoke transferred SQL result into R and executed R code |
| `./gradlew clean check` | PASS, `BUILD SUCCESSFUL in 58s`; included Vitest, typecheck, Vite build, backend tests and Playwright |

## WebR-R-Labor Entry

Date: 2026-07-06

Scope:

- Added Explore context version 3 with `rLaboratory`, default `datenportal.explore.webr-enabled=true`, WebR runtime URL `/webr/0.6.0/`, package repo `/webr-packages/`, curated R package list and row limits.
- Added WebR build assets: runtime copy from `webr@0.6.0`, same-origin R-4.6 package mirror under `/webr-packages/bin/emscripten/contrib/4.6/`, lockfile, precompression and cache coverage.
- Added `SQL-Labor`/`R-Labor` main tabs, SQL `Nach R übernehmen`, R dataframe side panel, R recipes, R execution/copy/export controls, console output and plot output.
- Added typed SQL-result snapshots and conservative DuckDB/Arrow-to-R mapping. In R, data is available as `daten`, `daten_schema` and `attr(daten, "duckdb_schema_json")`.
- Kept WebR V1 browser-only, PostMessage-based and without direct R DuckDB/Parquet access. R editor uses the robust textarea fallback; Monaco R highlighting remains a later isolated editor task.
- Kept the real WebR browser runtime smoke as opt-in with `-Ddatenportal.playwright.webr=true`; at initial R-Labor delivery it still exposed browser-runtime instability. The 2026-07-07 WebR CSP fix made the targeted opt-in smoke pass in Playwright-Chromium.

Test evidence:

| Command | Result |
|---|---|
| `npm --prefix src/main/frontend/explore run typecheck` | PASS, `tsc --noEmit` without errors |
| `npm --prefix src/main/frontend/explore test` | PASS, `Test Files 20 passed (20)`, `Tests 103 passed (103)` |
| `./gradlew mirrorWebRPackages --no-daemon` | PASS, mirrored 40 WebR packages to `bin/emscripten/contrib/4.6` |
| `./gradlew playwrightTest --no-daemon` | PASS, normal Playwright suite; real WebR runtime smoke skipped unless opted in |
| `./gradlew test --no-daemon` | PASS, `BUILD SUCCESSFUL in 15s` |

## SQL-Labor Query-Fehlerhandling Entry

Date: 2026-07-05

Scope:

- Added query-specific error classification for source, CORS, Range, HTTP, Parquet, DuckDB and SQL errors.
- Kept the Workbench ready when source-related schema refresh fails, falling back to the backend context columns.
- Rendered source-file query errors as neutral result-panel alerts with technical details behind `Technische Details`.
- Updated the broken-Parquet browser check to assert the result-panel error instead of a global runtime overlay.

Test evidence:

| Command | Result |
|---|---|
| `npm --prefix src/main/frontend/explore test` | PASS, `Test Files 15 passed (15)`, `Tests 91 passed (91)` |
| `npm --prefix src/main/frontend/explore run typecheck` | PASS, `tsc --noEmit` without errors |
| `./gradlew playwrightTest --tests 'ch.so.agi.datenportal.explore.ExploreIslandParquetPlaywrightTest.parquetLoadingFailureShowsReadableErrorWithoutDatasetLink'` | PASS, missing source file renders neutral query error |
| `./gradlew clean check` | PASS, `BUILD SUCCESSFUL in 1m 34s`; included Vitest, typecheck, Vite build, backend tests and Playwright |

## DuckDB Catalog Direct Query Entry

Date: 2026-07-04

Scope:

- Upgraded the Explore DuckDB-Wasm dependency resolution to the latest published `@duckdb/duckdb-wasm` package and mirrored official `v1.5.4/wasm_mvp` HTTPFS, Parquet and Excel extensions.
- Updated `tools/create_opendata_duckdb.py` to require `duckdb==1.5.4` and Python 3.10+.
- Regenerated `spec/fixtures/catalog.duckdb`, the dev seed `catalog.duckdb` and the small Playwright `explore_fixture_catalog.duckdb` with DuckDB 1.5.4.
- Removed the query-side `memory.opendata` mirror and old per-table Parquet registration helpers. Browser SQL now runs directly after `ATTACH 'catalog.duckdb' AS "catalog" (READ_ONLY)` and `USE "catalog"."opendata"`.
- Updated configuration, architecture, troubleshooting and testing documentation for direct Catalog queries and cross-Parquet joins.

Test evidence:

| Command | Result |
|---|---|
| `npm --prefix src/main/frontend/explore test` | PASS, `Test Files 15 passed (15)`, `Tests 83 passed (83)` |
| `npm --prefix src/main/frontend/explore run typecheck` | PASS, `tsc --noEmit` without errors |
| `./gradlew playwrightTest --tests 'ch.so.agi.datenportal.explore.ExploreIslandParquetPlaywrightTest'` | PASS, direct `catalog.duckdb` queries, exports and charts covered |
| `./gradlew clean check` | PASS, `BUILD SUCCESSFUL in 53s`; included Vitest, typecheck, Vite build, backend tests and Playwright |

## SQL-Labor Diagrammfarben Entry

Date: 2026-07-04

Scope:

- Added a shared chart color helper with the configured additional single colors and a non-red multi-color palette.
- Replaced indirect Recharts `var(--color-value)` usage with explicit SVG `fill`/`stroke` values for bar, line, scatter and histogram charts.
- Added a `Farbe` control to `ChartPanel`, honored allowed `preferredChart.color` values and limited `Farben neu` to multi-color chart types.
- Applied stable multi-color cells to bars, histograms, Pie and Donut while keeping line/scatter single-color.
- Clarified X/Y labels and allowed numeric X columns for line charts.
- Documented that charts visualize only the current SQL result and do not synthesize missing categories such as municipalities.

Test evidence:

| Command | Result |
|---|---|
| `npm --prefix src/main/frontend/explore test` | PASS, `Test Files 16 passed (16)`, `Tests 91 passed (91)` |
| `npm --prefix src/main/frontend/explore run typecheck` | PASS, `tsc --noEmit` without errors |
| `npm --prefix src/main/frontend/explore run build` | PASS, Vite built Explore assets; expected large DuckDB-Wasm chunk warning remains |
| `./gradlew playwrightTest --tests 'ch.so.agi.datenportal.explore.*'` | PASS, `BUILD SUCCESSFUL in 37s`; includes non-black bar fills, multi-color bars and Pie/Donut colors |
| `./gradlew clean check` | PASS, `BUILD SUCCESSFUL in 58s`; included Vitest, typecheck, Vite build, backend tests and Playwright |

## SQL-Labor fuer Serienausgaben Entry

Date: 2026-07-04

Scope:

- Added Explore URLs, breadcrumbs and page chrome for current and historical series issues.
- Reused the existing `ExplorePageController` with one page handler, one JSON handler and a shared target resolver for dataset and issue routes.
- Generalized Explore context/table generation from normal datasets to concrete dataset issues while keeping series root entries non-explorable.
- Activated the `Erkunden` action on Open-Data issue detail pages; non-open issue pages keep the lock state.
- Updated MVC, unit and Playwright coverage for issue links, route validation, JSON context shape and browser-local DuckDB execution on an issue route.

Test evidence:

| Command | Result |
|---|---|
| `npm --prefix src/main/frontend/explore test` | PASS, `Test Files 16 passed (16)`, `Tests 88 passed (88)` |
| `npm --prefix src/main/frontend/explore run typecheck` | PASS, `tsc --noEmit` without errors |
| `./gradlew test --tests 'ch.so.agi.datenportal.explore.ExploreContextServiceTest' --tests 'ch.so.agi.datenportal.web.DetailPageVmFactoryTest' --tests 'ch.so.agi.datenportal.web.CatalogUrlFactoryTest' --tests 'ch.so.agi.datenportal.web.BreadcrumbFactoryTest'` | PASS, `BUILD SUCCESSFUL in 6s` |
| `./gradlew test --tests 'ch.so.agi.datenportal.explore.ExplorePageControllerMvcTest' --tests 'ch.so.agi.datenportal.web.CatalogDetailControllerMvcTest'` | PASS, `BUILD SUCCESSFUL in 4s` |
| `./gradlew test --tests 'ch.so.agi.datenportal.explore.*' --tests 'ch.so.agi.datenportal.web.*'` | PASS, `BUILD SUCCESSFUL in 5s` |
| `./gradlew playwrightTest --tests 'ch.so.agi.datenportal.explore.*'` | PASS, `BUILD SUCCESSFUL in 29s` |
| `./gradlew clean check` | PASS, `BUILD SUCCESSFUL in 54s`; included Vitest, typecheck, Vite build, backend tests and Playwright |

## SQL-Labor Loading-Overlay Styling Entry

Date: 2026-07-03

Scope:

- Styled the runtime loading overlay as a white, shadowless Frutiger card on a darkened backdrop.
- Added an accessible indeterminate progressbar for DuckDB initialization and Parquet registration; error overlays remain alerts without a progressbar.
- Restored the top workbench border directly on `.dp-explore-workbench` without reintroducing the removed global status topbar.
- Updated Vitest and Playwright coverage for the new loading copy, progressbar semantics, error overlay styling and the 1px workbench border.

Test evidence:

| Command | Result |
|---|---|
| `npm --prefix src/main/frontend/explore test` | PASS, `Test Files 15 passed (15)`, `Tests 71 passed (71)` |
| `npm --prefix src/main/frontend/explore run typecheck` | PASS, `tsc --noEmit` without errors |
| `npm --prefix src/main/frontend/explore run build` | PASS, Vite built Explore assets; expected large DuckDB-Wasm chunk warning remains |
| `./gradlew test --tests 'ch.so.agi.datenportal.explore.*'` | PASS, `BUILD SUCCESSFUL in 10s` |
| `./gradlew playwrightTest --tests 'ch.so.agi.datenportal.explore.*'` | PASS after tightening the `Bereit` assertion to exact text, `BUILD SUCCESSFUL in 27s` |
| `./gradlew clean check` | PASS, `BUILD SUCCESSFUL in 49s`; included Vitest, typecheck, Vite build, backend tests and Playwright |

## SQL-Labor Status-Overlay Entry

Date: 2026-07-03

Scope:

- Removed the persistent global `Bereit` badge and the workbench topbar so the SQL lab uses the full vertical space after loading.
- Added a centered non-layouting runtime overlay for DuckDB initialization, Parquet registration and runtime errors.
- Renamed the per-table schema badge from `Geladen` to `Tabelle geladen` and aligned its radius with the shared small badge radius.
- Updated Vitest and Playwright coverage to wait for the table-level ready signal, assert that `Bereit` is absent and verify that the workbench body starts at the top without reserved topbar height.

Test evidence:

| Command | Result |
|---|---|
| `npm --prefix src/main/frontend/explore test` | PASS, `Test Files 15 passed (15)`, `Tests 71 passed (71)` |
| `npm --prefix src/main/frontend/explore run typecheck` | PASS, `tsc --noEmit` without errors |
| `npm --prefix src/main/frontend/explore run build` | PASS, Vite built Explore assets; expected large DuckDB-Wasm chunk warning remains |
| `./gradlew test --tests 'ch.so.agi.datenportal.explore.*'` | PASS, `BUILD SUCCESSFUL in 10s` |
| `./gradlew playwrightTest --tests 'ch.so.agi.datenportal.explore.*'` | PASS, `BUILD SUCCESSFUL in 27s` |
| `./gradlew clean check` | PASS, `BUILD SUCCESSFUL in 50s`; included Vitest, typecheck, Vite build, backend tests and Playwright |

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
- Superseded by the 2026-07-04 DuckDB Catalog Direct Query entry: the current mirror is `v1.5.4/wasm_mvp`.
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
- Beispiel- und History-Pfade bleiben ausserhalb des produktiven Vertrags.
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

- Beispiel- und History-Pfade bleiben ausserhalb des produktiven Vertrags.
- Broader UX hardening, manual browser matrix checks and real external `https://data.so.ch` Parquet smoke tests remain deferred to Phase 7.
- Vite still reports expected large DuckDB-Wasm bundle warnings.

## Phase 6 Entry

Date: 2026-07-01

Branch: `main`

Scope:

- Ein früherer Beispiel- und History-Ausbau ist historisch dokumentiert und
  wurde in der Code-Quality-Remediation entfernt.
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

## SQL-Labor Beispielabfragen Dokumentation

Date: 2026-07-04

Scope:

- Documented the generated SQL-Labor recipe categories, including `preview`, `profile`, `quality`, `category`, `numeric`, `time` and the currently unused `custom` category.
- Documented recipe limits for null profiles, category grouping, numeric summaries and time-series recipes.
- Documented the role-detection rules for category, measure, year and date columns.
- Documented how recipe `preferredChart` interacts with chart inference for changed or manual SQL.
- Added the concrete `ch_so_wasserqualitaet_grundwasser` dropdown state with category, numeric and time SQL examples.

Test evidence:

| Command | Result |
|---|---|
| Markdown/diff review | PASS, documentation-only change; no code or runtime behavior changed |
| `git diff --check` | PASS |

## Phase 8 Entry

Date: 2026-07-01

Branch: `main`

Scope:

- Ein früherer Zukunftscode-Ausbau wurde in der Code-Quality-Remediation
  vollständig entfernt.
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

## SQL-Labor Diagramme Entry

Date: 2026-07-03

Branch: `main`

Scope:

- Added a compact example-query selector to the SQL toolbar. Selecting a recipe loads SQL into Monaco but does not execute it.
- Added a local result-view toggle for `Tabelle` and `Diagramm` without restoring the old top-level `Vorschau` / `SQL-Labor` / `Diagramm` / `Code` tabs.
- Reconnected `ChartPanel` to the current `QueryResultState`, keeping SQL as the source of truth and avoiding server-side SQL execution.
- Kept `@sqlrooms/recharts@0.28.0` for chart rendering and extended chart types to `Balken`, `Linie`, `Punkte`, `Histogramm`, `Pie` and `Donut`.
- Added stable pseudo-random segment colors for Pie/Donut plus the compact `Farben neu` action.
- Updated frontend, backend DTO enum, Playwright coverage and Erkunden documentation.

Implementation notes:

- Pie and Donut are manual category-plus-value chart types. Automatic inference still prefers bar/line/scatter/histogram.
- Histogram remains the only chart type with data binning in the chart layer; other calculations remain SQL responsibility.
- The first full `./gradlew clean check` rerun had a timing-sensitive Monaco suggestion failure and the new chart smoke used an unnecessarily fixture-specific SQL query. Focused reruns passed, the chart smoke now uses a constant SQL result, and the final full run passed.
- The Vite build still reports expected large DuckDB-Wasm chunk warnings.

Test evidence:

| Command | Result |
|---|---|
| `npm --prefix src/main/frontend/explore test` | PASS, `Test Files 16 passed (16)`, `Tests 83 passed (83)`, duration `1.99s` |
| `npm --prefix src/main/frontend/explore run typecheck` | PASS, `tsc --noEmit` without errors |
| `npm --prefix src/main/frontend/explore run build` | PASS, Vite built Explore, Monaco and DuckDB-Wasm assets under `/explore/assets/`; expected large chunk warning remains |
| `./gradlew test --tests 'ch.so.agi.datenportal.explore.*'` | PASS, `BUILD SUCCESSFUL in 11s` |
| `./gradlew playwrightTest --tests 'ch.so.agi.datenportal.explore.*'` | PASS, `BUILD SUCCESSFUL in 27s`; includes same-origin Parquet, Monaco, SQL result, export, result chart toggle and Pie/Donut color smoke |
| `./gradlew clean check` | PASS, `BUILD SUCCESSFUL in 51s`; included Vitest, TypeScript, Vite build, precompression, backend tests and Playwright |

Known limitations:

- Real `data.so.ch` CORS, byte Range and Safari/Firefox runtime behavior still need an operator/manual smoke test from a network where `data.so.ch` resolves.


## SQL-Labor: mehrere Y-Attribute (2026-09-18)

Implementiert: kompakte Checkbox-Auswahl für Linie/Balken/Punkte, gemerkte
Mehrfachauswahl bei Typwechseln, automatische Mehrfarbenwahl, stabile
Reihenfarben, gemeinsame Legende, sichere interne Spaltenschlüssel und
fehlende Messwerte als Lücken. Der PNG-Export misst die bereinigte Darstellung
inklusive vollständiger Legende. Pie/Donut/Histogramm bleiben einspaltig;
Kontext V4, Backend und Dev-Stack-Verträge bleiben unverändert.

Eingabestand: lokales `main` auf `571fd87` im sodata-Working-Tree
(Schwesterpfad `../datenportal-sodata` aus dem Dev-Stack).
Vorhandene uncommittete Änderungen an RDataFramePanel, dessen Test sowie
R-Labor-Abschnitten in CSS und Dokumentation wurden erhalten. Verwendet
wurden lokale Testfixtures, keine Änderungen an anderen Repositories.

Verifikation mit JDK 25 über `JAVA_HOME`:

- Frontend: 22 Testdateien, 129 Tests erfolgreich; TypeScript und Vite erfolgreich.
- Backend: 311 Tests erfolgreich.
- Playwright: 48 Tests, davon 47 erfolgreich und der optionale echte WebR-Smoke
  übersprungen. Die neuen Tests prüfen Desktop und schmale Fenster, drei
  Diagrammtypen, Farben, Scatter-Tooltip, Panel-Resize, Popup und PNG-Export.
- `./gradlew clean check` wurde ausgeführt; erste Läufe scheiterten an
  beschädigten SQL-Testeingaben bei zeichenweisem Playwright-Tippen.
  Nach Umstellung der betroffenen Testvorlagen auf `insertText` lief der
  abschliessende vollständige `./gradlew check` erfolgreich durch (`1m 35s`).
- `git diff --check` erfolgreich. Erwartete Vite-Warnung zu grossen Bundles bleibt.
- PNGs und Dropdown-Screenshots aus den Browserprüfungen visuell kontrolliert;
  generierte Bilder liegen ausschliesslich unter dem ignorierten `build/`.

Bestehende Einschränkung ausserhalb dieser Änderung: Der Wechsel über den
Desktop-/Mobil-Breakpoint baut das SQL-Labor neu auf und verwirft dessen
lokalen Zustand. Deshalb werden beide Layouts separat ab Start und
Grössenänderungen innerhalb des jeweiligen Layouts geprüft.
