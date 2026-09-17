# UI-Implementation-Contract v5

Status: verbindlich für die Datenportal-Webanwendung ab UI-v4  
Package-Basis: `ch.so.agi.datenportal`  
Gilt für: Spring Boot, JTE, HTMX, Web Components, CSS, ViewModels und UI-Tests

Dieser Contract ist der primäre UI-Vertrag für den LLM-Coding-Agenten. Er ersetzt die früheren Annahmen, dass die Startseite eine marketingartige Hero-/Landingpage ist. Die Startseite ist im MVP eine funktionale Katalogseite.

---

## 1. Referenzen und Verbindlichkeit

Fuer Chips, Badges und Action Pills gilt zusaetzlich `docs/ui-primitives.md` als primitive source of truth. Die neutrale Flaechenhierarchie folgt dort den Ebenen `surface`, `surface-subtle` und `badge`.

### 1.1 Verbindliche Screenshots

| Datei | Rolle | Verbindlichkeit |
|---|---|---|
| `spec/mockups/current/startseite_liste.png` | Ausgangslage für Startseite/Katalogseite in Listenansicht | hoch |
| `spec/mockups/current/cards.png` | Ausgangslage für Startseite/Katalogseite in Kartenansicht | hoch |
| `spec/mockups/current/web-components.png` | Ausgangslage für Header und Breadcrumb via Web Components | hoch |

### 1.2 Interpretationsregel

Die Screenshots sind keine pixelgenauen Blaupausen. Verbindlich sind Struktur, Informationshierarchie, Interaktionslogik und Tonalität. Abstände, Spaltenbreiten und Umbrüche dürfen technisch sinnvoll angepasst werden, solange das Ergebnis klar am jeweiligen Screenshot erkennbar bleibt.

Wenn sich Dokumente widersprechen, gilt diese Reihenfolge:

1. `docs/ui-primitives.md` fuer Filter Chips, Status Badges und Action Pills
2. `docs/ui-implementation-contract.md`
3. `datenportal_webapp_agent_spec_detailed_v4.md`
4. `docs/spec-ui-addendum.md`
5. `AGENTS.md` und Skills im Zielrepo
6. `spec/mockups/current/*.png`
7. ältere Spezifikationen und frühere Mockups

### 1.3 Nicht mehr verbindlich

Die früheren generierten Hero-Startseiten und Detailseiten sind nicht mehr führend. Sie dürfen höchstens noch als sekundäre Stil-Inspiration dienen. Für das MVP gelten:

- keine grosse Landschafts-/Hero-Marketing-Startseite als initiale Ansicht
- keine Datenvorschau auf Detailseiten
- keine roten Sonderbuttons für Datenreihen in der Tabellenansicht
- keine Themen-/Datensatz-Icons in der Tabellenansicht, ausser Plus/Minus für Datenreihen-Expansion

---

## 2. Globaler Page Chrome

Alle öffentlichen Seiten verwenden den offiziellen Header und das offizielle Breadcrumb über die Web Components.

### 2.1 Seitenstruktur

```html
<body class="dp-page">
  <a class="dp-skip-link" href="#main-content">Zum Inhalt springen</a>
  @template.components.webComponentsLoader()
  @template.components.soHeader(page.header())
  @template.components.soBreadcrumb(page.breadcrumb())
  <main id="main-content" class="dp-main">
    ...
  </main>
  @template.components.footer(page.footer())
</body>
```

### 2.1.1 Breitenmodell

Für die öffentliche UI gelten drei Breiten-Tokens mit klar getrennten Aufgaben:

- `--dp-page-max-width`: maximale Breite strukturierter Seiteninhalte wie Katalog, Tabellen, Karten, Detail-Layouts und Footer-Inhalt
- `--dp-control-band-max-width`: gemeinsame Desktop-Breite für Suchfeld und Filterleiste im Katalog
- `--dp-readable-width`: Lesebreite für längere Prosa wie Leadtexte und Beschreibungen

Nicht erlaubt:

- zusätzliche globale Breiten-Tokens ausserhalb dieser drei Rollen
- lokale Kappen, die Result Controls oder Resultate der Katalogseite deutlich unter die Page Width zwingen

Chrome-Regel:

- Header und Breadcrumb sind immer full width.
- Web-Component-Header und -Breadcrumb dürfen keine zusätzliche `max-width`-Begrenzung durch JTE-Wrapper erhalten.
- Semantische Fallbacks für Header und Breadcrumb dürfen Innenabstände haben, aber ebenfalls keine `max-width`-Begrenzung.
- Der Footer steht bei kurzem Inhalt am unteren Viewport-Rand und bleibt bei langem Inhalt im normalen Dokumentfluss; er darf Inhalte nicht als `fixed` Overlay überdecken.

### 2.2 Header

Der Header wird nicht in JTE nachgebaut, sondern über die Web Component integriert. Die visuelle Referenz ist `spec/mockups/current/web-components.png`.

Pflicht:

- Komponente: `so-header` oder die im installierten `so-web-components`-Paket dokumentierte Header-Komponente.
- Navigation links/oben gemäss kantonalem Stil.
- Hauptnavigation enthält mindestens: `Daten`, `Themen`, `Statistiken`, `Karten`, `APIs`, `Über uns`.
- Utility-Navigation enthält mindestens: `Services`, `Verwaltung`, `my.so.ch`.
- Aktiver Bereich bei Katalog- und Detailseiten: `Daten` beziehungsweise der passende Bereich gemäss Component-API.
- Das Projektlogo soll `Datenportal` / `Kanton Solothurn` zeigen, sofern die Component-Konfiguration dies erlaubt.
- Kein selbstgebauter Header darf als Primärlösung verwendet werden.

Fallback:

- Falls die Web Component lokal nicht geladen werden kann, darf ein semantischer Fallback gerendert werden.
- Der Fallback ist nur technische Absicherung, nicht das Designziel.
- Auch der Fallback bleibt full width; nur Innenabstände sind erlaubt.

JTE-Komponente:

