# Erkunden Architektur-Notizen

Status: SQL-Labor redesign implemented

Dieses Dokument beschreibt den Ist-Zustand des Repositories, die Backend-Integration, die Frontend-Insel und die Architekturentscheidungen fuer die folgenden Erkunden-Phasen.

## Bestehender Anwendungskontext

- Package-Basis: `ch.so.agi.datenportal`
- Laufzeit: Java 25, Spring Boot 4.1.0, Gradle Groovy DSL.
- UI: serverseitig gerenderte JTE-Templates mit HTMX als Progressive Enhancement.
- Header und Breadcrumb: vendorte `so-web-components@0.1.10` unter `src/main/resources/static/vendor/so-web-components/0.1.10/`.
- Katalogquelle: PublishedCatalog-XTF/XML, standardmaessig `published_catalog_full_62_entries.xtf` vom Classpath.
- Katalogzustand: immutable `CatalogSnapshot`, atomar austauschbar ueber `CatalogService`.
- Suche: Lucene-Index im aktiven Snapshot.
- Statische Assets: CSS/JS unter `src/main/resources/static/css` und `src/main/resources/static/js`, mit Cache-Regeln in `StaticAssetCachingConfiguration`.

## Bestehende Routen

Aktuelle Katalog- und Detailrouten:

- `/` und `/datasets`: Katalogseite.
- `/datasets/{identifier}`: normaler Datensatz.
- `/datasets/{identifier}/structure-quality-origin`: Struktur, Qualitaet und Herkunft.
- `/datasets/{identifier}/usage`: Daten verwenden.
- `/series/{seriesIdentifier}`: Datenreihen-Uebersicht.
- `/series/{seriesIdentifier}/issues/current`: aktuelle Ausgabe.
- `/series/{seriesIdentifier}/issues/{issueIdentifier}`: spezifische Ausgabe.

Folgerung fuer die Explore-Routen: Die Explore-Seite muss mit der bestehenden Detailroute fuer normale Datensaetze und mit den bestehenden Ausgaben-Detailrouten sauber zusammenarbeiten. Die technischen Routen sind:

```text
GET /datasets/{datasetId}/explore
GET /datasets/{datasetId}/explore/context.json
GET /series/{seriesIdentifier}/issues/current/explore
GET /series/{seriesIdentifier}/issues/current/explore/context.json
GET /series/{seriesIdentifier}/issues/{issueIdentifier}/explore
GET /series/{seriesIdentifier}/issues/{issueIdentifier}/explore/context.json
```

Die Route verwendet `explore`, nicht `erkunden`. UI-Texte bleiben deutsch.

## Backend-Grenze ab Phase 1

Phase 1 legt die neue Funktion unter `ch.so.agi.datenportal.explore` an. Die Struktur:

```text
ch.so.agi.datenportal.explore
  ExplorePageController
  ExploreContextService
  ExploreTableService
  ExploreRecipeService
  ExploreCodeSnippetService
  ExploreProperties
  dto/view records
```

Controller bleiben duenn und lesen Daten ueber den bestehenden `CatalogService`. Templates erhalten vorbereitete ViewModels und keine Domain- oder Parserlogik.

Die oeffentlichen Explore-Routen sind:

```text
GET /datasets/{datasetId}/explore
GET /datasets/{datasetId}/explore/context.json
GET /series/{seriesIdentifier}/issues/current/explore
GET /series/{seriesIdentifier}/issues/current/explore/context.json
GET /series/{seriesIdentifier}/issues/{issueIdentifier}/explore
GET /series/{seriesIdentifier}/issues/{issueIdentifier}/explore/context.json
```

Normale `DatasetEntry`-Identifier sind nur unter `/datasets/.../explore` gueltig. Konkrete `DatasetIssueEntry`-Identifier sind nur unter der zugehoerigen `/series/{seriesIdentifier}/issues/.../explore`-Route gueltig. Datenreihen-Root-Eintraege, Ausgaben auf der falschen Serie und unbekannte Identifier laufen ueber das bestehende 404-Verhalten.

## Phase-1-Kontext

Der Backend-Kontext folgt `ExploreContextDto` Version `1`.

Phase-1-Quellen:

- Datensatz-Metadaten aus dem aktiven `CatalogSnapshot`.
- Parquet-Tabellen aus `DistributionFormat.PARQUET`.
- Attribute aus `CatalogEntryMetadata.attributes()`, wenn vorhanden.
- Generierte Startrezepte aus den bekannten Tabellen und Spaltenrollen.
- Statische Code-Snippets fuer DuckDB CLI, Python und R.

