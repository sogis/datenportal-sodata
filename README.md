# Datenportal Webapp

Bootstrap der serverseitig gerenderten Datenportal-Webanwendung für den Kanton Solothurn.

## Phase 0 Status

Phase 0 liefert nur das technische Grundgerüst:

- Java 25
- Spring Boot 4.1.0
- Gradle Groovy DSL
- JTE-Templates
- JTE im Development-Mode für den Bootstrap
- lokal vendortes HTMX
- minimale Katalogseite auf `/` und `/datasets`
- semantischer Header-/Breadcrumb-Fallback

Noch nicht enthalten:

- XTF-/PublishedCatalog-Parsing
- Lucene-Suche
- Reload-Endpunkt
- Listenansicht, Kartenansicht oder Detailseiten
- finale `so-web-components`-Integration

## Voraussetzungen

- JDK 25

Der Build verwendet den Gradle Wrapper; eine lokale Gradle-Installation ist nicht erforderlich.

## Starten

```bash
./gradlew bootRun
```

Danach sind die minimalen Phase-0-Seiten erreichbar unter:

- `http://localhost:8080/`
- `http://localhost:8080/datasets`

## Tests Und Checks

```bash
./gradlew test
./gradlew clean check
```

## Relevante Dokumente

- `AGENTS.md`
- `datenportal_webapp_agent_spec_detailed_v5.md`
- `docs/ui-implementation-contract.md`
- `docs/architecture.md`
