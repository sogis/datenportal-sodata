# Erkunden Troubleshooting

Status: Phase 8 future hooks

Dieses Dokument sammelt bekannte Risikofelder fuer die DuckDB-Wasm-, SQLRooms- und Parquet-Phasen. Seit Phase 3 initialisiert die React-Insel DuckDB-Wasm im Browser, registriert backendseitig gelieferte Parquet-Dateien als Views und laedt eine Standardvorschau. Seit Phase 4 koennen generierte Rezepte und manuelles SQL lokal ausgefuehrt und als aktuelles Resultat exportiert werden. Seit Phase 5 koennen SQL-Resultate als einfache Recharts-Diagramme angezeigt werden. Seit Phase 6 koennen statische Codebeispiele kopiert und erfolgreiche Abfragen lokal im Browser wiedergefunden werden. Seit Phase 7 sind Lade-/Fehlerzustaende, Tastaturbedienung und mobile Layoutchecks gehaertet. Seit Phase 8 sind Zukunftsflags vorbereitet, bleiben aber deaktiviert und laden keine schweren Runtime-Pakete.

## Phase-2-Island laedt nicht

Pruefen:

- Ist `/explore/assets/explore.js` erreichbar?
- Ist `/explore/assets/explore.css` erreichbar?
- Wurde `npm --prefix src/main/frontend/explore run build` oder ein Gradle-Task mit `processResources` ausgefuehrt?
- Enthaelt die Seite `#datenportal-explore-context` mit gueltigem JSON?

Geplantes Verhalten:

- Wenn der Kontext fehlt oder ungueltig ist, zeigt die Insel eine kurze Fehlermeldung.
- Die normale Datensatzseite und Downloads bleiben erreichbar.

## DuckDB-Wasm startet nicht

Pruefen:

- Sind `/explore/assets/duckdb-browser-mvp.worker.js` und `/explore/assets/duckdb-mvp.wasm` erreichbar?
- Enthaelt die CSP `worker-src 'self' blob:`?
- Enthaelt die CSP `script-src 'self' 'wasm-unsafe-eval'`?
- Wurde der Vite-Build mit `base: '/explore/'` ausgefuehrt, damit Worker/Wasm-URLs unter `/explore/assets/` liegen?

Phase-3-Fund:

- Die DuckDB-Wasm Worker- und Wasm-Dateien werden lokal ueber Vite `?url` aus `@duckdb/duckdb-wasm` gebuendelt; jsDelivr/CDN-Bundles werden nicht verwendet.
- Die SQLRooms-Auswahl verwendet im MVP die stabile DuckDB-Wasm-MVP-Variante. Die EH-Variante wurde in Chromium Headless zwar automatisch bevorzugt, brach in dieser Umgebung aber mit `RuntimeError: function signature mismatch` ab.

## DuckDB Parquet Extension

DuckDB-Wasm laedt die Parquet-Erweiterung beim ersten `read_parquet(...)`. Ohne weitere Konfiguration versucht DuckDB dafuer `https://extensions.duckdb.org/.../parquet.duckdb_extension.wasm`.

Phase-3-Entscheid:

- Die signierte offizielle Parquet-Erweiterung fuer DuckDB-Wasm `v1.4.3/wasm_mvp` liegt same-origin unter `/explore-extensions/v1.4.3/wasm_mvp/parquet.duckdb_extension.wasm`.
- Die React-Insel setzt beim DuckDB-Start `custom_extension_repository` auf `${location.origin}/explore-extensions`.
- Dadurch bleibt `connect-src 'self' https://data.so.ch` eng, und CI/Playwright braucht keinen Zugriff auf `extensions.duckdb.org`.

## Keine Parquet-Distribution

Verhalten:

- Die Explore-Seite bleibt im normalen Datenportal-Layout nutzbar.
- Es wird kurz erklaert, dass Erkunden fuer dieses Datenthema noch nicht verfuegbar ist, weil keine Parquet-Datei publiziert ist.
- Die normale Datensatzdetailseite, Downloads und Metadaten bleiben unveraendert nutzbar.

