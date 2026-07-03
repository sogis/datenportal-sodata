# Erkunden Troubleshooting

Status: SQL-Labor redesign

Dieses Dokument sammelt bekannte Risikofelder fuer die DuckDB-Wasm-, SQLRooms- und Parquet-Phasen. Die React-Insel initialisiert DuckDB-Wasm im Browser, registriert backendseitig gelieferte Parquet-Dateien als Views und rendert ein vollflaechiges SQL-Labor mit Schema-Karten, Monaco-Editor, rotem `Ausfuehren`-Button, kompakter Resultattabelle, Row-Limit-Combobox und Exporten fuer CSV, XLSX und Parquet. Diagramme, Codebeispiele und sichtbare Query-Historie sind im aktuellen Primaerpfad nicht sichtbar. Zukunftsflags bleiben deaktiviert und laden keine schweren Runtime-Pakete.

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
- Werden fuer grosse Assets bei `Accept-Encoding: br, gzip` komprimierte Varianten ausgeliefert, z.B. mit `Content-Encoding: br` fuer `/explore/assets/duckdb-mvp.wasm`?
- Enthaelt die CSP `worker-src 'self' blob:`?
- Enthaelt die CSP `script-src 'self' 'wasm-unsafe-eval'`?
- Wurde der Vite-Build mit `base: '/explore/'` ausgefuehrt, damit Worker/Wasm-URLs unter `/explore/assets/` liegen?

Aktueller Stand:

- Die DuckDB-Wasm Worker- und Wasm-Dateien werden lokal ueber Vite `?url` aus `@duckdb/duckdb-wasm` gebuendelt; jsDelivr/CDN-Bundles werden nicht verwendet.
- Die Runtime verwendet weiterhin die stabile DuckDB-Wasm-MVP-Variante. Die EH-Variante wurde erneut in Chromium Playwright angeboten, erreichte aber den Explore-Ready-State nicht; deshalb bleibt EH als Laufzeit-Bundle gesperrt.
- Vite baut EH/COI-Assets weiterhin mit, aber `createLocalDuckDbBundles()` bietet sie der DuckDB-Auswahl nicht an. COI bleibt out of scope, solange keine Cross-Origin-Isolation eingefuehrt wird.
- Der Gradle-Build erzeugt vorgerechnete `.br`- und `.gz`-Dateien fuer Explore-CSS/JS/Wasm und die lokale Parquet-Extension. Spring liefert diese ueber `EncodedResourceResolver` aus, wenn der Browser sie akzeptiert.

## DuckDB Export Extensions

DuckDB-Wasm laedt die Parquet-Erweiterung beim ersten `read_parquet(...)` oder Parquet-Export. Der XLSX-Export laedt zusaetzlich die Excel-Erweiterung. Ohne weitere Konfiguration versucht DuckDB dafuer `https://extensions.duckdb.org/.../*.duckdb_extension.wasm`.

Phase-3-Entscheid:

- Die signierten offiziellen Parquet- und Excel-Erweiterungen fuer DuckDB-Wasm `v1.4.3/wasm_mvp` liegen same-origin unter `/explore-extensions/v1.4.3/wasm_mvp/`.
- Die React-Insel setzt beim DuckDB-Start `custom_extension_repository` auf `${location.origin}/explore-extensions`.
- Dadurch bleibt `connect-src` eng konfigurierbar, und CI/Playwright braucht keinen Zugriff auf `extensions.duckdb.org`.

## SQL-Editor bleibt beim Laden

Pruefen:

- Wird im Network-Panel ein blockierter Request auf `cdn.jsdelivr.net/.../monaco-editor/.../loader.js` oder `unpkg.com/.../monaco-editor/...` angezeigt?
- Erscheint im SQL-Labor keine `.monaco-editor`-Instanz, sondern nur der Ladeindikator?
- Wurde der Vite-Build nach Dependency- oder Bundle-Aenderungen neu ausgefuehrt?

Aktueller Stand:

- Der Produktions-Build konfiguriert den SQLRooms Monaco-Loader vor dem React-Bootstrap ueber `configureMonacoLoader`.
- `monaco-editor` und der Editor-Worker werden lokal ueber Vite gebuendelt; Browser laden sie same-origin unter `/explore/assets/`.
- Die lokale Monaco-Konfiguration importiert neben `editor.api.js` explizit die Suggest-Contribution, damit `editor.action.triggerSuggest`, Quick-Suggestions und das Suggest-Widget ohne CDN-Bundle verfuegbar sind.
- Die CSP erlaubt weiterhin keine externen Monaco-CDNs. Ein jsDelivr- oder unpkg-Request ist deshalb ein Regressionssignal, nicht ein erlaubter Fallback.
- Playwright prueft im SQL-Labor, dass eine echte Monaco-Instanz rendert und kein externer Monaco-CDN-Request entsteht.

