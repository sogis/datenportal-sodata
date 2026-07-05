# Erkunden

Status: SQL-Labor with catalog-backed Schema Explorer implemented

Erkunden ist ein lokales SQL-Labor pro Datenthema. Die Abfragen laufen im Browser mit DuckDB-Wasm gegen Views aus dem Open-Data-DuckDB-Catalog.

Diese Dokumentation begleitet die Umsetzung des SQLRooms-MVP aus `datenportal-erkunden-sqlrooms-mvp-agent-spec.md`. Der aktuelle Stand rendert Explore-Seiten fuer normale Datensaetze und konkrete Serienausgaben als vollflaechiges, kompaktes SQL-Labor direkt unter Header und Breadcrumb. Die React/Vite-Insel lädt `/catalog/catalog.duckdb`, registriert die Datei in DuckDB-Wasm, attached sie read-only als Datenbank `catalog`, lädt `httpfs`, setzt `USE "catalog"."opendata"` und aktualisiert daraus die SQLRooms-SchemaTrees. Links wird daraus ein lokaler `SCHEMA EXPLORER` im Stil des SQLRooms-Beispiels gerendert; der Catalog-Knoten ist initial offen, das Schema `opendata` bleibt geschlossen, und nach manuellem Aufklappen wird der aktuell erkundete View markiert und mit Spalten angezeigt. Die SQL-Ausfuehrung laeuft direkt gegen die attached Catalog-Datenbank, damit auch Joins zwischen Views verschiedener Parquet-Dateien moeglich sind. Der SQL-Editor verwendet weiterhin SQLRooms-Schema-Autocomplete, rotem `Ausfuehren`-Button, kompakter Beispielabfrage-Auswahl und Exporten fuer CSV, XLSX und Parquet. Der Resultatbereich schaltet zwischen Tabelle und Diagramm; Diagramme verwenden `@sqlrooms/recharts` und visualisieren ausschliesslich das aktuelle SQL-Resultat. Ladezustaende erscheinen als weisses, shadowfreies Overlay-Fenster mit abgedunkeltem Hintergrund und rotem indeterminiertem Ladebalken; Fehlerzustaende erscheinen im gleichen Overlay als Alert ohne Ladebalken. Ein globaler `Bereit`-Badge wird im Erfolgsfall nicht mehr gerendert. Linker Schema Explorer sowie Editor/Resultat sind auf Desktop resizable und werden pro Kontext-Identifier im Browser gespeichert. Codebeispiel- und Query-Historie-Code bleibt fuer spaetere Wiederaufnahme vorhanden, ist in der primaeren Labor-UI aber nicht sichtbar.

## Produktidee

Die Seite soll pro Datenthema eine kleine, nuetzliche Explorationsflaeche anbieten:

- Der gesamte Open-Data-DuckDB-Catalog wird lokal im Browser attached; der aktuelle Datensatz bleibt der Fokus im Baum.
- Die SQL-Ausfuehrung nutzt direkt den attached DuckDB-Catalog; dadurch koennen Views aus mehreren Parquet-Dateien in einer Abfrage gejoint werden.
- `catalog.duckdb` enthaelt ein `opendata`-Schema und eine View pro Parquet-Distribution aus der PublishedCatalog-XTF.
- Die XTF bleibt die Quelle fuer fachliche Metadaten und Datensatzzuordnung. Der DuckDB-Catalog liefert die sichtbaren Views, Spalten und SQLRooms-SchemaTrees.
- Nutzerinnen und Nutzer koennen Tabellen, Attribute, SQL und Resultate in einer dichten Laboroberflaeche erkunden.
- SQL bleibt sichtbar und reproduzierbar.
- Die Arbeitsbereiche koennen auf Desktop wie im SQLRooms-Beispiel per Griffleisten vergroessert oder verkleinert werden.
- Die Startabfrage nutzt das `opendata`-Schema ohne sichtbares `limit`, zum Beispiel `SELECT * FROM opendata.ch_so_bauinventar;` im Editor auf zwei Zeilen; das Resultatlimit wird beim Ausfuehren ueber den Query-Guard angewendet.
- Beispielabfragen werden automatisch aus Tabellen- und Spaltenmetadaten erzeugt; die Auswahl ist absichtlich begrenzt, damit das Dropdown kompakt bleibt. Details stehen in `architecture.md`.
- Resultate koennen als Tabelle oder Diagramm betrachtet werden. Diagramme unterstuetzen Balken, Linien, Punkte, Histogramm, Pie und Donut; Berechnungen bleiben im SQL, abgesehen vom Histogramm-Binning und der Diagramm-Farbzuweisung. Einzelfarben stammen aus den definierten Zusatzfarben; Rot ist keine waehlenbare Diagrammfarbe.
- Resultate koennen als CSV, XLSX und Parquet exportiert werden; exportiert wird nur das aktuell gelieferte Query-Resultat. CSV bleibt wegen Semikolon/CRLF clientseitig, XLSX und Parquet werden per DuckDB-Wasm `COPY` erzeugt.
- Codebeispiele und sichtbare Query-Historie sind aktuell aus der primaeren UI entfernt.
- Lade-, Catalog-, Parquet- und Query-Fehler werden sichtbar und ohne serverseitige SQL-Ausfuehrung behandelt.
- Zukunftsfunktionen bleiben standardmaessig deaktiviert und laden keine schweren Runtime-Pakete.
- Es gibt keine serverseitige SQL-Ausfuehrung und keine gespeicherten Sessions.

