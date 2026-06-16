# Betrieb

## Lokal starten

```bash
./gradlew bootRun
```

Bei belegtem Port:

```bash
./gradlew bootRun --args='--server.port=8081'
```

Standard-URLs:

- `http://localhost:8080/`
- `http://localhost:8080/datasets`
- `http://localhost:8080/actuator/health`
- `http://localhost:8080/actuator/info`

## Health und Info

```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8080/actuator/info
```

Health zeigt:

- `catalogSnapshot`: aktiver Snapshot, Ladezeitpunkt und Counts.
- `catalogReload`: Reload-Laufstatus, letzter erfolgreicher Reload oder initialer Load, letzter Fehler falls vorhanden.
- `catalogSearchIndex`: Verfügbarkeit des aktiven Lucene-Index.

Die Endpunkte enthalten keine Reload-Tokens und keine credential-haltigen Quell-URLs. Produktiv sollte der Zugriff zusätzlich über Infrastruktur eingeschränkt werden.

Der Standard-`diskSpace`-Health-Contributor ist deaktiviert, damit keine lokalen Serverpfade im Health-JSON erscheinen.

## Reload auslösen

Token setzen:

```bash
export DATENPORTAL_ADMIN_RELOAD_TOKEN='change-me'
./gradlew bootRun
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
- `502 Bad Gateway`: HTTP-Quelle konnte nicht geladen werden oder lieferte keinen `2xx`-Status.
- `422 Unprocessable Entity`: XML/XTF ist ungültig oder die Katalogvalidierung schlägt fehl.
- `500 Internal Server Error`: unerwarteter Fehler oder Lucene-Reindexing fehlgeschlagen.

Bei jedem Fehler bleibt der bisherige `CatalogSnapshot` samt bisherigem Lucene-Index aktiv.

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

## Smoke-Test

Nach Änderungen an Betrieb, UI oder Reload:

```bash
./gradlew test
./gradlew playwrightTest
./gradlew check
./gradlew bootRun
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