Wenn ein Datensatz keine Parquet-Distribution hat, rendert die Explore-Seite eine klare Nicht-verfuegbar-Meldung. Der JSON-Kontext bleibt gueltig, enthaelt aber leere `tables`, `recipes` und `codeSnippets`.

Der eingebettete JSON-Kontext wird mit einem kleinen projektlokalen Writer erzeugt und fuer das `application/json`-Script-Element gegen `</script>`-Sequenzen abgesichert. Das vermeidet eine neue JSON-Bibliotheksabhaengigkeit im Application Compile Classpath.

## Frontend-Insel ab Phase 2

Die interaktive Erkunden-Oberflaeche ist als isolierte React/Vite-Insel unter folgendem Pfad angelegt:

```text
src/main/frontend/explore/
```

Die Insel liest den eingebetteten JSON-Kontext aus `#datenportal-explore-context`, validiert ihn mit Zod und rendert in `#datenportal-explore-root`. Sie initialisiert DuckDB-Wasm im Browser, laedt `/catalog/catalog.duckdb`, registriert die Datei im Wasm-Dateisystem, attached sie read-only als Datenbank `catalog`, laedt `httpfs`, setzt `USE "catalog"."opendata"` und aktualisiert daraus die SQLRooms-SchemaTrees. Die Startabfrage verwendet das `opendata`-Schema ohne sichtbares Preview-Limit, zum Beispiel `SELECT * FROM opendata.ch_so_bauinventar;` im Editor auf zwei Zeilen. Die aktuelle UI besteht aus linker SQLRooms-artiger `SCHEMA EXPLORER`-Spalte, editierbarem Monaco-SQL-Editor mit SQLRooms-Completion ueber `tableSchemas` und memoisiertes `getLatestSchemas`, kompakter Beispielabfrage-Auswahl, rotem Run-Button mit Play-Icon, SQL-Copy-Button und Export-Splitbutton fuer CSV, XLSX und Parquet. Der Backend-Kontext kann normale Datensaetze und konkrete Serienausgaben liefern; das Labor selbst zeigt dafuer keine separate Serien-UI. Die XTF bleibt Quelle fuer fachliche Metadaten und Datensatzzuordnung, waehrend der DuckDB-Catalog die sichtbaren Catalog-Views, Spalten und die direkt querybaren Views liefert. Die SQL-Ausfuehrung laeuft direkt gegen die attached read-only Catalog-Datenbank; dadurch koennen Nutzerinnen und Nutzer Views aus mehreren Parquet-Dateien in einer Abfrage joinen. Der aktuell erkundete View ist im Schema Explorer automatisch geoeffnet und markiert; seine Zeilenzahl kommt weiterhin aus dem Explore-Kontext. Lade- und Fehlerzustaende liegen als zentriertes Overlay absolut ueber der Workbench; Ladezustaende nutzen eine weisse shadowfreie Karte auf dunklem Backdrop mit rotem indeterminiertem Progressbar, Fehler bleiben Alerts ohne Progressbar. Sobald die Runtime bereit ist, verschwindet das Overlay ohne Layout-Sprung und ohne globalen `Bereit`-Badge; der obere Workbench-Border bleibt direkt am Workbench-Container erhalten. Der DuckDB-Connector wird fuer Query-Ausfuehrung, Schema-Refresh und Exporte verwendet, aber nicht an `SqlMonacoEditor` uebergeben, weil SQLRooms `0.28.0` fuer dynamische `duckdb_functions()`-Metadaten einen CSP-blockierten `Function(...)`-Pfad nutzt. Auf Desktop nutzt sie `react-resizable-panels`, um Schema/Labor horizontal sowie Editor/Resultat vertikal pro Kontext-Identifier in `localStorage` zu speichern; versionierte Auto-Save-IDs ignorieren alte defekte Panelgroessen. Auf Mobile bleibt die Ansicht gestapelt und nicht resizable. Der Resultatbereich hat eine lokale Umschaltung zwischen Tabelle und Diagramm; es gibt weiterhin keine alten Haupt-Tabs `Vorschau`, `SQL-Labor`, `Diagramm` und `Code`. Codebeispiel- und lokale History-Komponenten bleiben im Code fuer spaetere Wiederaufnahme, werden im Primaerpfad aber nicht gerendert. Seit Phase 8 enthaelt die Insel ausserdem einen stillen Erweiterungspunkt fuer spaetere AI-, WebR-, Vega-, Mosaic- und Geodaten-Funktionen.

