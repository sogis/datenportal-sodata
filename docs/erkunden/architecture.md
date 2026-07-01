# Erkunden Architektur-Notizen

Status: Phase 2 frontend island bootstrap implemented

Dieses Dokument beschreibt den Ist-Zustand des Repositories, die Backend-Integration, die Phase-2-Frontend-Insel und die Architekturentscheidungen fuer die folgenden Erkunden-Phasen.

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

Die Insel liest den eingebetteten JSON-Kontext aus `#datenportal-explore-context`, validiert ihn mit Zod und rendert in `#datenportal-explore-root`. Phase 2 zeigt nur Titel, Tabellenanzahl, Tabs und den Status `DuckDB wird vorbereitet`. Es gibt noch keine DuckDB-Initialisierung, keine Parquet-Registrierung und keine SQL-Ausfuehrung.

Wichtige Dateien:

- `src/main/frontend/explore/src/main.tsx`
- `src/main/frontend/explore/src/app/ExploreApp.tsx`
- `src/main/frontend/explore/src/app/ExploreContextLoader.ts`
- `src/main/frontend/explore/src/app/ExploreContext.ts`
- `src/main/frontend/explore/src/styles/explore.css`

Das Frontend nutzt npm, React 19, Vite 8, TypeScript, Vitest und Testing Library. SQLRooms-Kernpakete sind bereits als Abhaengigkeiten vorhanden, werden aber in Phase 2 noch nicht importiert. `@sqlrooms/ui` ist bewusst nicht eingebunden, weil es Tailwind-Peer-Dependencies einfuehrt und die Datenportal-UI eigene Design-Tokens nutzt.

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

## Bekannte Risiken

- Route-Konflikte mit bestehenden `/datasets/{identifier}`-Detailseiten.
- CORS- und Range-Request-Verhalten echter Parquet-URLs.
- DuckDB-Wasm-Ladeverhalten in Safari und in restriktiven Browserumgebungen.
- CSP-Erweiterungen fuer Wasm und Worker, falls SQLRooms/DuckDB-Wasm sie ab Phase 3 benoetigt.
- SQLRooms transitive Peer-Warnings mit React 19, insbesondere `react-virtual` und `react-dnd-multi-backend`.
