# Erkunden Architektur-Notizen

Status: SQL- und R-Labor implemented

Dieses Dokument beschreibt den Ist-Zustand des Repositories, die Backend-Integration, die Frontend-Insel und die Architekturentscheidungen fuer die folgenden Erkunden-Phasen.

## Bestehender Anwendungskontext

- Package-Basis: `ch.so.agi.datenportal`
- Laufzeit: Java 25, Spring Boot 4.1.0, Gradle Groovy DSL.
- UI: serverseitig gerenderte JTE-Templates mit HTMX als Progressive Enhancement.
- Header und Breadcrumb: vendorte `so-web-components@0.1.10` unter `src/main/resources/static/vendor/so-web-components/0.1.10/`.
- Katalogquelle: PublishedCatalog-XTF/XML, im lokalen `local`- und Testprofil
  explizit `published_catalog_full_62_entries.xtf` vom Classpath; die
  Basiskonfiguration startet ohne explizite Quelle nicht.
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
- Produktive Startrezepte aus Tabellen- und Spaltenmetadaten.

Wenn ein Datensatz keine Parquet-Distribution hat, rendert die Explore-Seite eine klare Nicht-verfuegbar-Meldung. Der JSON-Kontext bleibt gueltig, enthaelt aber leere `tables` und `recipes`.

Der eingebettete JSON-Kontext wird durch den von Spring bereitgestellten Jackson-`ObjectMapper` erzeugt und fuer das `application/json`-Script-Element gegen `</script>`, `<!--` und `-->` abgesichert.

## Frontend-Insel ab Phase 2

Die interaktive Erkunden-Oberflaeche ist als isolierte React/Vite-Insel unter folgendem Pfad angelegt:

```text
src/main/frontend/explore/
```

Die Insel liest den eingebetteten JSON-Kontext aus `#datenportal-explore-context`, validiert ihn mit Zod und rendert in `#datenportal-explore-root`. Sie initialisiert DuckDB-Wasm im Browser, laedt `/catalog/catalog.duckdb`, registriert die Datei im Wasm-Dateisystem, attached sie read-only als Datenbank `catalog`, laedt `httpfs`, setzt `USE "catalog"."opendata"` und aktualisiert daraus die SQLRooms-SchemaTrees. Die Startabfrage verwendet das `opendata`-Schema ohne sichtbares Preview-Limit, zum Beispiel `SELECT * FROM opendata.ch_so_bauinventar;` im Editor auf zwei Zeilen. Die aktuelle UI besteht aus zwei Haupttabs `SQL-Labor` und `R-Labor`. Im SQL-Labor bleibt die linke Spalte der SQLRooms-artige `SCHEMA EXPLORER`; rechts stehen editierbarer Monaco-SQL-Editor, kompakte produktive Beispielabfrage-Auswahl, roter Run-Button mit Play-Icon, SQL-Copy-Button und `Nach R übernehmen`. Der Ergebnis-Header bietet im Tabellenmodus den Split-Export fuer CSV, XLSX und Parquet und im Diagrammmodus einen PNG-Export des sichtbaren Diagramms. Der Backend-Kontext kann normale Datensaetze und konkrete Serienausgaben liefern; das Labor selbst zeigt dafuer keine separate Serien-UI. Die XTF bleibt Quelle fuer fachliche Metadaten und Datensatzzuordnung, waehrend der DuckDB-Catalog die sichtbaren Catalog-Views, Spalten und die direkt querybaren Views liefert. Die SQL-Ausfuehrung laeuft direkt gegen die attached read-only Catalog-Datenbank; dadurch koennen Nutzerinnen und Nutzer Views aus mehreren Parquet-Dateien in einer Abfrage joinen. Der Catalog-Knoten ist im Schema Explorer initial offen, das Schema `opendata` bleibt geschlossen; nach manuellem Aufklappen wird der aktuell erkundete View markiert, aufgeklappt und mit seiner Zeilenzahl aus dem Explore-Kontext angezeigt. Lade- und Runtime-Fehlerzustaende liegen als zentriertes Overlay absolut ueber der Workbench; Ladezustaende nutzen eine weisse shadowfreie Karte auf dunklem Backdrop mit rotem indeterminiertem Progressbar, Runtime-Fehler bleiben Alerts ohne Progressbar. Nicht erreichbare Parquet-Quelldateien beim Schema-Refresh oder beim Ausfuehren einer Query blockieren die Workbench nicht; die Query zeigt stattdessen im Resultatbereich eine neutrale Hauptmeldung mit nachrangigen technischen Details. Sobald die Runtime bereit ist, verschwindet das Overlay ohne Layout-Sprung und ohne globalen `Bereit`-Badge; der obere Workbench-Border bleibt direkt am Workbench-Container erhalten. Der DuckDB-Connector wird fuer Query-Ausfuehrung, Schema-Refresh und Exporte verwendet, aber nicht an `SqlMonacoEditor` uebergeben, weil SQLRooms `0.28.0` fuer dynamische `duckdb_functions()`-Metadaten einen CSP-blockierten `Function(...)`-Pfad nutzt. Auf Desktop nutzt sie `react-resizable-panels`, um Schema/Labor horizontal sowie Editor/Resultat vertikal pro Kontext-Identifier in `localStorage` zu speichern; versionierte Auto-Save-IDs ignorieren alte defekte Panelgroessen. Auf Mobile bleibt die Ansicht gestapelt und nicht resizable. Der SQL-Resultatbereich hat eine lokale Umschaltung zwischen Tabelle und Diagramm; es gibt keine vorbereiteten Code-, History- oder Zukunfts-Slots.

