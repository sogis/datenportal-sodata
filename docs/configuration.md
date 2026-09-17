# Konfiguration

Diese Datei beschreibt die produktionsnahen Laufzeit-Properties des Datenportals. Secrets werden immer über Umgebung oder externe Konfiguration gesetzt und nie ins Repository geschrieben.

## Profile und Startmodi

`src/main/resources/application.yml` enthält nur sichere technische Defaults.
Es enthält weder eine Katalogquelle noch einen DuckDB-Ort, keine
localhost-Downloadbasis und keinen JTE-Development-Mode. Ein Start ohne
explizite Quelle schlägt deshalb früh mit einer verständlichen
Konfigurationsmeldung fehl.

Für die lokale Entwicklung wird das Profil `local` verwendet:

```bash
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
```

`application-local.yml` aktiviert ausdrücklich die 62-Einträge-Fixture, die
fertige `catalog.duckdb`, die lokale Downloadbasis und den JTE-Development-Mode.
Das Testprofil `test` aktiviert dieselben deterministischen Fixtures, aber mit
deaktiviertem JTE-Development-Mode; es wird für Tests über
`src/test/resources/application.properties` automatisch aktiviert.

In Produktion muss die Quellart samt passendem Ort über externe
Konfiguration gesetzt werden. Zulässige Quellarten sind `classpath`, `file`
sowie `http` und `manifest` für XTF. DuckDB unterstützt weiterhin `classpath`,
`file` und `http`. Secrets und Umgebungsvariablen gehören nicht in das Repository.

Wichtige Umgebungsvariablen:

- `SPRING_PROFILES_ACTIVE`: typischerweise `local` lokal; in Produktion ein
  eigenes externes Profil oder externe Properties.
- `DATENPORTAL_ADMIN_RELOAD_TOKEN`: Token für geschützten Reload und Status.
- `DOWNLOAD_URL`: optionale Basis für `${DOWNLOAD_URL}` in XTF-Dateien; ohne
  Wert bleibt `download-url` ungesetzt.

## Katalogquelle

Bevorzugte Konfiguration:

```yaml
datenportal:
  catalog:
    source-type: classpath # classpath | http | file | manifest
    classpath-location: published_catalog_full_62_entries.xtf
    file-location: ./config/catalog.xtf
    http-url: https://example.org/published_catalog.xtf
    http-connect-timeout: 5s
    http-read-timeout: 30s
    max-size: 50MB
    download-url: ${DOWNLOAD_URL:}
```

`source-type` ist verpflichtend. Für `classpath` muss
`classpath-location`, für `file` muss `file-location` und für `http` oder `manifest` muss
`http-url` gesetzt sein. Nicht zur gewählten Quellart gehörende Orte dürfen
leer bleiben. `source-type=http` lädt die vollständige PublishedCatalog-XTF/XML-
Datei per HTTP GET; `source-type=manifest` löst zunächst `current.json` auf
und lädt daraus die Katalogdatei. `source-type=file` ist für lokale Entwicklung,
Tests und extern gemountete Artefakte vorgesehen.

HTTP-Quellen:

- `http-connect-timeout` begrenzt den Verbindungsaufbau.
- `http-read-timeout` begrenzt den vollständigen HTTP-Request.
- `max-size` begrenzt die eingelesenen Katalogbytes für alle Quellen.
- Bei `manifest` ist der Verweis selbst auf 64 KiB begrenzt; für die referenzierte
  XTF gilt `max-size`. Verbindungs- und Request-Timeouts gelten je HTTP-Abruf.
- HTTP-Status ausserhalb `2xx` führen zu einem kontrollierten Reload-Fehler.
- Die Source-Beschreibung enthält Schema, Host, Port und Pfad, aber keine Userinfo, Query-Parameter oder Fragments.

Download-URL-Platzhalter:

- XTF-Dateien dürfen in Download-URLs den Platzhalter `${DOWNLOAD_URL}` enthalten, zum Beispiel `${DOWNLOAD_URL}/ch.2581.baumkataster.parquet`.
- Vor dem XML-Parsing ersetzt die Anwendung den Platzhalter durch `datenportal.catalog.download-url`.
- Doppelte Slashes an der Join-Stelle werden bereinigt: `download-url: http://localhost:8081/ch.so.datenportal/downloads/` plus `${DOWNLOAD_URL}//file.parquet` wird zu `http://localhost:8081/ch.so.datenportal/downloads/file.parquet`.
- Enthält ein XTF `${DOWNLOAD_URL}` und ist `download-url` leer, schlägt der Katalog-Load kontrolliert fehl.
- `download-url` darf eine absolute `http(s)`-URL oder ein root-relativer Pfad wie `/downloads` sein.

