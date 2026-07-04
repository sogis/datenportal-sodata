package ch.so.agi.datenportal.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;

@SpringBootTest
@AutoConfigureMockMvc
class CatalogDetailControllerMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void datasetDetailRendersTitleDescriptionDownloadsMetadataAndBreadcrumb() throws Exception {
        mockMvc.perform(get("/datasets/ch.so.bauinventar"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<so-header")))
                .andExpect(content().string(containsString("<so-breadcrumb>")))
                .andExpect(content().string(containsString("<section class=\"dp-detail-page dp-dataset-detail-page\">")))
                .andExpect(content().string(containsString("<so-breadcrumb-item href=\"/datasets\">Daten und Statistiken</so-breadcrumb-item>")))
                .andExpect(content().string(containsString("Bauinventar")))
                .andExpect(content().string(containsString("Inventar schützenswerter und erhaltenswerter Bauten.")))
                .andExpect(content().string(containsString("Open Data")))
                .andExpect(content().string(containsString("href=\"http://localhost:8081/ch.so.datenportal/downloads/ch.so.bauinventar.csv\"")))
                .andExpect(content().string(containsString("href=\"http://localhost:8081/ch.so.datenportal/downloads/ch.so.bauinventar.xlsx\"")))
                .andExpect(content().string(containsString("href=\"http://localhost:8081/ch.so.datenportal/downloads/ch.so.bauinventar.parquet\"")))
                .andExpect(content().string(containsString("Datenmerkmale")))
                .andExpect(content().string(containsString("Übersicht")))
                .andExpect(content().string(containsString("Identifier")))
                .andExpect(content().string(containsString("Typ")))
                .andExpect(content().string(containsString("Zugriff")))
                .andExpect(content().string(containsString("Herkunft")))
                .andExpect(content().string(containsString("Kanton")))
                .andExpect(content().string(containsString("Publikationsstatus")))
                .andExpect(content().string(containsString("veröffentlicht")))
                .andExpect(content().string(containsString("Aktualisierungsintervall")))
                .andExpect(content().string(containsString("bei Bedarf")))
                .andExpect(content().string(containsString("Lizenz")))
                .andExpect(content().string(containsString("https://creativecommons.org/licenses/by/4.0/")))
                .andExpect(content().string(containsString("Zeitliche Abdeckung")))
                .andExpect(content().string(containsString("Stichtag")))
                .andExpect(content().string(containsString("19.05.2026")))
                .andExpect(content().string(containsString("Themen und Schlagworte")))
                .andExpect(content().string(containsString("Thema")))
                .andExpect(content().string(containsString("Bau- und Wohnungswesen, Kultur, Medien, Informationsgesellschaft und Sport")))
                .andExpect(content().string(containsString("Schlagworte")))
                .andExpect(content().string(containsString("Bauinventar, Kulturgüter, Gebäude")))
                .andExpect(content().string(containsString("Zuständigkeiten und Kontakt")))
                .andExpect(content().string(containsString("Datenproduzent")))
                .andExpect(content().string(containsString("Amt für Raumplanung")))
                .andExpect(content().string(containsString("href=\"https://arp.so.ch\"")))
                .andExpect(content().string(containsString("href=\"https://arp.so.ch\" target=\"_blank\"")))
                .andExpect(content().string(containsString("Kontakt")))
                .andExpect(content().string(containsString("Lukas Meier")))
                .andExpect(content().string(containsString("Abteilung Richt- und Nutzungsplanung")))
                .andExpect(content().string(containsString("href=\"mailto:arp@bd.so.ch\"")))
                .andExpect(content().string(containsString("arp@bd.so.ch")))
                .andExpect(content().string(containsString("Herausgeber")))
                .andExpect(content().string(containsString("Amt für Geoinformation")))
                .andExpect(content().string(containsString("href=\"https://agi.so.ch\"")))
                .andExpect(content().string(containsString("href=\"https://agi.so.ch\" target=\"_blank\"")))
                .andExpect(content().string(containsString("href=\"mailto:agi@bd.so.ch\"")))
                .andExpect(content().string(containsString("agi@bd.so.ch")))
                .andExpect(content().string(containsString("Attribute beschrieben")))
                .andExpect(content().string(containsString("Daten validiert")))
                .andExpect(content().string(containsString("class=\"bi bi-check-circle\"")))
                .andExpect(content().string(containsString("<aside class=\"dp-detail-side\" aria-label=\"Daten nutzen\">")))
                .andExpect(content().string(containsString("Daten nutzen")))
                .andExpect(content().string(containsString("Struktur, Qualität und Herkunft")))
                .andExpect(content().string(containsString("Erkunden")))
                .andExpect(content().string(containsString("Verwenden")))
                .andExpect(content().string(containsString("Attribute, Datenmodell, Validierung und Qualitätshinweise einsehen.")))
                .andExpect(content().string(containsString("Struktur, Qualität und Herkunft anzeigen")))
                .andExpect(content().string(containsString("Datenvorschau öffnen und Inhalte fachlich einordnen.")))
                .andExpect(content().string(containsString("Datenvorschau anzeigen")))
                .andExpect(content().string(containsString("Downloads, Formate und Codebeispiele anzeigen.")))
                .andExpect(content().string(containsString("Downloads anzeigen")))
                .andExpect(content().string(containsString("href=\"/datasets/ch.so.bauinventar/structure-quality-origin\"")))
                .andExpect(content().string(containsString("href=\"/datasets/ch.so.bauinventar/explore\"")))
                .andExpect(content().string(containsString("href=\"/datasets/ch.so.bauinventar/usage\"")))
                .andExpect(content().string(not(containsString("href=\"#\""))))
                .andExpect(content().string(not(containsString("dp-detail-action-link--disabled"))))
                .andExpect(content().string(containsString("&rarr;")))
                .andExpect(content().string(containsString("class=\"bi bi-shield-check\"")))
                .andExpect(content().string(containsString("class=\"bi bi-search\"")))
                .andExpect(content().string(containsString("class=\"bi bi-code-slash\"")))
                .andExpect(content().string(containsString("Daten und Statistiken")))
                .andExpect(content().string(containsString("aria-current=\"page\"")))
                .andExpect(content().string(not(containsString("dp-detail-badges"))))
                .andExpect(content().string(not(containsString("<dl class=\"dp-detail-facts\""))))
                .andExpect(content().string(not(containsString(">Struktur beschrieben</span>"))))
                .andExpect(content().string(not(containsString("Verantwortlichkeit"))))
                .andExpect(content().string(not(containsString("DCAT-Thema"))))
                .andExpect(content().string(not(containsString("dp-preview"))))
                .andExpect(content().string(not(containsString("chart"))))
                .andExpect(content().string(not(containsString("row-disclosure"))));
    }

    @Test
    void datasetStructureQualityOriginRendersAttributesQualityOriginAndUsage() throws Exception {
        mockMvc.perform(get("/datasets/ch.so.bauinventar/structure-quality-origin"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<so-header")))
                .andExpect(content().string(containsString("<so-breadcrumb>")))
                .andExpect(content().string(containsString("<so-breadcrumb-item href=\"/datasets/ch.so.bauinventar\">Bauinventar</so-breadcrumb-item>")))
                .andExpect(content().string(containsString("<so-breadcrumb-item iscurrentpage>Struktur, Qualität und Herkunft</so-breadcrumb-item>")))
                .andExpect(content().string(containsString("Struktur, Qualität und Herkunft")))
                .andExpect(content().string(containsString("class=\"dp-structure-kpis\"")))
                .andExpect(content().string(containsString("class=\"bi bi-shield-check\"")))
                .andExpect(content().string(containsString("class=\"bi bi-database\"")))
                .andExpect(content().string(containsString("class=\"bi bi-table\"")))
                .andExpect(content().string(containsString("Validierung")))
                .andExpect(content().string(containsString("<p class=\"dp-structure-kpi__value\">Erfolgreich</p>")))
                .andExpect(content().string(containsString("0 Fehler")))
                .andExpect(content().string(containsString("Objekte")))
                .andExpect(content().string(containsString("<p class=\"dp-structure-kpi__value\">26349</p>")))
                .andExpect(content().string(containsString("Attribute")))
                .andExpect(content().string(containsString("<p class=\"dp-structure-kpi__value\">6</p>")))
                .andExpect(content().string(containsString("class=\"dp-attribute-table\"")))
                .andExpect(content().string(containsString("<th scope=\"col\">Attribut</th>")))
                .andExpect(content().string(containsString("<th scope=\"col\">Typ</th>")))
                .andExpect(content().string(containsString("<th scope=\"col\">Pflicht</th>")))
                .andExpect(content().string(containsString("<th scope=\"col\">Einheit</th>")))
                .andExpect(content().string(containsString("<th scope=\"col\">Beschreibung</th>")))
                .andExpect(content().string(containsString("<th scope=\"row\">objekt_id</th>")))
                .andExpect(content().string(containsString("Stabiler Objektidentifikator.")))
                .andExpect(content().string(containsString("<th scope=\"row\">flaeche_m2</th>")))
                .andExpect(content().string(containsString("<td>m2</td>")))
                .andExpect(content().string(containsString("<td>Nein</td>")))
                .andExpect(content().string(containsString("class=\"dp-detail-summary-layout\"")))
                .andExpect(content().string(containsString("class=\"dp-detail-summary-main\"")))
                .andExpect(content().string(containsString("class=\"dp-quality-card\"")))
                .andExpect(content().string(containsString("id=\"quality-title\"")))
                .andExpect(content().string(containsString("Qualität")))
                .andExpect(content().string(containsString("SO_AGI_Geobasisdaten_Publikation_20260624")))
                .andExpect(content().string(containsString("Validierungsreport")))
                .andExpect(content().string(containsString("ilivalidator.log")))
                .andExpect(content().string(not(containsString("class=\"dp-detail-panel dp-quality-card\""))))
                .andExpect(content().string(not(containsString("Gemeinden"))))
                .andExpect(content().string(not(containsString("Felder"))))
                .andExpect(content().string(not(containsString("dp-status-badge--positive"))))
                .andExpect(content().string(not(containsString("dp-color-status-ok-circle"))))
                .andExpect(content().string(not(containsString("Beispiel"))))
                .andExpect(content().string(not(containsString("dp-detail-actions"))))
                .andExpect(result -> {
                    String html = result.getResponse().getContentAsString();
                    int firstSummaryLayout = html.indexOf("class=\"dp-detail-summary-layout\"");
                    int kpis = html.indexOf("class=\"dp-structure-kpis\"");
                    int attributeTable = html.indexOf("class=\"dp-attribute-table\"");
                    int cardSummaryLayout = html.indexOf("class=\"dp-detail-summary-layout\"", attributeTable);
                    assertThat(firstSummaryLayout).isGreaterThanOrEqualTo(0);
                    assertThat(firstSummaryLayout).isLessThan(kpis);
                    assertThat(kpis).isLessThan(attributeTable);
                    assertThat(cardSummaryLayout).isGreaterThan(attributeTable);
                });
    }

    @Test
    void datasetStructureQualityOriginShowsQualityMessageWhenModelIsMissing() throws Exception {
        mockMvc.perform(get("/datasets/ch.so.wasserqualitaet_grundwasser/structure-quality-origin"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<th scope=\"row\">jahr</th>")))
                .andExpect(content().string(containsString("Jahr der Messung.")))
                .andExpect(content().string(containsString("<td>Jahr</td>")))
                .andExpect(content().string(containsString("<p class=\"dp-structure-kpi__value\">Nicht prüfbar</p>")))
                .andExpect(content().string(containsString("Kein Datenmodell")))
                .andExpect(content().string(containsString("<p class=\"dp-structure-kpi__value\">36176</p>")))
                .andExpect(content().string(containsString("<p class=\"dp-structure-kpi__value\">7</p>")))
                .andExpect(content().string(containsString("dp-quality-card")))
                .andExpect(content().string(containsString("Für dieses Datenthema ist kein Datenmodell hinterlegt. Ohne Datenmodell kann die Struktur nicht automatisiert geprüft oder validiert werden.")))
                .andExpect(content().string(containsString("id=\"metadata-origin-usage\"")))
                .andExpect(content().string(containsString("Herkunft &amp; Verwendung")))
                .andExpect(content().string(containsString("Erhebungs- / Messmethode")))
                .andExpect(content().string(containsString("Hilfsdaten")))
                .andExpect(content().string(containsString("Weitere Verwendungen")))
                .andExpect(content().string(containsString("Verfügbare Daten ab")))
                .andExpect(content().string(not(containsString("Gemeinden"))))
                .andExpect(content().string(not(containsString("Felder"))))
                .andExpect(content().string(not(containsString(">Herkunft</h2>"))))
                .andExpect(content().string(not(containsString(">Verwendung</h2>"))))
                .andExpect(content().string(not(containsString("dp-data-model-card"))));
    }

    @Test
    void datasetUsageRendersDirectAccessCodeExamplesAndStarterRecipesWithoutAside() throws Exception {
        mockMvc.perform(get("/datasets/ch.so.verwaltungseinheiten/usage"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<so-header")))
                .andExpect(content().string(containsString("<so-breadcrumb>")))
                .andExpect(content().string(containsString("<so-breadcrumb-item href=\"/datasets/ch.so.verwaltungseinheiten\">Verwaltungseinheiten</so-breadcrumb-item>")))
                .andExpect(content().string(containsString("<so-breadcrumb-item iscurrentpage>Daten verwenden</so-breadcrumb-item>")))
                .andExpect(content().string(containsString("<h1 class=\"dp-detail-title\">Daten verwenden</h1>")))
                .andExpect(content().string(containsString("Direktzugriff")))
                .andExpect(content().string(containsString("Codebeispiele")))
                .andExpect(content().string(containsString("Starter-Rezepte")))
                .andExpect(content().string(containsString("class=\"dp-detail-summary-layout dp-usage-layout\"")))
                .andExpect(content().string(containsString("<th scope=\"col\">Format</th>")))
                .andExpect(content().string(containsString("<th scope=\"col\">Beschreibung</th>")))
                .andExpect(content().string(containsString("<th scope=\"col\">Aktion</th>")))
                .andExpect(content().string(not(containsString("<th scope=\"col\">Inhalt</th>"))))
                .andExpect(content().string(containsString("href=\"http://localhost:8081/ch.so.datenportal/downloads/ch.so.verwaltungseinheiten.csv\"")))
                .andExpect(content().string(containsString("href=\"http://localhost:8081/ch.so.datenportal/downloads/ch.so.verwaltungseinheiten.xlsx\"")))
                .andExpect(content().string(containsString("href=\"http://localhost:8081/ch.so.datenportal/downloads/ch.so.verwaltungseinheiten.parquet\"")))
                .andExpect(content().string(containsString("class=\"dp-usage-format-badge\">CSV</span>")))
                .andExpect(content().string(containsString("class=\"dp-usage-format-badge\">XLSX</span>")))
                .andExpect(content().string(containsString("class=\"dp-usage-format-badge\">Parquet</span>")))
                .andExpect(content().string(containsString("Trennzeichen: Semikolon (;) · Texttrenner: Kein Texttrenner · Encoding: UTF-8")))
                .andExpect(content().string(containsString("data-copy-value=\"http://localhost:8081/ch.so.datenportal/downloads/ch.so.verwaltungseinheiten.csv\"")))
                .andExpect(content().string(containsString("data-copy-success-label=\"URL kopiert: CSV\"")))
                .andExpect(content().string(containsString("URL kopieren")))
                .andExpect(content().string(containsString("URL kopiert")))
                .andExpect(content().string(containsString("class=\"bi bi-download\"")))
                .andExpect(content().string(containsString("bi bi-clipboard")))
                .andExpect(content().string(containsString("class=\"bi bi-check-lg")))
                .andExpect(content().string(containsString("role=\"tablist\"")))
                .andExpect(content().string(containsString("data-usage-tab=\"curl\"")))
                .andExpect(content().string(containsString("data-usage-tab=\"python\"")))
                .andExpect(content().string(containsString("data-usage-tab=\"duckdb\"")))
                .andExpect(content().string(containsString("data-copy-success-label=\"cURL-Code kopiert\"")))
                .andExpect(content().string(containsString("curl -L -o")))
                .andExpect(content().string(containsString("ch.so.verwaltungseinheiten.csv")))
                .andExpect(content().string(containsString("pd.read_csv")))
                .andExpect(content().string(containsString("read_parquet")))
                .andExpect(content().string(containsString("Mit DuckDB analysieren")))
                .andExpect(content().string(containsString("In R auswerten")))
                .andExpect(content().string(containsString("Daten in R einlesen und analysieren (readr oder data.table).")))
                .andExpect(content().string(containsString("In Python weiterverarbeiten")))
                .andExpect(content().string(containsString("Daten mit pandas laden und weiterverarbeiten.")))
                .andExpect(content().string(containsString("src=\"/images/usage-recipes/excel.png\"")))
                .andExpect(content().string(containsString("src=\"/images/usage-recipes/duckdb.png\"")))
                .andExpect(content().string(containsString("src=\"/images/usage-recipes/r.png\"")))
                .andExpect(content().string(containsString("src=\"/images/usage-recipes/python.png\"")))
                .andExpect(content().string(containsString("width=\"24\" height=\"24\"")))
                .andExpect(content().string(containsString("class=\"bi bi-box-arrow-up-right\"")))
                .andExpect(content().string(containsString("class=\"dp-usage-recipe__title\"")))
                .andExpect(content().string(containsString("class=\"dp-usage-recipe__name\"")))
                .andExpect(content().string(not(containsString("<strong>In Excel öffnen</strong>"))))
                .andExpect(content().string(containsString("href=\"#\"")))
                .andExpect(content().string(not(containsString("class=\"bi bi-file-earmark-excel\""))))
                .andExpect(content().string(not(containsString("class=\"bi bi-r-circle\""))))
                .andExpect(content().string(not(containsString("class=\"bi bi-filetype-py\""))))
                .andExpect(content().string(not(containsString("In QGIS laden"))))
                .andExpect(content().string(not(containsString("In PostGIS importieren"))))
                .andExpect(content().string(not(containsString("<aside"))))
                .andExpect(content().string(not(containsString("dp-usage-layout__reserve"))))
                .andExpect(content().string(not(containsString("Hinweise zur Verwendung"))))
                .andExpect(content().string(not(containsString("<h2>1."))))
                .andExpect(content().string(not(containsString(">Verwaltungseinheiten</h2>"))));
    }

    @Test
    void datasetDetailShowsAttributeFeatureWithoutValidationWhenModelIsMissing() throws Exception {
        mockMvc.perform(get("/datasets/ch.so.wasserqualitaet_grundwasser"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Wasserqualität Grundwasser")))
                .andExpect(content().string(containsString("Attribute beschrieben")))
                .andExpect(content().string(containsString("Daten validiert")))
                .andExpect(content().string(containsString("class=\"bi bi-check-circle\"")))
                .andExpect(content().string(containsString("class=\"bi bi-x-circle\"")))
                .andExpect(content().string(not(containsString("Übrige Informationen"))))
                .andExpect(content().string(not(containsString("Erhebungs- / Messmethode"))))
                .andExpect(content().string(not(containsString("Verfügbare Daten ab"))))
                .andExpect(content().string(not(containsString("Weitere Verwendungen"))))
                .andExpect(content().string(not(containsString("Hilfsdaten"))));
    }

    @Test
    void seriesDetailRendersSimplifiedIssueListWithoutCurrentIssuePanelDownloadsOrMetadata() throws Exception {
        mockMvc.perform(get("/series/ch.so.abstimmungsresultate"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Abstimmungsresultate")))
                .andExpect(content().string(containsString("Kantonale und eidgenössische Abstimmungsresultate nach Gemeinde.")))
                .andExpect(content().string(containsString("<p class=\"dp-series-issues-intro\">Zu dieser Serie sind folgende Ausgaben verfügbar:</p>")))
                .andExpect(content().string(containsString("dp-series-issues")))
                .andExpect(content().string(containsString("<section class=\"dp-series-issues\" aria-label=\"Ausgaben\">")))
                .andExpect(content().string(not(containsString("id=\"series-issues-title\""))))
                .andExpect(content().string(not(containsString(">Ausgaben</h2>"))))
                .andExpect(content().string(not(containsString("class=\"dp-detail-panel dp-series-issues\""))))
                .andExpect(content().string(containsString("Aktuelle Ausgabe")))
                .andExpect(content().string(containsString("class=\"dp-status-badge dp-status-badge--info\"")))
                .andExpect(content().string(containsString("Ausgabe 2026")))
                .andExpect(content().string(containsString("href=\"/series/ch.so.abstimmungsresultate/issues/current\"")))
                .andExpect(content().string(containsString("<h2>")))
                .andExpect(content().string(containsString("<a href=\"/series/ch.so.abstimmungsresultate/issues/current\">Abstimmungsresultate 2026</a>")))
                .andExpect(content().string(not(containsString("<h3>\n              <a href=\"/series/ch.so.abstimmungsresultate/issues/current\">Abstimmungsresultate 2026</a>"))))
                .andExpect(content().string(containsString("<a class=\"dp-series-issue__detail-link\" href=\"/series/ch.so.abstimmungsresultate/issues/current\">Detailseite anzeigen <span aria-hidden=\"true\">→</span></a>")))
                .andExpect(content().string(containsString("Abstimmungsresultate 2025")))
                .andExpect(content().string(containsString("Abstimmungsresultate 2024")))
                .andExpect(content().string(containsString("CSV herunterladen: Abstimmungsresultate 2026")))
                .andExpect(content().string(not(containsString("(aktuelle Ausgabe)"))))
                .andExpect(content().string(containsString("href=\"http://localhost:8081/ch.so.datenportal/downloads/ch.so.abstimmungsresultate_2026.csv\"")))
                .andExpect(content().string(not(containsString("dp-current-issue"))))
                .andExpect(content().string(not(containsString("class=\"dp-detail-panel dp-detail-downloads\""))))
                .andExpect(content().string(not(containsString("<aside class=\"dp-detail-side\" aria-label=\"Downloads\">"))))
                .andExpect(content().string(not(containsString("<dl class=\"dp-detail-facts\""))))
                .andExpect(content().string(not(containsString("id=\"metadata-overview\""))))
                .andExpect(content().string(not(containsString("id=\"metadata-topics\""))))
                .andExpect(content().string(not(containsString("id=\"metadata-responsibility\""))))
                .andExpect(content().string(not(containsString("id=\"metadata-usage\""))))
                .andExpect(content().string(not(containsString("id=\"metadata-time\""))))
                .andExpect(content().string(not(containsString("id=\"metadata-resources\""))));
    }

    @Test
    void currentIssueDetailRendersConcreteIssueWithDatasetDetailCards() throws Exception {
        mockMvc.perform(get("/series/ch.so.abstimmungsresultate/issues/current"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<so-header")))
                .andExpect(content().string(containsString("<so-breadcrumb>")))
                .andExpect(content().string(containsString("<section class=\"dp-detail-page dp-issue-detail-page\">")))
                .andExpect(content().string(containsString("<so-breadcrumb-item href=\"/datasets\">Daten und Statistiken</so-breadcrumb-item>")))
                .andExpect(content().string(containsString("<so-breadcrumb-item href=\"/series/ch.so.abstimmungsresultate\">Abstimmungsresultate</so-breadcrumb-item>")))
                .andExpect(content().string(containsString("<so-breadcrumb-item iscurrentpage>Abstimmungsresultate 2026</so-breadcrumb-item>")))
                .andExpect(content().string(containsString("Abstimmungsresultate 2026")))
                .andExpect(content().string(containsString("<span class=\"dp-status-badge dp-status-badge--info\">Aktuelle Ausgabe</span>")))
                .andExpect(content().string(containsString("Ausgabe 2026 der Datenreihe Abstimmungsresultate.")))
                .andExpect(content().string(containsString("Datenreihe:")))
                .andExpect(content().string(containsString("href=\"/series/ch.so.abstimmungsresultate\"")))
                .andExpect(content().string(containsString("Datenmerkmale")))
                .andExpect(content().string(containsString("Open Data")))
                .andExpect(content().string(containsString("Attribute beschrieben")))
                .andExpect(content().string(containsString("Daten validiert")))
                .andExpect(content().string(containsString("href=\"http://localhost:8081/ch.so.datenportal/downloads/ch.so.abstimmungsresultate_2026.csv\"")))
                .andExpect(content().string(containsString("Übersicht")))
                .andExpect(content().string(containsString("Identifier")))
                .andExpect(content().string(containsString("ch.so.abstimmungsresultate_2026")))
                .andExpect(content().string(containsString("Typ")))
                .andExpect(content().string(containsString("Ausgabe")))
                .andExpect(content().string(containsString("Zeitliche Abdeckung")))
                .andExpect(content().string(containsString("Stichtag")))
                .andExpect(content().string(containsString("31.12.2026")))
                .andExpect(content().string(containsString("Themen und Schlagworte")))
                .andExpect(content().string(containsString("Abstimmungen, Resultate, Gemeinden, 2026")))
                .andExpect(content().string(containsString("Zuständigkeiten und Kontakt")))
                .andExpect(content().string(containsString("Staatskanzlei")))
                .andExpect(content().string(containsString("Thomas Müller")))
                .andExpect(content().string(not(containsString("Übrige Informationen"))))
                .andExpect(content().string(not(containsString("Erhebungs- / Messmethode"))))
                .andExpect(content().string(containsString("Weitere Ausgaben")))
                .andExpect(content().string(containsString("class=\"dp-detail-panel dp-related-issues\"")))
                .andExpect(content().string(containsString("dp-related-issues")))
                .andExpect(content().string(containsString("Abstimmungsresultate 2025")))
                .andExpect(content().string(containsString("href=\"/series/ch.so.abstimmungsresultate/issues/ch.so.abstimmungsresultate_2025\"")))
                .andExpect(content().string(containsString("Ausgabe 2025 · Publiziert")))
                .andExpect(content().string(containsString("Struktur, Qualität und Herkunft")))
                .andExpect(content().string(containsString("Erkunden")))
                .andExpect(content().string(containsString("Verwenden")))
                .andExpect(content().string(containsString("href=\"/series/ch.so.abstimmungsresultate/issues/current/structure-quality-origin\"")))
                .andExpect(content().string(containsString("href=\"/series/ch.so.abstimmungsresultate/issues/current/explore\"")))
                .andExpect(content().string(containsString("href=\"/series/ch.so.abstimmungsresultate/issues/current/usage\"")))
                .andExpect(content().string(not(containsString("dp-detail-action-item dp-detail-action-item--disabled"))))
                .andExpect(content().string(not(containsString("dp-detail-action-link dp-detail-action-link--disabled"))))
                .andExpect(content().string(not(containsString("dp-detail-action-lock"))))
                .andExpect(content().string(not(containsString("Datenvorschau nicht verfügbar"))))
                .andExpect(content().string(not(containsString("href=\"#\""))))
                .andExpect(content().string(containsString("Datenvorschau anzeigen <span aria-hidden=\"true\">&rarr;</span>")))
                .andExpect(content().string(containsString("<aside class=\"dp-detail-side\" aria-label=\"Daten nutzen\">")))
                .andExpect(content().string(not(containsString("Diese Ausgabe ist aktuell."))))
                .andExpect(content().string(not(containsString("dp-series-issues"))))
                .andExpect(content().string(not(containsString("series-issues-title"))))
                .andExpect(content().string(not(containsString("<dl class=\"dp-detail-facts\""))))
                .andExpect(content().string(not(containsString("CSV herunterladen: Abstimmungsresultate 2025"))))
                .andExpect(content().string(not(containsString("<aside class=\"dp-detail-side\" aria-label=\"Downloads\">"))))
                .andExpect(content().string(not(containsString("id=\"metadata-responsibility\""))))
                .andExpect(content().string(not(containsString("id=\"metadata-usage\""))))
                .andExpect(content().string(not(containsString("id=\"metadata-time\""))))
                .andExpect(content().string(not(containsString("id=\"metadata-resources\""))));
    }

    @Test
    void currentIssueStructureQualityOriginRendersIssueAttributesAndDataModel() throws Exception {
        mockMvc.perform(get("/series/ch.so.abstimmungsresultate/issues/current/structure-quality-origin"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<so-breadcrumb-item href=\"/series/ch.so.abstimmungsresultate\">Abstimmungsresultate</so-breadcrumb-item>")))
                .andExpect(content().string(containsString("<so-breadcrumb-item href=\"/series/ch.so.abstimmungsresultate/issues/current\">Abstimmungsresultate 2026</so-breadcrumb-item>")))
                .andExpect(content().string(containsString("<so-breadcrumb-item iscurrentpage>Struktur, Qualität und Herkunft</so-breadcrumb-item>")))
                .andExpect(content().string(containsString("<th scope=\"row\">datum</th>")))
                .andExpect(content().string(containsString("Datum der Abstimmung oder Wahl.")))
                .andExpect(content().string(containsString("<th scope=\"row\">ja_stimmen</th>")))
                .andExpect(content().string(containsString("<td>Stimmen</td>")))
                .andExpect(content().string(containsString("SO_SK_Politik_Abstimmungen_Publikation_20260624")))
                .andExpect(content().string(containsString("ilivalidator.log")));
    }

    @Test
    void currentIssueUsageRendersConcreteIssueDownloadsWithoutAside() throws Exception {
        mockMvc.perform(get("/series/ch.so.abstimmungsresultate/issues/current/usage"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<so-breadcrumb-item href=\"/series/ch.so.abstimmungsresultate\">Abstimmungsresultate</so-breadcrumb-item>")))
                .andExpect(content().string(containsString("<so-breadcrumb-item href=\"/series/ch.so.abstimmungsresultate/issues/current\">Abstimmungsresultate 2026</so-breadcrumb-item>")))
                .andExpect(content().string(containsString("<so-breadcrumb-item iscurrentpage>Daten verwenden</so-breadcrumb-item>")))
                .andExpect(content().string(containsString("<h1 class=\"dp-detail-title\">Daten verwenden</h1>")))
                .andExpect(content().string(containsString("href=\"http://localhost:8081/ch.so.datenportal/downloads/ch.so.abstimmungsresultate_2026.csv\"")))
                .andExpect(content().string(containsString("href=\"http://localhost:8081/ch.so.datenportal/downloads/ch.so.abstimmungsresultate_2026.xlsx\"")))
                .andExpect(content().string(containsString("href=\"http://localhost:8081/ch.so.datenportal/downloads/ch.so.abstimmungsresultate_2026.parquet\"")))
                .andExpect(content().string(containsString("curl -L -o")))
                .andExpect(content().string(containsString("pd.read_csv")))
                .andExpect(content().string(containsString("read_parquet")))
                .andExpect(content().string(not(containsString("<aside"))))
                .andExpect(content().string(not(containsString("Hinweise zur Verwendung"))));
    }

    @Test
    void historicalIssueDetailRendersSelectedIssue() throws Exception {
        mockMvc.perform(get("/series/ch.so.abstimmungsresultate/issues/ch.so.abstimmungsresultate_2025"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Abstimmungsresultate 2025")))
                .andExpect(content().string(containsString("<section class=\"dp-detail-page dp-issue-detail-page\">")))
                .andExpect(content().string(containsString("Ausgabe 2025")))
                .andExpect(content().string(containsString("href=\"http://localhost:8081/ch.so.datenportal/downloads/ch.so.abstimmungsresultate_2025.csv\"")))
                .andExpect(content().string(containsString("Datenmerkmale")))
                .andExpect(content().string(containsString("Übersicht")))
                .andExpect(content().string(containsString("Zuständigkeiten und Kontakt")))
                .andExpect(content().string(containsString("Weitere Ausgaben")))
                .andExpect(content().string(containsString("Abstimmungsresultate 2026")))
                .andExpect(content().string(containsString("href=\"/series/ch.so.abstimmungsresultate/issues/current\"")))
                .andExpect(content().string(containsString("<span class=\"dp-status-badge dp-status-badge--info\">Aktuelle Ausgabe</span>")))
                .andExpect(content().string(containsString("<aside class=\"dp-detail-side\" aria-label=\"Daten nutzen\">")))
                .andExpect(content().string(containsString("href=\"/series/ch.so.abstimmungsresultate/issues/ch.so.abstimmungsresultate_2025/structure-quality-origin\"")))
                .andExpect(content().string(containsString("href=\"/series/ch.so.abstimmungsresultate/issues/ch.so.abstimmungsresultate_2025/explore\"")))
                .andExpect(content().string(containsString("href=\"/series/ch.so.abstimmungsresultate/issues/ch.so.abstimmungsresultate_2025/usage\"")))
                .andExpect(content().string(not(containsString("dp-detail-action-item dp-detail-action-item--disabled"))))
                .andExpect(content().string(not(containsString("dp-detail-action-link dp-detail-action-link--disabled"))))
                .andExpect(content().string(not(containsString("Datenvorschau nicht verfügbar"))))
                .andExpect(content().string(not(containsString("href=\"#\""))))
                .andExpect(content().string(containsString("Datenvorschau anzeigen <span aria-hidden=\"true\">&rarr;</span>")))
                .andExpect(content().string(not(containsString("Diese Ausgabe ist aktuell."))))
                .andExpect(content().string(not(containsString("dp-series-issues"))))
                .andExpect(content().string(not(containsString("series-issues-title"))))
                .andExpect(content().string(not(containsString("<dl class=\"dp-detail-facts\""))))
                .andExpect(content().string(not(containsString("CSV herunterladen: Abstimmungsresultate 2026"))));
    }

    @Test
    void historicalIssueStructureQualityOriginRendersSelectedIssue() throws Exception {
        mockMvc.perform(get("/series/ch.so.abstimmungsresultate/issues/ch.so.abstimmungsresultate_2025/structure-quality-origin"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<so-breadcrumb-item href=\"/series/ch.so.abstimmungsresultate/issues/ch.so.abstimmungsresultate_2025\">Abstimmungsresultate 2025</so-breadcrumb-item>")))
                .andExpect(content().string(containsString("<th scope=\"row\">stimmbeteiligung_prozent</th>")))
                .andExpect(content().string(containsString("Stimmbeteiligung in Prozent.")))
                .andExpect(content().string(containsString("SO_SK_Politik_Abstimmungen_Publikation_20260624")));
    }

    @Test
    void historicalIssueUsageRendersSelectedIssueDownloads() throws Exception {
        mockMvc.perform(get("/series/ch.so.abstimmungsresultate/issues/ch.so.abstimmungsresultate_2025/usage"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<so-breadcrumb-item href=\"/series/ch.so.abstimmungsresultate/issues/ch.so.abstimmungsresultate_2025\">Abstimmungsresultate 2025</so-breadcrumb-item>")))
                .andExpect(content().string(containsString("href=\"http://localhost:8081/ch.so.datenportal/downloads/ch.so.abstimmungsresultate_2025.csv\"")))
                .andExpect(content().string(not(containsString("href=\"http://localhost:8081/ch.so.datenportal/downloads/ch.so.abstimmungsresultate_2026.csv\""))))
                .andExpect(content().string(not(containsString("<aside"))));
    }

    @Test
    void nonOpenDatasetDetailShowsAccessBadgeAndLockInsteadOfDownloads() throws Exception {
        mockMvc.perform(get("/datasets/ch.2581.baumkataster"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Baumkataster")))
                .andExpect(content().string(containsString("class=\"bi bi-lock\"")))
                .andExpect(content().string(containsString("class=\"bi bi-x-circle\"")))
                .andExpect(content().string(containsString("Struktur, Qualität und Herkunft anzeigen")))
                .andExpect(content().string(containsString("href=\"/datasets/ch.2581.baumkataster/structure-quality-origin\"")))
                .andExpect(content().string(containsString("Erkunden")))
                .andExpect(content().string(containsString("Verwenden")))
                .andExpect(content().string(containsString("dp-detail-action-item dp-detail-action-item--disabled")))
                .andExpect(content().string(containsString("dp-detail-action-link dp-detail-action-link--disabled")))
                .andExpect(content().string(containsString("dp-detail-action-lock")))
                .andExpect(content().string(containsString("Datenvorschau nicht verfügbar")))
                .andExpect(content().string(containsString("Downloads nicht verfügbar")))
                .andExpect(content().string(containsString("aria-disabled=\"true\"")))
                .andExpect(content().string(not(containsString("href=\"http://localhost:8081/ch.so.datenportal/downloads/ch.2581.baumkataster.csv\""))))
                .andExpect(content().string(not(containsString("href=\"/datasets/ch.2581.baumkataster/usage\""))))
                .andExpect(content().string(not(containsString("href=\"#\""))))
                .andExpect(content().string(not(containsString("Datenvorschau anzeigen <span aria-hidden=\"true\">&rarr;</span>"))))
                .andExpect(content().string(not(containsString("Downloads anzeigen <span aria-hidden=\"true\">&rarr;</span>"))))
                .andExpect(content().string(not(containsString("dp-status-badge dp-status-badge--positive"))));
    }

    @Test
    void nonOpenSeriesDetailShowsLockInIssueList() throws Exception {
        mockMvc.perform(get("/series/ch.so.baustellen.koordinationsplanung"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Baustellen-Koordinationsplanung")))
                .andExpect(content().string(containsString("Öffentlich mit Bedingungen")))
                .andExpect(content().string(containsString("class=\"bi bi-lock\"")))
                .andExpect(content().string(not(containsString("href=\"http://localhost:8081/ch.so.datenportal/downloads/ch.so.baustellen.koordinationsplanung_2026.csv\""))))
                .andExpect(content().string(not(containsString("href=\"http://localhost:8081/ch.so.datenportal/downloads/ch.so.baustellen.koordinationsplanung_2025.csv\""))))
                .andExpect(content().string(not(containsString(">Open Data</span>"))));
    }

    @Test
    void nonOpenIssueDetailShowsAccessBadgeAndLockInsteadOfDownloads() throws Exception {
        mockMvc.perform(get("/series/ch.so.baustellen.koordinationsplanung/issues/current"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Baustellen-Koordinationsplanung 2026")))
                .andExpect(content().string(containsString("Öffentlich mit Bedingungen")))
                .andExpect(content().string(containsString("class=\"bi bi-lock\"")))
                .andExpect(content().string(containsString("Open Data")))
                .andExpect(content().string(containsString("class=\"bi bi-x-circle\"")))
                .andExpect(content().string(containsString("Struktur, Qualität und Herkunft anzeigen")))
                .andExpect(content().string(containsString("href=\"/series/ch.so.baustellen.koordinationsplanung/issues/current/structure-quality-origin\"")))
                .andExpect(content().string(containsString("Erkunden")))
                .andExpect(content().string(containsString("Verwenden")))
                .andExpect(content().string(containsString("dp-detail-action-item dp-detail-action-item--disabled")))
                .andExpect(content().string(containsString("dp-detail-action-link dp-detail-action-link--disabled")))
                .andExpect(content().string(containsString("dp-detail-action-lock")))
                .andExpect(content().string(containsString("Datenvorschau nicht verfügbar")))
                .andExpect(content().string(containsString("Downloads nicht verfügbar")))
                .andExpect(content().string(containsString("aria-disabled=\"true\"")))
                .andExpect(content().string(not(containsString("href=\"http://localhost:8081/ch.so.datenportal/downloads/ch.so.baustellen.koordinationsplanung_2026.csv\""))))
                .andExpect(content().string(not(containsString("href=\"/series/ch.so.baustellen.koordinationsplanung/issues/current/usage\""))))
                .andExpect(content().string(not(containsString("href=\"#\""))))
                .andExpect(content().string(not(containsString("Datenvorschau anzeigen <span aria-hidden=\"true\">&rarr;</span>"))))
                .andExpect(content().string(not(containsString("Downloads anzeigen <span aria-hidden=\"true\">&rarr;</span>"))))
                .andExpect(content().string(not(containsString("dp-status-badge dp-status-badge--positive"))));
    }

    @Test
    void detailRoutePathVariablesDeclareExplicitNames() throws NoSuchMethodException {
        assertPathVariableName("datasetDetail", new Class<?>[] {String.class, Model.class}, 0, "identifier");
        assertPathVariableName("datasetStructureQualityOrigin", new Class<?>[] {String.class, Model.class}, 0, "identifier");
        assertPathVariableName("datasetUsage", new Class<?>[] {String.class, Model.class}, 0, "identifier");
        assertPathVariableName("seriesDetail", new Class<?>[] {String.class, Model.class}, 0, "seriesIdentifier");
        assertPathVariableName("currentIssueDetail", new Class<?>[] {String.class, Model.class}, 0, "seriesIdentifier");
        assertPathVariableName(
                "currentIssueStructureQualityOrigin",
                new Class<?>[] {String.class, Model.class},
                0,
                "seriesIdentifier");
        assertPathVariableName(
                "currentIssueUsage",
                new Class<?>[] {String.class, Model.class},
                0,
                "seriesIdentifier");
        assertPathVariableName(
                "issueDetail",
                new Class<?>[] {String.class, String.class, Model.class},
                0,
                "seriesIdentifier");
        assertPathVariableName(
                "issueDetail",
                new Class<?>[] {String.class, String.class, Model.class},
                1,
                "issueIdentifier");
        assertPathVariableName(
                "issueStructureQualityOrigin",
                new Class<?>[] {String.class, String.class, Model.class},
                0,
                "seriesIdentifier");
        assertPathVariableName(
                "issueStructureQualityOrigin",
                new Class<?>[] {String.class, String.class, Model.class},
                1,
                "issueIdentifier");
        assertPathVariableName(
                "issueUsage",
                new Class<?>[] {String.class, String.class, Model.class},
                0,
                "seriesIdentifier");
        assertPathVariableName(
                "issueUsage",
                new Class<?>[] {String.class, String.class, Model.class},
                1,
                "issueIdentifier");
    }

    @Test
    void unknownIdentifierReturns404() throws Exception {
        mockMvc.perform(get("/datasets/does-not-exist"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Seite nicht gefunden")))
                .andExpect(content().string(containsString("Zurück zu Daten")));
    }

    @Test
    void wrongRouteTypeReturns404() throws Exception {
        mockMvc.perform(get("/datasets/ch.so.abstimmungsresultate"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Seite nicht gefunden")));
    }

    @Test
    void wrongStructureQualityOriginRouteTypeReturns404() throws Exception {
        mockMvc.perform(get("/datasets/ch.so.abstimmungsresultate/structure-quality-origin"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Seite nicht gefunden")));
    }

    @Test
    void oldDatasetStructureQualityRouteReturns404() throws Exception {
        mockMvc.perform(get("/datasets/ch.so.bauinventar/structure-quality"))
                .andExpect(status().isNotFound());
    }

    @Test
    void oldCurrentIssueStructureQualityRouteReturns404() throws Exception {
        mockMvc.perform(get("/series/ch.so.abstimmungsresultate/issues/current/structure-quality"))
                .andExpect(status().isNotFound());
    }

    @Test
    void oldHistoricalIssueStructureQualityRouteReturns404() throws Exception {
        mockMvc.perform(get("/series/ch.so.abstimmungsresultate/issues/ch.so.abstimmungsresultate_2025/structure-quality"))
                .andExpect(status().isNotFound());
    }

    @Test
    void issueIdentifierUnderWrongSeriesReturns404() throws Exception {
        mockMvc.perform(get("/series/ch.so.gemeindegrenzen/issues/ch.so.abstimmungsresultate_2026"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Seite nicht gefunden")));
    }

    @Test
    void structureQualityOriginIssueIdentifierUnderWrongSeriesReturns404() throws Exception {
        mockMvc.perform(get("/series/ch.so.gemeindegrenzen/issues/ch.so.abstimmungsresultate_2026/structure-quality-origin"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Seite nicht gefunden")));
    }

    private static void assertPathVariableName(
            String methodName,
            Class<?>[] parameterTypes,
            int parameterIndex,
            String expectedName) throws NoSuchMethodException {
        Method method = CatalogDetailController.class.getMethod(methodName, parameterTypes);
        PathVariable annotation = method.getParameters()[parameterIndex].getAnnotation(PathVariable.class);

        assertThat(annotation).isNotNull();
        assertThat(annotation.value()).isEqualTo(expectedName);
    }
}
