# UI Component Map v4

Dieses Dokument ordnet UI-Anforderungen den Implementierungsartefakten zu.

## Page Chrome

| UI-Bereich | JTE | Java | Tests |
|---|---|---|---|
| Web-Component-Loader | `components/chrome/webComponentsLoader.jte` | `WebComponentsProperties`, `WebAssetsVmFactory`, `WebAssetsVm` | Asset-Pfad vorhanden |
| Header | `components/chrome/soHeader.jte`, `components/chrome/headerFallback.jte` | `HeaderVm`, `HeaderViewModelFactory` | enthält `<so-header` oder Fallback |
| Breadcrumb | `components/chrome/soBreadcrumb.jte`, `components/chrome/breadcrumbFallback.jte` | `BreadcrumbVm`, `BreadcrumbFactory` | Detailseite enthält letztes Breadcrumb-Element |
| Layout | `layouts/main.jte` | `PageChromeVm`, `PageChromeFactory` | Skip-Link und `main#main-content` |

## Katalogseite

| UI-Bereich | JTE | Java | Tests |
|---|---|---|---|
| Seite | `pages/catalog.jte` | `CatalogController`, `CatalogPageVm` | `/` rendert Titel und Suche |
| Resultatfragment | `fragments/catalogResults.jte` | `HtmxRequest` | HTMX liefert Fragment |
| Suche | `components/searchForm.jte` | `CatalogQueryParams.q` | Auto-Suche ab 3 Zeichen, Clear-Reset, No-JS-GET-Fallback, gemeinsames Desktop-Control-Band |
| Filterbar | `components/filterBar.jte` | `FilterVmFactory`, `FilterPanelVm` | Desktop-Trigger, lokale Panel-Hosts und Reset sichtbar, bündig zum Suchfeld im Control-Band |
| Desktop-Popover | `fragments/filterPopover.jte`, `components/filterFieldList.jte` | `CatalogFilterController`, `FilterGroupVm` | nur angeforderte Gruppe, lokal angedocktes HTMX-Panel ohne globales Overlay |
| Mobile-Sheet | `fragments/mobileFilters.jte`, `components/mobileFilterButton.jte` | `CatalogFilterController`, `FilterPanelVm` | Narrow viewport, Apply/Reset |
| aktive Filterchips | `components/activeFilterChips.jte` | `FilterChipVm` | einzelner Remove-Href |
| Ansichttoggle | `components/viewToggle.jte` | `ViewToggleVm` | `aria-current` korrekt |
| Result Controls | `components/resultControls.jte` | `ResultControlsVm` | Result count, auto-submit Sortierung, Mobile-Button, im gemeinsamen Resultat-Stack |
| Resultat-Shell | `components/resultsShell.jte` | `ResultsVm` | stabiler HTMX-Target-Bereich, verdichteter Abstand zu Result Controls |

## Listenansicht

| UI-Bereich | JTE | Java | Tests |
|---|---|---|---|
| Tabelle | `components/entryTable.jte` | `ResultsVm.rows` | reduzierte Spalten ohne Typ |
| Datensatz-Zeile | `components/entryRow.jte` | `EntryRowVm` | keine Themenzeile und kein Typ-Badge |
| Datenreihen-Root | `components/entryRow.jte` | `EntryRowVm.series=true` | `aria-expanded`, Plus/Minus |
| Ausgabezeile | `components/issueRow.jte` | `IssueRowVm` | Downloads ohne aktuelle-Ausgabe-Text, ohne Typ-Badge |
| Downloadbutton | `components/downloadButton.jte` | `DownloadButtonVm` | zugänglicher Name |

## Kartenansicht

| UI-Bereich | JTE | Java | Tests |
|---|---|---|---|
| Grid | `components/entryCardGrid.jte` | `ResultsVm.cards` | `view=cards` rendert Grid |
| Card | `components/entryCard.jte` | `EntryCardVm` | Typ-Badge, Open Data, Struktur beschrieben |
| Chips | `components/chipList.jte` | `EntryCardVm.keywords` | Keywords als neutrale Status-Badge-Familie gerendert |

## Detailseiten

| UI-Bereich | JTE | Java | Tests |
|---|---|---|---|
| Datensatzdetail | `pages/datasetDetail.jte` | `DatasetDetailPageVm` | keine Datenvorschau |
| Datenreihendetail | `pages/seriesDetail.jte` | `SeriesDetailPageVm` | aktuelle und historische Ausgaben |
| Ausgabendetail | `pages/issueDetail.jte` | `IssueDetailPageVm` | Rücklink zur Datenreihe und weitere Ausgaben |
| Datensatz-Teaserreihe | `components/detailTeaserRow.jte` | statisch im Template | drei Spalten, mobile gestapelt |
| Downloadbereich | `components/detailDownloadPanel.jte` | `DownloadLinkVm` | CSV/XLSX/Parquet |
| Metadaten | `components/metadataSection.jte` | `MetadataSectionVm` | erwartete Gruppen |
| weitere Ausgaben | `components/seriesIssues.jte` | `SeriesIssuesVm` | aktuelle/ältere Ausgaben |
| Fehlerseite | `pages/notFound.jte` | `CatalogErrorControllerAdvice` | unbekannte Identifier liefern 404 |

## CSS

| Datei | Zweck |
|---|---|
| `design-tokens.css` | Farben, Abstände, Typografie, Lizenzfont-Anbindung |
| `base.css` | Grundelemente, Accessibility, Fokus |
| `layout.css` | Page Chrome, Hauptbreiten, Grid |
| `components.css` | Buttons, Filter Chips, Status-Badges, Action Pills, Cards |
| `catalog.css` | Filter, Toolbar, Tabelle, Cards |
| `detail.css` | Detailseiten und Metadatenbereiche |
