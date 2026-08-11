# Erkunden Troubleshooting

Status: SQL- und R-Labor

Dieses Dokument sammelt bekannte Risikofelder fuer die DuckDB-Wasm-, SQLRooms-, WebR- und Parquet-Phasen. Die React-Insel initialisiert DuckDB-Wasm im Browser, laedt `catalog.duckdb`, attached sie read-only als `catalog`, setzt `USE "catalog"."opendata"` und rendert daraus den Schema Explorer. SQL wird direkt gegen die attached Catalog-Views ausgefuehrt, damit auch Joins zwischen mehreren Parquet-Dateien moeglich sind. Das SQL-Labor enthaelt Monaco-Editor, roten `Ausfuehren`-Button, kompakte Resultattabelle, Diagrammansicht, Row-Limit-Combobox und Exporte fuer CSV, XLSX und Parquet. Das R-Labor laedt WebR same-origin und kann auch ohne SQL-Result genutzt werden; ein Dataframe `daten` steht erst nach expliziter Uebernahme aus dem SQL-Labor bereit. Der Explore-Kontext V4 enthält ausschließlich produktive SQL-, Chart- und WebR-Felder.

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
- Enthaelt die CSP fuer Explore-Seiten und `/explore/**` `script-src 'self' 'wasm-unsafe-eval' 'unsafe-eval'`?
- Wurde der Vite-Build mit `base: '/explore/'` ausgefuehrt, damit Worker/Wasm-URLs unter `/explore/assets/` liegen?

Aktueller Stand:

- Die DuckDB-Wasm Worker- und Wasm-Dateien werden lokal ueber Vite `?url` aus `@duckdb/duckdb-wasm` gebuendelt; jsDelivr/CDN-Bundles werden nicht verwendet.
- Die Runtime verwendet weiterhin die stabile DuckDB-Wasm-MVP-Variante. Die EH-Variante wurde erneut in Chromium Playwright angeboten, erreichte aber den Explore-Ready-State nicht; deshalb bleibt EH als Laufzeit-Bundle gesperrt.
- Vite baut EH/COI-Assets weiterhin mit, aber `createLocalDuckDbBundles()` bietet sie der DuckDB-Auswahl nicht an. COI bleibt out of scope, solange keine Cross-Origin-Isolation eingefuehrt wird.
- Der Gradle-Build erzeugt vorgerechnete `.br`- und `.gz`-Dateien fuer Explore-CSS/JS/Wasm und die lokale Parquet-Extension. Spring liefert diese ueber `EncodedResourceResolver` aus, wenn der Browser sie akzeptiert.

## DuckDB Export Extensions

DuckDB-Wasm laedt die HTTPFS-Erweiterung beim Catalog-Attach, die Parquet-Erweiterung beim ersten `read_parquet(...)` oder Parquet-Export und die Excel-Erweiterung beim XLSX-Export. Ohne weitere Konfiguration versucht DuckDB dafuer `https://extensions.duckdb.org/.../*.duckdb_extension.wasm`.

Phase-3-Entscheid:

- Die signierten offiziellen HTTPFS-, Parquet- und Excel-Erweiterungen fuer DuckDB-Wasm `v1.5.4/wasm_mvp` liegen same-origin unter `/explore-extensions/v1.5.4/wasm_mvp/`.
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

## WebR startet nicht

Pruefen:

- Sind `/webr/0.6.0/webr.js`, `/webr/0.6.0/webr-worker.js`, `/webr/0.6.0/R.js`, `/webr/0.6.0/R.wasm`, `libRblas.so` und `libRlapack.so` erreichbar?
- Sind `/webr-packages/bin/emscripten/contrib/4.6/PACKAGES` und `/webr-packages/bin/emscripten/contrib/4.6/PACKAGES.rds` erreichbar?
- Erzeugt der Browser Requests an `webr.r-wasm.org` oder `repo.r-wasm.org`? Das waere ein Regressionssignal; V1 muss same-origin laufen.
- Enthaelt die CSP fuer die Explore-Seite, `/webr/**` und `/webr-packages/**` weiterhin `worker-src 'self' blob:` und `script-src 'self' 'wasm-unsafe-eval' 'unsafe-eval'`?
- Wurde nach Lockdatei-Aenderungen `./gradlew mirrorWebRPackages` bzw. ein Build mit `processResources` ausgefuehrt?

Aktueller Stand:

