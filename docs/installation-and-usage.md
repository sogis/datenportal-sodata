# Installation und Nutzung

Dieses Dokument ist nur noch eine kompakte Orientierung. Für den aktuellen MVP-Stand gelten:

- `README.md` für Quickstart und Projektüberblick
- `docs/configuration.md` für alle Laufzeit-Properties
- `docs/operations.md` für Betrieb, Reload, Health/Info und Smoke-Tests
- `docs/web-components.md` für Header-/Breadcrumb-Assets

## Lokaler Start

```bash
./gradlew bootRun
```

Die Anwendung lädt standardmässig `published_catalog_full_62_entries.xtf` vom Classpath.

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
./gradlew bootRun
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
