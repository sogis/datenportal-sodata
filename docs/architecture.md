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
- `CatalogSnapshotLoader` baut aus Quelle, Parser und Validator den initialen `CatalogSnapshot`.
- `CatalogService` hält den aktuell aktiven `CatalogSnapshot` in-memory und bietet lesende Zugriffe für Web und spätere Reload-Phasen.
- `CatalogController` rendert die Katalog-Startseite auf `/` und `/datasets` mit Top-Level-Einträgen aus dem geladenen Snapshot.
- `CatalogPageVmFactory` mappt Domainobjekte in einfache, UI-orientierte ViewModels.
- `CatalogSnapshot` kapselt den veröffentlichten Read-Model-Stand inklusive sichtbarer Top-Level-Einträge und identifizierbarer Ausgaben.
- `DatasetEntry`, `DatasetSeriesEntry` und `DatasetIssueEntry` bilden normale Datensätze, Datenreihen und einzelne Ausgaben immutable ab.

## Startup-Ablauf

1. Spring bindet `datenportal.catalog.source`.
2. `CatalogSource` lädt die vollständige XTF-Datei in `CatalogBytes`.
3. `XtfPublishedCatalogParser` prüft Header-Modell, XML-Struktur und Pflichtfelder und mappt auf das bestehende Domain-Read-Model.
4. `CatalogValidator` prüft das resultierende `Catalog` fachlich.
5. `CatalogSnapshotLoader` erzeugt daraus den initialen `CatalogSnapshot`.
6. `CatalogService` veröffentlicht diesen Snapshot für Controller und spätere Phasen.

Fehlschläge beim Laden, Parsen oder Validieren sind fail-fast: Die Anwendung startet in Phase 2 nicht mit leerem Katalog.

## Bewusste Grenzen

Phase 2 enthält bewusst noch keine:

- HTTP-Quelle
- Lucene-Suche oder Filterlogik
- Reload-Endpunkte oder atomischen Snapshot/Index-Swap
- Persistenz zusätzlicher XTF-Metadaten wie Kontakt, Lizenz oder Temporal Coverage im Domain-Modell
- generische INTERLIS-Framework-Abstraktion

Diese Metadaten werden bereits gelesen und validiert, aber noch nicht in das Phase-1-Read-Model übernommen.
