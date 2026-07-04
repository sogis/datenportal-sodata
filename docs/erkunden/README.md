# Erkunden

Status: SQL-Labor with result charts implemented

Erkunden ist ein lokales SQL-Labor pro Datenthema. Die Abfragen laufen im Browser mit DuckDB-Wasm direkt auf den Parquet-Dateien.

Diese Dokumentation begleitet die Umsetzung des SQLRooms-MVP aus `datenportal-erkunden-sqlrooms-mvp-agent-spec.md`. Der aktuelle Stand rendert Explore-Seiten fuer normale Datensaetze und konkrete Serienausgaben als vollflaechiges, kompaktes SQL-Labor direkt unter Header und Breadcrumb. Die React/Vite-Insel registriert Parquet-Dateien als DuckDB-Wasm-Views, liest danach per `DESCRIBE` das echte DuckDB-Schema sowie per `count(*)` den echten DuckDB-Rowcount, zeigt links visuelle Schema-Karten mit `Tabelle geladen`-Status, bietet einen editierbaren Monaco-SQL-Editor mit SQLRooms-Schema-Autocomplete, rotem `Ausfuehren`-Button, kompakter Beispielabfrage-Auswahl und Exporten fuer CSV, XLSX und Parquet. Der Resultatbereich schaltet zwischen Tabelle und Diagramm; Diagramme verwenden `@sqlrooms/recharts` und visualisieren ausschliesslich das aktuelle SQL-Resultat. Ladezustaende erscheinen als weisses, shadowfreies Overlay-Fenster mit abgedunkeltem Hintergrund und rotem indeterminiertem Ladebalken; Fehlerzustaende erscheinen im gleichen Overlay als Alert ohne Ladebalken. Ein globaler `Bereit`-Badge wird im Erfolgsfall nicht mehr gerendert. Linke Schema-Spalte sowie Editor/Resultat sind auf Desktop resizable und werden pro Kontext-Identifier im Browser gespeichert. Codebeispiel- und Query-Historie-Code bleibt fuer spaetere Wiederaufnahme vorhanden, ist in der primaeren Labor-UI aber nicht sichtbar.

## Produktidee

Die Seite soll pro Datenthema eine kleine, nuetzliche Explorationsflaeche anbieten:

- Parquet-Dateien des Datenthemas werden lokal im Browser mit DuckDB-Wasm registriert.
- `Tabelle geladen` bedeutet, dass die Parquet-Datei als lokaler DuckDB-Wasm-View im Browser verfuegbar ist.
- Die Katalog-/XTF-Spaltenmetadaten und XTF-Objektzahlen werden nicht als initial sichtbare Schema- oder Rowcount-Werte angezeigt. Katalogdaten dienen nur als Merge-Metadaten fuer passende Runtime-Spalten; nach erfolgreicher Registrierung gewinnen DuckDB-Schema und DuckDB-Rowcount fuer sichtbare Schema-Karte und SQL-Autocomplete.
- Nutzerinnen und Nutzer koennen Tabellen, Attribute, SQL und Resultate in einer dichten Laboroberflaeche erkunden.
- SQL bleibt sichtbar und reproduzierbar.
- Die Arbeitsbereiche koennen auf Desktop wie im SQLRooms-Beispiel per Griffleisten vergroessert oder verkleinert werden.
- Die Startabfrage nutzt den registrierten DuckDB-View ohne sichtbares `limit`, zum Beispiel `SELECT * FROM ch_so_bauinventar;` im Editor auf zwei Zeilen; das Resultatlimit wird beim Ausfuehren ueber den Query-Guard angewendet.
- Beispielabfragen werden automatisch aus Tabellen- und Spaltenmetadaten erzeugt; die Auswahl ist absichtlich begrenzt, damit das Dropdown kompakt bleibt. Details stehen in `architecture.md`.
- Resultate koennen als Tabelle oder Diagramm betrachtet werden. Diagramme unterstuetzen Balken, Linien, Punkte, Histogramm, Pie und Donut; Berechnungen bleiben im SQL, abgesehen vom Histogramm-Binning und der Diagramm-Farbzuweisung. Einzelfarben stammen aus den definierten Zusatzfarben; Rot ist keine waehlenbare Diagrammfarbe.
- Resultate koennen als CSV, XLSX und Parquet exportiert werden; exportiert wird nur das aktuell gelieferte Query-Resultat. CSV bleibt wegen Semikolon/CRLF clientseitig, XLSX und Parquet werden per DuckDB-Wasm `COPY` erzeugt.
- Codebeispiele und sichtbare Query-Historie sind aktuell aus der primaeren UI entfernt.
- Lade-, Parquet- und Query-Fehler werden sichtbar und ohne serverseitige SQL-Ausfuehrung behandelt.
- Wenn Runtime-Schema oder Runtime-Rowcount nicht gelesen werden koennen, bleibt der jeweilige sichtbare Wert leer statt auf potenziell veraltete XTF-Werte zurueckzufallen.
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
