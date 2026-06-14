# UI-Implementation-Contract v5

Status: verbindlich für die Datenportal-Webanwendung ab UI-v4  
Package-Basis: `ch.so.agi.datenportal`  
Gilt für: Spring Boot, JTE, HTMX, Web Components, CSS, ViewModels und UI-Tests

Dieser Contract ist der primäre UI-Vertrag für den LLM-Coding-Agenten. Er ersetzt die früheren Annahmen, dass die Startseite eine marketingartige Hero-/Landingpage ist. Die Startseite ist im MVP eine funktionale Katalogseite.

---

## 1. Referenzen und Verbindlichkeit

### 1.1 Verbindliche Screenshots

| Datei | Rolle | Verbindlichkeit |
|---|---|---|
| `spec/mockups/current/startseite_liste.png` | Ausgangslage für Startseite/Katalogseite in Listenansicht | hoch |
| `spec/mockups/current/cards.png` | Ausgangslage für Startseite/Katalogseite in Kartenansicht | hoch |
| `spec/mockups/current/web-components.png` | Ausgangslage für Header und Breadcrumb via Web Components | hoch |

### 1.2 Interpretationsregel

Die Screenshots sind keine pixelgenauen Blaupausen. Verbindlich sind Struktur, Informationshierarchie, Interaktionslogik und Tonalität. Abstände, Spaltenbreiten und Umbrüche dürfen technisch sinnvoll angepasst werden, solange das Ergebnis klar am jeweiligen Screenshot erkennbar bleibt.

Wenn sich Dokumente widersprechen, gilt diese Reihenfolge:

1. `docs/ui-implementation-contract.md`
2. `datenportal_webapp_agent_spec_detailed_v4.md`
3. `docs/spec-ui-addendum.md`
4. `AGENTS.md` und Skills im Zielrepo
5. `spec/mockups/current/*.png`
6. ältere Spezifikationen und frühere Mockups

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
so.ch > Datenportal > Daten & Statistiken
so.ch > Datenportal > Daten & Statistiken > <Titel>
so.ch > Datenportal > Daten & Statistiken > <Datenreihe> > <Ausgabe>
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
resourceTypes=[]
expandedSeries=[]
page=1
size=10
sort=newest
```

Der Screenshot `startseite_liste.png` ist die Referenz für diesen Zustand.

### 3.2 Inhalt oberhalb der Resultate

Pflicht:

- Breadcrumb via Web Component.
- Titel: `Daten & Statistiken`.
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

---

## 4. Filter

Die Filterdarstellung aus `startseite_liste.png` gefällt und ist die Grundlage. Zusätzlich müssen pro Filter mehrere Werte auswählbar sein.

### 4.1 Filtergruppen

MVP-Filter:

1. Thema
2. Fachstelle / Amt
3. Publikationsdatum
4. Ressourcentyp

Publikationsdatum darf als vordefinierte Mehrfachauswahl umgesetzt werden, zum Beispiel:

- Letzte 30 Tage
- Letzte 6 Monate
- Dieses Jahr
- Letztes Jahr
- Älter

Später kann daraus eine Datumsbereich-Auswahl werden.

### 4.2 Mehrfachauswahl-Best-Practice

Jede Filtergruppe ist ein aufklappbarer Button/Popover oder ein `details`-Element mit Checkboxen.

Pflichtverhalten:

- Jede Gruppe zeigt ihren Namen und den Zustand.
- Wenn nichts gewählt ist: `Alle Themen`, `Alle Fachstellen`, `Alle Zeiträume`, `Alle Typen`.
- Wenn Werte gewählt sind: erster Wert plus Anzahl, z. B. `Umwelt +2` oder `3 ausgewählt`.
- Unter oder über der Resultatliste wird eine aktive Filter-Chip-Zeile angezeigt.
- Jeder aktive Chip kann einzeln entfernt werden.
- Es gibt `Filter zurücksetzen`.
- Ausgewählte Filterwerte bleiben bei Suche, Pagination, Ansichtwechsel und Sortierung erhalten.
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

### 4.4 HTMX-Verhalten

Filterformular:

```html
<form
  class="dp-filter-form"
  method="get"
  action="/datasets"
  hx-get="/datasets"
  hx-target="#dataset-results"
  hx-select="#dataset-results"
  hx-push-url="true">
