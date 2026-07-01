# Erkunden Architektur-Notizen

Status: Phase 6 code snippets and local history implemented

Dieses Dokument beschreibt den Ist-Zustand des Repositories, die Backend-Integration, die Frontend-Insel und die Architekturentscheidungen fuer die folgenden Erkunden-Phasen.

## Bestehender Anwendungskontext

- Package-Basis: `ch.so.agi.datenportal`
- Laufzeit: Java 25, Spring Boot 4.1.0, Gradle Groovy DSL.
- UI: serverseitig gerenderte JTE-Templates mit HTMX als Progressive Enhancement.
- Header und Breadcrumb: vendorte `so-web-components@0.1.10` unter `src/main/resources/static/vendor/so-web-components/0.1.10/`.
- Katalogquelle: PublishedCatalog-XTF/XML, standardmaessig `published_catalog_full_62_entries.xtf` vom Classpath.
- Katalogzustand: immutable `CatalogSnapshot`, atomar austauschbar ueber `CatalogService`.
- Suche: Lucene-Index im aktiven Snapshot.
- Statische Assets: CSS/JS unter `src/main/resources/static/css` und `src/main/resources/static/js`, mit Cache-Regeln in `StaticAssetCachingConfiguration`.

## Bestehende Routen

Aktuelle Katalog- und Detailrouten:

- `/` und `/datasets`: Katalogseite.
- `/datasets/{identifier}`: normaler Datensatz.
- `/datasets/{identifier}/structure-quality-origin`: Struktur, Qualitaet und Herkunft.
- `/datasets/{identifier}/usage`: Daten verwenden.
- `/series/{seriesIdentifier}`: Datenreihen-Uebersicht.
- `/series/{seriesIdentifier}/issues/current`: aktuelle Ausgabe.
- `/series/{seriesIdentifier}/issues/{issueIdentifier}`: spezifische Ausgabe.

Folgerung fuer Phase 1: Die neue Explore-Route muss mit der bestehenden Detailroute `/datasets/{identifier}` sauber zusammenarbeiten. Die Zielroute ist:

```text
GET /datasets/{datasetId}/explore
GET /datasets/{datasetId}/explore/context.json
```

Die Route verwendet `explore`, nicht `erkunden`. UI-Texte bleiben deutsch.

## Backend-Grenze ab Phase 1

Phase 1 legt die neue Funktion unter `ch.so.agi.datenportal.explore` an. Die Struktur:

```text
ch.so.agi.datenportal.explore
  ExplorePageController
  ExploreContextService
  ExploreTableService
  ExploreRecipeService
  ExploreCodeSnippetService
  ExploreProperties
  dto/view records
```

Controller bleiben duenn und lesen Daten ueber den bestehenden `CatalogService`. Templates erhalten vorbereitete ViewModels und keine Domain- oder Parserlogik.

Die oeffentlichen Phase-1-Routen sind:

```text
GET /datasets/{datasetId}/explore
GET /datasets/{datasetId}/explore/context.json
```

Nur normale `DatasetEntry`-Identifier sind gueltig. Datenreihen, Ausgaben und unbekannte Identifier laufen ueber das bestehende 404-Verhalten.

## Phase-1-Kontext

Der Backend-Kontext folgt `ExploreContextDto` Version `1`.

Phase-1-Quellen:

- Datensatz-Metadaten aus dem aktiven `CatalogSnapshot`.
- Parquet-Tabellen aus `DistributionFormat.PARQUET`.
- Attribute aus `CatalogEntryMetadata.attributes()`, wenn vorhanden.
- Generierte Startrezepte aus den bekannten Tabellen und Spaltenrollen.
- Statische Code-Snippets fuer DuckDB CLI, Python und R.

Wenn ein Datensatz keine Parquet-Distribution hat, rendert die Explore-Seite eine klare Nicht-verfuegbar-Meldung. Der JSON-Kontext bleibt gueltig, enthaelt aber leere `tables`, `recipes` und `codeSnippets`.

Der eingebettete JSON-Kontext wird mit einem kleinen projektlokalen Writer erzeugt und fuer das `application/json`-Script-Element gegen `</script>`-Sequenzen abgesichert. Das vermeidet eine neue JSON-Bibliotheksabhaengigkeit im Application Compile Classpath.

## Frontend-Insel ab Phase 2