```java
package ch.so.agi.datenportal.web.view;

public record HeaderVm(
    String activeSection,
    String logoHref,
    String siteName,
    String siteClaim,
    List<NavItemVm> primaryNav,
    List<NavItemVm> utilityNav,
    String componentConfigJson
) {}
```

```html
@param ch.so.agi.datenportal.web.view.HeaderVm header

<so-header
  active-section="${header.activeSection()}"
  logo-href="${header.logoHref()}"
  site-name="${header.siteName()}"
  config='@raw(header.componentConfigJson())'>
</so-header>

<noscript>
  @template.components.headerFallback(header)
</noscript>
```

Falls die tatsächliche Component-API andere Attributnamen verwendet, muss der Coding-Agent die Namen an die installierte Version anpassen, ohne die fachlichen Anforderungen zu ändern.

### 2.3 Breadcrumb

Das Breadcrumb wird ebenfalls über Web Components umgesetzt. Die visuelle Referenz ist `spec/mockups/current/web-components.png`.

Pflichtpfade:

```text
so.ch > Datenportal > Daten und Statistiken
so.ch > Datenportal > Daten und Statistiken > <Titel>
so.ch > Datenportal > Daten und Statistiken > <Datenreihe> > <Ausgabe>
```

JTE-Komponente:

```java
package ch.so.agi.datenportal.web.view;

public record BreadcrumbVm(List<BreadcrumbItemVm> items) {
    public boolean isEmpty() { return items == null || items.isEmpty(); }
}

public record BreadcrumbItemVm(
    String label,
    Optional<String> href,
    boolean currentPage
) {}
```

```html
@param ch.so.agi.datenportal.web.view.BreadcrumbVm breadcrumb

@if(!breadcrumb.isEmpty())
  <so-breadcrumb>
    @for(var item : breadcrumb.items())
      @if(item.currentPage())
        <so-breadcrumb-item iscurrentpage>${item.label()}</so-breadcrumb-item>
      @elseif(item.href().isPresent())
        <so-breadcrumb-item href="${item.href().get()}">${item.label()}</so-breadcrumb-item>
      @else
        <so-breadcrumb-item>${item.label()}</so-breadcrumb-item>
      @endif
    @endfor
  </so-breadcrumb>
@endif
```

Auch hier gilt: Attributnamen sind an die echte Component-API anzupassen.

### 2.4 Asset-Integration

Die Web Components werden im MVP als vendored static assets oder über den Projekt-Build bereitgestellt. Kein CDN als einzige Produktionsquelle.

Empfohlene Ablage:

```text
src/main/resources/static/vendor/so-web-components/
  so-web-components.js
  so-web-components.css
```

JTE:

```html
<script type="module" src="/vendor/so-web-components/so-web-components.js"></script>
<link rel="stylesheet" href="/vendor/so-web-components/so-web-components.css">
```

Wenn die Component eigene CSS-Tokens verlangt, werden diese in `src/main/resources/static/css/design-tokens.css` gekapselt.

---

## 3. Startseite/Katalogseite

Die Route `/` ist die Startseite und zugleich die Katalogseite. Sie zeigt initial alle sichtbaren Top-Level-Einträge in Listenansicht.

Zusätzliche Route:

- `GET /datasets` rendert denselben Inhalt wie `/`.
- Bei HTMX-Requests darf `/datasets` nur das Resultat-Fragment zurückgeben.

### 3.1 Startzustand

Initialer Zustand:

```text
view=list
q=
themes=[]
offices=[]
publicationDate=[]
expandedSeries=[]
sort=newest
```

Der Screenshot `startseite_liste.png` ist die Referenz für diesen Zustand.

### 3.2 Inhalt oberhalb der Resultate

Pflicht:

- Breadcrumb via Web Component.
- Titel: `Daten und Statistiken`.
- Leadtext:

```text
Finden und nutzen Sie offene Daten, Geodaten und Statistiken des Kantons Solothurn.
Alle Datensätze sind – sofern verfügbar – als Open Data mit freien Lizenzen nutzbar.
```

- Suchfeld mit Placeholder:

```text
Suche nach Datensätzen, Themen, Fachstellen, Schlagworten ...
```

- Suchfeld ist ein GET-Formularfeld mit `name="q"`.
- Suche/Filter funktionieren ohne JavaScript durch normalen Submit.
- HTMX darf für progressive Enhancement verwendet werden.
- Mit JavaScript startet die Suche automatisch ab 3 Zeichen mit ca. `300ms` Debounce und aktualisiert Resultate und Controls via HTMX.
- Queries mit 1-2 Zeichen gelten fachlich als leer und dürfen serverseitig nicht als aktive Suche weitergetragen werden.
- Das Suchfeld verwendet links ein Such-Icon und rechts ein `x`-Icon zum Zurücksetzen der Suche; ein separater Such-Button ist nicht Teil des UI.
- Das Zurücksetzen entfernt nur den Suchbegriff, nicht aktive Filter, Ansicht oder Sortierung.
- Leadtexte nutzen die Lesebreite `--dp-readable-width` von `76ch`.
- Suchfeld und Desktop-Filterleiste teilen sich ein linksbündiges Control-Band mit `min(100%, var(--dp-control-band-max-width))`; der Default-Tokenwert ist `64rem`.
- Im Katalog bleibt der Vertikalrhythmus zwischen den grossen Sektionen auf `--dp-space-5`; zwischen Result Controls und Resultattabelle wird er gezielt auf `--dp-space-4` verdichtet.
- Result Controls und Resultate nutzen weiterhin die volle `--dp-page-max-width`.

---

## 4. Filter

Die Filterdarstellung aus `startseite_liste.png` gefällt und ist die Grundlage. Zusätzlich müssen pro Filter mehrere Werte auswählbar sein.

### 4.1 Filtergruppen

MVP-Filter:

1. Thema
2. Fachstelle / Amt
3. Publikationsdatum

Publikationsdatum wird im MVP als vordefinierter Single-Select mit Presets umgesetzt:

- Letzte 30 Tage
- Letzte 6 Monate
- Dieses Jahr
- Letztes Jahr
- Älter

URL-seitig gilt:

