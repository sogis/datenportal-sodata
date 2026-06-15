# Betrieb

## Reload auslösen

Der laufende Katalog kann ohne Neustart ersetzt werden:

```bash
curl -X POST \
  -H "X-Reload-Token: ${DATENPORTAL_ADMIN_RELOAD_TOKEN}" \
  http://localhost:8080/admin/catalog/reload
```

Erfolgreiche Reloads liefern `200 OK` und JSON mit Snapshot-Zeitpunkt, Source-Beschreibung, Content-Hash, Counts und Warnungen.

## Status abfragen

```bash
curl \
  -H "X-Reload-Token: ${DATENPORTAL_ADMIN_RELOAD_TOKEN}" \
  http://localhost:8080/admin/catalog/status
```

Der Status zeigt den aktuell aktiven Snapshot sowie Zeitpunkt und Meldung des letzten erfolgreichen oder fehlgeschlagenen Reloads.

## Fehlerverhalten

- `401 Unauthorized`: Token fehlt oder ist falsch.
- `503 Service Unavailable`: Reload-Token ist nicht konfiguriert.
- `409 Conflict`: Ein Reload läuft bereits.
- `502 Bad Gateway`: HTTP-Quelle konnte nicht geladen werden oder lieferte keinen `2xx`-Status.
- `422 Unprocessable Entity`: XML/XTF ist ungültig oder die Katalogvalidierung schlägt fehl.
- `500 Internal Server Error`: unerwarteter Fehler oder Lucene-Reindexing fehlgeschlagen.

Bei jedem Fehler bleibt der bisherige `CatalogSnapshot` samt bisherigem Lucene-Index aktiv.

## Logging

Der Reload loggt Start, Quelle, Download-Ergebnis, Parsing/Validierung über die Snapshot-Build-Pipeline, Reindexing, Snapshot-Austausch und Abschluss. Tokens und credential-haltige URLs dürfen nicht in Logs erscheinen.