Wichtige Dateien:

- `src/main/frontend/explore/src/main.tsx`
- `src/main/frontend/explore/src/app/ExploreApp.tsx`
- `src/main/frontend/explore/src/app/ExploreContextLoader.ts`
- `src/main/frontend/explore/src/app/ExploreContext.ts`
- `src/main/frontend/explore/src/app/SchemaExplorerPanel.tsx`
- `src/main/frontend/explore/src/app/FutureExtensionSlots.tsx`
- `src/main/frontend/explore/src/duckdb/attachCatalogDatabase.ts`
- `src/main/frontend/explore/src/sql/SqlLaboratory.tsx`
- `src/main/frontend/explore/src/recipes/RecipeList.tsx`
- `src/main/frontend/explore/src/results/ResultPanel.tsx`
- `src/main/frontend/explore/src/charts/ChartPanel.tsx`
- `src/main/frontend/explore/src/charts/chartInference.ts`
- `src/main/frontend/explore/src/code/CodeSnippetsPanel.tsx`
- `src/main/frontend/explore/src/sql/QueryHistory.ts`
- `src/main/frontend/explore/src/styles/explore.css`

Das Frontend nutzt npm, React 19, Vite 8, TypeScript, Vitest und Testing Library. SQLRooms DuckDB- und SQL-Editor-Pakete werden fuer DuckDB-Wasm und den SQL-Editor verwendet. Phase 5 verwendet `@sqlrooms/recharts@0.28.0` fuer Recharts-Primitive und SQLRooms-Chart-Wrappers; die Styles bleiben Datenportal-eigene CSS-Tokens. `react-resizable-panels@3.0.6` ist direkte Explore-Abhaengigkeit fuer die SQLRooms-aehnlichen Griffleisten. `@radix-ui/react-scroll-area@1.2.13` ist direkte Explore-Abhaengigkeit fuer die Resultat-Scrollbars, weil native Overlay-Scrollbars Hover auf macOS/Chromium nicht verlaesslich sichtbar machen. `@sqlrooms/ui` wird nicht direkt in Datenportal-Komponenten eingebunden, weil die Datenportal-UI eigene Design-Tokens nutzt.

## SQL-Labor ab Phase 4

- Backend-generierte `ExploreRecipeDto` bleiben Teil des Kontextes. Die primaere Labor-UI zeigt sie als kompakte Beispielabfrage-Auswahl im Toolbar-Bereich; Auswahl laedt SQL in den Editor, fuehrt aber nicht automatisch aus.
- `Ausfuehren` oder `Ctrl/Cmd + Enter` startet die lokale DuckDB-Abfrage.
- Der Monaco-Editor ist sichtbar und editierbar; `readOnly` wird nur waehrend einer laufenden Query gesetzt.
- `SQL kopieren` ist ein sekundar rot gerahmter Button mit stabilem Feedback `✓ SQL kopiert`.
- Jede Abfrage laeuft durch `querySafety`: genau eine read-only-Anweisung, blockierte Mutations-/Systemkommandos und automatische Row-Limit-Begrenzung fuer `select`/`with`, sofern kein Top-Level-`limit` vorhanden ist. Die UI bietet `100`, `1'000` und `10'000` Zeilen an; Standard ist `1'000`.
- Query-Ausfuehrung verwendet den in Phase 3 initialisierten DuckDB-Connector mit `AbortSignal` fuer Timeout und Abbruch.
- Resultate werden standardmaessig als kompakte HTML-Tabelle im SQLRooms-Stil gerendert: sticky Header, Zeilenindex, Typ-Badges im Header, Radix-ScrollArea mit Datenportal-eigenen Hover-/Fokus-Scrollbars und Footerzeile mit Row-Limit-Combobox.
- CSV-Export erzeugt clientseitig eine Semikolon-getrennte CSV-Datei mit CRLF-Zeilenenden. XLSX und Parquet werden in DuckDB-Wasm per `COPY (<executedSql>) TO '<tmp>' WITH (...)` erzeugt und anschliessend aus dem virtuellen DuckDB-Dateisystem heruntergeladen. Alle Exportformate enthalten nur das aktuell gelieferte Query-Resultat, nicht die originalen Quelldateien.