Die interaktive Erkunden-Oberflaeche ist als isolierte React/Vite-Insel unter folgendem Pfad angelegt:

```text
src/main/frontend/explore/
```

Die Insel liest den eingebetteten JSON-Kontext aus `#datenportal-explore-context`, validiert ihn mit Zod und rendert in `#datenportal-explore-root`. Seit Phase 3 initialisiert sie DuckDB-Wasm im Browser, registriert Parquet-Distributionen als Views und laedt eine Standardvorschau. Seit Phase 4 stellt sie ein SQL-Labor mit generierten Rezepten, Editor, guard/limit-normalisierter Ausfuehrung, Resultattabelle und CSV-Export bereit. Seit Phase 5 erzeugt sie einfache Diagramme aus dem aktuellen SQL-Resultat. Seit Phase 6 rendert sie statische Codebeispiele und eine lokale Query-Historie.

Wichtige Dateien:

- `src/main/frontend/explore/src/main.tsx`
- `src/main/frontend/explore/src/app/ExploreApp.tsx`
- `src/main/frontend/explore/src/app/ExploreContextLoader.ts`
- `src/main/frontend/explore/src/app/ExploreContext.ts`
- `src/main/frontend/explore/src/sql/SqlLaboratory.tsx`
- `src/main/frontend/explore/src/recipes/RecipeList.tsx`
- `src/main/frontend/explore/src/results/ResultPanel.tsx`
- `src/main/frontend/explore/src/charts/ChartPanel.tsx`
- `src/main/frontend/explore/src/charts/chartInference.ts`
- `src/main/frontend/explore/src/code/CodeSnippetsPanel.tsx`
- `src/main/frontend/explore/src/sql/QueryHistory.ts`
- `src/main/frontend/explore/src/styles/explore.css`

Das Frontend nutzt npm, React 19, Vite 8, TypeScript, Vitest und Testing Library. SQLRooms DuckDB- und SQL-Editor-Pakete werden fuer DuckDB-Wasm und den SQL-Editor verwendet. Phase 5 verwendet `@sqlrooms/recharts@0.28.0` fuer Recharts-Primitive und SQLRooms-Chart-Wrappers; die Styles bleiben Datenportal-eigene CSS-Tokens. `@sqlrooms/ui` wird nicht direkt in Datenportal-Komponenten eingebunden, weil die Datenportal-UI eigene Design-Tokens nutzt.

## SQL-Labor ab Phase 4

- Backend-generierte `ExploreRecipeDto` werden gruppiert nach Tabelle angezeigt.
- Ein Klick auf ein Rezept laedt dessen SQL in den Editor; `Ausfuehren` oder `Ctrl/Cmd + Enter` startet die lokale DuckDB-Abfrage.
- Jede Abfrage laeuft durch `querySafety`: genau eine read-only-Anweisung, blockierte Mutations-/Systemkommandos und automatische `maxResultRows`-Begrenzung fuer `select`/`with`, sofern kein Top-Level-`limit` vorhanden ist.
- Query-Ausfuehrung verwendet den in Phase 3 initialisierten DuckDB-Connector mit `AbortSignal` fuer Timeout und Abbruch.
- Resultate werden als bewusst einfache, portalgestylte HTML-Tabelle gerendert. `@sqlrooms/data-table` bleibt installiert, wird aber fuer Phase 4 nicht als Primaerrenderer verwendet, weil die vorhandene Tabelle stabiler zu den Datenportal-Styles und Tests passt.
- CSV-Export erzeugt clientseitig eine Semikolon-getrennte CSV-Datei mit CRLF-Zeilenenden und exportiert nur die aktuell gerenderten Resultatzeilen.

## Diagramme ab Phase 5

- `ChartPanel` erhaelt ausschliesslich das aktuelle `QueryResultState`; es fuehrt keine eigene SQL-Abfrage aus.
- `chartInference` klassifiziert Resultatspalten aus den angezeigten Zeilen und schlaegt Balken-, Linien-, Punkt- oder Histogramm-Diagramme vor.
- Rezept-`preferredChart` wird nur verwendet, wenn das unveraenderte Rezept-SQL ausgefuehrt wurde. Geaendertes oder manuelles SQL verwendet immer Inferenz aus dem Resultat.
- Die UI bleibt bewusst klein: Diagrammtyp, X-/Y-Spalten und Diagramm-Zeilenlimit. Es gibt keinen Dashboard-Builder und keinen Spec-Editor.
- DuckDB `count(*)` liefert im Browser BigInt-Werte. Fuer Recharts werden nur die Diagrammzeilen in plain JavaScript-Zahlen/Strings normalisiert; Resultattabelle und CSV-Export behalten die originalen Resultatwerte.
- Vitest mockt `@sqlrooms/recharts`, weil das Paket wie `@sqlrooms/sql-editor` extensionless interne ESM-Imports enthaelt, die der Test-Runner nicht direkt aufloest. Typecheck, Vite-Build und Playwright pruefen den echten Produktionspfad.