- `modified` enthält höchstens einen Wert.
- Wiederholte oder ungültige Werte werden serverseitig auf den ersten gültigen Preset-Wert normalisiert.
- Ein expliziter "alle"-Zustand wird durch fehlenden Parameter ausgedrückt.

Später kann daraus eine Datumsbereich-Auswahl werden.

### 4.2 Filter-UX

Die Filtergruppen werden serverseitig definiert und in zwei Präsentationen verwendet:

- Desktop: Trigger-Buttons mit HTMX-Popover pro Filtergruppe.
- Mobile: vollhohes Filter-Sheet mit `details`-/Accordion-Sektionen.

Es gibt keinen globalen clientseitigen Filter-State. Entwürfe leben nur in den geöffneten Formularen der Popover oder des mobilen Sheets, bis `Anwenden` ausgelöst wird.

Pflichtverhalten:

- Jede Gruppe zeigt ihren Namen und den Zustand.
- Wenn nichts gewählt ist: `Alle Themen`, `Alle Fachstellen`, `Alle Zeiträume`.
- Wenn Werte gewählt sind: erster Wert plus Anzahl, z. B. `Umwelt +2` oder `3 ausgewählt`.
- Unter oder über der Resultatliste wird eine aktive Filter-Chip-Zeile angezeigt.
- Jeder aktive Chip kann einzeln entfernt werden.
- Es gibt `Filter zurücksetzen`.
- Ausgewählte Filterwerte bleiben bei Suche, Ansichtwechsel und Sortierung erhalten.
- Bei Desktop darf immer nur ein Popover gleichzeitig offen sein.
- `Escape` und Outside-Click schliessen offene Desktop-Popover ohne Übernahme des Draft-State.
- Filter sind keyboard-bedienbar.

### 4.3 URL-Format

Mehrfachwerte werden als wiederholte Query-Parameter codiert:

```text
/datasets?theme=bevoelkerung&theme=umwelt&office=agi&office=afu&view=list
```

Nicht verwenden:

```text
theme=bevoelkerung,umwelt
```

Begründung: Wiederholte Parameter sind robust, HTML-form-kompatibel und in Spring MVC direkt als `List<String>` bindbar.

Zusätzlich gilt:

- `modified` ist ein einzelner Preset-Parameter, z. B. `/datasets?modified=last30`.
- `page` und `size` dürfen im Request-Modell für eine spätere Reaktivierung vorhanden sein, werden im aktuellen MVP-Katalog aber ignoriert und nicht in UI-Links oder Formularen weitergetragen.
- `expanded` wird nur für explizite Row-Toggles verwendet und bei Suche/Filter/Sort/View/Page verworfen.

### 4.4 HTMX-Verhalten

Filterformular:

```html
<form
  class="dp-filter-form"
  method="get"
  action="/datasets"
  hx-get="/datasets"
  hx-target="#dataset-results-shell"
  hx-swap="outerHTML"
  hx-push-url="true">
```

Pflicht:

- `GET /datasets` liefert bei HTMX-Requests das Resultatfragment mit `#dataset-results-shell` als Hauptziel.
- `GET /datasets/filter-popover` liefert genau eine Desktop-Filtergruppe.
- `GET /datasets/mobile-filters` liefert das mobile Filter-Sheet.
- Resultatresponses dürfen zusätzlich `#filter-toolbar`, `#mobile-filter-button`, `#active-filter-chips`, `#results-summary` und `#dataset-results-shell` per `hx-swap-oob` aktualisieren.
- Das Öffnen eines Desktop-Popovers darf keinen Layout-Sprung der Ergebnisliste verursachen.
- Ohne JavaScript bleibt der fachliche GET-Flow über Formulare und `Anwenden` nutzbar.

### 4.5 ViewModels

```java
package ch.so.agi.datenportal.web.view;

public record FilterPanelVm(
    List<FilterGroupVm> groups,
    List<ActiveFilterVm> activeFilters,
    String resetHref,
    boolean hasActiveFilters
) {}

public record FilterGroupVm(
    String id,
    String label,
    String parameterName,
    String collapsedLabel,
    int selectedCount,
    List<FilterOptionVm> options
) {}

public record FilterOptionVm(
    String value,
    String label,
    long resultCount,
    boolean selected,
    boolean disabled
) {}

public record ActiveFilterVm(
    String parameterName,
    String value,
    String label,
    String removeHref
) {}
```

---

## 5. Ansicht-Umschaltung

Es gibt zwei Ansichten:

- `list`: initial und Standard
- `cards`: Kartenansicht

In der sichtbaren Toggle-Beschriftung heisst `cards` im MVP `Kachelansicht`.

Der Toggle ist ein Link-/Button-Paar und darf nicht nur clientseitiger State sein.

```text
/datasets?view=list&...
/datasets?view=cards&...
```

Aktive Ansicht:

- Klasse `is-active`
- `aria-current="true"`

ViewModel:

```java
public enum ViewMode { LIST, CARDS }

public record ViewToggleVm(
    ViewMode current,
    String listHref,
    String cardsHref
) {}
```

Die Sortierung der Result Controls bleibt ein GET-Formular. Der Wechsel der Sortieroption submittet das Formular sofort; ein separater sichtbarer `Sortieren`-Button ist nicht Teil der UI. Die öffentliche Sortierung bietet im MVP nur `Neueste zuerst` und `Titel A-Z`; eine Sortieroption `Relevanz` wird nicht angeboten.

---

## 6. Listenansicht

Referenz: `spec/mockups/current/startseite_liste.png`.

### 6.1 Tabellenstruktur

Pflichtspalten:

```text
Thema / Datensatz | Publiziert | Details | Daten herunterladen
```

Die erste Spalte enthält keine Themen-/Datensatz-Icons mehr. Ausnahme: Datenreihen erhalten ganz links einen Plus-/Minus-Button zum Aufklappen.

In der Desktop-Listenansicht wird die Spalte `Daten herunterladen` mit fester Reserve fuer mindestens drei Standard-Pills `CSV`, `XLSX`, `Parquet` eingeplant. Die Titel-/Beschreibungsspalte nimmt den verbleibenden Platz ein.

