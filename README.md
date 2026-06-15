# Datenportal Webapp

Serverseitig gerenderte Datenportal-Webanwendung für den Kanton Solothurn.

## Phase 3 Status

Phase 3 ergänzt das technische Grundgerüst, das Domain-Read-Model und den PublishedCatalog-XTF-Import um eine funktionale Katalogseite:

- Java 25
- Spring Boot 4.1.0
- Gradle Groovy DSL
- JTE-Templates im Development-Mode
- lokal vendortes HTMX
- immutable Katalog-Domainmodell unter `catalog.domain`
- PublishedCatalog-XTF-Fixture als konfigurierbare Classpath-Katalogquelle
- Katalogseite auf `/` und `/datasets`
- serverseitige In-Memory-Suche ohne Lucene
- Mehrfachfilter für Thema, Fachstelle/Amt, Publikationsdatum und Ressourcentyp
- aktive Filterchips mit Einzel-Entfernen-Links
- Listenansicht als Default und Kartenansicht über `view=cards`
- Datenreihen-Expansion über `expanded=<seriesId>`
- normale Datensätze und Datenreihen mit CSV-/XLSX-/Parquet-Downloads
- semantischer Header-/Breadcrumb-Fallback

Noch nicht enthalten:

- Lucene-Suche
- Reload-Endpunkt
- Frontend-Pagination
- Detailseiten
- finale `so-web-components`-Integration

## Voraussetzungen

- JDK 25

Der Build verwendet den Gradle Wrapper; eine lokale Gradle-Installation ist nicht erforderlich.

## Starten

```bash
./gradlew bootRun
```

Danach ist die Katalogseite erreichbar unter:

- `http://localhost:8080/`
- `http://localhost:8080/datasets`

## Query-Parameter

Die Katalogseite unterstützt in Phase 3:

- `q=<text>`
- `theme=<themeId>` wiederholt
- `office=<officeId>` wiederholt
- `modified=last30|last6months|thisYear|lastYear|older` wiederholt
- `resourceType=csv|xlsx|parquet` wiederholt
- `sort=modified-desc|title-asc|relevance`
- `view=list|cards`
- `expanded=<seriesId>` wiederholt

`page` und `size` werden in Phase 3 nicht verwendet und nicht in Links erhalten.

## Tests Und Checks

```bash
./gradlew test
./gradlew clean check
```

## Entwicklungsdaten

Standardkonfiguration in `src/main/resources/application.yml`:

```yaml
datenportal:
  catalog:
    source: classpath:published_catalog_full_54_entries.xtf
```

Die Datei `spec/fixtures/published_catalog_full_54_entries.xtf` ist als zusätzliche Main-Resource auf dem Classpath eingebunden. Damit startet die Anwendung und auch `@SpringBootTest` standardmässig mit der Full Fixture.

Unterstützte Quellen in Phase 3:

- `classpath:published_catalog_full_54_entries.xtf`
- `file:./pfad/zum/catalog.xtf`

## Relevante Dokumente

- `AGENTS.md`
- `datenportal_webapp_agent_spec_detailed_v5.md`
- `docs/ui-implementation-contract.md`
- `docs/architecture.md`