```

Ohne JavaScript muss ein Button `Anwenden` sichtbar oder zumindest bedienbar sein. Mit HTMX darf zusätzlich auf `change` aktualisiert werden.

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

---

## 6. Listenansicht

Referenz: `spec/mockups/current/startseite_liste.png`.

### 6.1 Tabellenstruktur

Pflichtspalten:

```text
Thema / Datensatz | Typ | Publikationsdatum | Metadaten | Daten herunterladen
```

Die erste Spalte enthält keine Themen-/Datensatz-Icons mehr. Ausnahme: Datenreihen erhalten ganz links einen Plus-/Minus-Button zum Aufklappen.

### 6.2 Normale Datensatz-Zeile

Pflicht:

- links kein Icon, nur optionaler leerer Spacer zur Ausrichtung mit Datenreihen
- Titel fett, darunter Kurzbeschreibung
- Typ-Badge `Datensatz` in Grau
- Publikationsdatum
- Info-Link `(i)` zur Detailseite dieses Datensatzes
- Downloadbuttons für verfügbare Formate, insbesondere `CSV`, `XLSX`, `Parquet`
- Wenn API vorhanden ist, zusätzlicher Button `API`

### 6.3 Datenreihen-Root-Zeile

Pflicht:

- links Plus-/Minus-Button
- Klick auf Plus/Minus klappt Ausgaben auf/zu
- Klick auf die Zeile klappt ebenfalls auf/zu
- Klick auf Info-Link oder Download-Link darf die Zeile nicht toggeln
- Typ-Badge `Datenreihe` hat exakt dasselbe graue Badge-Aussehen wie `Datensatz`
- Publikationsdatum der aktuellen Ausgabe oder der Serien-Aktualisierung
- Info-Link führt auf die Detailseite der aktuellen Ausgabe
- Downloads zeigen die aktuellen Ausgaben direkt in derselben Spalte

Downloadbeschriftung für Datenreihen-Root:

```text
CSV (aktuelle Ausgabe)
XLSX (aktuelle Ausgabe)
Parquet (aktuelle Ausgabe)
```

oder, falls responsive besser:

```text
CSV
aktuelle Ausgabe
```

als zweizeiliger Button. Wichtig ist, dass kein roter Sonderbutton `Aktuelle Ausgabe` verwendet wird.

### 6.4 Aufgeklappte Ausgaben

Beim Aufklappen einer Datenreihe erscheinen zusätzliche Tabellenzeilen direkt unter der Root-Zeile.

Pflicht für Ausgabezeilen:

- leicht eingerückt oder visuell als Kindzeile erkennbar
- kein Typ-Badge zwingend nötig; falls vorhanden ebenfalls grau
- Titel: z. B. `Gemeindegrenzen — Ausgabe 2025`
- Beschreibung optional, kurz
- Publikationsdatum der Ausgabe
- Info-Link zur Detailseite genau dieser Ausgabe
- Downloadbuttons `CSV`, `XLSX`, `Parquet` ohne Klammertext `aktuelle Ausgabe`

### 6.5 Progressive Enhancement für Row Click

Das Plus-/Minus-Verhalten muss ohne eigenes JavaScript über einen Link/Button funktionieren.

Zusätzlich darf eine kleine JavaScript-Insel ergänzt werden, um den Zeilenklick komfortabler zu machen:

```text
src/main/resources/static/js/row-disclosure.js
```

Regel für die JS-Insel:

- Event Delegation auf `tr[data-disclosure-row]`
- Ignoriere Klicks auf `a`, `button`, `input`, `select`, `label`, `[data-no-row-toggle]`
- Löst intern den Plus-/Minus-Button aus
- Keine weitere UI-Logik in dieser Datei

### 6.6 Listen-ViewModels

```java
public record ResultsVm(
    ViewMode viewMode,
    long totalResults,
    List<EntryRowVm> rows,
    List<EntryCardVm> cards,
    PaginationVm pagination,
    ResultsToolbarVm toolbar
) {}

public record EntryRowVm(
    String id,
    String title,
    String description,
    String typeLabel,
    String typeCssClass,
    String publicationDateLabel,
    String detailHref,
    List<DownloadButtonVm> downloads,
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
    String publicationDateLabel,
    String detailHref,
    List<DownloadButtonVm> downloads
) {}