### 6.2 Normale Datensatz-Zeile

Pflicht:

- links kein Icon, nur optionaler leerer Spacer zur Ausrichtung mit Datenreihen
- Titel fett in `18px`, darunter Kurzbeschreibung in `16px`
- keine zusätzliche Themenzeile in der Listenansicht
- Publikationsdatum in `18px`
- Info-Link `(i)` zur Detailseite dieses Datensatzes
- Downloadbuttons für verfügbare Formate, insbesondere `CSV`, `XLSX`, `Parquet`, in `18px`
- Wenn API vorhanden ist, zusätzlicher Button `API`

### 6.3 Datenreihen-Root-Zeile

Pflicht:

- links Plus-/Minus-Button
- Klick auf Plus/Minus klappt Ausgaben auf/zu
- Klick auf die Zeile klappt ebenfalls auf/zu
- Klick auf Info-Link oder Download-Link darf die Zeile nicht toggeln
- Titel fett in `18px`, Beschreibung in `16px`
- keine zusätzliche Themenzeile in der Listenansicht
- Publikationsdatum der aktuellen Ausgabe oder der Serien-Aktualisierung in `18px`
- Info-Link führt auf die Detailseite der aktuellen Ausgabe
- Downloads zeigen die aktuellen Ausgaben direkt in derselben Spalte und bleiben in `18px`
- Die Desktop-Breitenaufteilung reserviert auch fuer diese Root-Zeile genug Platz fuer `CSV`, `XLSX`, `Parquet` in einer Zeile plus kleine Reserve

Downloadbeschriftung für Datenreihen-Root:

```text
CSV
XLSX
Parquet
```

oder, falls responsive besser:

```text
CSV
Hinweis auf aktuelle Ausgabe separat ausserhalb des Pills
```

als zweizeiliger Button. Wichtig ist, dass kein roter Sonderbutton `Aktuelle Ausgabe` verwendet wird.

### 6.4 Aufgeklappte Ausgaben

Beim Aufklappen einer Datenreihe erscheinen zusätzliche Tabellenzeilen direkt unter der Root-Zeile.

Pflicht für Ausgabezeilen:

- leicht eingerückt oder visuell als Kindzeile erkennbar
- kein Typ-Badge in der Listenansicht
- Titel: z. B. `Gemeindegrenzen — Ausgabe 2025` in `18px`
- Beschreibung optional, kurz, in `16px`
- Publikationsdatum der Ausgabe in `18px`
- Info-Link zur Detailseite genau dieser Ausgabe
- Downloadbuttons `CSV`, `XLSX`, `Parquet` ohne Klammertext `aktuelle Ausgabe`, in `18px`
- Dieselbe Desktop-Reservierung der Downloadspalte gilt auch fuer aufgeklappte Ausgabezeilen

### 6.5 Progressive Enhancement für Row Click

Das Plus-/Minus-Verhalten muss ohne eigenes JavaScript über einen Link/Button funktionieren.

Zusätzlich darf die bestehende kleine JavaScript-Insel `src/main/resources/static/js/catalog-filters.js` den Zeilenklick komfortabler machen.

Regel für die JS-Insel:

- Event Delegation auf `tr[data-series-expand-href]`
- Ignoriere Klicks auf `a`, `button`, `input`, `select`, `textarea`, `label`, Formelemente und `[data-no-row-toggle]`
- Löst intern den Plus-/Minus-Button aus
- Keine weitere UI-Logik oder Client-State in dieser Datei

### 6.6 Listen-ViewModels

```java
public record ResultsVm(
    int totalElements,
    ViewMode viewMode,
    String listHref,
    String cardsHref,
    SortMode sortMode,
    List<EntryRowVm> rows,
    List<EntryCardVm> cards
) {}

public record EntryRowVm(
    String id,
    String title,
    String description,
    String typeLabel,
    String publicationDateLabel,
    String detailHref,
    List<DownloadLinkVm> downloads,
    boolean series,
    boolean expandable,
    boolean expanded,
    String expandHref,
    List<IssueRowVm> issueRows
) {}

public record IssueRowVm(
    String id,
    String title,
    String description,
    String typeLabel,
    String publicationDateLabel,
    String detailHref,
    AccessStateVm accessState,
    List<DownloadLinkVm> downloads
) {}
```

`contextLabel` ist optional und nicht für den MVP-Download-Pill der aktuellen Ausgabe vorgesehen.

### 6.7 JTE-Komponenten

```text
src/main/jte/pages/catalog.jte
src/main/jte/fragments/catalogResults.jte
src/main/jte/components/filterBar.jte
src/main/jte/components/activeFilterChips.jte
src/main/jte/components/viewToggle.jte
src/main/jte/components/resultControls.jte
src/main/jte/components/resultsShell.jte
src/main/jte/components/mobileFilterButton.jte
src/main/jte/components/queryStateInputs.jte
src/main/jte/components/filterFieldList.jte
src/main/jte/components/entryTable.jte
src/main/jte/components/entryRow.jte
src/main/jte/components/issueRow.jte
src/main/jte/components/downloadButton.jte
src/main/jte/fragments/filterPopover.jte
src/main/jte/fragments/mobileFilters.jte
```

---

## 7. Kartenansicht

Referenz: `spec/mockups/current/cards.png`.

### 7.1 Card-Struktur

Die Card-Ansicht zeigt ein Grid. Die Card folgt dem Screenshot `cards.png`.

Pflicht je Card:

- blaues Info-Typ-Badge `Datensatz` oder `Datenreihe`, im gemeinsamen `dp-status-badge--info`-Stil
- der Typ-Badge in Cards zeigt als Prefix ein Datei-Icon fuer `Datensatz` und ein Collection-Icon fuer `Datenreihe`, vertikal mittig zum Text ausgerichtet
- keine zusätzlichen Access- oder Struktur-Badges; nicht offene Eintraege signalisieren fehlende Downloads weiterhin durch ein Schloss im Downloadbereich
- Titel `18px` und Beschreibung `18px` wie im Screenshot
- graue Keyword-/Themen-Badges mit `14px`
- Formate als kompakte Textlinks oder Buttons
- Downloads, Trennlinie, Datum und Detailpfeil bilden einen gemeinsamen Bottom-Cluster, der bei gleich hohen Cards bündig am unteren Rand sitzt
- zwischen Keyword-Badges und dem Bottom-Cluster liegt mindestens `var(--dp-space-5)` Abstand
- Aktualisierungs-/Publikationsdatum unten links
- roter Pfeil/Link unten rechts zur Detailseite

