# Datenportal Webapp

Serverseitig gerenderte Datenportal-Webanwendung für den Kanton Solothurn.

## Phase 6 Status

Phase 6 ergänzt die bestehende PublishedCatalog-, Such-, Listen-/Karten- und Detailseiten-Anwendung um die finale Header-/Breadcrumb-Integration via `so-web-components`:

- Java 25
- Spring Boot 4.1.0
- Gradle Groovy DSL
- JTE-Templates im Development-Mode
- lokal vendortes HTMX
- lokal vendortes `so-web-components@0.1.9`
- immutable Katalog-Domainmodell unter `catalog.domain`
- PublishedCatalog-XTF-Fixture als konfigurierbare Classpath-Katalogquelle
- Katalogseite auf `/` und `/datasets`
- serverseitige Lucene-Suche mit In-Memory-Index
- Reindexing beim Startup aus dem geladenen `CatalogSnapshot`
- Ranking mit starken Identifier-/Titel-Treffern und schwächeren Beschreibungstreffern
- Mehrfachfilter für Thema, Fachstelle/Amt, Publikationsdatum und Ressourcentyp
- aktive Filterchips mit Einzel-Entfernen-Links
- Listenansicht als Default und Kartenansicht über `view=cards`
- Datenreihen-Expansion über `expanded=<seriesId>`
- normale Datensätze und Datenreihen mit CSV-/XLSX-/Parquet-Downloads
- Detailseiten für Datensätze, Datenreihen und Ausgaben
- Header und Breadcrumb über `so-web-components`
- semantischer Header-/Breadcrumb-Fallback über `datenportal.web-components.enabled=false`

Noch nicht enthalten:

- Reload-Endpunkt
- Frontend-Pagination
- HTTP-XTF-Quelle
- Adminbereich

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

Die Katalogseite unterstützt in Phase 6:

- `q=<text>`
- `theme=<themeId>` wiederholt
- `office=<officeId>` wiederholt
- `modified=last30|last6months|thisYear|lastYear|older` wiederholt
- `resourceType=csv|xlsx|parquet` wiederholt
- `sort=modified-desc|title-asc|relevance`
- `view=list|cards`
- `expanded=<seriesId>` wiederholt

`page` und `size` sind service-seitig im Suchmodell vorbereitet, werden aber in Phase 6 noch nicht in der UI verwendet und nicht in Links erhalten.

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
  search:
    max-results: 500
    default-page-size: 20
    max-page-size: 100
  web-components:
    enabled: true
    version: "0.1.9"
    asset-base-path: "/vendor/so-web-components/0.1.9"
    use-cdn: false
```

Die Datei `spec/fixtures/published_catalog_full_54_entries.xtf` ist als zusätzliche Main-Resource auf dem Classpath eingebunden. Damit startet die Anwendung und auch `@SpringBootTest` standardmässig mit der Full Fixture.

Unterstützte Katalogquellen in Phase 6:

- `classpath:published_catalog_full_54_entries.xtf`
- `file:./pfad/zum/catalog.xtf`

Die Web-Component-Assets liegen unter:

```text
src/main/resources/static/vendor/so-web-components/0.1.9/
```

Produktionsnahe Deployments verwenden die vendored Assets. `datenportal.web-components.use-cdn=true` ist nur für lokale Tests vorgesehen.

## Relevante Dokumente

- `AGENTS.md`
- `datenportal_webapp_agent_spec_detailed_v5.md`
- `docs/ui-implementation-contract.md`
- `docs/architecture.md`
- `docs/web-components.md`
