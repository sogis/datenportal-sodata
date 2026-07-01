# Erkunden

Status: Phase 2 frontend island bootstrap implemented

Erkunden ist ein lokales SQL-Labor pro Datenthema. Die Abfragen laufen im Browser mit DuckDB-Wasm direkt auf den Parquet-Dateien.

Diese Dokumentation begleitet die Umsetzung des SQLRooms-MVP aus `datenportal-erkunden-sqlrooms-mvp-agent-spec.md`. Bis Phase 2 existieren die Backend-Kontext-Route und eine eingebettete React/Vite-Insel als sichtbarer Bootstrap. DuckDB-Wasm wird noch nicht initialisiert.

## Produktidee

Die Seite soll pro Datenthema eine kleine, nuetzliche Explorationsflaeche anbieten:

- Parquet-Dateien des Datenthemas werden lokal im Browser mit DuckDB-Wasm registriert.
- Nutzerinnen und Nutzer koennen Tabellen, Attribute, SQL-Rezepte, Resultate und einfache Diagramme erkunden.
- SQL bleibt sichtbar und reproduzierbar.
- Diagramme entstehen aus SQL-Resultaten.
- Es gibt keine serverseitige SQL-Ausfuehrung und keine gespeicherten Sessions.

## Benennung

Die Nutzeroberflaeche verwendet deutschsprachige Texte und den Begriff `Erkunden`.

Technische Artefakte werden englisch benannt:

- Package: `ch.so.agi.datenportal.explore`
- Controller: `ExplorePageController`
- Host-Route: `/datasets/{datasetId}/explore`
- Kontext-Route: `/datasets/{datasetId}/explore/context.json`
- Template: `pages/explore.jte`

Die urspruengliche Spezifikation nennt `/erkunden`. Fuer die Implementierung wird `/explore` als technische Route verwendet. Es gibt im MVP keinen `/erkunden`-Alias, solange dies nicht explizit entschieden wird.

## Grenzen des MVP

Nicht Teil des MVP:

- BI-Plattform oder Dashboard-Builder
- Notebook- oder Jupyter-Ersatz
- serverseitige SQL-API
- AI-Assistent in Produktion
- WebR-Ausfuehrung
- Uploads, Schreibfunktionen oder Mutation der Quelldaten

## Dokumente

- `architecture.md`: Repository-Befunde, Zielgrenzen und technische Anschlussstellen.
- `testing.md`: Teststrategie und Baseline-Befehle.
- `troubleshooting.md`: bekannte technische Risikofelder fuer DuckDB-Wasm und Parquet.
- `progress.md`: Phasenstatus, Testevidenz und Folgeentscheidungen.