- WebR `0.6.0` wird ueber `/webr/0.6.0/` ausgeliefert. Der Browser-Loader importiert `webr.js`; `webr.mjs` ist im npm-Dist der Node-ESM-Pfad und darf nicht direkt im Browser importiert werden.
- Der Paketmirror ist auf den R-4.6-Pfad `bin/emscripten/contrib/4.6` gelockt und liefert `PACKAGES`, `PACKAGES.gz` sowie `PACKAGES.rds` same-origin aus. Der alte 4.5-Pfad passt nicht zu WebR `0.6.0` (`R version 4.6.0`).
- Das R-Labor nutzt `ChannelType.PostMessage`, `interactive: false`, `captureR()` und `webr::canvas()`. COOP/COEP und SharedArrayBuffer bleiben out of scope.
- Der echte WebR-Browser-E2E ist opt-in: `./gradlew playwrightTest -Ddatenportal.playwright.webr=true`. Nach CSP- und Paketindex-Fix laeuft der gezielte Smoke in Playwright-Chromium durch; der normale Playwright-Lauf ueberspringt ihn weiterhin aus Laufzeitgruenden und prueft UI-, Transfer- und Same-Origin-Guards ohne echte WebR-Initialisierung.

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
- Source-bezogene Fehler beim Schema-Refresh blockieren die Workbench nicht. Die Query kann erneut ausgefuehrt werden, sobald die Datendatei wieder erreichbar ist.
- Nicht erreichbare Quelldateien beim Ausfuehren einer Query erscheinen im Resultatbereich mit der neutralen Hauptmeldung `Quelldatei nicht erreichbar. Die zugrunde liegende Datendatei konnte momentan nicht geladen werden. Bitte versuchen Sie es später erneut.` Technische Details bleiben nachrangig unter `Technische Details`.
- Playwright prueft eine fehlende same-origin Parquet-Datei als reproduzierbaren Query-Fehlerpfad.

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
- Diagramme visualisieren nur das aktuelle SQL-Resultat. Fehlende Kategorien wie Gemeinden werden nicht kuenstlich mit `0` ergaenzt; die SQL-Abfrage muss solche Zeilen selbst liefern, falls sie angezeigt werden sollen. Resultatwerte bleiben fuer Tabelle und Exporte unveraendert; nur die Diagrammzeilen werden fuer Recharts normalisiert.

## Query-Fehler

Verhalten ab Phase 4:

- SQL bleibt sichtbar.
- Fehlermeldungen werden lesbar angezeigt.
- Nicht erreichbare Parquet-Quelldateien werden nicht als globaler Runtime-Fehler angezeigt, sondern als neutraler Alert im Resultatbereich. SQL-Editor, Schema Explorer und Run-Button bleiben nutzbar.
- Clientseitige Query-Guards sind UX-Schutz, keine Sicherheitskontrolle.
- Mutation und gefaehrliche DuckDB-Kommandos werden nicht als unterstuetzter Workflow angeboten.
- Erlaubt sind im MVP `select`, `with`, `describe`, `show` und `pragma table_info`.
- Blockiert werden offensichtliche Mutations- und Systemkommandos wie `insert`, `update`, `delete`, `drop`, `alter`, `create table`, `copy ... to`, `attach`, `install`, `load`, `call` und `set`.

## Explore-Kontext V4

Der Kontext wird gemeinsam mit Backend und Insel ausgeliefert und muss
Version `4` sowie die direkten Felder `chartsEnabled` und `webREnabled`
enthalten. Eine ältere oder generische Flag-Struktur ist kein unterstütztes
Format. Bei einem Schemafehler erscheint die verständliche
Kontext-Fehlermeldung; ein Fallback auf einen älteren Vertrag erfolgt nicht.

## SQLRooms Editor in Tests

Der Produktions-Build verwendet `SqlMonacoEditor` aus `@sqlrooms/sql-editor@0.28.0`. In Vitest wird dieser Editor gemockt, weil das installierte Paket extensionless interne ESM-Imports verwendet, die der Test-Runner nicht direkt aufloest. Die Query-Guards, Ausfuehrungslogik, Copy-Feedback, Row-Limit-Logik, Schema-Autocomplete-Verdrahtung und Exporthelfer werden unabhaengig davon getestet. Der Editor erhaelt SQLRooms-`tableSchemas` und ein memoisiertes `getLatestSchemas`; die Tabellen-, Spalten-, Keyword- und statischen Funktionsvorschlaege kommen damit aus dem SQLRooms-eigenen Completion-Provider statt aus einem lokalen Datenportal-Fallback. Der DuckDB-Connector wird weiterhin bewusst nicht an `SqlMonacoEditor` uebergeben; die dynamischen `duckdb_functions()`-Metadaten von SQLRooms `0.28.0` duerfen erst wieder aktiviert werden, wenn dieser Pfad explizit mit der Explore-CSP und Browser-Smokes abgedeckt ist. Die fuer WebR noetige `unsafe-eval`-Erlaubnis ist auf Explore-/Runtime-Pfade begrenzt und oeffnet keine externen Script- oder Connect-Quellen.

## SQLRooms Recharts in Tests

`@sqlrooms/recharts@0.28.0` rendert die Diagrammansicht fuer aktuelle SQL-Resultate. Playwright prueft den echten Browserpfad inklusive nicht-schwarzer Balkenfarben, mehrfarbiger Balken sowie Pie-/Donut-Farben; Vitest mockt die Recharts-Exports weiterhin, weil `@sqlrooms/recharts` extensionless interne ESM-Imports verwendet, die Vitest in dieser Konfiguration nicht direkt aufloest.