public record DownloadButtonVm(
    String formatLabel,
    Optional<String> contextLabel,
    String href,
    String contentType,
    boolean enabled
) {}
```

`contextLabel` ist für `aktuelle Ausgabe` vorgesehen.

### 6.7 JTE-Komponenten

```text
src/main/jte/pages/catalog.jte
src/main/jte/fragments/catalogResults.jte
src/main/jte/components/filterBar.jte
src/main/jte/components/activeFilterChips.jte
src/main/jte/components/viewToggle.jte
src/main/jte/components/resultsToolbar.jte
src/main/jte/components/entryTable.jte
src/main/jte/components/entryRow.jte
src/main/jte/components/issueRow.jte
src/main/jte/components/downloadButton.jte
src/main/jte/components/pagination.jte
```

---

## 7. Kartenansicht

Referenz: `spec/mockups/current/cards.png`.

### 7.1 Card-Struktur

Die Card-Ansicht zeigt ein Grid. Die Card folgt dem Screenshot `cards.png`.

Pflicht je Card:

- graues Typ-Badge `Datensatz` oder `Datenreihe`, gleiches Aussehen wie Listenansicht
- Badge `Open Data`, im MVP hardcodiert sichtbar
- zusätzlicher Badge `Struktur beschrieben`, wenn Attribute beschrieben sind oder ein Datenmodell vorhanden ist
- Titel und Beschreibung wie im Screenshot
- graue Keyword-/Themen-Badges
- Formate als kompakte Textlinks oder Buttons
- Aktualisierungs-/Publikationsdatum unten links
- roter Pfeil/Link unten rechts zur Detailseite

### 7.2 Badge `Struktur beschrieben`

Name: `Struktur beschrieben`

Bedeutung:

- Es existiert ein Datenmodell, oder
- Attribute/Spalten/Strukturinformationen sind im Modell beschrieben.

Falls fachlich unterschieden werden soll, kann der Tooltip oder `title`-Text lauten:

```text
Attribute beschrieben oder Datenmodell vorhanden
```

Nicht verwenden:

- `Verfügbar`
- `Metadaten komplett`

Begründung: `Verfügbar` sagt nichts über Struktur/Attribute aus. `Metadaten komplett` wäre zu stark und müsste validiert werden.

### 7.3 Card-ViewModel

```java
public record EntryCardVm(
    String id,
    String title,
    String description,
    String typeLabel,
    String typeCssClass,
    boolean openData,
    boolean structureDescribed,
    List<String> keywords,
    List<DownloadButtonVm> downloads,
    String dateLabel,
    String detailHref
) {}
```

### 7.4 JTE-Komponenten

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
3. Badges: `Datensatz`, `Open Data`, optional `Struktur beschrieben`
4. Kurzbeschreibung/Abstract
5. Downloadbereich mit `CSV`, `XLSX`, `Parquet`, optional `API`
6. Metadatenbereiche:
   - Übersicht
   - Verantwortlichkeit
   - Zeit und Raum
   - Nutzung und Lizenz
   - Ressourcen und Formate
   - Struktur / Datenmodell
   - Kontakt
7. Footer/Statuszeile

### 8.2 Detailseite für Datenreihen und Ausgaben

Wichtig: In der Liste zeigt der Info-Link der Root-Datenreihe auf die Detailseite der aktuellen Ausgabe.

Die Detailseite der aktuellen Ausgabe muss den Serienkontext zeigen:

- Titel der Datenreihe
- Hinweis auf aktuelle Ausgabe, z. B. `Aktuelle Ausgabe 2026`
- Beschreibung der Datenreihe und/oder Ausgabe
- Downloadbereich der aktuellen Ausgabe
- Abschnitt `Weitere Ausgaben`
- Links auf ältere Ausgaben, z. B. 2025, 2024, 2023

Für eine ältere Ausgabe ist die Struktur gleich, aber mit Badge/Label `Ausgabe 2025` und Link zurück zur aktuellen Ausgabe.

### 8.3 Vorgeschlagene URLs

```text
GET /datasets/{identifier}
  Detailseite eines normalen Datensatzes.

GET /series/{seriesIdentifier}/issues/current
  Detailseite der aktuellen Ausgabe einer Datenreihe.

GET /series/{seriesIdentifier}/issues/{issueIdentifier}
  Detailseite einer spezifischen Ausgabe.
```

Alternativ darf der Agent eine slugbasierte Variante verwenden, wenn die Identifiers stabil und URL-sicher normalisiert werden. Wichtig ist die fachliche Trennung von Datensatz und Datenreihen-Ausgabe.

### 8.4 Detail-ViewModels

```java
public sealed interface DetailPageVm permits DatasetDetailPageVm, SeriesIssueDetailPageVm {
    PageChromeVm chrome();
    String title();
    String description();
    List<StatusBadgeVm> badges();
    List<DownloadButtonVm> downloads();
    List<MetadataSectionVm> metadataSections();
}

public record DatasetDetailPageVm(
    PageChromeVm chrome,
    String title,
    String description,
    List<StatusBadgeVm> badges,
    List<DownloadButtonVm> downloads,
    List<MetadataSectionVm> metadataSections
) implements DetailPageVm {}

public record SeriesIssueDetailPageVm(
    PageChromeVm chrome,
    String seriesTitle,
    String issueTitle,
    String title,
    String description,
    boolean currentIssue,
    List<StatusBadgeVm> badges,
    List<DownloadButtonVm> downloads,
    List<MetadataSectionVm> metadataSections,
    List<OtherIssueVm> otherIssues,
    Optional<String> currentIssueHref
) implements DetailPageVm {}

public record MetadataSectionVm(
    String id,
    String title,
    List<MetadataRowVm> rows
) {}

