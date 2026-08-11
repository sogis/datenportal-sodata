# Installation und Nutzung

Dieses Dokument ist nur noch eine kompakte Orientierung. Für den aktuellen MVP-Stand gelten:

- `README.md` für Quickstart und Projektüberblick
- `docs/configuration.md` für alle Laufzeit-Properties
- `docs/operations.md` für Betrieb, Reload, Health/Info und Smoke-Tests
- `docs/web-components.md` für Header-/Breadcrumb-Assets

## Lokaler Start

```bash
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
```

Das Profil `local` lädt ausdrücklich `published_catalog_full_62_entries.xtf`
und `catalog.duckdb` vom Classpath. `./gradlew bootRun` ohne Profil startet
nicht mit Demo-Daten, sondern schlägt wegen der fehlenden Katalogquelle fehl.

## Alternative Katalogquellen

Datei:

```bash
./gradlew bootRun --args='--datenportal.catalog.source-type=file --datenportal.catalog.file-location=./tmp/catalog.xtf'
```

HTTP:

```bash
./gradlew bootRun --args='--datenportal.catalog.source-type=http --datenportal.catalog.http-url=http://localhost:18080/catalog.xtf'
```

## Runtime Reload

```bash
export DATENPORTAL_ADMIN_RELOAD_TOKEN='change-me'
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
```

```bash
curl -X POST \
  -H "X-Reload-Token: ${DATENPORTAL_ADMIN_RELOAD_TOKEN}" \
  http://localhost:8080/admin/catalog/reload
```

## Checks

```bash
./gradlew test
./gradlew check
```

Der lokale Smoke-Test ist in `docs/operations.md` beschrieben.