Die URL im Kontext ist snapshotgebunden und enthält den SHA-256-Hash des
aktiven DuckDB-Artefakts: `/catalog/catalog.duckdb?v=<sha256>`. Dadurch
kommen Kontext und ausgelieferte Datei aus demselben Runtime-Stand; der
unversionierte Endpoint bleibt für manuelle Downloads `no-cache`.

Wichtige Dateien:

- `src/main/frontend/explore/src/main.tsx`
- `src/main/frontend/explore/src/app/ExploreApp.tsx`
- `src/main/frontend/explore/src/app/ExploreContextLoader.ts`
- `src/main/frontend/explore/src/app/ExploreContext.ts`
- `src/main/frontend/explore/src/app/SchemaExplorerPanel.tsx`
- `src/main/frontend/explore/src/duckdb/attachCatalogDatabase.ts`
- `src/main/frontend/explore/src/sql/SqlLaboratory.tsx`
- `src/main/frontend/explore/src/results/ResultPanel.tsx`
- `src/main/frontend/explore/src/results/sqlResultSnapshot.ts`
- `src/main/frontend/explore/src/charts/ChartPanel.tsx`
- `src/main/frontend/explore/src/charts/chartInference.ts`
- `src/main/frontend/explore/src/webr/WebRRuntime.ts`
- `src/main/frontend/explore/src/webr/WebRBridge.ts`
- `src/main/frontend/explore/src/webr/RPanel.tsx`
- `src/main/frontend/explore/src/webr/RRecipes.ts`
- `src/main/frontend/explore/src/styles/explore.css`

Das Frontend nutzt npm, React 19, Vite 8, TypeScript, Vitest und Testing Library. SQLRooms DuckDB- und SQL-Editor-Pakete werden fuer DuckDB-Wasm und den SQL-Editor verwendet. Phase 5 verwendet `@sqlrooms/recharts@0.28.0` fuer Recharts-Primitive und SQLRooms-Chart-Wrappers; die Styles bleiben Datenportal-eigene CSS-Tokens. `react-resizable-panels@3.0.6` ist direkte Explore-Abhaengigkeit fuer die SQLRooms-aehnlichen Griffleisten. `@radix-ui/react-scroll-area@1.2.13` ist direkte Explore-Abhaengigkeit fuer die Resultat-Scrollbars, weil native Overlay-Scrollbars Hover auf macOS/Chromium nicht verlaesslich sichtbar machen. `@sqlrooms/ui` wird nicht direkt in Datenportal-Komponenten eingebunden, weil die Datenportal-UI eigene Design-Tokens nutzt.