### 7.2 Card-ViewModel

```java
public record EntryCardVm(
    String id,
    String title,
    String description,
    String typeLabel,
    AccessStateVm accessState,
    List<String> keywords,
    List<DownloadLinkVm> downloads,
    String dateLabel,
    String detailHref
) {}
```

### 7.3 JTE-Komponenten

```text
src/main/jte/components/entryCardGrid.jte
src/main/jte/components/entryCard.jte
src/main/jte/components/statusBadge.jte
src/main/jte/components/chipList.jte
```

---

## 8. Detailseiten

Die bisherigen Detailseiten-Mockups sind nicht mehr verbindlich. Der Agent soll eine ruhige, fachlich klare Detailseite gestalten, die nur Informationen aus dem Datenmodell und den Distributionen verwendet.

Keine Datenvorschau im MVP.

### 8.1 Detailseite für normalen Datensatz

Pflichtbereiche:

1. Header/Breadcrumb via Web Components
2. Titel
3. Der Hero der normalen Datensatz-Detailseite rendert aktuell keine Badge-Zeile.
4. Kurzbeschreibung/Abstract
5. Vertikale, ungerahmte Bereiche `Downloads` und danach `Datenmerkmale`, ohne Card-Hintergrund, Border, Radius oder Padding und mit derselben Abschnittslogik wie `Übersicht`; `Datenmerkmale` enthält `Open Data`, `Attribute beschrieben` und `Datenmodell vorhanden`, `Downloads` enthält `CSV`, `XLSX` und `Parquet`; nicht offene Eintraege zeigen stattdessen ein Schloss
6. Metadatenbereiche:
   - Übersicht
   - Zeitliche Abdeckung
   - Themen und Schlagworte
   - spätere Bereiche: Verantwortlichkeit, Nutzung und Lizenz, Ressourcen und Formate, Struktur / Datenmodell, Kontakt
7. Footer/Statuszeile

Temporäre Umsetzungsnotiz:

- Die aktuelle MVP-Iteration zeigt auf der normalen Datensatz-Detailseite den oberen Bereich mit Hero, die vertikal gestapelten, ungerahmten Bereiche `Downloads` und `Datenmerkmale` ohne zusätzliches Padding, die ersten drei ungerahmten Metadatenbereiche (`Übersicht`, `Zeitliche Abdeckung`, `Themen und Schlagworte`) und ein rechtes vertikales Seitenpanel `Daten nutzen` (`Struktur, Qualität und Herkunft`, `Verwenden`, `Erkunden`) mit sekundären Textlinks. Downloads, Datenmerkmale und Übersicht folgen derselben Abschnittslogik; das Seitenpanel beginnt auf Desktop auf derselben Höhe wie `Downloads`.
- `Datenmodell vorhanden` zeigt ausschliesslich `metadata.model().isPresent()` an. Ein Qualitätsreport ohne Datenmodell aktiviert das Merkmal nicht; es wird keine durchgeführte oder erfolgreiche Validierung behauptet.
- Die Datenmerkmale `Open Data`, `Attribute beschrieben` und `Datenmodell vorhanden` verwenden Feature-Status-Icons: verfuegbare Merkmale nutzen `var(--dp-color-status-ok-circle)`, nicht verfuegbare Merkmale nutzen den gedämpften Grauton `var(--dp-color-text-muted)`. Diese Icons sind keine Status-Badges; Badge-Background-Tokens wie `var(--dp-color-badge-positive-bg)` sowie rote oder gelbe Warning-Farben duerfen nicht als Icon-Farbe verwendet werden.
- Zusätzlich zeigt die normale Datensatz-Detailseite die Card `Zuständigkeiten und Kontakt` direkt nach `Themen und Schlagworte`. Sie verwendet dieselben Metadaten-Card-Styles und enthält `Datenproduzent`, `Kontakt` und `Herausgeber`.
- Die frühere Card `Übrige Informationen` wird auf Datensatz- und Ausgabe-Detailseiten nicht mehr gerendert. `Erhebungs- / Messmethode`, `Hilfsdaten`, `Weitere Verwendungen` und `Verfügbare Daten ab` gehören zur Subseite `Struktur, Qualität und Herkunft`.
- Die tieferen Metadatenbereiche unterhalb dieser Cards sind auf dieser Seite vorübergehend ausgeblendet und werden in einer Folgephase wieder integriert.

Die Subseite `Struktur, Qualität und Herkunft` ist serverseitig gerendert und wird ueber das Seitenpanel `Daten nutzen` aufgerufen. Sie zeigt:

- Titel `Struktur, Qualität und Herkunft` in derselben H1-Groesse und Farbe wie Detailseiten.
- KPI-Reihe vor der Attributtabelle mit `Validierung`, `Objekte` und `Attribute`. Die Werte stammen aus `qualitySummary` und `structureSummary`; ohne `qualitySummary` zeigt `Validierung` den Status `Nicht prüfbar` und den Hinweis `Kein Datenmodell`. Die KPI-Cards verwenden eingebettete Bootstrap-SVG-Icons und keine grüne Erfolgsfarbe.
- Attribute als plain table ohne Card. Wenn keine Attribute beschrieben sind, erscheint `Für dieses Datenthema sind keine Attribute beschrieben.`
- Card `Qualität` mit dem Datenmodell als Text, falls vorhanden, und einem Validierungsreport-Link nur dann, wenn `qualitySummary` vorhanden ist. Der sichtbare Reportname wird aus dem URI-Pfad gelesen; ohne sinnvollen Dateinamen wird `Validierungsreport` angezeigt. Es gibt keine Platzhalter-Links wie `href="#"`. Ohne Datenmodell erscheint `Für dieses Datenthema ist kein Datenmodell hinterlegt. Ohne Datenmodell kann die Struktur nicht automatisiert geprüft oder validiert werden.`
- Optionale Card `Herkunft & Verwendung` mit `Erhebungs- / Messmethode`, `Hilfsdaten`, `Weitere Verwendungen` und `Verfügbare Daten ab`.
- Cards unterhalb der Attributtabelle nutzen dieselbe Breite wie die Cards auf Detailseiten; die Attributtabelle bleibt full-width.