### Generierte Beispielabfragen

Die Beispielabfragen im Dropdown sind kein manuell gepflegter Query-Katalog. `ExploreRecipeService` generiert sie pro `ExploreTableDto` aus den Parquet-Distributionen und den backendseitig bekannten Katalog-/XTF-Attributen samt Spaltenrollen. Das spaeter im Browser gelesene DuckDB-Runtime-Schema bestimmt die sichtbare Schema-Karte und Autocomplete-Spalten, erzeugt aber keine zusaetzlichen Beispielabfragen. Attributnamen werden in Rezepttiteln mit Schweizer Anfuehrungszeichen hervorgehoben, zum Beispiel `Nach «gemeinde» gruppieren`.

| Kategorie | Sichtbarer Typ | Wann entsteht sie? | Anzahl pro Tabelle | Limit | Diagramm-Vorgabe | SQL-Muster |
|---|---|---|---:|---|---|---|
| `preview` | `Vorschau` | immer | 1 | keines | keine | `SELECT * FROM <tabelle>;` |
| `profile` | `Anzahl Datensaetze` | immer | 1 | keines | keine | `SELECT count(*) AS anzahl FROM <tabelle>;` |
| `profile` | `Tabellenstruktur` | immer | 1 | keines | keine | `DESCRIBE <tabelle>;` |
| `quality` | `Fehlende Werte` | wenn die Tabelle Spalten hat | 0 oder 1 | maximal 8 gepruefte Spalten | keine | `count(*) FILTER (WHERE <spalte> IS NULL)` pro Spalte |
| `category` | `Nach «<spalte>» gruppieren` | fuer Textspalten, die keine Identifier sind | 0 bis 3 | `CATEGORY_RECIPE_LIMIT = 3` | `bar` | `GROUP BY <spalte> ORDER BY anzahl DESC LIMIT 50` |
| `numeric` | `«<spalte>» zusammenfassen` | fuer numerische Messwertspalten | 0 bis 3 | `NUMERIC_RECIPE_LIMIT = 3` | keine | `min`, `avg`, `max` fuer eine Spalte |
| `time` | `Zeitreihe nach «<spalte>»` | fuer Datum- oder Jahrspalten | 0 bis 2 | `TIME_RECIPE_LIMIT = 2` | `line` | `GROUP BY <zeitspalte> ORDER BY <zeitspalte>` |
| `custom` | aktuell keines | DTO/Enum ist vorbereitet | 0 | aktuell nicht erzeugt | moeglich | aktuell kein Generatorpfad |

Die Limits greifen unabhaengig voneinander:

| Konstante | Wert | Gilt fuer | Wirkung |
|---|---:|---|---|
| `NULL_PROFILE_COLUMN_LIMIT` | 8 | `Fehlende Werte` | Das Null-Profil zaehlt hoechstens die ersten 8 beschriebenen Spalten. |
| `CATEGORY_RECIPE_LIMIT` | 3 | Gruppierungsqueries | Es werden hoechstens 3 Kategorie-Spalten als `Nach ... gruppieren` angeboten. |
| `NUMERIC_RECIPE_LIMIT` | 3 | Numerikqueries | Es werden hoechstens 3 Messwertspalten als Zusammenfassung angeboten. |
| `TIME_RECIPE_LIMIT` | 2 | Zeitqueries | Es werden hoechstens 2 Datum-/Jahrspalten als Zeitreihe angeboten. |

Die Spaltenrollen entstehen heuristisch in `ExploreColumnRoleDetector`:

- `CATEGORY`: Textspalte (`char`, `text`, `string`, `varchar`), sofern sie nicht als Identifier erkannt wird.
- `MEASURE`: numerischer Typ (`int`, `double`, `float`, `decimal`, `numeric`, `number`, `real`), sofern die Spalte kein Identifier und keine Jahrspalte ist.
- `YEAR`: Spaltennamen `jahr`, `year`, `periode` oder `berichtsjahr`.
- `DATE`: Datentyp mit `date`/`time` oder Spaltennamen `datum`, `date`, `stand`, `stichtag`, `gueltig_ab`, `gueltig_bis`, `updated_at`.

