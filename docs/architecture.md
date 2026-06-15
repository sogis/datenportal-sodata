# Architektur

## Phase 1

Die Anwendung läuft in Phase 1 als serverseitig gerenderte Spring-Boot-Webanwendung mit JTE, lokalem HTMX-Asset und einem ersten immutable Katalog-Read-Model.
JTE läuft in dieser Phase bewusst im Development-Mode, damit das Bootstrap ohne separate Precompile-Strategie lauffähig ist.

Aktuell materialisierte Pakete:

- `ch.so.agi.datenportal`
- `ch.so.agi.datenportal.catalog.domain`
- `ch.so.agi.datenportal.catalog.service`
- `ch.so.agi.datenportal.web`
- `ch.so.agi.datenportal.web.view`

## Verantwortlichkeiten

- `DatenportalApplication` startet die Webanwendung.
- `CatalogService` hält den aktuell aktiven `CatalogSnapshot` in-memory und bietet lesende Zugriffe für Web und spätere Phasen.
- `StaticCatalogFactory` erzeugt einen deterministischen Beispielkatalog für lokale Entwicklung und Tests.
- `CatalogController` rendert die Katalog-Startseite auf `/` und `/datasets` mit echten Top-Level-Einträgen.
- `CatalogPageVmFactory` mappt Domainobjekte in einfache, UI-orientierte ViewModels.
- `PageChromeFactory`, `HeaderViewModelFactory` und `BreadcrumbFactory` bereiten das wiederverwendbare Page-Chrome vor.
- `CatalogSnapshot` kapselt den veröffentlichten Read-Model-Stand inklusive sichtbarer Top-Level-Einträge und identifizierbarer Ausgaben.
- `DatasetEntry`, `DatasetSeriesEntry` und `DatasetIssueEntry` bilden normale Datensätze, Datenreihen und einzelne Ausgaben immutable ab.
- JTE rendert Layout, Page Chrome und eine erste minimale Tabellenansicht mit Downloadbuttons.

## Bewusste Grenzen

Phase 1 enthält bewusst noch keine Laufzeitquelle ausserhalb des Codes. Insbesondere fehlen:

- Parser- oder XTF-spezifische Klassen
- Such- oder Filterlogik
- HTMX-Fragmente mit echter Interaktion
- Detailseiten
- Reload- oder Admin-Endpunkte
- Lucene-Index oder atomischer Reload-Swap

## Nächster Sinnvoller Schritt

Die nächste fachliche Phase sollte die Katalogquelle abstrahieren, XTF/XML parsen und den statischen Snapshot durch ein validiertes Startup-Loading ersetzen.
