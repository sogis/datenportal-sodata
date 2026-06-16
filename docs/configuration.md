# Konfiguration

Diese Datei beschreibt die produktionsnahen Laufzeit-Properties des Datenportals. Secrets werden immer über Umgebung oder externe Konfiguration gesetzt und nie ins Repository geschrieben.

## Katalogquelle

Bevorzugte Konfiguration:

```yaml
datenportal:
  catalog:
    source-type: classpath # classpath | http | file
    classpath-location: published_catalog_full_54_entries.xtf
    file-location: ./config/catalog.xtf
    http-url: https://example.org/published_catalog.xtf
    http-connect-timeout: 5s
    http-read-timeout: 30s
    max-size: 50MB
```

`source-type=classpath` ist der lokale Standard. `source-type=http` lädt die vollständige PublishedCatalog-XTF/XML-Datei per HTTP GET. `source-type=file` ist für lokale Entwicklung und Tests vorgesehen.

Die Legacy-Property bleibt vorerst gültig:

```yaml
datenportal:
  catalog:
    source: classpath:published_catalog_full_54_entries.xtf
```

Wenn `source-type` gesetzt ist, gewinnt die neue Konfiguration gegenüber `source`.

HTTP-Quellen:

- `http-connect-timeout` begrenzt den Verbindungsaufbau.
- `http-read-timeout` begrenzt den vollständigen HTTP-Request.
- `max-size` begrenzt die eingelesenen Katalogbytes für alle Quellen.
- HTTP-Status ausserhalb `2xx` führen zu einem kontrollierten Reload-Fehler.
- Die Source-Beschreibung enthält Schema, Host, Port und Pfad, aber keine Userinfo, Query-Parameter oder Fragments.

## Reload

Der geschützte Runtime-Reload ist nur aktiv, wenn ein Token gesetzt ist:

```yaml
datenportal:
  admin:
    reload-token: ${DATENPORTAL_ADMIN_RELOAD_TOKEN:}
```

Admin-Endpunkte:

```http
POST /admin/catalog/reload
GET /admin/catalog/status
X-Reload-Token: <token>
```

Regeln:

- `DATENPORTAL_ADMIN_RELOAD_TOKEN` darf nicht leer sein, wenn Reload/Status genutzt werden sollen.
- Bei leerem Token liefern die Admin-Endpunkte `503 Service Unavailable`.
- Fehlendes oder falsches `X-Reload-Token` liefert `401 Unauthorized`.
- Der Token wird constant-time verglichen und nicht geloggt.
- Fehlerhafte Reloads lassen den aktiven `CatalogSnapshot` und Lucene-Index unverändert.

## Suche

```yaml
datenportal:
  search:
    max-results: 500
    default-page-size: 20
    max-page-size: 100
```

Die UI verwendet aktuell keine sichtbare Pagination, die Suchschicht normalisiert Page-Requests aber bereits service-seitig.

## Web Components

```yaml
datenportal:
  web-components:
    enabled: true
    version: "0.1.10"
    asset-base-path: "/vendor/so-web-components/0.1.10"
    use-cdn: false
```

- `enabled=true` rendert `<so-header>` und `<so-breadcrumb>`.
- `enabled=false` rendert semantische JTE-Fallbacks.
- `use-cdn=false` ist der produktionsnahe Standard.
- `use-cdn=true` ist nur für lokale Experimente mit gepinnter Version vorgesehen.

## Actuator

Exponiert sind nur:

```text
/actuator/health
/actuator/info
```

Konfiguration:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info
  endpoint:
    health:
      show-details: always
    info:
      enabled: true
```

Health enthält nicht geheime Betriebsdetails zu `catalogSnapshot`, `catalogReload` und `catalogSearchIndex`. Der Info-Endpunkt enthält App-Name, Package-Basis, Java-Version und, falls vorhanden, Gradle-Build-Informationen.

Der Standard-`diskSpace`-Health-Contributor ist deaktiviert, damit keine lokalen Serverpfade über Health-Details ausgegeben werden:

```yaml
management:
  health:
    diskspace:
      enabled: false
```

## Error-Defaults

Die Anwendung rendert eigene JTE-Fehlerseiten. Boot-Fehlerdetails bleiben deaktiviert:

```yaml
server:
  server-header: ""
  error:
    include-message: never
    include-stacktrace: never
    include-binding-errors: never
    whitelabel:
      enabled: false
```

Entsprechende Property-Namen:

- `server.error.include-message`
- `server.error.include-stacktrace`
- `server.error.include-binding-errors`
- `server.error.whitelabel.enabled`
- `server.server-header`

## Statische Assets und Cache

Cache-Header:

| Pfad | Cache-Control |
|---|---|
| `/vendor/so-web-components/**` | `public, max-age=31536000` |
| `/js/**` | `public, max-age=2592000` |
| `/css/**` | `public, max-age=3600` |
| `/images/**` | `public, max-age=2592000`, falls Bilder ausgeliefert werden |

## Security-Header

Jede Antwort erhält grundlegende sichere Header:

- `X-Content-Type-Options: nosniff`
- `Referrer-Policy: strict-origin-when-cross-origin`
- `X-Frame-Options: DENY`
- `Permissions-Policy` mit deaktivierten Browser-Funktionen, die diese App nicht benötigt
- `Content-Security-Policy` für Self-hosted Assets; `style-src` erlaubt Inline-Styles, weil die aktuellen Web Components Shadow-DOM-Styles erzeugen.