Die Subseite `Daten verwenden` ist serverseitig gerendert und wird ueber das Seitenpanel `Daten nutzen` aufgerufen. Sie zeigt:

- Titel `Daten verwenden` ohne Untertitel, ohne Kapitelnummern.
- Keine rechte Spalte und kein sichtbares Seitenpanel; die sichtbaren Inhalte bleiben aber auf der Hauptspaltenbreite der Detailseiten.
- Abschnitt `Direktzugriff` als Tabelle in derselben Breite und Schriftgroesse wie die Attributtabelle, aber ohne horizontale Scrollbar. Der Tabellenkopf verwendet `font-weight: 700`; Tabelleninhalte bleiben normalgewichtig.
- Die Tabelle zeigt die Spalten `Format`, `Beschreibung` und `Aktion`; eine separate Spalte `Inhalt` wird nicht gerendert.
- Formatwerte verwenden ein graues, gebordertes Badge-Styling auf Basis der neutralen Design-Tokens.
- CSV-Details verwenden `Trennzeichen: Semikolon (;) · Texttrenner: Kein Texttrenner · Encoding: UTF-8`.
- Aktionen `Herunterladen` und `URL kopieren` verwenden eingebettete Bootstrap-SVG-Icons, transparente Flaeche und `1px solid var(--dp-color-border)`. `URL kopieren` kopiert die vorhandene URL in die Zwischenablage und zeigt kurz `URL kopiert` mit gruenem Check-Icon.
- Abschnitt `Codebeispiele` mit Tabs `cURL`, `Python`, `DuckDB` im Tab-Styling des Datenblatt-Editors. Codetext verwendet den lokal vendorten Font `JetBrains Mono`; Bash-Beispiele sind so umbrochen, dass keine horizontale Scrollbar noetig ist.
- Codebeispiel-Copy verwendet dasselbe Clipboard-zu-Check-Feedback.
- Keine vorbereiteten Starter-Rezepte ohne echte Ziel-URLs. Ein Link auf `#` ist nicht zulässig.
- Keine Card `Hinweise zur Verwendung`.

### 8.2 Detailseite für Datenreihen und Ausgaben

Wichtig: In der Liste zeigt der Info-Link der Root-Datenreihe auf die Detailseite der aktuellen Ausgabe.

Die Serienübersicht unter `/series/{seriesIdentifier}` ist bewusst schlank:

- Titel der Datenreihe
- Beschreibung der Datenreihe
- Intro-Absatz `Zu dieser Serie sind folgende Ausgaben verfügbar:`
- Ausgaben-Card ohne sichtbaren Titel innerhalb der Card
- aktuelle Ausgabe immer zuoberst
- weitere Ausgaben alphanumerisch absteigend, z. B. `foo 2025` vor `foo 2024`

Die Serienübersicht zeigt keinen separaten Downloadbereich, keine Current-Issue-Hinweiskarte und keine Metadatenkarten.

Die Detailseite einer Ausgabe zeigt den Serienkontext nur im oberen Kicker und übernimmt danach die Card-Struktur der normalen Datensatz-Detailseite:

- Kicker `Datenreihe: <Titel>` mit Link zur Serienübersicht
- Titel der Ausgabe, bei der aktuellen Ausgabe mit Info-Badge `Aktuelle Ausgabe` als Titel-Suffix
- Beschreibung der Ausgabe
- Vertikale, ungerahmte Bereiche `Downloads` und danach `Datenmerkmale` der konkreten Ausgabe, ohne Card-Hintergrund, Border, Radius oder Padding analog zu `Übersicht`
- Metadaten-Cards analog zur normalen Datensatz-Detailseite
- Card `Weitere Ausgaben` als schlanke Linkliste ohne Downloads; die verlinkten Ausgabentitel sind rote Textlinks analog zu Metadatenlinks wie `Lizenz`
- rechtes Seitenpanel `Daten nutzen` analog zur normalen Datensatz-Detailseite

Auch auf Ausgaben-Detailseiten gilt fuer die Datenmerkmale: verfuegbare Feature-Icons verwenden `var(--dp-color-status-ok-circle)`, nicht verfuegbare Feature-Icons verwenden `var(--dp-color-text-muted)`. Badge-Background-Tokens bleiben Status-Badges vorbehalten und duerfen nicht fuer diese Icon-Farben eingesetzt werden.

Die Ausgaben-Detailseite zeigt keinen separaten Hinweis `Diese Ausgabe ist aktuell`, keine Download-Duplikate in `Weitere Ausgaben` und keine generische Metadatenliste unterhalb der Ausgabenliste. Falls es keine andere Ausgabe derselben Reihe gibt, zeigt die Card den Hinweis `Zu dieser Datenreihe sind keine weiteren Ausgaben verfügbar.`

### 8.3 Vorgeschlagene URLs

```text
GET /datasets/{identifier}
  Detailseite eines normalen Datensatzes.

GET /series/{seriesIdentifier}
  Vereinfachte Übersicht einer Datenreihe mit Ausgabenliste.

GET /series/{seriesIdentifier}/issues/current
  Detailseite der aktuellen Ausgabe einer Datenreihe.

GET /series/{seriesIdentifier}/issues/{issueIdentifier}
  Detailseite einer spezifischen Ausgabe.

GET /datasets/{identifier}/structure-quality-origin
  Struktur, Qualität und Herkunft eines normalen Datensatzes.

GET /series/{seriesIdentifier}/issues/current/structure-quality-origin
  Struktur, Qualität und Herkunft der aktuellen Ausgabe einer Datenreihe.

GET /series/{seriesIdentifier}/issues/{issueIdentifier}/structure-quality-origin
  Struktur, Qualität und Herkunft einer spezifischen Ausgabe.
```