Der PNG-Export verwendet `html-to-image@1.11.13` ausschliesslich im Browser. Der Aufruf deaktiviert die Font-Einbettung (`skipFonts: true`), weil die globale App-CSS relative Font-URLs importiert, die der Font-Inliner browserabhängig nicht zuverlässig auflösen kann. Vor dem Klonen prüft `ChartExport` mit einem kleinen Vollton-Canvas, ob der Browser Canvas-Pixel unverändert auslesen lässt. Bei fehlendem Kontext, einem Lesefehler oder absichtlich verrauschten Pixeln (zum Beispiel durch Firefox-/LibreWolf-Fingerprinting-Schutz) wird kein Download gestartet; der Ergebnis-Header zeigt stattdessen eine verständliche Handlungsanweisung. Die Diagrammtexte werden weiterhin aus den berechneten Elementstilen gerendert; Titel, Grafik und Legende bleiben unverändert. Die Abhängigkeit erzeugt keine zusätzlichen Backend- oder Kontext-Verträge.

## SQL-Labor ab Phase 4

- Backend-generierte `ExploreRecipeDto` bleiben Teil des Kontextes. Die primaere Labor-UI zeigt sie als kompakte Beispielabfrage-Auswahl im Toolbar-Bereich; Auswahl laedt SQL in den Editor, fuehrt aber nicht automatisch aus.
- `Ausfuehren` oder `Ctrl/Cmd + Enter` startet die lokale DuckDB-Abfrage.
- Der Monaco-Editor ist sichtbar und editierbar; `readOnly` wird nur waehrend einer laufenden Query gesetzt.
- `SQL kopieren` ist ein sekundar rot gerahmter Button mit stabilem Feedback `✓ SQL kopiert`.
- Jede Abfrage laeuft durch `querySafety`: genau eine read-only-Anweisung, blockierte Mutations-/Systemkommandos und automatische Row-Limit-Begrenzung fuer `select`/`with`, sofern kein Top-Level-`limit` vorhanden ist. Die UI bietet `100`, `1'000` und `10'000` Zeilen an; Standard ist `1'000`.
- Query-Ausfuehrung verwendet den in Phase 3 initialisierten DuckDB-Connector mit `AbortSignal` fuer Timeout und Abbruch.
- Resultate werden standardmaessig als kompakte HTML-Tabelle im SQLRooms-Stil gerendert: sticky Header, Zeilenindex, Typ-Badges im Header, Radix-ScrollArea mit Datenportal-eigenen Hover-/Fokus-Scrollbars und Footerzeile mit Row-Limit-Combobox. Der Ergebnis-Header enthält die Tabellenexport-Steuerung; sie bleibt auch im `table-only`-Modus ohne Diagrammumschalter sichtbar.
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

### Generierte R-Beispiele

Die R-Beispiele werden nicht vom Backend erzeugt. `RRecipes.ts` baut sie im Browser aus dem aktuellen `SqlResultSnapshot`, also aus dem Resultat, das explizit aus dem SQL-Labor als Dataframe `daten` uebernommen wurde. Ohne Snapshot gibt es nur ein eigenstaendiges `R-Beispiel`, das WebR und die Ausgabe auch ohne SQL-Result nutzbar macht.

Mit Snapshot entstehen immer `Datenüberblick`, `Spaltenstruktur` und `Fehlende Werte`. Danach waehlt die Logik je hoechstens eine Datum-/Jahrspalte, eine numerische Messwertspalte und eine Kategorie-Spalte. Die numerische Auswahl bevorzugt `numeric`, die Rolle `measure` und Namen wie `messwert`, `wert`, `quote`, `rate` oder `index`; sie bestraft Jahr-/Monat-/Tag-Spalten sowie IDs, Nummern und Codes. Die Kategorie-Auswahl bevorzugt Rollen wie `category`, `municipality` und `label` sowie Namen wie `gemeinde`, `name`, `parameter`, `status` oder `thema`; Codes, IDs und Nummern werden nach hinten sortiert.