## Keine Parquet-Distribution

Verhalten:

- Die Explore-Seite bleibt im vollflaechigen Explore-Layout mit Header und Breadcrumb nutzbar.
- Es wird kurz erklaert, dass Erkunden fuer dieses Datenthema noch nicht verfuegbar ist, weil keine Parquet-Datei publiziert ist.
- Die normale Datensatzdetailseite, Downloads und Metadaten bleiben unveraendert nutzbar.

## CORS und Range Requests

DuckDB-Wasm liest Parquet-Dateien im Browser. Echte Download-URLs muessen deshalb browserseitig abrufbar sein. Die CSP-`connect-src`-Direktive erlaubt standardmaessig same-origin, `https://data.so.ch` und automatisch die Origin aus `datenportal.catalog.download-url`:

```text
connect-src 'self' https://data.so.ch http://localhost:8081
```

Die konkrete Download-Basis kann lokal ueber `DOWNLOAD_URL` bzw. `datenportal.catalog.download-url` gesetzt werden. XTF-Dateien koennen dafuer `${DOWNLOAD_URL}/datei.parquet` verwenden; die Anwendung ersetzt den Platzhalter vor dem XML-Parsing und bereinigt doppelte Slashes an der Join-Stelle.

Zu pruefen fuer produktive Parquet-Hosts:

- `Access-Control-Allow-Origin`
- `Accept-Ranges`
- Verhalten bei `Range`-Requests
- Weiterleitungen und signierte URLs
- Content-Type und Content-Length

Fehler sollen klar zwischen Netzwerk-, CORS-, Range-Request- und Parquet-Ladeproblemen unterscheiden, soweit technisch moeglich.

Verhalten:

- Die React-Insel klassifiziert Browserfehler best-effort in DuckDB-Wasm-, CORS-, Range-, HTTP/IO- und Parquet-Ladefehler.
- Die Fehlerbox bleibt im SQL-Labor sichtbar; Navigation zur Datensatzseite laeuft ueber Breadcrumb/Header, nicht ueber einen sichtbaren Labor-Link.
- Playwright prueft eine fehlende same-origin Parquet-Datei als reproduzierbaren Fehlerpfad.

Phase-3-Fund:

- CI/Playwright verwendet eine same-origin Fixture unter `/explore-fixtures/ch.so.oev_haltestellen.parquet`.
- `data.so.ch`-Fixture-URLs konnten in der Implementierungs-/Planungsumgebung nicht per DNS aufgeloest werden (`Could not resolve host: data.so.ch`). Die echte externe Parquet-Pruefung bleibt deshalb ein manueller/operativer Smoke-Test, sobald der Produktionshost aus der Zielumgebung erreichbar ist.
- Externe Parquet-Hosts muessen CORS fuer den Portal-Origin erlauben und Byte Range Requests unterstuetzen. Ohne Range-Unterstuetzung kann DuckDB-Wasm grosse Parquet-Dateien ineffizient oder gar nicht laden.

Phase-7-Fund:

- `curl -I --max-time 10 https://data.so.ch/download/ch.so.oev_haltestellen.parquet` schlug in der Agent-Umgebung weiterhin fehl mit `curl: (6) Could not resolve host: data.so.ch`.

## Safari und WebAssembly

Automatisiert geprueft:

- Chromium Headless via Java Playwright fuer DuckDB-Wasm-Initialisierung, same-origin Parquet, lokales Monaco, SQL-Ausfuehrung, Resultat-Export, fehlende sichtbare Diagramme, Fehlerzustand und mobile Overflow-Checks.

Weiterhin manuell/operativ zu pruefen:

- DuckDB-Wasm-Initialisierung.
- Worker- und Wasm-Ladepfade.
- Speichergrenzen bei grossen Dateien.
- CSP-Anforderungen fuer Wasm und Worker.

Chrome, Firefox und Safari sind auf dem lokalen System als Apps vorhanden, wurden in diesem Agent-Lauf aber nicht als kontrollierbare GUI-Browser manuell verifiziert.

## Grosse Dateien und Resultate

Verhalten ab Phase 4:

