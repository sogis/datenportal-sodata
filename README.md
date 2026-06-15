# Datenportal Webapp

Bootstrap der serverseitig gerenderten Datenportal-Webanwendung für den Kanton Solothurn.

## Phase 1 Status

Phase 1 ergänzt das technische Grundgerüst um ein erstes fachliches Read-Model:

- Java 25
- Spring Boot 4.1.0
- Gradle Groovy DSL
- JTE-Templates
- JTE im Development-Mode für den Bootstrap
- lokal vendortes HTMX
- immutable Katalog-Domainmodell unter `catalog.domain`
- `CatalogSnapshot` und lesender `CatalogService`
- statische Katalogquelle via `StaticCatalogFactory`
- Katalogseite auf `/` und `/datasets` mit echten Entwicklungsdaten
- normale Datensätze und Datenreihen mit CSV-/XLSX-/Parquet-Downloads
- semantischer Header-/Breadcrumb-Fallback

Noch nicht enthalten:

- XTF-/PublishedCatalog-Parsing
- Lucene-Suche
- Reload-Endpunkt
- Filter, HTMX-Fragmente, Kartenansicht oder Detailseiten
- finale `so-web-components`-Integration

## Voraussetzungen

- JDK 25

Der Build verwendet den Gradle Wrapper; eine lokale Gradle-Installation ist nicht erforderlich.

## Starten

```bash
./gradlew bootRun
```

Danach ist die Phase-1-Katalogseite erreichbar unter:

- `http://localhost:8080/`
- `http://localhost:8080/datasets`

## Tests Und Checks

```bash
./gradlew test
./gradlew clean check
```

## Entwicklungsdaten

Phase 1 verwendet bewusst keine XTF-Quelle. Die Anwendung startet mit einem deterministischen statischen Snapshot aus `ch.so.agi.datenportal.catalog.service.StaticCatalogFactory`.

Der Snapshot enthält:

- normale Datensätze
- Datenreihen mit einzelnen Ausgaben
- eine explizit aktuelle Ausgabe
- eine Datenreihe ohne explizite aktuelle Ausgabe, damit die Fallback-Logik auf die neueste Ausgabe getestet bleibt

## Relevante Dokumente

- `AGENTS.md`
- `datenportal_webapp_agent_spec_detailed_v5.md`
- `docs/ui-implementation-contract.md`
- `docs/architecture.md`
