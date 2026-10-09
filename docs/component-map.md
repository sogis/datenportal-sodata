# UI Component Map v4

Dieses Dokument ordnet UI-Anforderungen den Implementierungsartefakten zu.

## Page Chrome

| UI-Bereich | JTE | Java | Tests |
|---|---|---|---|
| Web-Component-Loader | `components/chrome/webComponentsLoader.jte` | `WebComponentsProperties`, `WebAssetsVmFactory`, `WebAssetsVm` | Asset-Pfad vorhanden |
| Header | `components/chrome/soHeader.jte`, `components/chrome/headerFallback.jte` | `HeaderVm`, `HeaderViewModelFactory` | enthält `<so-header` oder Fallback |
| Breadcrumb | `components/chrome/soBreadcrumb.jte`, `components/chrome/breadcrumbFallback.jte` | `BreadcrumbVm`, `BreadcrumbFactory` | Detailseite enthält letztes Breadcrumb-Element |
| Layout | `layouts/main.jte` | `PageChromeVm`, `PageChromeFactory` | Skip-Link und `main#main-content` |
| Druckkopf | `components/chrome/printHeader.jte`, `layouts/main.jte` | vorhandene `HeaderVm` und `BreadcrumbVm`, `WebAssetsVm.printCss` | nur im Druck sichtbar, Portal und Seitenpfad auch ohne JavaScript; Explore-Layout ausgenommen |

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
| Result Controls | `components/resultControls.jte` | `ResultsVm`, `CatalogQueryParams` | Result count, auto-submit Sortierung, Mobile-Button, im gemeinsamen Resultat-Stack; Suchbegriff und Sortierung im Druck auch nach HTMX-Updates |
| Resultat-Shell | `components/resultsShell.jte` | `ResultsVm` | stabiler HTMX-Target-Bereich, verdichteter Abstand zu Result Controls |

## Listenansicht

| UI-Bereich | JTE | Java | Tests |
|---|---|---|---|
| Tabelle | `components/entryTable.jte` | `ResultsVm.rows` | reduzierte Spalten ohne Typ |
| Datensatz-Zeile | `components/entryRow.jte` | `EntryRowVm` | keine Themenzeile und kein Typ-Badge |
| Datenreihen-Root | `components/entryRow.jte` | `EntryRowVm.series=true` | `aria-expanded`, Plus/Minus |
| Ausgabezeile | `components/issueRow.jte` | `IssueRowVm` | Downloads ohne aktuelle-Ausgabe-Text, ohne Typ-Badge |
| Downloadlink | `components/downloadLink.jte` | `DownloadLinkVm` | zugänglicher Name |

## Kartenansicht

| UI-Bereich | JTE | Java | Tests |
|---|---|---|---|
| Grid | `components/entryCardGrid.jte` | `ResultsVm.cards` | `view=cards` rendert Grid |
| Card | `components/entryCard.jte` | `EntryCardVm` | ausschließlich Typ-Badge; Zugang wird bei nicht offenen Downloads durch ein Schloss signalisiert |
| Chips | `components/chipList.jte` | `EntryCardVm.keywords` | Keywords als neutrale Status-Badge-Familie gerendert |

## Detailseiten

| UI-Bereich | JTE | Java | Tests |
|---|---|---|---|
| Datensatz-/Ausgabendetail | `pages/entryDetail.jte` | `EntryDetailPageVm` | gemeinsames Detail-ViewModel und Template, keine Datenvorschau |
| Datenreihendetail | `pages/seriesDetail.jte` | `SeriesDetailPageVm` | vereinfachte Serienübersicht mit unframed Ausgabenliste |
| Ausgabendetail | `pages/entryDetail.jte` | `EntryDetailPageVm` | Serien-Kicker, aktuelles-Ausgabe-Badge, unframed Bereiche `Downloads` und `Datenmerkmale` untereinander, unframed Metadatenbereiche, Seitenpanel `Daten nutzen` und Card `Weitere Ausgaben` |
| Struktur, Qualität und Herkunft | `pages/structureQualityOrigin.jte`, `components/structureKpis.jte`, `components/attributeTable.jte`, `components/qualityCard.jte`, `components/metadataSection.jte` | `StructureQualityOriginPageVm`, `KpiVm`, `AttributeRowVm`, `QualityVm`, `MetadataSectionVm`, `MetadataLineVm` | Datensatz- und Ausgabe-Routen, neutrale KPI-Reihe, Attribute als Tabelle, unframed Qualität sowie `Herkunft & Verwendung` optional |
| Daten verwenden | `pages/usage.jte`, `components/usageDirectAccess.jte`, `components/usageCodeExamples.jte` | `UsagePageVm`, `DirectAccessRowVm`, `CodeExampleVm` | Datensatz- und Ausgabe-Routen, Direktzugriff ohne Inhalt-Spalte, Format-Badges, Copy-Feedback, Codebeispiel-Tabs, keine vorbereiteten Starter-Rezepte |
| Datensatz-Datenmerkmale | `components/detailFeatureDownloadStrip.jte` | `DetailFeatureVm`, `AccessStateVm`, `DownloadLinkVm` | ungerahmte Bereiche `Downloads` und `Datenmerkmale` in dieser Reihenfolge untereinander, ohne Card-Padding analog zu `Übersicht` |
| Datensatz-Übersicht | `components/metadataSection.jte` | `EntryDetailPageVm.overview` | unframed Metadatenbereich unterhalb Downloads und Datenmerkmale |
| Datensatz-Zeitliche Abdeckung | `components/metadataSection.jte` | `EntryDetailPageVm.temporalCoverage` | unframed Metadatenbereich mit Stichtag oder Zeitraum |
| Datensatz-Themen und Schlagworte | `components/metadataSection.jte` | `EntryDetailPageVm.topics` | unframed Metadatenbereich, Themen und Schlagworte komma-separiert |
| Datensatz-Zuständigkeiten und Kontakt | `components/metadataSection.jte` | `EntryDetailPageVm.responsibilitiesContact`, `MetadataLineVm` | unframed Metadatenbereich, Datenproduzent, Kontakt und Herausgeber mehrzeilig mit Links |
| Datensatz-Seitenpanel `Daten nutzen` | `components/detailActionPanel.jte` | `EntryDetailPageVm.structureQualityOriginHref`, `EntryDetailPageVm.exploreHref` | unframed Seitenpanel mit vertikalen Nutzungshinweisen und sekundären Textlinks |
| Metadaten | `components/metadataSection.jte` | `MetadataSectionVm` | erwartete Gruppen |
| Serien-Ausgabenliste | `components/seriesIssues.jte` | `List<SeriesIssueVm>` | unframed aktuelle/ältere Ausgaben auf der Serienübersicht mit Trennlinien |
| Weitere Ausgaben | `components/relatedIssuesCard.jte` | `List<RelatedIssueVm>` | schlanke Linkliste anderer Ausgaben auf Ausgabendetails |
| Fehlerseite | `pages/notFound.jte` | `CatalogErrorControllerAdvice` | unbekannte Identifier liefern 404 |