Fuer Diagramme liefern Kategorie- und Zeitrezepte eine `preferredChart`-Vorgabe. Diese Vorgabe wird im Frontend nur verwendet, wenn genau das unveraenderte Rezept-SQL ausgefuehrt wurde. Sobald Nutzerinnen oder Nutzer das SQL aendern oder freies SQL ausfuehren, klassifiziert `chartInference` die Resultatspalten und die angezeigten Werte neu: Jahr-/Datum plus Zahl ergibt eine Linie, Kategorie plus Zahl einen Balken, zwei Zahlenwerte Punkte und ein einzelner Zahlenwert ein Histogramm.

Balken- und Histogramm-Diagramme duerfen nicht gleich behandelt werden:

- Ein Balkendiagramm vergleicht diskrete Kategorien. Die X-Achse enthaelt Textwerte oder benannte Gruppen wie Gemeinden, Parameter oder Statuswerte; die Reihenfolge kommt aus SQL, zum Beispiel `ORDER BY anzahl DESC`. Deshalb geben `category`-Rezepte mit `GROUP BY <spalte>` bewusst `preferredChart = bar` vor.
- Ein Histogramm zeigt die Verteilung numerischer Einzelwerte ueber Wertebereiche. Die X-Achse enthaelt zusammenhaengende Klassen/Bins, nicht die urspruenglichen Kategorien. Es ist passend, wenn das Resultat eine numerische Wertspalte wie `messwert` enthaelt und die Frage lautet, wie haeufig Werte in bestimmten Bereichen vorkommen.
- SQL bleibt die Quelle der Daten. Beim Balkendiagramm liefert SQL die Kategorien und Kennzahlen direkt. Beim Histogramm darf das Frontend nur die Diagramm-Bins fuer die Anzeige bilden; Tabelle und Exporte behalten das unveraenderte Query-Resultat.

Ist-Situation fuer `ch_so_wasserqualitaet_grundwasser`:

| Reihenfolge | Dropdown-Query | Kategorie | Warum vorhanden? |
|---:|---|---|---|
| 1 | `Vorschau` | `preview` | Basisrezept pro Tabelle. |
| 2 | `Anzahl Datensaetze` | `profile` | Basisrezept pro Tabelle. |
| 3 | `Tabellenstruktur` | `profile` | Basisrezept pro Tabelle. |
| 4 | `Fehlende Werte` | `quality` | Die Tabelle hat beschriebene Spalten. |
| 5 | `Nach «messstelle_code» gruppieren` | `category` | Erste als Kategorie erkannte Textspalte. |
| 6 | `Nach «gemeinde» gruppieren` | `category` | Zweite als Kategorie erkannte Textspalte. |
| 7 | `Nach «parameter» gruppieren` | `category` | Dritte als Kategorie erkannte Textspalte; danach stoppt `CATEGORY_RECIPE_LIMIT`. |
| 8 | `«messwert» zusammenfassen` | `numeric` | `messwert` ist eine numerische Messwertspalte. |
| 9 | `Zeitreihe nach «jahr»` | `time` | `jahr` wird anhand des Namens als `YEAR` erkannt. |

Beispiel fuer eine Gruppierungsquery:

```sql
SELECT "gemeinde", count(*) AS anzahl
FROM ch_so_wasserqualitaet_grundwasser
WHERE "gemeinde" IS NOT NULL
GROUP BY "gemeinde"
ORDER BY anzahl DESC
LIMIT 50;
```

Beispiel fuer eine Numerikquery:

```sql
SELECT
  min("messwert") AS minimum,
  avg("messwert") AS durchschnitt,
  max("messwert") AS maximum
FROM ch_so_wasserqualitaet_grundwasser
WHERE "messwert" IS NOT NULL;
```

Beispiel fuer eine Zeitquery:

```sql
SELECT "jahr", count(*) AS anzahl
FROM ch_so_wasserqualitaet_grundwasser
WHERE "jahr" IS NOT NULL
GROUP BY "jahr"
ORDER BY "jahr";
```

## Diagramme

