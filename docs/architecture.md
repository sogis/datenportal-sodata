# Architektur

## Phase 2

Die Anwendung läuft ab Phase 2 weiterhin als serverseitig gerenderte Spring-Boot-Webanwendung mit JTE, lokalem HTMX-Asset und immutablem Katalog-Read-Model, lädt den Katalog aber nicht mehr aus einer hartcodierten `StaticCatalogFactory`.
Stattdessen wird beim Spring-Startup eine PublishedCatalog-XTF-Datei aus einer konfigurierten Quelle geladen, sicher geparst, validiert und als `CatalogSnapshot` veröffentlicht.

JTE läuft weiterhin bewusst im Development-Mode, damit das Projekt ohne zusätzliche Precompile-Strategie lokal lauffähig bleibt.

Aktuell materialisierte Pakete:

- `ch.so.agi.datenportal`
- `ch.so.agi.datenportal.config`
- `ch.so.agi.datenportal.catalog.domain`
- `ch.so.agi.datenportal.catalog.importxtf`
- `ch.so.agi.datenportal.catalog.service`
- `ch.so.agi.datenportal.web`
- `ch.so.agi.datenportal.web.view`

## Verantwortlichkeiten

- `CatalogImportConfiguration` verdrahtet `CatalogProperties`, `CatalogSource`, Parser und initialen Snapshot für den Spring-Startup.
- `CatalogProperties` steuert die Katalogquelle über `datenportal.catalog.source`.
- `ClasspathCatalogSource` und `FileCatalogSource` laden die rohen XTF-Bytes aus Classpath oder Dateisystem.
- `XtfPublishedCatalogParser` liest PublishedCatalog per namespace-aware StAX, ist prefix-unabhängig und erzwingt XML-Sicherheitsregeln ohne DTD oder externe Entities.
- `CatalogValidator` prüft das gemappte Domain-Modell auf fachliche Mindestregeln wie eindeutige Identifier, vorhandene Distributions und aktuelle Ausgaben.
- `CatalogSnapshotLoader` baut aus Quelle, Parser, Validator und Lucene-Index-Builder den initialen `CatalogSnapshot`.
- `CatalogService` hält den aktuell aktiven `CatalogSnapshot` in-memory, tauscht ihn atomar aus und schliesst beim Austausch den alten Suchindex.
- `CatalogController` rendert die Katalog-Startseite auf `/` und `/datasets` mit Top-Level-Einträgen aus dem geladenen Snapshot und Lucene-backed Suche.
- `HomePageVmFactory`, `FilterVmFactory` und `ResultsVmFactory` mappen Domainobjekte und Suchresultate in UI-orientierte ViewModels.
- `CatalogSnapshot` kapselt den veröffentlichten Read-Model-Stand inklusive sichtbarer Top-Level-Einträge, identifizierbarer Ausgaben und aktivem `CatalogSearchIndex`.
- `DatasetEntry`, `DatasetSeriesEntry` und `DatasetIssueEntry` bilden normale Datensätze, Datenreihen und einzelne Ausgaben immutable ab.

## Startup-Ablauf

1. Spring bindet `datenportal.catalog.source`.
2. `CatalogSource` lädt die vollständige XTF-Datei in `CatalogBytes`.
3. `XtfPublishedCatalogParser` prüft Header-Modell, XML-Struktur und Pflichtfelder und mappt auf das bestehende Domain-Read-Model.
4. `CatalogValidator` prüft das resultierende `Catalog` fachlich.
5. `CatalogSearchIndexBuilder` baut einen neuen In-Memory-Lucene-Index aus allen sichtbaren Top-Level-Einträgen.
6. `CatalogSnapshotLoader` erzeugt daraus den initialen `CatalogSnapshot` inklusive Suchindex.
7. `CatalogService` veröffentlicht diesen Snapshot für Controller und spätere Phasen.

Fehlschläge beim Laden, Parsen, Validieren oder Indexieren sind fail-fast: Die Anwendung startet nicht mit leerem Katalog oder teilweisem Suchindex.

## Bewusste Grenzen

Phase 2 enthält bewusst noch keine:

- HTTP-Quelle
- Lucene-Suche oder Filterlogik
- Reload-Endpunkte oder atomischen Snapshot/Index-Swap
- Persistenz zusätzlicher XTF-Metadaten wie Kontakt, Lizenz oder Temporal Coverage im Domain-Modell
- generische INTERLIS-Framework-Abstraktion

Diese Metadaten werden bereits gelesen und validiert, aber noch nicht in das Phase-1-Read-Model übernommen.

## Phase 3

Phase 3 ergänzt die Startseite um eine serverseitige Katalogabfrage ohne Lucene. Die Query-Parameter werden in `CatalogQueryParams` normalisiert und in eine fachliche `SearchQuery` überführt. `CatalogQueryService` sucht und filtert ausschliesslich im aktiven `CatalogSnapshot`; dadurch bleibt die Phase unabhängig von Indexaufbau und Reload.

