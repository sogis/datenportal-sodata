# UI Primitives

Status: verbindlich fuer Chips, Badges und Action Pills in der Datenportal-Webanwendung

Dieses Dokument ist die source of truth fuer kleine UI-Primitive mit grauer oder statusfarbener Flaeche. Es definiert Semantik, Typografie, Farbregeln und verbotene Vermischungen.

Wenn sich andere UI-Dokumente an dieser Stelle widersprechen, gilt dieses Dokument fuer:

- aktive Filter-Chips
- Status-Badges
- Action Pills fuer kompakte Aktionen wie Downloads

## 1. Primitive-Familien

### 1.1 Filter Chip

Zweck:

- zeigt einen ausgewaehlten, entfernbaren Zustand
- ist immer interaktiv

Beispiel:

- `Fachstelle / Amt: Amt fuer Raumplanung x`

Regeln:

- `font-size: 14px`
- `font-weight: 400`
- kein Border
- neutrale Flaeche `var(--dp-color-badge)`
- klarer Hover- und Focus-Zustand

Nicht verwenden fuer:

- reine Informationslabels
- Downloads oder andere Aktionen

### 1.2 Status Badge

Zweck:

- zeigt nicht klickbare Information oder Status
- ist kein Button und kein ausgewaehlter Filterzustand

Beispiele:

- `Datensatz`
- `Datenreihe`
- `Open Data`
- `Struktur beschrieben`
- `Aktuelle Ausgabe`

Regeln:

- `font-size: 16px`
- `font-weight: 400`
- kein Border
- keine interaktive Hover-Behandlung

Status-Varianten:

- neutral: `Datensatz`, `Datenreihe`, `Struktur beschrieben`, Keyword-/Themen-Labels auf Cards
- positive: `Open Data`
- info: `Aktuelle Ausgabe`

### 1.3 Action Pill

Zweck:

- zeigt eine kompakte klickbare Aktion
- ist visuell ruhig, aber als Aktion erkennbar

Beispiele:

- `CSV`
- `XLSX`
- `Parquet`

Regeln:

- `font-size: 18px`
- `font-weight: 400`
- kein Border
- neutrale Flaeche `var(--dp-color-badge)`
- klarer Hover- und Focus-Zustand

Nicht verwenden fuer:

- Filterzustand
- reine Statusinformation

## 2. Farbregeln

Die drei Familien unterscheiden sich primaer ueber Semantik und Typografie, nicht ueber zufaellige Grauabstufungen.

Neutrale Hierarchie:

- `surface`: weisse Hauptflaechen wie Cards, Controls und Seitenflaechen
- `surface-subtle`: grosse passive neutrale Flaechen wie das Suchband und ruhige Panel-Hintergruende
- `badge`: kleinere kompakte Primitive wie Filter Chips, neutrale Status-Badges und Action Pills

Pflicht:

- Filter Chips und Action Pills verwenden dieselbe neutrale Flaeche: `var(--dp-color-badge)`
- neutrale Status-Badges verwenden dieselbe neutrale Flaeche: `var(--dp-color-badge)`
- grosse passive neutrale Flaechen verwenden `var(--dp-color-surface-subtle)`
- `Open Data` verwendet eine positive Badge-Variante
- `Aktuelle Ausgabe` verwendet eine sachliche Info-/Ink-Variante

Nicht erlaubt:

- unterschiedliche neutrale Grautoene fuer Filter Chip und Action Pill ohne explizite fachliche Begruendung
- Download-Links als rote Sonderbuttons

## 3. Mapping im Datenportal

- aktive Filter oberhalb der Resultate = Filter Chips
- `Datensatz` und `Datenreihe` = neutrale Status-Badges
- `Struktur beschrieben` = neutrales Status-Badge
- `Open Data` = positives Status-Badge
- `Aktuelle Ausgabe` = Info-Status-Badge
- `CSV`, `XLSX`, `Parquet` = Action Pills
- Keyword-/Themen-Labels auf Cards = neutrale Status-Badge-Familie

## 4. Benennung im Code

Semantische CSS-Familien:

- `.dp-filter-chip`
- `.dp-status-badge`
- `.dp-status-badge--neutral`
- `.dp-status-badge--positive`
- `.dp-status-badge--info`
- `.dp-action-pill`

Bestehende Marker duerfen als Alias bestehen bleiben, wenn Templates oder Tests darauf bauen:

- `.dp-type-badge`
- `.dp-download-link`

## 5. Verbotene Vermischungen

- Ein Download-Link ist kein Chip.
- Ein Status-Badge ist keine Aktion.
- Ein Filter Chip ist kein allgemeines graues Label fuer Keywords oder Metadaten.
- Unterschiede zwischen den drei Familien werden nicht ueber `font-weight: 700` hergestellt.

## 6. Filter-Panel-Typografie

- Filter-Einträge in Desktop-Popovern, mobilem Filter-Sheet und No-JS-Fallback verwenden `14px`.
- Aktionsbuttons im Filterkontext, insbesondere `Anwenden`, `Zurücksetzen` und `Ergebnisse anzeigen`, verwenden weiterhin `16px`.
- Diese lokale Primitive ändert die globale `18px`-Body-Typografie nicht.