- `ChartPanel` erhaelt ausschliesslich das aktuelle `QueryResultState`; es fuehrt keine eigene SQL-Abfrage aus.
- `chartInference` klassifiziert Resultatspalten aus den angezeigten Zeilen und schlaegt Balken-, Linien-, Punkt- oder Histogramm-Diagramme vor; Pie und Donut stehen als manuelle Kategorie-plus-Wert-Diagrammtypen zur Verfuegung.
- Rezept-`preferredChart` wird nur verwendet, wenn das unveraenderte Rezept-SQL ausgefuehrt wurde. Geaendertes oder manuelles SQL verwendet immer Inferenz aus dem Resultat.
- `ChartPanel` wird ueber die lokale Resultatansicht `Diagramm` angezeigt. Es gibt keinen alten Haupt-`Diagramm`-Tab, keinen Dashboard-Builder und keinen Spec-Editor.
- Diagrammfarben kommen aus den definierten Zusatzfarben `Dunkelblau`, `Hellblau`, `Orange`, `Gold`, `Dunkelgrün` und `Hellgrün`; Rot wird nicht angeboten. `Mehrfarbig` verwendet diese Farben plus passende Blau-, Gruen-, Gelb- und Orange-Ergaenzungen stabil pro Resultat. `Farben neu` mischt die stabile Palette fuer mehrfarbige Balken, Histogramme, Pie und Donut neu. Bei vielen Pie-/Donut-Segmenten wird ein Hinweis angezeigt.
- DuckDB `count(*)` liefert im Browser BigInt-Werte. Fuer Recharts werden nur die Diagrammzeilen in plain JavaScript-Zahlen/Strings normalisiert; Resultattabelle und CSV-Export behalten die originalen Resultatwerte.
- Vitest mockt `@sqlrooms/recharts`, weil das Paket wie `@sqlrooms/sql-editor` extensionless interne ESM-Imports enthaelt, die der Test-Runner nicht direkt aufloest. Typecheck, Vite-Build und Playwright pruefen den echten Produktionspfad.

## Codebeispiele und lokale Historie ab Phase 6

- `ExploreCodeSnippetService` generiert statische Beispiele fuer DuckDB CLI, Python mit DuckDB und R mit `duckdb`.
- Bei mehreren Parquet-Tabellen verwenden die Codebeispiele die primaere Tabelle; ohne markierte primaere Tabelle wird die erste Tabelle verwendet.
- `CodeSnippetsPanel` bleibt im Code, ist im SQL-Labor-Redesign aber nicht sichtbar. Es gibt keine WebR-Laufzeit.
- `QueryHistory.ts` speichert erfolgreiche lokale SQL-Ausfuehrungen pro Datenthema unter `datenportal.explore.history.<datasetId>` in `localStorage`.
- Gespeichert werden SQL, Ausfuehrungszeitpunkt und optionale Metadaten wie Rezepttitel, Zeilenzahl und Dauer. Resultatzeilen werden nie gespeichert.
- Die Historie ist auf 20 Eintraege begrenzt, newest first, bleibt aber im redesignierten Primaerpfad unsichtbar. Fehler beim Lesen oder Schreiben von `localStorage` duerfen die SQL-Ausfuehrung nicht unterbrechen.

## Zukunfts-Hooks ab Phase 8

Der Kontext enthaelt deaktivierte Feature Flags fuer spaetere Erweiterungen:

```json
{
  "aiAssistant": false,
  "webR": false,
  "vega": false,
  "mosaic": false,
  "geospatial": false
}
```

Die Flags werden ueber `datenportal.explore.*-enabled` konfiguriert und bleiben im MVP standardmaessig `false`. `FutureExtensionSlots` rendert bei deaktivierten Flags nichts und importiert keine Zukunftspakete.

Geplante Anschlussstellen:

- AI: spaeter nur hinter Flag, mit begrenztem Kontext, Nutzerfreigabe vor SQL-Ausfuehrung und denselben Query-Guards wie manuelles SQL.
- WebR / r-stats: spaeter als optionales Panel fuer kleine aktuelle SQL-Resultate, nicht fuer direkte grosse Parquet-Verarbeitung.
- Vega-Lite: spaeter als erweiterter Chartmodus nach Recharts V1, nicht als Default.
- Mosaic: spaeter als Advanced-Crossfilter-Labor, weil es Produkt- und Performance-Erwartungen veraendert.
- Geospatial: spaeter zuerst Geometrieprofil und kleine Karten-Vorschau, bevor schwere Kartenframeworks geprueft werden.
- Shareable SQL URLs: spaeter nur SQL und optionale Chart-Konfiguration im URL-Hash, nie Resultatzeilen.