Neu materialisierte Pakete und Komponenten:

- `ch.so.agi.datenportal.search` mit `SearchQuery`, `SearchFilters`, `ModifiedDateRange`, `SortMode`, `SearchResult`, `FacetService` und `CatalogQueryService`
- `CatalogQueryParams`, `ViewMode`, `HtmxRequest` und `CatalogUrlFactory` im Web-Layer
- UI-ViewModels für Filtergruppen, aktive Filterchips, Resultcontrols, Listenzeilen, Ausgabezeilen und Cards
- JTE-Fragmente für Resultbereich, Filterbar, aktive Chips, View Toggle, Tabellenansicht und Kartenansicht

Die Filter folgen der URL-Konvention des UI-Vertrags: Mehrfachwerte werden als wiederholte Query-Parameter übertragen. Die Such- und Filterlogik verwendet AND zwischen Kategorien und OR innerhalb einer Kategorie. Die Textsuche normalisiert Gross-/Kleinschreibung und Akzente und durchsucht Titel, Beschreibung, Keywords, Themen, Fachstelle/Amt sowie bei Datenreihen auch Ausgabe-Titel und Ausgabe-Labels.

HTMX ist nur progressive Enhancement. Normale GET-Requests liefern die vollständige Seite, HTMX-Requests mit `HX-Target=dataset-results` liefern nur `fragments/catalogResults.jte`.

Phase 3 enthält bewusst keine Frontend-Pagination. Alle passenden Top-Level-Einträge werden sortiert gerendert; manuell gesendete `page`- oder `size`-Parameter werden nicht modelliert und nicht in Links zurückgegeben.

## Phase 4

Phase 4 ersetzt die In-Memory-Textsuche durch einen Lucene-backed Suchindex, ohne die Phase-3-UI neu zu schneiden. `CatalogSearchService` ist der fachliche Einstiegspunkt für Query, Filter, Sortierung und service-seitige Pagination. Leere Suchanfragen verwenden weiterhin direkt `CatalogSnapshot.visibleEntries()`, damit die bestehende Sortier- und Filterlogik erhalten bleibt.

Neu materialisierte Suchkomponenten:

- `CatalogSearchIndex` als austauschbare Index-Abstraktion.
- `LuceneCatalogSearchIndex` mit `ByteBuffersDirectory`, `GermanAnalyzer`, `IndexReader` und `IndexSearcher`.
- `CatalogSearchIndexBuilder` für vollständiges Reindexing aus sichtbaren Top-Level-Einträgen.
- `CatalogDocumentMapper` für Lucene-Documents aus `DatasetEntry` und `DatasetSeriesEntry`.
- `SearchHit` und `PageRequest` als vorbereitete Ergebnis- und Paging-Objekte.
- `SearchProperties` unter `datenportal.search` mit `max-results`, `default-page-size` und `max-page-size`.

Indexierte Felder sind zentral in `CatalogSearchFields` dokumentiert. Wichtige Felder sind `entry_id`, `entry_type`, `identifier_exact`, `identifier_text`, `title`, `title_exact`, `description`, `keywords`, `theme_text`, `theme_exact`, `office_text`, `office_exact`, `formats`, `modified_date_epoch_day`, `open_data`, `structure_described`, `issue_years`, `issue_text` und `all_text`.

Ranking:

- Exakte Identifier erhalten den höchsten Boost.
- Titel und Identifier-Text ranken vor Keywords.
- Keywords und Issue-Metadaten ranken vor Thema/Amt.
- Beschreibung und `all_text` sind schwache Fallback-Felder.
- Bei `sort=title-asc` oder `sort=modified-desc` bestimmt weiterhin die fachliche Sortierung die Reihenfolge; Lucene bildet nur die Treffermenge.

Datenreihen werden als ein Top-Level-Dokument indexiert. Historische und aktuelle Ausgaben fliessen über `issue_text` und `issue_years` in die Suchbarkeit der Datenreihe ein, werden aber nicht als eigene Top-Level-Treffer zurückgegeben.

Reindexing ist für spätere Reload-Phasen vorbereitet: Für jeden neuen Katalogstand wird ein vollständiger neuer In-Memory-Index gebaut und erst mit dem neuen `CatalogSnapshot` veröffentlicht. Beim späteren `CatalogService.replaceSnapshot(...)` wird der alte Snapshot nach dem atomaren Austausch geschlossen; dadurch wird auch der alte Lucene-Index freigegeben. Ein Reload-Endpunkt ist in Phase 4 weiterhin nicht enthalten.

Phase 4 enthält nur service-seitige Pagination in `SearchQuery`/`SearchResult`. Die sichtbare Katalogseite rendert weiterhin alle Treffer und gibt keine `page`- oder `size`-Links aus.