## CORS und Range Requests

DuckDB-Wasm liest Parquet-Dateien im Browser. Echte Download-URLs muessen deshalb browserseitig abrufbar sein. Phase 3 erlaubt browserseitig nur same-origin und `https://data.so.ch`:

```text
connect-src 'self' https://data.so.ch
```

Zu pruefen fuer produktive Parquet-Hosts:

- `Access-Control-Allow-Origin`
- `Accept-Ranges`
- Verhalten bei `Range`-Requests
- Weiterleitungen und signierte URLs
- Content-Type und Content-Length

Fehler sollen klar zwischen Netzwerk-, CORS-, Range-Request- und Parquet-Ladeproblemen unterscheiden, soweit technisch moeglich.

Verhalten ab Phase 7:

- Die React-Insel klassifiziert Browserfehler best-effort in DuckDB-Wasm-, CORS-, Range-, HTTP/IO- und Parquet-Ladefehler.
- Die Fehlerbox bleibt im normalen Datenportal-Layout; der Link `Zur Datensatzseite` im Hostbereich bleibt erreichbar.
- Playwright prueft eine fehlende same-origin Parquet-Datei als reproduzierbaren Fehlerpfad.

Phase-3-Fund:

- CI/Playwright verwendet eine same-origin Fixture unter `/explore-fixtures/ch.so.oev_haltestellen.parquet`.
- `data.so.ch`-Fixture-URLs konnten in der Implementierungs-/Planungsumgebung nicht per DNS aufgeloest werden (`Could not resolve host: data.so.ch`). Die echte externe Parquet-Pruefung bleibt deshalb ein manueller/operativer Smoke-Test, sobald der Produktionshost aus der Zielumgebung erreichbar ist.
- Externe Parquet-Hosts muessen CORS fuer den Portal-Origin erlauben und Byte Range Requests unterstuetzen. Ohne Range-Unterstuetzung kann DuckDB-Wasm grosse Parquet-Dateien ineffizient oder gar nicht laden.

Phase-7-Fund:

- `curl -I --max-time 10 https://data.so.ch/download/ch.so.oev_haltestellen.parquet` schlug in der Agent-Umgebung weiterhin fehl mit `curl: (6) Could not resolve host: data.so.ch`.

## Safari und WebAssembly

Automatisiert geprueft ab Phase 7:

- Chromium Headless via Java Playwright fuer DuckDB-Wasm-Initialisierung, same-origin Parquet, SQL, Diagramm, Code, Fehlerzustand und mobile Overflow-Checks.

Weiterhin manuell/operativ zu pruefen:

- DuckDB-Wasm-Initialisierung.
- Worker- und Wasm-Ladepfade.
- Speichergrenzen bei grossen Dateien.
- CSP-Anforderungen fuer Wasm und Worker.

Chrome, Firefox und Safari sind auf dem lokalen System als Apps vorhanden, wurden in diesem Agent-Lauf aber nicht als kontrollierbare GUI-Browser manuell verifiziert.

## Grosse Dateien und Resultate

Verhalten ab Phase 4:

- Resultate werden begrenzt.
- `select`- und `with`-Abfragen ohne Top-Level-`limit` werden clientseitig auf `datenportal.explore.max-result-rows` begrenzt.
- Rezepte mit eigenem `limit` behalten dieses Limit.
- CSV-Export exportiert nur das aktuelle Resultat, nicht die gesamte Quelldatei.
- Nutzertexte duerfen keine serverseitige Ausfuehrung versprechen.
- Diagrammvorschlaege warnen bei mehr als 500 Zeilen fuer Balken- und Liniencharts und zeigen nur das gewaehlt begrenzte Diagramm-Subset.
- Recharts kann keine BigInt-Werte skalieren. DuckDB `count(*)`-Resultate werden deshalb nur fuer die Diagrammdaten in JavaScript-`number` normalisiert; die eigentlichen SQL-Resultatwerte bleiben unveraendert.

## Query-Fehler

Verhalten ab Phase 4:

