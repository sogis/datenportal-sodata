# UI Primitives

Status: verbindlich fuer Chips, Badges und Action Pills in der Datenportal-Webanwendung

Dieses Dokument ist die source of truth fuer kleine UI-Primitive mit grauer oder statusfarbener Flaeche. Es definiert Semantik, Typografie, Farbregeln und verbotene Vermischungen.

Wenn sich andere UI-Dokumente an dieser Stelle widersprechen, gilt dieses Dokument fuer:

- aktive Filter-Chips
- Status-Badges
- Action Pills fuer kompakte Aktionen wie Downloads
- Feature-Status-Icons in der Datenmerkmale-Zeile

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
- `Oeffentlich mit Bedingungen`
- `Struktur beschrieben`
- `Aktuelle Ausgabe`

Regeln:

- `font-size: 16px`
- `font-weight: 400`
- kein Border
- keine interaktive Hover-Behandlung

Status-Varianten:

- neutral: `Struktur beschrieben`, Keyword-/Themen-Labels auf Cards
- positive: `Open Data`
- warning: nicht offene Zugriffsrechte wie `Oeffentlich mit Bedingungen`, `Eingeschraenkt`, `Intern`, `Vertraulich`
- info: `Datensatz`, `Datenreihe`, `Aktuelle Ausgabe`

Lokale Ausnahme fuer Card-Keywords:

- Keyword-/Themen-Labels auf Cards bleiben Teil der neutralen Badge-Familie.
- Sie verwenden jedoch bewusst `font-size: 14px`, damit sie sich unterhalb von Titel und Beschreibung ruhiger einordnen.
- Sie verwenden auf Cards ausserdem nur `6px` horizontales Padding und `min-height: 1.6rem`, damit sie kompakter als Typ- und Status-Badges wirken.

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
- feiner neutraler Border `1px solid var(--dp-color-border)`
- neutrale Flaeche `var(--dp-color-badge)`
- klarer Hover- und Focus-Zustand
- Border bleibt auch auf grossen neutralen Flaechen wie Serien-Ausgaben sichtbar

Nicht verwenden fuer:

- Filterzustand
- reine Statusinformation

### 1.4 Feature-Status-Icon

Zweck:

- zeigt in der Datenmerkmale-Zeile, ob ein Merkmal verfuegbar ist
- ist kein Badge und keine klickbare Aktion

Beispiele:

- Check-Circle fuer `Open Data`, wenn der Eintrag offen ist
- Check-Circle fuer `Attribute beschrieben`, wenn Attribute vorhanden sind
- X-Circle fuer `Daten validiert`, wenn kein Datenmodell vorhanden ist

Regeln:

- verfuegbare Features verwenden als Icon-Farbe `var(--dp-color-status-ok-circle)`
- nicht verfuegbare Features verwenden als Icon-Farbe `var(--dp-color-text-muted)`
- der positive Status-Circle-Token wird nur fuer Icon-Farben verwendet, nicht als Badge-Hintergrund

Nicht verwenden:

- `var(--dp-color-badge-positive-bg)` fuer Feature-Icon-Farben
- andere Badge-Background-Tokens fuer Feature-Icon-Farben
- Status-Badge-Klassen fuer die Datenmerkmale-Icons

## 2. Farbregeln

Die drei Familien unterscheiden sich primaer ueber Semantik und Typografie, nicht ueber zufaellige Grauabstufungen.

Neutrale Hierarchie:

- `surface`: weisse Hauptflaechen wie Cards, Controls und Seitenflaechen
- `surface-subtle`: grosse passive neutrale Flaechen wie das Suchband und ruhige Panel-Hintergruende
- `badge`: kleinere kompakte Primitive wie Filter Chips, neutrale Status-Badges und Action Pills

Pflicht:

- Filter Chips und Action Pills verwenden dieselbe neutrale Flaeche: `var(--dp-color-badge)`
- Action Pills verwenden zusaetzlich einen feinen neutralen Border: `1px solid var(--dp-color-border)`
- neutrale Status-Badges verwenden dieselbe neutrale Flaeche: `var(--dp-color-badge)`
- grosse passive neutrale Flaechen verwenden `var(--dp-color-surface-subtle)`
- `Open Data` verwendet eine positive Badge-Variante
- nicht offene Zugriffsrechte verwenden eine gelbe Warning-Badge-Variante
- `Aktuelle Ausgabe` verwendet eine sachliche Info-/Ink-Variante
- Feature-Status-Icons verwenden `var(--dp-color-status-ok-circle)` fuer verfuegbare und `var(--dp-color-text-muted)` fuer nicht verfuegbare Merkmale

Nicht erlaubt:

- unterschiedliche neutrale Grautoene fuer Filter Chip und Action Pill ohne explizite fachliche Begruendung
- Download-Links als rote Sonderbuttons
- Badge-Background-Tokens wie `var(--dp-color-badge-positive-bg)` als Farbe fuer Feature-Status-Icons

## 3. Mapping im Datenportal

- aktive Filter oberhalb der Resultate = Filter Chips
- `Datensatz` und `Datenreihe` = sachliche Info-/Ink-Status-Badges
- `Struktur beschrieben` = neutrales Status-Badge
- `Open Data` = positives Status-Badge
- `Oeffentlich mit Bedingungen`, `Eingeschraenkt`, `Intern`, `Vertraulich` = Warning-Status-Badge
- `Aktuelle Ausgabe` = Info-Status-Badge
- `CSV`, `XLSX`, `Parquet` = Action Pills
- Keyword-/Themen-Labels auf Cards = neutrale Status-Badge-Familie mit lokaler `14px`-Typografie-Ausnahme
- Datenmerkmale `Open Data`, `Attribute beschrieben`, `Daten validiert` = Feature-Status-Icons mit positivem Gruen bzw. gedämpftem Text-Grauton

## 4. Benennung im Code

Semantische CSS-Familien:

- `.dp-filter-chip`
- `.dp-status-badge`
- `.dp-status-badge--neutral`
- `.dp-status-badge--positive`
- `.dp-status-badge--warning`
- `.dp-status-badge--info`
- `.dp-action-pill`
- `.dp-detail-feature--available .dp-detail-feature__icon`
- `.dp-detail-feature--unavailable .dp-detail-feature__icon`

Bestehende Marker duerfen als Alias bestehen bleiben, wenn Templates oder Tests darauf bauen:

- `.dp-type-badge`
- `.dp-download-link`

## 5. Verbotene Vermischungen

- Ein Download-Link ist kein Chip.
- Ein Status-Badge ist keine Aktion.
- Ein Filter Chip ist kein allgemeines graues Label fuer Keywords oder Metadaten.
- Unterschiede zwischen den drei Familien werden nicht ueber `font-weight: 700` hergestellt.
- Ein Feature-Status-Icon ist kein Status-Badge; es darf keine Badge-Background-Farbe als Icon-Farbe verwenden.

## 6. Filter-Panel-Typografie

- Filter-Einträge in Desktop-Popovern, mobilem Filter-Sheet und No-JS-Fallback verwenden `14px`.
- Aktionsbuttons im Filterkontext, insbesondere `Anwenden`, `Zurücksetzen` und `Ergebnisse anzeigen`, verwenden weiterhin `16px`.
- Diese lokale Primitive ändert die globale `18px`-Body-Typografie nicht.