Öffentliche Katalog-Artefakte:

- `GET /catalog/published-catalog.xtf` liefert das im aktiven Snapshot gespeicherte PublishedCatalog-XTF-Artefakt aus. Der `${DOWNLOAD_URL}`-Platzhalter ist dabei bereits ersetzt; ETag, Content-Length und `If-None-Match` werden unterstützt.
- `GET /catalog/catalog.duckdb` liefert das im aktiven Snapshot gespeicherte DuckDB-View-Catalog-Artefakt aus. Ohne Versionsparameter bleibt die Antwort `no-cache`; Explore verwendet `?v=<sha256>`, womit `public, immutable`-Caching aktiviert wird. Eine veraltete Version wird mit `409 Conflict` abgewiesen, statt still die aktuelle Datei zu liefern.

## Veröffentlichungsverweis auf S3

Mit `DATENPORTAL_CATALOG_SOURCE_TYPE=manifest` und
`DATENPORTAL_CATALOG_HTTP_URL=https://downloads.example.org/current.json` liest die
Anwendung je Start/Reload genau einen Veröffentlichungsverweis und danach dessen
Katalog-XTF. Es wird kein S3-SDK benötigt. Der Manifestvertrag ist im
[Themenrepo](https://codeberg.org/edigonzales/datenportal-themenrepo/src/branch/main/docs/biblios/lieferverarbeitung.adoc)
definiert: `schemaVersion: 1`, `releaseId`, `datasheets`, `catalog` und `duckdb`; Dateinamen
müssen zur Kennung passen und dürfen keine fremden URLs oder Pfadwechsel enthalten.

`catalog: null` ist ein gültiger Zustand vor der ersten Datenlieferung: leere
öffentliche Sicht und Suchindex, Health `UP` bei ansonsten gesunden Komponenten,
404 unter `/catalog/published-catalog.xtf`.
Der interne Snapshot-Health führt `catalogState=awaiting-first-delivery`;
die öffentliche Health-Antwort blendet Details aus. Der geschützte Admin-Status
zeigt Quelle und Eintragszahlen. Es wird keine künstliche XTF erzeugt.
Eine fehlende referenzierte Datei oder ein ungültiger Verweis bleibt dagegen ein
Fehler. Fehlgeschlagene Reloads erhalten den bisherigen Snapshot und Suchindex.
Mit `DATENPORTAL_CATALOG_DUCKDB_SOURCE_TYPE=manifest` lädt dieselbe Auflösung
zusätzlich `duckdb: "catalog-<releaseId>.duckdb"`. XTF und DuckDB stammen dadurch
immer aus derselben Manifestgeneration, auch wenn sich `current.json` während
der Downloads ändert. Auch `catalog: null` benötigt eine gültige DuckDB mit
leerem `opendata`-Schema. Bei Fehlern bleiben beide bisherigen Artefakte aktiv.
Es gibt keinen automatischen Rückfall auf eine gebündelte Fixture.

DuckDB-Manifestmodus setzt `datenportal.catalog.source-type=manifest` voraus.
Eine separate DuckDB-HTTP-Adresse, Datei- oder Classpath-Location darf dann
nicht gesetzt sein. Die XTF-Konfiguration bestimmt Manifestadresse und
Manifest-Timeouts; beide Artefakte behalten ihre jeweiligen Timeouts und
Grössenlimits. Explizit unabhängige Classpath-/Datei-/HTTP-Quellen bleiben möglich.

Bei der Umstellung zuerst den Publisher und dessen Laufzeit aktualisieren und
eine reguläre Veröffentlichung ausführen. Erst danach DuckDB auf `manifest`
umstellen; alte Manifeste ohne `duckdb` werden in diesem Modus abgewiesen.

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
      source-type: classpath # classpath | http | file | manifest
      classpath-location: catalog.duckdb
      file-location: ./config/catalog.duckdb
      http-url: https://example.org/catalog.duckdb
      http-connect-timeout: 5s
      http-read-timeout: 30s
      max-size: 50MB
      schema: opendata
```

`source-type` ist auch für DuckDB verpflichtend. Für die gewählte Quellart
muss der passende Ort gesetzt sein. `source-type=http` lädt die DuckDB-Datei
per HTTP GET; `source-type=file` ist fuer lokale Entwicklung, Tests und extern
gemountete Artefakte vorgesehen. Timeouts, `max-size` und die sichere
Source-Beschreibung folgen derselben Logik wie bei der XTF-Quelle.

Das Schema `opendata` wird im Explore-Kontext an den Browser geliefert. Der
Browser attached die Datei read-only als Datenbank `catalog`, lädt `httpfs`,
setzt `USE "catalog"."opendata"` und aktualisiert daraus die
SQLRooms-SchemaTrees fuer den Schema Explorer. Die eigentliche SQL-Ausfuehrung
läuft direkt gegen diese attached Catalog-Datenbank. Dadurch koennen Abfragen
auch Views aus mehreren Parquet-Dateien joinen, solange sie im Catalog-Artefakt
enthalten sind.

Beim Start und bei einem Reload werden XTF und DuckDB jeweils einmal geladen
und erst nach erfolgreichem XTF-Parse, Validierung und Lucene-Indexbau als ein
Snapshot aktiviert. Die Anwendung prüft bei DuckDB nur die technische
Signatur (mindestens zwölf Bytes, `DUCK` an Byteposition 8 bis 11); sie
erzeugt, repariert oder fachlich analysiert die Datei nicht. Die externe
Publishing-Pipeline muss daher sicherstellen, dass XTF und DuckDB zueinander
passen. GRETL erzeugt die Views aus dem vollständigen Publikationskandidaten,
prüft ihre Bindung und publiziert XTF und DuckDB über denselben Manifeststand.
Auch bei `catalog: null` verweist dieser auf eine gültige leere DuckDB.
Bereits offene Playgrounds behalten ihre eingelesene Datenbank bis zum Neuladen.
Parquet-Dateien behalten feste URLs und sind nicht Teil der atomaren Versionierung.

## WebR fuer Erkunden

Das R-Labor ist standardmaessig aktiv und kann auch ohne vorherige SQL-Ausfuehrung starten. Ein Dataframe `daten` entsteht aber ausschliesslich durch explizit aus dem SQL-Labor uebernommene Query-Resultate; R bekommt keinen direkten DuckDB- oder Parquet-Zugriff.

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
- `mirrorWebRPackages` erzeugt aus `src/main/frontend/explore/scripts/webr-packages.lock.json` ein kuratiertes Repository unter `build/generated-resources/webr/static/webr-packages/bin/emscripten/contrib/4.6` und spiegelt neben `PACKAGES`/`PACKAGES.gz` auch `PACKAGES.rds`, damit WebR/R beim Paketindex keine 404-Fallback-Meldung erzeugt.
- `precompressStaticAssets` erzeugt Brotli-/Gzip-Varianten fuer WebR-Runtime und Paketmirror.

Der Browser darf fuer WebR keine externen Requests an `webr.r-wasm.org` oder `repo.r-wasm.org` benoetigen. Fuer Explore-Seiten sowie `/explore/**`, `/webr/**` und `/webr-packages/**` erlaubt die CSP `worker-src 'self' blob:` und `script-src 'self' 'wasm-unsafe-eval' 'unsafe-eval'`, weil WebR 0.6.0/Emscripten beim Runtime-Start dynamische JavaScript-Auswertung nutzt. Normale Katalog- und Detailseiten bleiben bei `script-src 'self' 'wasm-unsafe-eval'`; Cross-Origin-Isolation ist fuer V1 nicht vorgesehen.

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
/actuator/health/liveness
/actuator/health/readiness
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
      show-details: never
      probes:
        enabled: true
      group:
        liveness:
          include: livenessState
        readiness:
          include: readinessState,catalogSnapshot,catalogReload,catalogSearchIndex
    info:
      enabled: true
```

`/actuator/health` zeigt öffentlich nur den Gesamtstatus. Interne Details zu
`catalogSnapshot`, `catalogReload` und `catalogSearchIndex` sind nicht Teil der
öffentlichen Antwort. `/actuator/health/liveness` enthält ausschließlich den
internen Prozesszustand. `/actuator/health/readiness` berücksichtigt zusätzlich
den aktiven Katalog-Snapshot und den Suchindex, gibt aber ebenfalls keine Details
aus. Der geschützte Admin-Status liefert die für den Betrieb notwendigen Details.
Der Info-Endpunkt enthält App-Name, Package-Basis, Java-Version und, falls
vorhanden, Gradle-Build-Informationen.

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