- SQL bleibt sichtbar.
- Fehlermeldungen werden lesbar angezeigt.
- Clientseitige Query-Guards sind UX-Schutz, keine Sicherheitskontrolle.
- Mutation und gefaehrliche DuckDB-Kommandos werden nicht als unterstuetzter Workflow angeboten.
- Erlaubt sind im MVP `select`, `with`, `describe`, `show` und `pragma table_info`.
- Blockiert werden offensichtliche Mutations- und Systemkommandos wie `insert`, `update`, `delete`, `drop`, `alter`, `create table`, `copy ... to`, `attach`, `install`, `load`, `call` und `set`.

## Codebeispiele

Verhalten ab Phase 6:

- Der Tab `Code` zeigt statische Beispiele fuer DuckDB CLI, Python und R.
- Die Beispiele werden nicht im Browser ausgefuehrt. Insbesondere wird keine WebR-Laufzeit geladen.
- Die Beispiele verwenden die primaere Parquet-Tabelle des Datenthemas oder, falls keine primaere Tabelle markiert ist, die erste Parquet-Tabelle.
- Wenn ein Datenthema keine Parquet-Distribution hat, bleiben `codeSnippets` leer und der Explore-Tab zeigt die Nicht-verfuegbar-Meldung.

## Lokale Query-Historie

Verhalten ab Phase 6:

- Erfolgreiche SQL-Abfragen werden pro Datenthema in `localStorage` gespeichert.
- Der Schluessel lautet `datenportal.explore.history.<datasetId>`.
- Es werden maximal 20 Eintraege gespeichert, newest first.
- Gespeichert werden SQL und kleine Metadaten wie Zeit, Rezepttitel, Zeilenzahl und Dauer. Resultatzeilen werden nicht gespeichert.
- Wenn `localStorage` nicht verfuegbar ist, voll ist oder ungueltige Daten enthaelt, wird die Historie leer angezeigt; SQL-Ausfuehrung, Resultattabelle, CSV-Export und Diagramme bleiben nutzbar.

## Zukunftsflags und schwere Pakete

Standardverhalten:

- `datenportal.explore.ai-enabled=false`
- `datenportal.explore.webr-enabled=false`
- `datenportal.explore.vega-enabled=false`
- `datenportal.explore.mosaic-enabled=false`
- `datenportal.explore.geospatial-enabled=false`

Wenn ein Zukunftsbereich versehentlich sichtbar wird, zuerst die JSON-Flags unter `/datasets/{datasetId}/explore/context.json` pruefen. Bei Standardkonfiguration darf `FutureExtensionSlots` nichts rendern.

`npm --prefix src/main/frontend/explore run check:future-deps` prueft:

- keine direkten Zukunftsabhaengigkeiten in `package.json`
- keine Source-Imports fuer AI, WebR, Vega, Mosaic oder Kartenframeworks
- keine entsprechenden Paketmarker in gebauten Explore-Assets

Das vorhandene transitive `react-mosaic-component` ist eine Abhaengigkeit aktueller SQLRooms Shell-/Editor-Pakete. Es ist kein aktivierter Mosaic-Crossfilter-Modus.

## SQLRooms Editor in Tests

Der Produktions-Build verwendet `SqlMonacoEditor` aus `@sqlrooms/sql-editor@0.28.0`. In Vitest wird dieser Editor gemockt, weil das installierte Paket extensionless interne ESM-Imports verwendet, die der Test-Runner nicht direkt aufloest. Die Query-Guards, Rezeptauswahl, Ausfuehrungslogik, Copy-Feedback und CSV-Export werden unabhaengig davon getestet.

## SQLRooms Recharts in Tests

Der Produktions-Build verwendet `@sqlrooms/recharts@0.28.0`. Typecheck, Vite-Build und Playwright laufen gegen den echten Produktionspfad. Vitest mockt die Recharts-Exports, weil `@sqlrooms/recharts` extensionless interne ESM-Imports verwendet, die Vitest in dieser Konfiguration nicht direkt aufloest. Die Inferenz-, Konfigurations- und Render-Entscheidungen der Datenportal-Komponenten werden trotzdem durch Unit- und Komponententests abgedeckt.
