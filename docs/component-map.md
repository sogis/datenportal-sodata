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
| Suche | `components/searchForm.jte` | `CatalogQueryParams.q` | Query bleibt erhalten |
| Filterbar | `components/filterBar.jte` | `FilterPanelVmFactory` | Mehrfachfilter sichtbar |
| aktive Filterchips | `components/activeFilterChips.jte` | `ActiveFilterVm` | einzelner Remove-Href |
| Ansichttoggle | `components/viewToggle.jte` | `ViewToggleVm` | `aria-current` korrekt |
| Toolbar | `components/resultsToolbar.jte` | `ResultsToolbarVm` | Result count und Sortierung |

## Listenansicht

| UI-Bereich | JTE | Java | Tests |
|---|---|---|---|
| Tabelle | `components/entryTable.jte` | `ResultsVm.rows` | Spalten vorhanden |
| Datensatz-Zeile | `components/entryRow.jte` | `EntryRowVm` | keine Themenicons |
| Datenreihen-Root | `components/entryRow.jte` | `EntryRowVm.series=true` | `aria-expanded`, Plus/Minus |
| Ausgabezeile | `components/issueRow.jte` | `IssueRowVm` | Downloads ohne aktuelle-Ausgabe-Text |
| Downloadbutton | `components/downloadButton.jte` | `DownloadButtonVm` | zugänglicher Name |
| Pagination | nach Phase 3 zurückgestellt | noch kein Frontend-VM | Phase 3 rendert alle Treffer ohne Pagination |

## Kartenansicht

| UI-Bereich | JTE | Java | Tests |
|---|---|---|---|
| Grid | `components/entryCardGrid.jte` | `ResultsVm.cards` | `view=cards` rendert Grid |
| Card | `components/entryCard.jte` | `EntryCardVm` | Typ-Badge, Open Data, Struktur beschrieben |
| Chips | `components/chipList.jte` | `EntryCardVm.keywords` | Keywords grau gerendert |

## Detailseiten

| UI-Bereich | JTE | Java | Tests |
|---|---|---|---|
| Datensatzdetail | `pages/datasetDetail.jte` | `DatasetDetailPageVm` | keine Datenvorschau |
| Datenreihendetail | `pages/seriesDetail.jte` | `SeriesDetailPageVm` | aktuelle und historische Ausgaben |
| Ausgabendetail | `pages/issueDetail.jte` | `IssueDetailPageVm` | Rücklink zur Datenreihe und weitere Ausgaben |
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
| `components.css` | Buttons, Badges, Chips, Cards |
| `catalog.css` | Filter, Toolbar, Tabelle, Cards |
| `detail.css` | Detailseiten und Metadatenbereiche |