public record MetadataRowVm(
    String label,
    String value,
    Optional<String> href
) {}

public record OtherIssueVm(
    String label,
    String publicationDateLabel,
    String detailHref,
    List<DownloadButtonVm> downloads,
    boolean current
) {}
```

### 8.5 Detail-JTE

```text
src/main/jte/pages/datasetDetail.jte
src/main/jte/pages/seriesIssueDetail.jte
src/main/jte/components/detailHero.jte
src/main/jte/components/detailDownloadPanel.jte
src/main/jte/components/metadataSection.jte
src/main/jte/components/otherIssues.jte
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

### 9.3 Typ-Badges

`Datensatz` und `Datenreihe` müssen denselben Badge-Stil verwenden:

```css
.dp-type-badge {
  background: var(--dp-color-gray-100);
  color: var(--dp-color-text);
  border: 1px solid var(--dp-color-gray-300);
  border-radius: .25rem;
  font-size: .875rem;
  font-weight: 600;
}
```

Keine rote Hervorhebung für `Datenreihe`.

### 9.4 Fokus und Accessibility

- Alle interaktiven Elemente brauchen sichtbaren Fokus.
- Tabellenzeilen mit Row Click dürfen nicht die Tastaturbedienung ersetzen.
- Plus-/Minus-Button hat `aria-expanded` und `aria-controls`.
- Filtergruppen haben zugängliche Namen.
- Downloadlinks müssen Format und Kontext im zugänglichen Namen enthalten, z. B. `CSV aktuelle Ausgabe herunterladen`.

---

## 10. Controller und Methoden

### 10.1 Query-Parameter

```java
package ch.so.agi.datenportal.web;

public record CatalogQueryParams(
    String q,
    List<String> theme,
    List<String> office,
    List<String> publicationDate,
    List<String> resourceType,
    ViewMode view,
    List<String> expandedSeries,
    int page,
    int size,
    SortMode sort
) {
    public CatalogQueryParams normalized() { ... }
    public boolean hasActiveFilters() { ... }
}
```

### 10.2 Controller

```java
@Controller
public class CatalogController {
    @GetMapping({"/", "/datasets"})
    public String catalog(CatalogQueryParams params, HttpServletRequest request, Model model) { ... }
}
```

Verhalten:

- Normaler Request rendert `pages/catalog.jte`.
- HTMX-Request rendert `fragments/catalogResults.jte` oder ein äquivalentes Fragment.
- `expandedSeries` steuert, welche Datenreihen aufgeklappt sind.
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
    public DatasetDetailPageVm dataset(DatasetEntry dataset) { ... }
    public SeriesIssueDetailPageVm seriesIssue(DatasetSeriesEntry series, DatasetIssueEntry issue) { ... }
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
- Listenansicht enthält Spalten `Thema / Datensatz`, `Typ`, `Publikationsdatum`, `Metadaten`, `Daten herunterladen`.
- Datensatz- und Datenreihe-Badge haben dieselbe CSS-Klasse `dp-type-badge`.
- Datenreihe-Root-Zeile enthält Plus-/Minus-Button mit `aria-expanded`.
- Datenreihe-Root-Zeile enthält Downloads mit Kontext `aktuelle Ausgabe`.
- Aufgeklappte Datenreihe rendert Ausgabezeilen ohne Kontext `aktuelle Ausgabe`.
- Kartenansicht rendert `Open Data` und `Struktur beschrieben` dort, wo fachlich zutreffend.
- Detailseite enthält keine Datenvorschau.
- Detailseite einer Serienausgabe enthält `Weitere Ausgaben`.

### 11.2 Accessibility Smoke Checks

Mindestens automatisiert oder manuell dokumentieren:

- Tab-Reihenfolge Suche → Filter → Ansichttoggle → Resultate → Pagination.
- Filtergruppen sind per Tastatur bedienbar.
- Row Toggle hat sichtbaren Fokus.
- Downloadlinks haben eindeutige Namen.

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
- Datensatz-/Datenreihe-Badges grau.
- Downloads für aktuelle Ausgabe.
- MVC-Tests.

### Phase UI-3: Datenreihen-Expansion

- `expandedSeries` Query-Parameter.
- Plus-/Minus-Button.
- HTMX-Fragment oder Full-Page-Fallback.
- optionale JS-Insel für Row Click.
- Tests für Root- und Ausgabezeilen.

### Phase UI-4: Kartenansicht

- Cards gemäss `cards.png`.
- `Open Data` hardcodiert sichtbar im MVP.
- `Struktur beschrieben`-Badge.
- Ansichttoggle und Zustandserhalt.
- Tests.

### Phase UI-5: Detailseiten

- Normale Datensatz-Detailseite ohne Vorschau.
- Serienausgabe-Detailseite mit weiteren Ausgaben.
- MetadataSections.
- Tests.