## Benennung

Die Nutzeroberflaeche verwendet deutschsprachige Texte und den Begriff `Erkunden`.

Technische Artefakte werden englisch benannt:

- Package: `ch.so.agi.datenportal.explore`
- Controller: `ExplorePageController`
- Host-Routen: `/datasets/{datasetId}/explore`, `/series/{seriesIdentifier}/issues/current/explore`, `/series/{seriesIdentifier}/issues/{issueIdentifier}/explore`
- Kontext-Routen: `/datasets/{datasetId}/explore/context.json`, `/series/{seriesIdentifier}/issues/current/explore/context.json`, `/series/{seriesIdentifier}/issues/{issueIdentifier}/explore/context.json`
- Template: `pages/explore.jte`

Die urspruengliche Spezifikation nennt `/erkunden`. Fuer die Implementierung wird `/explore` als technische Route verwendet. Es gibt im MVP keinen `/erkunden`-Alias, solange dies nicht explizit entschieden wird.

## Grenzen des MVP

Nicht Teil des MVP:

- BI-Plattform oder Dashboard-Builder
- Notebook- oder Jupyter-Ersatz
- serverseitige SQL-API
- AI-Assistent in Produktion
- WebR-Ausfuehrung
- Vega-Lite-Spec-Editor
- Mosaic-Crossfilter-Labor
- Karten- oder Geodatenviewer
- Uploads, Schreibfunktionen oder Mutation der Quelldaten

## Dokumente

- `architecture.md`: Repository-Befunde, Zielgrenzen und technische Anschlussstellen.
- `testing.md`: Teststrategie und Baseline-Befehle.
- `troubleshooting.md`: bekannte technische Risikofelder fuer DuckDB-Wasm und Parquet.
- `progress.md`: Phasenstatus, Testevidenz und Folgeentscheidungen.

## Opendata DuckDB Catalog

`tools/create_opendata_duckdb.py` erzeugt lokal eine DuckDB-Datei mit einem
`opendata`-Schema und je einer View pro Parquet-Distribution aus dem
PublishedCatalog-XTF. Das Artefakt liegt standardmaessig unter
`build/catalog.duckdb`. Die fuer die Anwendung ausgelieferte Fixture-Kopie liegt
unter `spec/fixtures/catalog.duckdb`; lokale Build-Artefakte unter `build/`
werden nicht versioniert.

Die Python-Umgebung braucht Python 3.10 oder neuer und `duckdb==1.5.4`,
passend zur aktuell gespiegelten DuckDB-Wasm-Extension-Version `v1.5.4`. Am
einfachsten wird dafuer ein lokales Virtual Environment unter `build/`
angelegt; dieser Ordner ist ignoriert und wird nicht versioniert:

```bash
python3 -m venv build/duckdb-tools-venv
source build/duckdb-tools-venv/bin/activate
python -m pip install --upgrade pip
python -m pip install duckdb==1.5.4
python tools/create_opendata_duckdb.py
```

Falls lokal nur Apples System-Python 3.9 vorhanden ist, kann eine passende
Tool-Runtime isoliert unter `build/` mit `uv` erzeugt werden:

```bash
mkdir -p build/uv-bin
curl -LsSf https://astral.sh/uv/install.sh \
  | env UV_INSTALL_DIR="$PWD/build/uv-bin" INSTALLER_NO_MODIFY_PATH=1 sh
build/uv-bin/uv python install 3.12
build/uv-bin/uv venv --python 3.12 build/duckdb-tools-venv
build/uv-bin/uv pip install --python build/duckdb-tools-venv/bin/python duckdb==1.5.4
build/duckdb-tools-venv/bin/python tools/create_opendata_duckdb.py
```

Wenn das Virtual Environment bereits existiert, reichen spaeter:

```bash
source build/duckdb-tools-venv/bin/activate
python tools/create_opendata_duckdb.py
```

Ohne Aktivierung kann das Script auch direkt mit dem Python aus dem Virtual
Environment gestartet werden:

```bash
build/duckdb-tools-venv/bin/python tools/create_opendata_duckdb.py
```

Die View-Namen werden aus dem Parquet-Dateinamen gebildet: `.parquet` wird
entfernt und Punkte werden durch `_` ersetzt. Das Script bricht ab, wenn ein
Name kein sicherer DuckDB-Identifier ist oder wenn zwei Parquet-Dateien auf den
gleichen View-Namen fallen.

Die Views koennen ueber das `opendata`-Schema angesprochen werden:

```sql
DESCRIBE opendata.ch_so_wasserqualitaet_grundwasser;
```

Der Browser attached die Datei read-only als `catalog` und setzt den Schema-
Kontext automatisch:

```sql
USE "catalog"."opendata";
DESCRIBE ch_so_wasserqualitaet_grundwasser;
```

In der Anwendung werden die Artefakte oeffentlich ausgeliefert:

- `/catalog/published-catalog.xtf`
- `/catalog/catalog.duckdb`