Alternativ darf der Agent eine slugbasierte Variante verwenden, wenn die Identifiers stabil und URL-sicher normalisiert werden. Wichtig ist die fachliche Trennung von Datensatz und Datenreihen-Ausgabe.

### 8.4 Detail-ViewModels

```java
public record EntryDetailPageVm(
    PageChromeVm chrome,
    Optional<String> seriesTitle,
    Optional<String> seriesHref,
    String title,
    String description,
    AccessStateVm accessState,
    String structureQualityOriginHref,
    String exploreHref,
    String usageHref,
    boolean currentIssue,
    List<DetailFeatureVm> features,
    List<DownloadLinkVm> downloads,
    MetadataSectionVm overview,
    MetadataSectionVm temporalCoverage,
    MetadataSectionVm topics,
    MetadataSectionVm responsibilitiesContact,
    List<RelatedIssueVm> relatedIssues
) {}

public record SeriesDetailPageVm(
    PageChromeVm chrome,
    String title,
    String description,
    List<SeriesIssueVm> issues
) {}

public record StructureQualityOriginPageVm(
    PageChromeVm chrome,
    String title,
    List<KpiVm> kpis,
    List<AttributeRowVm> attributes,
    String emptyAttributesText,
    QualityVm quality,
    Optional<MetadataSectionVm> originUsage
) {}

public record RelatedIssueVm(
    String issueLabel,
    String title,
    String publicationDateLabel,
    String detailHref,
    boolean current
) {}

public record MetadataSectionVm(
    String id,
    String title,
    List<MetadataItemVm> items
) {}

public record MetadataItemVm(
    String label,
    List<MetadataLineVm> lines
) {}

public record MetadataLineVm(String value, Optional<String> href) {}

public record SeriesIssueVm(
    String issueLabel,
    String title,
    String publicationDateLabel,
    String detailHref,
    boolean current,
    AccessStateVm accessState,
    List<DownloadLinkVm> downloads
) {}
```

### 8.5 Detail-JTE

```text
src/main/jte/pages/entryDetail.jte
src/main/jte/components/detailHero.jte
src/main/jte/components/metadataSection.jte
src/main/jte/components/relatedIssuesCard.jte
```

---

## 9. CSS-Designregeln

### 9.1 Tonalität

- Offiziell, ruhig, sachlich.
- Weiss als Grundfläche.
- Dunkles Blau/Grau für Text.
- Rot nur für Aktionen, aktive Zustände, Links und Fokusakzente.
- Grau für Typ-Badges und Keyword-Chips.
- Keine starken Schatten.
- Keine stark gerundeten Karten.
- Keine verspielten Icons.

### 9.2 CSS-Struktur

```text
src/main/resources/static/css/
  design-tokens.css
  base.css
  layout.css
  components.css
  catalog.css
  detail.css
```

### 9.3 Typografie

- Die öffentliche UI verwendet `18px` als Standard-Schriftgrösse auf `body`.
- Diese Basis wird nicht auf `html` gesetzt, damit `rem`-basierte Layout-, Abstand- und Breiten-Tokens stabil bleiben.
- Im Katalog verwenden Filter-Trigger, Filter-Reset, Mobile-Filter-Button, Resultatsummary, View-Switcher sowie der komplette Sortierblock `16px`.
- Das Suchfeld bleibt typografisch eigenständig und wird nicht auf `16px` abgesenkt.
- In der Listenansicht verwenden Tabellenheader `18px`, Titel `18px`, Beschreibungen `16px`, Publikationsdatum `18px` und Downloadbuttons `18px`.
- In der Kartenansicht verwenden Card-Titel `18px`, Card-Beschreibungen `18px`, Keyword-Badges `14px`, Typ-/Status-Badges `16px`, Downloadbuttons `18px` und Schloss-Hinweise `24px`.
- Tabellenheader in der Listenansicht duerfen innerhalb ihrer eigenen Spalte umbrechen, aber nicht in Nachbarspalten hineinlaufen.

### 9.4 Typ-Badges

Wenn `Datensatz`- oder `Datenreihe`-Badges gerendert werden, verwenden sie denselben sachlichen Info-/Ink-Status-Badge-Stil. Die konkrete Primitive-Definition liegt in `docs/ui-primitives.md`. Dies gilt weiterhin für Kartenansicht und Detailseiten:

```css
.dp-type-badge {
  background: var(--dp-color-badge-info-bg);
  color: var(--dp-color-badge-info-text);
  border: 0;
  border-radius: .25rem;
  font-size: 16px;
  font-weight: 400;
}
```

Keine rote Hervorhebung für `Datenreihe`.

### 9.5 Fokus und Accessibility

- Alle interaktiven Elemente brauchen sichtbaren Fokus.
- Tabellenzeilen mit Row Click dürfen nicht die Tastaturbedienung ersetzen.
- Plus-/Minus-Button hat `aria-expanded` und `aria-controls`.
- Filtergruppen haben zugängliche Namen.
- Downloadlinks müssen einen eindeutigen zugänglichen Namen mit Format und Zielbezug tragen, z. B. `CSV herunterladen: Abstimmungsresultate`.

---

## 10. Controller und Methoden

### 10.1 Query-Parameter

```java
package ch.so.agi.datenportal.web;

public record CatalogQueryParams(
    String q,
    List<String> theme,
    List<String> office,
    List<String> modified,
    String view,
    List<String> expanded,
    int page,
    int size,
    String sort
) {
    public CatalogQueryParams normalized() { ... }
    public boolean hasActiveFilters() { ... }
}
```

`page` und `size` dürfen im Request-Modell weiterhin existieren, werden im aktuellen öffentlichen Katalog-Flow aber nicht ausgewertet.

### 10.2 Controller

