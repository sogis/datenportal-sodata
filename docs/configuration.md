# Konfiguration

Diese Datei beschreibt die produktionsnahen Laufzeit-Properties des Datenportals. Secrets werden immer über Umgebung oder externe Konfiguration gesetzt und nie ins Repository geschrieben.

## Katalogquelle

Bevorzugte Konfiguration:

```yaml
datenportal:
  catalog:
    source-type: classpath # classpath | http | file
    classpath-location: published_catalog_full_62_entries.xtf
    file-location: ./config/catalog.xtf
    http-url: https://example.org/published_catalog.xtf
    http-connect-timeout: 5s
    http-read-timeout: 30s
    max-size: 50MB
    download-url: ${DOWNLOAD_URL:http://localhost:8081/ch.so.datenportal/downloads}
```

`source-type=classpath` ist der lokale Standard. `source-type=http` lädt die vollständige PublishedCatalog-XTF/XML-Datei per HTTP GET. `source-type=file` ist für lokale Entwicklung und Tests vorgesehen.

Die Legacy-Property bleibt vorerst gültig:

```yaml
datenportal:
  catalog:
    source: classpath:published_catalog_full_62_entries.xtf
```

Wenn `source-type` gesetzt ist, gewinnt die neue Konfiguration gegenüber `source`.

HTTP-Quellen:

- `http-connect-timeout` begrenzt den Verbindungsaufbau.
- `http-read-timeout` begrenzt den vollständigen HTTP-Request.
- `max-size` begrenzt die eingelesenen Katalogbytes für alle Quellen.
- HTTP-Status ausserhalb `2xx` führen zu einem kontrollierten Reload-Fehler.
- Die Source-Beschreibung enthält Schema, Host, Port und Pfad, aber keine Userinfo, Query-Parameter oder Fragments.

Download-URL-Platzhalter:

- XTF-Dateien dürfen in Download-URLs den Platzhalter `${DOWNLOAD_URL}` enthalten, zum Beispiel `${DOWNLOAD_URL}/ch.2581.baumkataster.parquet`.
- Vor dem XML-Parsing ersetzt die Anwendung den Platzhalter durch `datenportal.catalog.download-url`.
- Doppelte Slashes an der Join-Stelle werden bereinigt: `download-url: http://localhost:8081/ch.so.datenportal/downloads/` plus `${DOWNLOAD_URL}//file.parquet` wird zu `http://localhost:8081/ch.so.datenportal/downloads/file.parquet`.
- Enthält ein XTF `${DOWNLOAD_URL}` und ist `download-url` leer, schlägt der Katalog-Load kontrolliert fehl.
- `download-url` darf eine absolute `http(s)`-URL oder ein root-relativer Pfad wie `/downloads` sein.

Öffentliche Katalog-Artefakte:

- `GET /catalog/published-catalog.xtf` liefert die aktuell konfigurierte PublishedCatalog-XTF-Datei aus. Der `${DOWNLOAD_URL}`-Platzhalter ist dabei bereits ersetzt.
- `GET /catalog/catalog.duckdb` liefert den aktuell konfigurierten DuckDB-View-Catalog fuer den Explore-Schema-Explorer aus.

## DuckDB-Catalog fuer Erkunden

Der Explore-Browser lädt neben der XTF-Metadatenquelle eine DuckDB-Datei mit
allen Open-Data-Parquet-Views. Die DuckDB-Datei ist ein eigenes Artefakt und
wird nicht aus der XTF-Binary gepatcht. Produktion und lokale Entwicklung
muessen deshalb eine `catalog.duckdb` verwenden, deren Views bereits mit den
passenden Download-URLs erzeugt wurden.

```yaml
datenportal:
  catalog:
    duckdb:
      source-type: classpath # classpath | http | file
      classpath-location: catalog.duckdb
      file-location: ./config/catalog.duckdb
      http-url: https://example.org/catalog.duckdb
      http-connect-timeout: 5s
      http-read-timeout: 30s
      max-size: 50MB
      schema: opendata
```

`source-type=classpath` ist der lokale Standard. `source-type=http` lädt die
DuckDB-Datei per HTTP GET; `source-type=file` ist fuer lokale Entwicklung,
Tests und extern gemountete Artefakte vorgesehen. Timeouts, `max-size` und die
sichere Source-Beschreibung folgen derselben Logik wie bei der XTF-Quelle.

Das Schema `opendata` wird im Explore-Kontext an den Browser geliefert. Der
Browser attached die Datei read-only als Datenbank `catalog`, lädt `httpfs`,
setzt `USE "catalog"."opendata"` und aktualisiert daraus die
SQLRooms-SchemaTrees fuer den Schema Explorer. Die eigentliche SQL-Ausfuehrung
läuft direkt gegen diese attached Catalog-Datenbank. Dadurch koennen Abfragen
auch Views aus mehreren Parquet-Dateien joinen, solange sie im Catalog-Artefakt
enthalten sind.

## WebR fuer Erkunden

Das R-Labor ist standardmaessig aktiv und arbeitet ausschliesslich mit explizit aus dem SQL-Labor uebernommenen Query-Resultaten. R bekommt keinen direkten DuckDB- oder Parquet-Zugriff.

```yaml
datenportal:
  explore:
    webr-enabled: true
```

Der Explore-Kontext liefert dem Browser die WebR-Konfiguration:

- Runtime-Basis: `/webr/0.6.0/`
- Paket-Repository: `/webr-packages/`
- R-Dataframe-Name: `daten`
- Limits: empfohlen `5'000`, Warnung `10'000`, hart `50'000` Zeilen

Die Runtime- und Paketdateien werden zur Buildzeit generiert:

- `copyWebRRuntime` kopiert `node_modules/webr/dist` nach `build/generated-resources/webr/static/webr/0.6.0`.
- `mirrorWebRPackages` erzeugt aus `src/main/frontend/explore/scripts/webr-packages.lock.json` ein kuratiertes Repository unter `build/generated-resources/webr/static/webr-packages/bin/emscripten/contrib/4.6`.
- `precompressStaticAssets` erzeugt Brotli-/Gzip-Varianten fuer WebR-Runtime und Paketmirror.

Der Browser darf fuer WebR keine externen Requests an `webr.r-wasm.org` oder `repo.r-wasm.org` benoetigen. Die CSP bleibt bei `worker-src 'self' blob:` und `script-src 'self' 'wasm-unsafe-eval'`; Cross-Origin-Isolation ist fuer V1 nicht vorgesehen.

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
    default-page-size: 10
    max-page-size: 100
```

Die öffentliche Katalog-UI verwendet aktuell keine sichtbare Pagination und ignoriert `page`/`size` im Request bewusst. Die Suchschicht normalisiert Page-Requests weiterhin service-seitig, damit Pagination später ohne Umbau der unteren Schichten wieder aktiviert werden kann.

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

Die `connect-src`-Direktive ist konfigurierbar:

```yaml
datenportal:
  security:
    csp:
      connect-src:
        - "'self'"
        - "https://data.so.ch"
      include-catalog-download-origin: true
```

Bei `include-catalog-download-origin=true` wird die Origin aus `datenportal.catalog.download-url` automatisch ergänzt, sofern `download-url` eine absolute `http(s)`-URL ist. Für den lokalen Standard bedeutet das zusätzlich `http://localhost:8081`.