Aus diesen Spalten entstehen optional `Numerische Zusammenfassung`, `Histogramm «<messwert>»`, `Boxplot «<messwert>» nach «<kategorie>»` und `Trend «<messwert>» nach «<datum>»`. Histogramm und Boxplot plotten Rohdaten und setzen deshalb im generierten R-Code `plot_limit <- 10000`; der Trend aggregiert zuerst nach Datum/Jahr. Die Rezepttitel verwenden Schweizer Anfuehrungszeichen, damit Spaltennamen im Dropdown gleich markiert sind wie im SQL-Labor.

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
- Ein renderbares Diagramm kann aus dem Ergebnis-Header als PNG exportiert werden. Der Export prüft zunächst die zuverlässige Canvas-Auslese, klont anschließend den Diagrammbereich, behält Titel, Grafik und Pie-/Donut-Legende auf weißem Hintergrund bei, entfernt Steuerfelder, Warnungen, Zeilenlimit-Hinweise und `Farben neu`, verwendet `pixelRatio: 2`, überspringt die browserabhängige Einbettung globaler Fonts und bereinigt den temporären DOM-Klon auch bei Fehlern. Browser mit aktivem Canvas-Fingerprinting-Schutz erhalten keinen unbrauchbaren Download; die Schutzentscheidung des Browsers wird nicht umgangen.
- Vitest mockt `@sqlrooms/recharts`, weil das Paket wie `@sqlrooms/sql-editor` extensionless interne ESM-Imports enthaelt, die der Test-Runner nicht direkt aufloest. Typecheck, Vite-Build und Playwright pruefen den echten Produktionspfad.

## Explore-Kontext V4

Der gemeinsam ausgelieferte Kontext akzeptiert ausschließlich Version `4`.
Neben den Metadaten, Tabellen und produktiven SQL-Rezepten enthält er die
direkten Booleans `chartsEnabled` und `webREnabled`. Es gibt keine generische
Flag-Map, keine Snippet-Liste und keine lokale Query-Historie.

## R-Labor mit WebR

Der Explore-Kontext ist Version `4` und enthaelt `rLaboratory`. Standardwerte:

- Dataframe-Name in R: `daten`
- WebR Runtime: `/webr/0.6.0/`
- WebR Paket-Repository: `/webr-packages/`
- Limits: empfohlen `5'000`, Warnung `10'000`, hart `50'000` Zeilen
- Kuratierte Root-Pakete: `ggplot2`, `dplyr`, `tidyr`, `readr`, `tibble`, `scales`, `RColorBrewer`, `viridisLite`, `jsonlite`

Das R-Labor kann WebR auch ohne SQL-Result initialisieren. Ein fachlicher Dataframe entsteht aber nur durch ein explizit aus dem SQL-Labor uebernommenes Resultat. Aus `QueryResultState.arrowTable` entsteht dann ein typisierter `SqlResultSnapshot` mit Spaltennamen, DuckDB-/Arrow-nahen Typen, Nullable-Info, Rollen und Zeilen. Das Mapping ist konservativ: IDs, `BIGINT`, `DECIMAL`, Geometrien und Binaries werden `character`; 32-bit Integer werden `integer`; Float/Double werden `numeric`; `DATE` wird `Date`; Timestamps werden `POSIXct` in UTC. Der Transfer erfolgt als JSON-Snapshot plus R-Konvertierungsskript. In WebR stehen danach `daten`, `daten_schema` und `attr(daten, "duckdb_schema_json")` zur Verfuegung.

WebR `0.6.0` und der Paketmirror werden same-origin ausgeliefert. `copyWebRRuntime` kopiert `node_modules/webr/dist` nach `/webr/0.6.0/`; `mirrorWebRPackages` erzeugt aus `scripts/webr-packages.lock.json` ein kleines Repository unter `/webr-packages/bin/emscripten/contrib/4.6/`. Browser duerfen fuer WebR keine Requests an `webr.r-wasm.org` oder `repo.r-wasm.org` erzeugen. Die Runtime nutzt `ChannelType.PostMessage`, `interactive: false`, `captureR()` und `webr::canvas()`; COOP/COEP bleibt unveraendert out of scope.

Der R-Editor verwendet aktuell eine robuste Textarea. Monaco-R-Syntaxhighlighting wurde nicht aktiviert, weil die vorhandene Monaco-Integration beim gleichzeitigen SQL- und R-Editor in Chromium Service-Fehler ausloeste. Die Textarea bleibt der sichere Fallback und kann spaeter durch einen isolierten R-Editor ersetzt werden.