## Codebeispiele und lokale Historie ab Phase 6

- `ExploreCodeSnippetService` generiert statische Beispiele fuer DuckDB CLI, Python mit DuckDB und R mit `duckdb`.
- Bei mehreren Parquet-Tabellen verwenden die Codebeispiele die primaere Tabelle; ohne markierte primaere Tabelle wird die erste Tabelle verwendet.
- Das Frontend rendert die Beispiele im Tab `Code` ueber `CodeSnippetsPanel` mit Kopieraktion. Es gibt keine Ausfuehren-Schaltflaeche und keine WebR-Laufzeit.
- `QueryHistory.ts` speichert erfolgreiche lokale SQL-Ausfuehrungen pro Datenthema unter `datenportal.explore.history.<datasetId>` in `localStorage`.
- Gespeichert werden SQL, Ausfuehrungszeitpunkt und optionale Metadaten wie Rezepttitel, Zeilenzahl und Dauer. Resultatzeilen werden nie gespeichert.
- Die Historie ist auf 20 Eintraege begrenzt, newest first, und ist eine Browser-Komfortfunktion. Fehler beim Lesen oder Schreiben von `localStorage` duerfen die SQL-Ausfuehrung nicht unterbrechen.

## Frontend-Asset-Build

Gradle besitzt eigene npm-Tasks:

```text
npmInstallExplore
npmBuildExplore
npmTestExplore
npmTypecheckExplore
```

`processResources` haengt von `npmBuildExplore` ab. Dadurch landen die Vite-Artefakte im normalen Spring-Boot-Classpath:

```text
build/generated-resources/explore/static/explore/assets/explore.js
build/generated-resources/explore/static/explore/assets/explore.css
```

Die oeffentlichen Pfade sind stabil und nicht fingerprinted:

```text
/explore/assets/explore.js
/explore/assets/explore.css
```

`StaticAssetCachingConfiguration` liefert `/explore/**` mit kurzer Cache-Zeit aus. Die JTE-Seite erhaelt die Pfade ueber `ExploreAssetLinks`.

## Datenvertrag

Der Backend-Kontext folgt dem `ExploreContextDto` aus der MVP-Spezifikation und ist seit Phase 1 implementiert.

Wichtige Leitplanken:

- Parquet-Distributionen aus dem bestehenden Katalogmodell sind die Grundlage.
- Tabellen- und Spaltennamen muessen fuer SQL sicher normalisiert werden.
- Wenn Strukturmetadaten fehlen, darf das Frontend spaeter die Laufzeitschema-Information ueber DuckDB ermitteln.
- Keine serverseitige SQL-Ausfuehrung.
- Clientseitige Query-Guards sind UX-Schutz und werden nicht als Sicherheitskontrolle beschrieben.

## Bekannte Risiken

- Route-Konflikte mit bestehenden `/datasets/{identifier}`-Detailseiten.
- CORS- und Range-Request-Verhalten echter Parquet-URLs.
- DuckDB-Wasm-Ladeverhalten in Safari und in restriktiven Browserumgebungen.
- CSP-Erweiterungen fuer Wasm und Worker, falls SQLRooms/DuckDB-Wasm sie ab Phase 3 benoetigt.
- SQLRooms transitive Peer-Warnings mit React 19, insbesondere `react-virtual` und `react-dnd-multi-backend`.
- Das installierte `@sqlrooms/sql-editor@0.28.0` exportiert `SqlMonacoEditor`, aber nicht den in neueren SQLRooms-Dokumenten beschriebenen `SqlCodeMirrorEditor`.
- Vitest kann die extensionless ESM-Internals von `@sqlrooms/sql-editor` und `@sqlrooms/recharts` nicht direkt aufloesen; die Tests mocken diese UI-Pakete und testen die Datenportal-Query- und Chartlogik separat.