## Beschreibende Metadaten

Beschreibungen in Katalogeinführung, Detail-Hero, Attributtabelle und den
Freitexten zu Herkunft/Verwendung erhalten vorbereiteten
`MetadataTextRenderer.Html`-Inhalt. Katalogeinführung und Detailtexte verwenden
Blockdarstellung; `entryRow`, `issueRow` und `entryCard` verwenden kompakte
Inline-Darstellung. `MetadataLineVm.formattedValue` kennzeichnet ausschliesslich
die drei beschreibenden Freitexte; normale Metadaten und Kontaktlinks bleiben
maskierte Strings. Parsing, Rendering und Wortkürzung liegen zentral in
`support.metadata`, siehe [Erfassungsregeln](metadata-text.md).

## CSS

| Datei | Zweck |
|---|---|
| `design-tokens.css` | Farben, Abstände, Typografie, Lizenzfont-Anbindung |
| `base.css` | Grundelemente, Accessibility, Fokus |
| `layout.css` | Page Chrome, Hauptbreiten, Grid |
| `components.css` | Buttons, Filter Chips, Status-Badges, Action Pills, Cards |
| `catalog.css` | Filter, Toolbar, Tabelle, Cards |
| `detail.css` | Detailseiten und Metadatenbereiche |
| `print.css` | nur im Hauptlayout mit `media="print"`: A4, Margin Boxes, Seitenzahlen, Tabellenumbrüche und kompakter Dokumentfluss |

## Druckprüfung

`CatalogPrintPlaywrightTest` prüft die öffentlichen Seitentypen mit Chromium
und Firefox bei einer bedruckbaren Breite von ca. 680 CSS-Pixeln. Die Tests
decken HTMX-Suche/Filter/Sortierung, aktive Code-Tabs, lange Beschreibungen,
URLs, mehrseitige Tabellen und die Ausgrenzung des SQL-/R-Labors ab.
Chromium erzeugt Prüf-PDFs unter `build/print-previews/` mit CSS-Seitengrösse,
100 % Skalierung und ausgeschalteten Hintergrundgrafiken. Diese sind
Build-Artefakte für die visuelle Kontrolle, keine eingecheckten Referenzbilder.

## Vendor Fonts

| Asset | Zweck |
|---|---|
| `vendor/jetbrains-mono/2.304/fonts.css` | lokal vendorte JetBrains-Mono-Webfont-Definition fuer Codebeispiele |
| `vendor/jetbrains-mono/2.304/JetBrainsMono-Regular.woff2` | JetBrains Mono Regular, nur fuer Codetext |

## Geometrieausgabe der Labore

| Bereich | Komponenten / Vertrag | Aufgabe |
|---|---|---|
| SQL-Karte | `geometry/MapPanel.tsx`, `lv95Map.ts`, `mapStyle.ts` | OpenLayers, LV95-WMTS, Attribute, Legende, PNG; vom SQL-Labor bei Bedarf geladen |
| Ergebnisgeometrie | `geometry/resultGeometry.ts`, `results/arrowResult.ts`, `sqlResultSnapshot.ts` | Arrow-WKB erkennen/validieren; unveränderte Bytes und CRS übertragen |
| R-Geometrie | `WebRRuntime.ts`, `WebRBridge.ts`, `RRecipes.ts` | sf bei Bedarf laden, Base64-WKB zu sf, Kartenrezepte, vorhandene Plot-Ausgabe |
| Kontext | `ExploreMapDto`, `ExploreRLaboratoryDto`, `ExploreContextDto` V5 | WMTS, Attribution, Budgets und Geo-Paketliste |
