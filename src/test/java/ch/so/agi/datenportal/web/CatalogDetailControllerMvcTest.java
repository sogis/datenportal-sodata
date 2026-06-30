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
                .andExpect(content().string(containsString("<so-breadcrumb-item href=\"/datasets\">Daten und Statistiken</so-breadcrumb-item>")))
                .andExpect(content().string(containsString("Bauinventar")))
                .andExpect(content().string(containsString("Inventar schützenswerter und erhaltenswerter Bauten.")))
                .andExpect(content().string(containsString("Open Data")))
                .andExpect(content().string(containsString("href=\"https://data.so.ch/download/ch.so.bauinventar.csv\"")))
                .andExpect(content().string(containsString("href=\"https://data.so.ch/download/ch.so.bauinventar.xlsx\"")))
                .andExpect(content().string(containsString("href=\"https://data.so.ch/download/ch.so.bauinventar.parquet\"")))
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
                .andExpect(content().string(containsString("Struktur, Qualität und Herkunft")))
                .andExpect(content().string(containsString("Erkunden")))
                .andExpect(content().string(containsString("Verwenden")))
                .andExpect(content().string(containsString("Details ansehen")))
                .andExpect(content().string(containsString("Datenvorschau anzeigen")))
                .andExpect(content().string(containsString("Downloads anzeigen")))
                .andExpect(content().string(containsString("href=\"/datasets/ch.so.bauinventar/structure-quality-origin\"")))
                .andExpect(content().string(containsString("href=\"#\"")))
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
                .andExpect(content().string(containsString("class=\"dp-detail-panel dp-quality-card\"")))
                .andExpect(content().string(containsString("Qualität")))
                .andExpect(content().string(containsString("SO_AGI_Geobasisdaten_Publikation_20260624")))
                .andExpect(content().string(containsString("Validierungsreport")))
                .andExpect(content().string(containsString("ilivalidator.log")))
                .andExpect(content().string(not(containsString("Beispiel"))))
                .andExpect(content().string(not(containsString("dp-detail-actions"))));
    }

    @Test
    void datasetStructureQualityOriginShowsQualityMessageWhenModelIsMissing() throws Exception {
        mockMvc.perform(get("/datasets/ch.so.wasserqualitaet_grundwasser/structure-quality-origin"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<th scope=\"row\">jahr</th>")))
                .andExpect(content().string(containsString("Jahr der Messung.")))
                .andExpect(content().string(containsString("<td>Jahr</td>")))
                .andExpect(content().string(containsString("dp-quality-card")))
                .andExpect(content().string(containsString("Für dieses Datenthema ist kein Datenmodell hinterlegt. Ohne Datenmodell kann die Struktur nicht automatisiert geprüft oder validiert werden.")))
                .andExpect(content().string(containsString("Herkunft &amp; Verwendung")))
                .andExpect(content().string(containsString("Erhebungs- / Messmethode")))
                .andExpect(content().string(containsString("Hilfsdaten")))
                .andExpect(content().string(containsString("Weitere Verwendungen")))
                .andExpect(content().string(containsString("Verfügbare Daten ab")))
                .andExpect(content().string(not(containsString(">Herkunft</h2>"))))
                .andExpect(content().string(not(containsString(">Verwendung</h2>"))))
                .andExpect(content().string(not(containsString("dp-data-model-card"))));
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
                .andExpect(content().string(containsString("<section class=\"dp-detail-panel dp-series-issues\" aria-label=\"Ausgaben\">")))
                .andExpect(content().string(not(containsString("id=\"series-issues-title\""))))
                .andExpect(content().string(not(containsString(">Ausgaben</h2>"))))
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
                .andExpect(content().string(containsString("href=\"https://data.so.ch/download/ch.so.abstimmungsresultate_2026.csv\"")))
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
                .andExpect(content().string(containsString("href=\"https://data.so.ch/download/ch.so.abstimmungsresultate_2026.csv\"")))
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
                .andExpect(content().string(containsString("dp-related-issues")))
                .andExpect(content().string(containsString("Abstimmungsresultate 2025")))
                .andExpect(content().string(containsString("href=\"/series/ch.so.abstimmungsresultate/issues/ch.so.abstimmungsresultate_2025\"")))
                .andExpect(content().string(containsString("Ausgabe 2025 · Publiziert")))
                .andExpect(content().string(containsString("Struktur, Qualität und Herkunft")))
                .andExpect(content().string(containsString("Erkunden")))
                .andExpect(content().string(containsString("Verwenden")))
                .andExpect(content().string(containsString("href=\"/series/ch.so.abstimmungsresultate/issues/current/structure-quality-origin\"")))
                .andExpect(content().string(containsString("<aside class=\"dp-detail-side\" aria-label=\"Aktionen\">")))
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
    void historicalIssueDetailRendersSelectedIssue() throws Exception {
        mockMvc.perform(get("/series/ch.so.abstimmungsresultate/issues/ch.so.abstimmungsresultate_2025"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Abstimmungsresultate 2025")))
                .andExpect(content().string(containsString("Ausgabe 2025")))
                .andExpect(content().string(containsString("href=\"https://data.so.ch/download/ch.so.abstimmungsresultate_2025.csv\"")))
                .andExpect(content().string(containsString("Datenmerkmale")))
                .andExpect(content().string(containsString("Übersicht")))
                .andExpect(content().string(containsString("Zuständigkeiten und Kontakt")))
                .andExpect(content().string(containsString("Weitere Ausgaben")))
                .andExpect(content().string(containsString("Abstimmungsresultate 2026")))
                .andExpect(content().string(containsString("href=\"/series/ch.so.abstimmungsresultate/issues/current\"")))
                .andExpect(content().string(containsString("<span class=\"dp-status-badge dp-status-badge--info\">Aktuelle Ausgabe</span>")))
                .andExpect(content().string(containsString("<aside class=\"dp-detail-side\" aria-label=\"Aktionen\">")))
                .andExpect(content().string(containsString("href=\"/series/ch.so.abstimmungsresultate/issues/ch.so.abstimmungsresultate_2025/structure-quality-origin\"")))
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
    void nonOpenDatasetDetailShowsAccessBadgeAndLockInsteadOfDownloads() throws Exception {
        mockMvc.perform(get("/datasets/ch.2581.baumkataster"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Baumkataster")))
                .andExpect(content().string(containsString("class=\"bi bi-lock\"")))
                .andExpect(content().string(containsString("class=\"bi bi-x-circle\"")))
                .andExpect(content().string(not(containsString("href=\"https://data.so.ch/download/ch.2581.baumkataster.csv\""))))
                .andExpect(content().string(not(containsString("dp-status-badge dp-status-badge--positive"))));
    }

    @Test
    void nonOpenSeriesDetailShowsLockInIssueList() throws Exception {
        mockMvc.perform(get("/series/ch.so.baustellen.koordinationsplanung"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Baustellen-Koordinationsplanung")))
                .andExpect(content().string(containsString("Öffentlich mit Bedingungen")))
                .andExpect(content().string(containsString("class=\"bi bi-lock\"")))
                .andExpect(content().string(not(containsString("href=\"https://data.so.ch/download/ch.so.baustellen.koordinationsplanung_2026.csv\""))))
                .andExpect(content().string(not(containsString("href=\"https://data.so.ch/download/ch.so.baustellen.koordinationsplanung_2025.csv\""))))
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
                .andExpect(content().string(not(containsString("href=\"https://data.so.ch/download/ch.so.baustellen.koordinationsplanung_2026.csv\""))))
                .andExpect(content().string(not(containsString("dp-status-badge dp-status-badge--positive"))));
    }

    @Test
    void detailRoutePathVariablesDeclareExplicitNames() throws NoSuchMethodException {
        assertPathVariableName("datasetDetail", new Class<?>[] {String.class, Model.class}, 0, "identifier");
        assertPathVariableName("datasetStructureQualityOrigin", new Class<?>[] {String.class, Model.class}, 0, "identifier");
        assertPathVariableName("seriesDetail", new Class<?>[] {String.class, Model.class}, 0, "seriesIdentifier");
        assertPathVariableName("currentIssueDetail", new Class<?>[] {String.class, Model.class}, 0, "seriesIdentifier");
        assertPathVariableName(
                "currentIssueStructureQualityOrigin",
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