Die R-Ausgabe folgt der kompakten SQL-Labor-Flaechenlogik: Konsole und Plot werden ohne sichtbare Paneltitel und ohne breite graue Innen-Gutters gerendert. Die Output-Panes sind mit einem `react-resizable-panels`-Handle getrennt. `Resultat exportieren` sitzt rechts in der R-Konsole und wird erst nach einer R-Ausfuehrung mit tabellarischem Resultat aktiv; `Plot exportieren` sitzt rechts im Plotbereich und wird erst aktiv, wenn ein echter Plot-Canvas vorhanden ist. Placeholder-Texte sind nicht exportierbar und linksbuendig wie andere Konsolenmeldungen.

Das R-Labor kann ohne vorherige SQL-Ausfuehrung starten. In diesem Zustand wird WebR initialisiert, ein eigenstaendiges R-Beispiel angeboten und die linke Datenbasis-Spalte weist nur darauf hin, dass noch kein SQL-Resultat als `daten` uebernommen wurde. Nach einer Uebernahme werden datenbezogene Rezepte generiert. Die Rezeptauswahl bevorzugt fachliche Messwertspalten gegenueber Jahren, IDs, Nummern und Codes; Plotrezepte begrenzen Rohdatenplots intern auf 10'000 Zeilen und setzen Spaltennamen in Titeln mit Schweizer Anfuehrungszeichen.

Die linke R-Datenbasis-Spalte verwendet dieselbe kompakte Titelhierarchie wie der SQL-Schema-Explorer: `DATENGRUNDLAGE` ist der einzige Seitentitel in Grossbuchstaben. `DATA FRAME`, `QUELLE`, Zeilen-/Spaltenanzahl und weitere Angaben bleiben kompakte Metadatenlabels.

## Explore-Erweiterungen

Nicht implementierte Erweiterungen sind kein Bestandteil des Kontextes oder des
Produktionsartefakts. Neue Fähigkeiten benötigen einen konkreten produktiven
Anwendungsfall und einen eigenen Vertrag; der aktuelle Pfad bleibt auf SQL,
Charts und das WebR-Labor begrenzt.

## UX-Hardening ab Phase 7

- Die React-Insel rendert den Runtime-Status als zugängliche Status-/Alert-Region und markiert den Arbeitsbereich waehrend Initialisierung und Registrierung als busy.
- Es gibt keine Haupt-Tabs `Vorschau`, `SQL-Labor`, `Diagramm` und `Code` mehr. Die Browserchecks pruefen stattdessen die Workbench, die Tastaturausloesung des Run-Buttons und das Fehlen der alten Tabs.
- Es gibt keine sichtbaren `Abfrage 1`-, `SQL`- oder `Resultat`-Header mehr; die Bereiche bleiben ueber `aria-label` benannt.
- Browserlokale Ladefehler werden best-effort klassifiziert: DuckDB-Wasm-Start, CORS, Range Requests, HTTP/IO und Parquet-Ladefehler. Source-bezogene Query-Fehler verwenden eine neutrale Hauptmeldung im Resultatbereich; die Klassifizierung ist UI-Hilfe und keine Garantie fuer exakte Netzwerkdiagnose.
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

## Y-Mehrfachauswahl im SQL-Labor

`ChartPanel` verwaltet eine geordnete lokale Y-Liste für Linie/Balken/Punkte
und eine unabhängige Pie-/Donut-Wertespalte. Der Kontext V4 und das einzelne
`preferredChart.y` bleiben unverändert. Neue Resultatobjekte setzen Auswahl,
Farben und Zeilenlimit zurück. Der Farbregisterzustand merkt die Reihenfolge
der erstmals gewählten Attribute bis zum nächsten Resultat.

`chartSeries` erzeugt sichere interne Schlüssel und normalisierte Messwerte;
SQL-Spaltennamen werden nie als Recharts-Pfade oder CSS-Schlüssel eingesetzt.
Scatter verwendet je Reihe gemeinsame interne X-/Y-Felder und entfernt nur
Punkte mit fehlenden Koordinaten. Die Renderer ändern das SQL-Resultat nicht.
Die Reihenlegende liegt im Exportziel, die Checkbox-Auswahl als positioniertes
Portal ausserhalb der abschneidenden Panels. Es gibt keine neue Abhängigkeit.