`npm run check:future-deps` prueft, dass keine direkten Zukunftsabhaengigkeiten oder Source-/Bundle-Imports fuer AI, WebR, Vega, Mosaic oder Kartenframeworks aktiv sind. Das vorhandene transitive `react-mosaic-component` stammt aus den bestehenden SQLRooms Shell-/Editor-Abhaengigkeiten und ist nicht `@sqlrooms/mosaic`.

## UX-Hardening ab Phase 7

- Die React-Insel rendert den Runtime-Status als zugängliche Status-/Alert-Region und markiert den Arbeitsbereich waehrend Initialisierung und Registrierung als busy.
- Es gibt keine Haupt-Tabs `Vorschau`, `SQL-Labor`, `Diagramm` und `Code` mehr. Die Browserchecks pruefen stattdessen die Workbench, die Tastaturausloesung des Run-Buttons und das Fehlen der alten Tabs.
- Es gibt keine sichtbaren `Abfrage 1`-, `SQL`- oder `Resultat`-Header mehr; die Bereiche bleiben ueber `aria-label` benannt.
- Browserlokale Ladefehler werden best-effort klassifiziert: DuckDB-Wasm-Start, CORS, Range Requests, HTTP/IO und Parquet-Ladefehler. Die Klassifizierung ist UI-Hilfe und keine Garantie fuer exakte Netzwerkdiagnose.
- Mobile CSS haelt Schema-Spalte, Toolbar, Monaco-Editor und Resultattabelle innerhalb des Viewports; breite Tabellen scrollen lokal statt die Seite zu verbreitern.
- Playwright verwendet weiterhin eine same-origin Parquet-Fixture fuer stabile CI-Pfade und eine absichtlich fehlende Parquet-Fixture fuer den Fehlerzustand.

## Frontend-Asset-Build

Gradle besitzt eigene npm-Tasks:

```text
npmInstallExplore
npmBuildExplore
npmTestExplore
npmTypecheckExplore
```

`processResources` haengt von `npmBuildExplore` ab. Dadurch landen die Vite-Artefakte im normalen Spring-Boot-Classpath:

```text
build/generated-resources/explore/static/explore/assets/explore.js
build/generated-resources/explore/static/explore/assets/explore.css
```

Die oeffentlichen Pfade sind stabil und nicht fingerprinted:

```text
/explore/assets/explore.js
/explore/assets/explore.css
```

`StaticAssetCachingConfiguration` liefert `/explore/**` mit kurzer Cache-Zeit aus. Die JTE-Seite erhaelt die Pfade ueber `ExploreAssetLinks`.

## Datenvertrag

Der Backend-Kontext folgt dem `ExploreContextDto` aus der MVP-Spezifikation und ist seit Phase 1 implementiert.

Wichtige Leitplanken:

- Parquet-Distributionen aus dem bestehenden Katalogmodell sind die Grundlage.
- Tabellen- und Spaltennamen muessen fuer SQL sicher normalisiert werden.
- Wenn Strukturmetadaten fehlen, darf das Frontend spaeter die Laufzeitschema-Information ueber DuckDB ermitteln.
- Keine serverseitige SQL-Ausfuehrung.
- Clientseitige Query-Guards sind UX-Schutz und werden nicht als Sicherheitskontrolle beschrieben.

## Bekannte Risiken

- Route-Konflikte mit bestehenden `/datasets/{identifier}`-Detailseiten.
- CORS- und Range-Request-Verhalten echter Parquet-URLs.
- DuckDB-Wasm-Ladeverhalten in Safari und in restriktiven Browserumgebungen.
- CSP-Erweiterungen fuer Wasm und Worker, falls SQLRooms/DuckDB-Wasm sie ab Phase 3 benoetigt.
- SQLRooms transitive Peer-Warnings mit React 19, insbesondere `react-virtual` und `react-dnd-multi-backend`.
- Das installierte `@sqlrooms/sql-editor@0.28.0` exportiert `SqlMonacoEditor`, aber nicht den in neueren SQLRooms-Dokumenten beschriebenen `SqlCodeMirrorEditor`.
- Vitest kann die extensionless ESM-Internals von `@sqlrooms/sql-editor` und `@sqlrooms/recharts` nicht direkt aufloesen; die Tests mocken diese UI-Pakete und testen die Datenportal-Query- und Chartlogik separat.
- Zukunftsflags sind nur vorbereitete Anschlussstellen. Das Aktivieren eines Flags implementiert noch keine produktive AI-, WebR-, Vega-, Mosaic- oder Kartenfunktion.