```java
@Controller
public class CatalogController {
    @GetMapping({"/", "/datasets"})
    public String catalog(CatalogQueryParams params, HttpServletRequest request, Model model) { ... }
}

@Controller
@RequestMapping("/datasets")
public class CatalogFilterController {
    @GetMapping("/filter-popover")
    public String filterPopover(...) { ... }

    @GetMapping("/mobile-filters")
    public String mobileFilters(...) { ... }
}
```

Verhalten:

- Normaler Request rendert `pages/catalog.jte`.
- HTMX-Request rendert `fragments/catalogResults.jte` oder ein äquivalentes Fragment.
- `expanded` steuert, welche Datenreihen aufgeklappt sind.
- Die Root-Datenreihen bleiben Top-Level-Resultate; Ausgaben erscheinen nur aufgeklappt.

```java
@Controller
public class DetailController {
    @GetMapping("/datasets/{identifier}")
    public String datasetDetail(@PathVariable String identifier, Model model) { ... }

    @GetMapping("/series/{seriesIdentifier}/issues/current")
    public String currentSeriesIssueDetail(@PathVariable String seriesIdentifier, Model model) { ... }

    @GetMapping("/series/{seriesIdentifier}/issues/{issueIdentifier}")
    public String seriesIssueDetail(
        @PathVariable String seriesIdentifier,
        @PathVariable String issueIdentifier,
        Model model
    ) { ... }
}
```

### 10.3 Factories

```java
@Service
public class CatalogPageVmFactory {
    public HomePageVm create(CatalogSnapshot snapshot, SearchResultPage results, CatalogQueryParams params) { ... }
}

@Service
public class RowVmFactory {
    public EntryRowVm toRow(CatalogEntry entry, CatalogQueryParams params) { ... }
    public IssueRowVm toIssueRow(DatasetIssueEntry issue) { ... }
}

@Service
public class CardVmFactory {
    public EntryCardVm toCard(CatalogEntry entry) { ... }
}

@Service
public class DetailPageVmFactory {
    public EntryDetailPageVm dataset(DatasetEntry dataset) { ... }
    public EntryDetailPageVm issue(DatasetSeriesEntry series, DatasetIssueEntry issue) { ... }
}

@Service
public class HeaderViewModelFactory {
    public HeaderVm forCatalogPage() { ... }
    public HeaderVm forDetailPage() { ... }
}

@Service
public class BreadcrumbFactory {
    public BreadcrumbVm catalog() { ... }
    public BreadcrumbVm datasetDetail(String title) { ... }
    public BreadcrumbVm seriesIssueDetail(String seriesTitle, String issueTitle) { ... }
}
```

---

## 11. Tests und Akzeptanzkriterien

### 11.1 MVC-/HTML-Strukturtests

Pflichttests:

- `GET /` enthält Header-Web-Component oder Fallback.
- `GET /` enthält Breadcrumb-Web-Component oder Fallback.
- `GET /` rendert standardmässig Listenansicht.
- Listenansicht enthält Spalten `Thema / Datensatz`, `Publiziert`, `Details`, `Daten herunterladen`.
- Listenansicht rendert weder `dp-type-badge` noch `dp-entry-themes`.
- Datenreihe-Root-Zeile enthält Plus-/Minus-Button mit `aria-expanded`.
- Datenreihe-Root-Zeile enthält Downloads der aktuellen Ausgabe ohne Zusatz im Pill-Label.
- Aufgeklappte Datenreihe rendert Ausgabezeilen ebenfalls ohne Zusatz `aktuelle Ausgabe`.
- Kartenansicht rendert ausschließlich `dp-type-badge`; Access- und Struktur-Badges werden nicht angezeigt. Nicht offene Eintraege zeigen im Downloadbereich weiterhin ein Schloss.
- Detailseite enthält keine Datenvorschau.
- Detailseite einer Serienausgabe zeigt Serien-Kicker, aktuelles-Ausgabe-Badge im Titel, Dataset-Detail-Cards, `Weitere Ausgaben` und das Seitenpanel `Daten nutzen`.

### 11.2 Accessibility Smoke Checks

Mindestens automatisiert oder manuell dokumentieren:

- Tab-Reihenfolge Suche → Filter → Ansichttoggle → Resultate.
- Filtergruppen sind per Tastatur bedienbar.
- Row Toggle hat sichtbaren Fokus.
- Downloadlinks haben eindeutige Namen.
- Playwright deckt mindestens folgende Browserflüsse ab: Desktop-Popover ohne Y-Shift, URL-/Reload-Wiederherstellung, Escape/Outside-Click mit Fokus-Rückgabe und Mobile-Sheet Apply/Reset.

### 11.3 Keine Pixeltests im MVP

Keine Screenshot-Pixelvergleiche im MVP. Stattdessen HTML-Struktur, Klassen, ARIA-Attribute und zentrale Texte testen.

---

## 12. Umsetzungsphasen

### Phase UI-1: Web Components und Page Chrome

- Web-Component-Assets integrieren.
- `soHeader.jte`, `soBreadcrumb.jte`, Fallbacks.
- Header/Breadcrumb-ViewModels und Factories.
- MVC-Tests.

### Phase UI-2: Katalogseite Listenansicht

- `CatalogQueryParams` mit Mehrfachfiltern.
- Filterbar mit aktiven Chips.
- Listenansicht gemäss `startseite_liste.png`.
- Datensatz-/Datenreihe-Badges im blauen Info-/Ink-Stil.
- Downloads zur aktuellen Ausgabe.
- MVC-Tests.

### Phase UI-3: Datenreihen-Expansion

- `expandedSeries` Query-Parameter.
- Plus-/Minus-Button.
- HTMX-Fragment oder Full-Page-Fallback.
- optionale JS-Insel für Row Click.
- Tests für Root- und Ausgabezeilen.

### Phase UI-4: Kartenansicht

- Cards gemäss `cards.png`.
- Access- und Struktur-Badges werden nicht gerendert; nicht offene Eintraege zeigen ein Schloss statt Downloadlinks.
- Ansichttoggle und Zustandserhalt.
- Tests.

### Phase UI-5: Detailseiten

- Normale Datensatz-Detailseite ohne Vorschau.
- Serienausgabe-Detailseite mit weiteren Ausgaben.
- MetadataSections.
- Tests.
