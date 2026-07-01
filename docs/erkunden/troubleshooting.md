# Erkunden Troubleshooting

Status: Phase 2 frontend island bootstrap

Dieses Dokument sammelt bekannte Risikofelder fuer die spaeteren DuckDB-Wasm-, SQLRooms- und Parquet-Phasen. Phase 2 laedt nur die React/Vite-Insel und validiert den eingebetteten JSON-Kontext. DuckDB-Wasm, Worker, Wasm-Dateien, CORS und Range Requests werden noch nicht ausgefuehrt.

## Phase-2-Island laedt nicht

Pruefen:

- Ist `/explore/assets/explore.js` erreichbar?
- Ist `/explore/assets/explore.css` erreichbar?
- Wurde `npm --prefix src/main/frontend/explore run build` oder ein Gradle-Task mit `processResources` ausgefuehrt?
- Enthaelt die Seite `#datenportal-explore-context` mit gueltigem JSON?

Geplantes Verhalten:

- Wenn der Kontext fehlt oder ungueltig ist, zeigt die Insel eine kurze Fehlermeldung.
- Die normale Datensatzseite und Downloads bleiben erreichbar.

## Phase 2 und DuckDB-Wasm

Noch nicht betroffen:

- CORS
- Range Requests
- DuckDB-Wasm-Initialisierung
- Worker-Ladepfade
- Wasm-CSP
- Parquet-HTTP-Fehler

Diese Pfade beginnen in Phase 3.

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