- Resultate werden begrenzt.
- Die Startabfrage gegen den registrierten View zeigt kein sichtbares `limit`.
- `select`- und `with`-Abfragen ohne Top-Level-`limit` werden clientseitig auf den aktuell gewaehlten Row-Limit-Wert begrenzt. Die UI bietet `100`, `1'000` und `10'000`; Standard ist `1'000`.
- Rezepte mit eigenem `limit` behalten dieses Limit.
- CSV-, XLSX- und Parquet-Export exportieren nur das aktuelle Query-Resultat, nicht die gesamte Quelldatei.
- CSV bleibt ein clientseitiger Serializer mit Semikolon und CRLF. XLSX und Parquet laufen ueber DuckDB-Wasm `COPY` gegen das bereits row-limitierte `executedSql` und werden danach aus dem virtuellen DuckDB-Dateisystem gelesen.
- Nutzertexte duerfen keine serverseitige Ausfuehrung versprechen.
- Diagramme sind aktuell nicht sichtbar; Resultatwerte bleiben fuer Tabelle und Exporte unveraendert.

## Query-Fehler

Verhalten ab Phase 4:

- SQL bleibt sichtbar.
- Fehlermeldungen werden lesbar angezeigt.
- Clientseitige Query-Guards sind UX-Schutz, keine Sicherheitskontrolle.
- Mutation und gefaehrliche DuckDB-Kommandos werden nicht als unterstuetzter Workflow angeboten.
- Erlaubt sind im MVP `select`, `with`, `describe`, `show` und `pragma table_info`.
- Blockiert werden offensichtliche Mutations- und Systemkommandos wie `insert`, `update`, `delete`, `drop`, `alter`, `create table`, `copy ... to`, `attach`, `install`, `load`, `call` und `set`.

## Codebeispiele

Verhalten:

- Backend und Komponenten koennen weiterhin statische Beispiele fuer DuckDB CLI, Python und R erzeugen.
- Im SQL-Labor-Redesign gibt es keinen sichtbaren Tab `Code`.
- Die Beispiele werden nicht im Browser ausgefuehrt. Insbesondere wird keine WebR-Laufzeit geladen.
- Die Beispiele verwenden die primaere Parquet-Tabelle des Datenthemas oder, falls keine primaere Tabelle markiert ist, die erste Parquet-Tabelle.
- Wenn ein Datenthema keine Parquet-Distribution hat, bleiben `codeSnippets` leer und die Explore-Seite zeigt die Nicht-verfuegbar-Meldung.

## Lokale Query-Historie

Verhalten:

- Der History-Code bleibt fuer spaetere Wiederaufnahme vorhanden, ist in der aktuellen Labor-UI aber nicht sichtbar.
- Der Schluessel lautet `datenportal.explore.history.<datasetId>`.
- Es werden maximal 20 Eintraege gespeichert, newest first.
- Gespeichert werden SQL und kleine Metadaten wie Zeit, Rezepttitel, Zeilenzahl und Dauer. Resultatzeilen werden nicht gespeichert.
- Wenn `localStorage` nicht verfuegbar ist, voll ist oder ungueltige Daten enthaelt, duerfen SQL-Ausfuehrung, Resultattabelle und Resultat-Export nicht beeintraechtigt werden.

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

Der Produktions-Build verwendet `SqlMonacoEditor` aus `@sqlrooms/sql-editor@0.28.0`. In Vitest wird dieser Editor gemockt, weil das installierte Paket extensionless interne ESM-Imports verwendet, die der Test-Runner nicht direkt aufloest. Die Query-Guards, Ausfuehrungslogik, Copy-Feedback, Row-Limit-Logik, Schema-Autocomplete-Verdrahtung und Exporthelfer werden unabhaengig davon getestet. Der Editor erhaelt SQLRooms-`tableSchemas` und ein memoisiertes `getLatestSchemas`; die Tabellen-, Spalten-, Keyword- und statischen Funktionsvorschlaege kommen damit aus dem SQLRooms-eigenen Completion-Provider statt aus einem lokalen Datenportal-Fallback. Der DuckDB-Connector wird bewusst nicht an `SqlMonacoEditor` uebergeben, weil SQLRooms `0.28.0` fuer dynamische `duckdb_functions()`-Metadaten intern `createTypedRowAccessor` nutzt, was unter der aktuellen CSP wegen `Function(...)` als `unsafe-eval` blockiert wird. Ein Paket-Upgrade oder ein CSP-sicherer SQLRooms-Fix kann dynamische Funktionsmetadaten spaeter wieder aktivieren.

## SQLRooms Recharts in Tests

`@sqlrooms/recharts@0.28.0` bleibt installiert, weil die Diagrammkomponenten fuer eine spaetere Wiederaufnahme im Code vorhanden sind. Im SQL-Labor-Redesign werden diese Komponenten nicht gerendert; Playwright prueft explizit, dass keine sichtbare Diagrammsteuerung erscheint. Vitest mockt die Recharts-Exports weiterhin, weil `@sqlrooms/recharts` extensionless interne ESM-Imports verwendet, die Vitest in dieser Konfiguration nicht direkt aufloest.
