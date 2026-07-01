# Erkunden Troubleshooting

Status: Phase 0 placeholder

Dieses Dokument sammelt bekannte Risikofelder fuer die spaeteren DuckDB-Wasm-, SQLRooms- und Parquet-Phasen. Phase 0 hat diese Laufzeitpfade noch nicht implementiert.

## Keine Parquet-Distribution

Geplantes Verhalten:

- Die Explore-Seite bleibt im normalen Datenportal-Layout nutzbar.
- Es wird kurz erklaert, dass Erkunden fuer dieses Datenthema noch nicht verfuegbar ist, weil keine Parquet-Datei publiziert ist.
- Die normale Datensatzdetailseite, Downloads und Metadaten bleiben unveraendert nutzbar.

## CORS und Range Requests

DuckDB-Wasm liest Parquet-Dateien im Browser. Echte Download-URLs muessen deshalb browserseitig abrufbar sein.

Zu pruefen ab Phase 3:

- `Access-Control-Allow-Origin`
- `Accept-Ranges`
- Verhalten bei `Range`-Requests
- Weiterleitungen und signierte URLs
- Content-Type und Content-Length

Fehler sollen klar zwischen Netzwerk-, CORS-, Range-Request- und Parquet-Ladeproblemen unterscheiden, soweit technisch moeglich.

## Safari und WebAssembly

Zu pruefen ab Phase 7:

- DuckDB-Wasm-Initialisierung.
- Worker- und Wasm-Ladepfade.
- Speichergrenzen bei grossen Dateien.
- CSP-Anforderungen fuer Wasm und Worker.

## Grosse Dateien und Resultate

Geplantes MVP-Verhalten:

- Resultate werden begrenzt.
- Diagrammvorschlaege warnen bei zu vielen Zeilen.
- CSV-Export exportiert nur das aktuelle Resultat, nicht die gesamte Quelldatei.
- Nutzertexte duerfen keine serverseitige Ausfuehrung versprechen.

## Query-Fehler

Geplantes MVP-Verhalten:

- SQL bleibt sichtbar.
- Fehlermeldungen werden lesbar angezeigt.
- Clientseitige Query-Guards sind UX-Schutz, keine Sicherheitskontrolle.
- Mutation und gefaehrliche DuckDB-Kommandos werden nicht als unterstuetzter Workflow angeboten.
