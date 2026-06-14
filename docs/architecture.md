# Architektur

## Phase 0

Die Anwendung startet in Phase 0 als kleine Spring-Boot-Webanwendung mit JTE und lokalem HTMX-Asset.
JTE läuft in dieser Phase bewusst im Development-Mode, damit das Bootstrap ohne separate Precompile-Strategie lauffähig ist.

Aktuell materialisierte Pakete:

- `ch.so.agi.datenportal`
- `ch.so.agi.datenportal.web`
- `ch.so.agi.datenportal.web.view`

## Verantwortlichkeiten

- `DatenportalApplication` startet die Webanwendung.
- `CatalogController` rendert die minimale Katalog-Startseite auf `/` und `/datasets`.
- `PageChromeFactory`, `HeaderViewModelFactory` und `BreadcrumbFactory` bereiten das wiederverwendbare Page-Chrome vor.
- JTE rendert Layout und semantische Fallback-Komponenten für Header und Breadcrumb.
- Die CSS-Struktur ist bereits in Token-, Basis-, Layout-, Komponenten- und Katalogdateien getrennt, obwohl fachlich erst ein Minimalzustand umgesetzt ist.

## Bewusste Grenzen

Phase 0 enthält noch keine fachliche Kataloglogik. Insbesondere fehlen:

- Snapshot-/Read-Model
- Parser- oder XTF-spezifische Klassen
- Such- oder Filterlogik
- HTMX-Fragmente mit echter Interaktion
- Detailseiten
- Reload- oder Admin-Endpunkte

## Nächster Sinnvoller Schritt

Die nächste Phase sollte das Domain-Read-Model ohne XTF einführen und die minimale Startseite mit ersten statischen Katalogdaten an echte ViewModels anbinden.
