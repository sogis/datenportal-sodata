# Konfiguration

## Katalogquelle

Bevorzugte Konfiguration ab Phase 7:

```yaml
datenportal:
  catalog:
    source-type: classpath # classpath | http | file
    classpath-location: published_catalog_full_54_entries.xtf
    http-url: https://example.org/published_catalog.xtf
    http-connect-timeout: 5s
    http-read-timeout: 30s
    max-size: 50MB
```

`source-type=classpath` ist der lokale Standard. `source-type=http` verwendet `http-url` und lädt den vollständigen PublishedCatalog per HTTP GET. `source-type=file` bleibt für lokale Entwicklung und Tests erhalten.

Die alte Property bleibt vorerst gültig:

```yaml
datenportal:
  catalog:
    source: classpath:published_catalog_full_54_entries.xtf
```

Wenn `source-type` gesetzt ist, gewinnt die neue Konfiguration gegenüber `source`.

## Reload-Token

Der Admin-Reload ist nur aktiv, wenn ein Token gesetzt ist:

```yaml
datenportal:
  admin:
    reload-token: ${DATENPORTAL_ADMIN_RELOAD_TOKEN:}
```

Der Token darf nicht ins Repository geschrieben werden. Bei leerem Token liefern die Admin-Endpunkte `503 Service Unavailable`.

## HTTP-Quelle

Für HTTP-Quellen gelten:

- `http-connect-timeout` begrenzt den Verbindungsaufbau.
- `http-read-timeout` wird als Java-`HttpRequest.timeout(...)` umgesetzt und begrenzt den HTTP-Request.
- `max-size` begrenzt die eingelesenen Katalogbytes für alle Quellen.
- HTTP-Status ausserhalb `2xx` führen zu einem kontrollierten Reload-Fehler.
- Die Source-Beschreibung in Logs enthält Schema, Host, Port und Pfad, aber keine Userinfo, Query-Parameter oder Fragments.
