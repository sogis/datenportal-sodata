# Betrieb

## Lokal mit Fixtures starten

```bash
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
```

Bei belegtem Port:

```bash
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun --args='--server.port=8082'
```

Standard-URLs:

- `http://localhost:8080/`
- `http://localhost:8080/datasets`
- `http://localhost:8080/actuator/health`
- `http://localhost:8080/actuator/info`

## An den lokalen Dev-Stack anschliessen

Zuerst Garage/Jenkins initialisieren, sodass `current.json` öffentlich lesbar
ist. Die vollständige Reihenfolge steht in der
[Stack-Inbetriebnahme](https://codeberg.org/edigonzales/datenportal-dev-stack/src/branch/main/docs/biblios/inbetriebnahme.adoc).

Im Dev-Stack läuft das Portal als Compose-Service `sodata` auf Host-Port 8082:

- Der Stack baut das Image lokal aus diesem Repository
  (`./scripts/up.sh --local-sodata`) oder verwendet ein Registry-Image mit
  lokalem Fallback.
- Compose setzt die Manifestadresse intern (`http://downloads:8081/...`), die
  öffentliche Downloadbasis auf dem Host-Port und DuckDB auf `source-type=manifest`.
- Derselbe `.env`-Wert `DATENPORTAL_PORTAL_RELOAD_TOKEN` versorgt Jenkins und
  den Portal-Container; die frühere Host-Gateway-Adresse entfällt.
- Vor der ersten Veröffentlichung fehlt `current.json`. Der Dev-Stack startet
  Sodata in diesem Zustand nicht. Nach der administrativen Erstpublikation wird
  der Container mit den lokalen Compose-Overrides gestartet.

Details und Grenzen: [Container-Deployment](container-deployment.md).

Die Quelle wird bei Start und jedem Reload einmal aufgelöst. GRETL publiziert
XTF und DuckDB gemeinsam über versionierte Manifestverweise. Auch ein
Null-Katalog besitzt eine gültige DuckDB mit leerem `opendata`-Schema.
Für Altstände ohne `duckdb` zuerst einen regulären Publikationslauf mit dem
aktualisierten Publisher ausführen; keine Neuinitialisierung. Erst danach das
Portal auf den gemeinsamen Manifestmodus umstellen.

Bei `catalog: null` sind leere Trefferlisten und ein leerer Suchindex korrekt;
`/catalog/published-catalog.xtf` liefert 404, da keine XTF vorliegt. Ein gültiger
Katalog ausschliesslich mit zurückgehaltenen Einträgen ergibt ebenfalls eine
leere Sicht, hat aber weiterhin eine herunterladbare vollständige Quelldatei.
Fehlendes Manifest, ungültiges JSON oder fehlende referenzierte Dateien sind
Ladefehler, keine leere Sicht. Beim Erststart kann dann kein Snapshot aufgebaut
werden; bei Reload bleibt der alte Zustand erhalten.

### Alternative: Start als Hostprozess

Ohne Container und mit JDK 25 im Portal-Repository starten (Port 8082 darf dann
nicht durch den Compose-Service belegt sein):

```bash
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun --args='--server.port=8082 --datenportal.catalog.source-type=manifest --datenportal.catalog.http-url=http://localhost:8081/ch.so.daten/current.json --datenportal.catalog.download-url=http://localhost:8081/ch.so.daten --datenportal.catalog.duckdb.source-type=manifest --datenportal.catalog.duckdb.classpath-location='
```

Dann muss der Reload-Token weiterhin extern als `DATENPORTAL_ADMIN_RELOAD_TOKEN`
gesetzt und Jenkins auf eine erreichbare Hostadresse konfiguriert werden. Der
Reload ändert weder Quellkonfiguration noch Startport. Die folgenden
Standalone-Beispiele verwenden Port 8080; im Stack entsprechend 8082 einsetzen.

## Health und Info

```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8080/actuator/health/liveness
curl http://localhost:8080/actuator/health/readiness
curl http://localhost:8080/actuator/info
```

Health zeigt öffentlich nur den Gesamtstatus. Die internen Komponenten
`catalogSnapshot`, `catalogReload` und `catalogSearchIndex` sowie ihre Counts
und Zeitpunkte sind nicht Teil der öffentlichen Health-Antwort. Der geschützte
Admin-Status ist für detaillierte Reload-Diagnose vorgesehen.

Wenn Gradle-Build-Informationen vorhanden sind, bezieht die Anwendung die Footer-Build-Kennung aus `META-INF/build-info.properties`. Der Commit-Anteil wird beim Build als 12-stelliger Git-Hash geschrieben.

Die Endpunkte enthalten keine Reload-Tokens und keine credential-haltigen Quell-URLs. Produktiv sollte der Zugriff zusätzlich über Infrastruktur eingeschränkt werden.

Der Standard-`diskSpace`-Health-Contributor ist deaktiviert, damit keine lokalen Serverpfade im Health-JSON erscheinen.

## Reload auslösen

Token setzen:

```bash
: "${DATENPORTAL_ADMIN_RELOAD_TOKEN:?Externes Reload-Secret bereitstellen}"
export DATENPORTAL_ADMIN_RELOAD_TOKEN
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
```

Reload:

```bash
curl -X POST \
  -H "X-Reload-Token: ${DATENPORTAL_ADMIN_RELOAD_TOKEN}" \
  http://localhost:8080/admin/catalog/reload
```

Status:

```bash
curl \
  -H "X-Reload-Token: ${DATENPORTAL_ADMIN_RELOAD_TOKEN}" \
  http://localhost:8080/admin/catalog/status
```

Fehlercodes:

- `401 Unauthorized`: Token fehlt oder ist falsch.
- `503 Service Unavailable`: Reload-Token ist nicht konfiguriert.
- `409 Conflict`: Ein Reload läuft bereits.
- `502 Bad Gateway`: Quellenladefehler, etwa HTTP ohne `2xx`, ungültiger Manifestverweis oder fehlende referenzierte Datei.
- `422 Unprocessable Entity`: XML/XTF ist ungültig oder die Katalogvalidierung schlägt fehl.
- `500 Internal Server Error`: unerwarteter Fehler oder Lucene-Reindexing fehlgeschlagen.

Bei jedem Reloadfehler bleibt der bisherige `CatalogSnapshot` samt bisherigem Lucene-Index aktiv.
Eine bereits angenommene S3-Veröffentlichung wird dadurch nicht zurückgerollt;
Jenkins kann deshalb eine angenommene Veröffentlichung mit fehlgeschlagenem
Reload melden. Nach Beheben der Ursache den Reload bewusst erneut auslösen.
Das gilt ebenso für das bisherige DuckDB-Artefakt: Ein nicht lesbares,
zu kleines oder technisch ungültiges DuckDB-Artefakt, ein XTF-Fehler oder ein
Lucene-Fehler veröffentlicht keinen Teilstand.

## Fehlerdiagnose

- Öffentliche 404- und 5xx-Seiten zeigen kontrollierte Meldungen mit normalem Page Chrome.
- Stacktraces, Exception-Klassen und Secret-Werte werden nicht an Nutzer ausgeliefert.
- Logs enthalten Reload-Start, Quelle ohne Credentials, Download-Ergebnis, Validierung, Reindexing und Abschluss.
- Tokens, Query-Parameter credential-haltiger URLs und Secrets dürfen nicht geloggt werden.

## Cache prüfen

```bash
curl -I http://localhost:8080/css/app.css
curl -I http://localhost:8080/js/htmx.min.js
curl -I http://localhost:8080/vendor/so-web-components/0.1.10/index.js
curl -I http://localhost:8080/vendor/so-web-components/0.1.10/styles/FrutigerLTW05-55Roman.woff2
```

Erwartet wird ein `Cache-Control`-Header. Versionierte Web-Component-Assets haben eine lange TTL, CSS hat eine kurze TTL, weil die Pfade nicht fingerprinted sind.

Katalog-Artefakte:

```bash
curl -i http://localhost:8080/catalog/catalog.duckdb
curl -i http://localhost:8080/datasets/ch.so.bauinventar/explore/context.json
```

Die JSON-Antwort enthält eine URL der Form
`/catalog/catalog.duckdb?v=<sha256>`. Diese URL muss `public, immutable`
liefern. Ohne `v` bleibt DuckDB `no-cache`; ein falscher Hash ergibt `409`.
Der ETag aus einer erfolgreichen Antwort kann mit `If-None-Match` für eine
`304 Not Modified`-Antwort wiederverwendet werden.

## Smoke-Test

Nach Änderungen an Betrieb, UI oder Reload:

```bash
./gradlew test
./gradlew playwrightTest
./gradlew check
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
```

Hinweis zu Playwright:

- `playwrightTest` installiert beim ersten Lauf die benötigten Browser unter `.gradle/playwright-browsers`.
- Danach nutzt `check` dieselbe lokale Browser-Installation wieder.

Manuelle Prüfung mit laufender App:

- `/` und `/datasets` liefern 200 mit Header und Breadcrumb.
- `/datasets?q=gemeinde&view=cards` liefert 200.
- eine unbekannte Route liefert eine gestaltete 404-Seite.
- `/actuator/health` liefert `UP`.
- `/actuator/info` enthält App-Basisinformationen.
- `curl -I` auf CSS, HTMX und Web-Component-JS zeigt Cache-Header.
- mit `DATENPORTAL_ADMIN_RELOAD_TOKEN` funktioniert `/admin/catalog/status`; ein Reload kann optional getestet werden.
