# Erkunden Architektur-Notizen

Status: Phase 0 repository findings

Dieses Dokument beschreibt den Ist-Zustand des Repositories und die Architekturentscheidungen fuer die folgenden Erkunden-Phasen. Es fuehrt noch kein Produktverhalten ein.

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

## Geplante Backend-Grenze

Phase 1 soll die neue Funktion unter `ch.so.agi.datenportal.explore` anlegen. Die geplante Struktur:

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

## Geplante Frontend-Grenze

Im Repository gibt es aktuell kein Node-, Vite-, React- oder TypeScript-Setup. Die bestehende Anwendung nutzt vendorte statische JS-Dateien.

Fuer die SQLRooms-Insel ist deshalb in Phase 2 eine neue, klar isolierte Frontend-Build-Strecke noetig. Sie soll sich in die bestehende Spring-Boot-Asset-Auslieferung einfuegen, statt die ganze UI in eine SPA umzubauen.

Offene Integrationsentscheidung fuer Phase 2:

- Wo die Frontend-Quellen liegen.
- Wie Vite-Artefakte in `src/main/resources/static` oder ein Build-Ausgabeverzeichnis kopiert werden.
- Wie fingerprinted Assets oder stabile Pfade mit den bestehenden Cache-Regeln zusammenspielen.

## Datenvertrag

Der Backend-Kontext folgt dem `ExploreContextDto` aus der MVP-Spezifikation. In Phase 0 wird der Vertrag nur dokumentiert, nicht implementiert.

Wichtige Leitplanken:

- Parquet-Distributionen aus dem bestehenden Katalogmodell sind die Grundlage.
- Tabellen- und Spaltennamen muessen fuer SQL sicher normalisiert werden.
- Wenn Strukturmetadaten fehlen, darf das Frontend spaeter die Laufzeitschema-Information ueber DuckDB ermitteln.
- Keine serverseitige SQL-Ausfuehrung.

## Bekannte Risiken

- Route-Konflikte mit bestehenden `/datasets/{identifier}`-Detailseiten.
- CORS- und Range-Request-Verhalten echter Parquet-URLs.
- DuckDB-Wasm-Ladeverhalten in Safari und in restriktiven Browserumgebungen.
- CSP-Erweiterungen fuer Modulskripte, Wasm und Worker, falls SQLRooms/DuckDB-Wasm sie benoetigt.
- Frontend-Build-Integration in ein bisher Java-zentriertes Gradle-Projekt.
