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
                .andExpect(content().string(containsString("<span class=\"dp-type-badge\">Datensatz</span>")))
                .andExpect(content().string(containsString("Open Data")))
                .andExpect(content().string(containsString("href=\"https://data.so.ch/download/ch.so.bauinventar.csv\"")))
                .andExpect(content().string(containsString("href=\"https://data.so.ch/download/ch.so.bauinventar.xlsx\"")))
                .andExpect(content().string(containsString("href=\"https://data.so.ch/download/ch.so.bauinventar.parquet\"")))
                .andExpect(content().string(containsString("Struktur &amp; Qualität")))
                .andExpect(content().string(containsString("Erkunden")))
                .andExpect(content().string(containsString("Verwenden")))
                .andExpect(content().string(containsString("Details ansehen")))
                .andExpect(content().string(containsString("Datenvorschau anzeigen")))
                .andExpect(content().string(containsString("Downloads anzeigen")))
                .andExpect(content().string(containsString("class=\"bi bi-shield-check\"")))
                .andExpect(content().string(containsString("class=\"bi bi-search\"")))
                .andExpect(content().string(containsString("class=\"bi bi-code-slash\"")))
                .andExpect(content().string(containsString("href=\"#\">Details ansehen</a>")))
                .andExpect(content().string(containsString("Daten und Statistiken")))
                .andExpect(content().string(containsString("aria-current=\"page\"")))
                .andExpect(content().string(not(containsString("Übersicht"))))
                .andExpect(content().string(not(containsString("Verantwortlichkeit"))))
                .andExpect(content().string(not(containsString("Themen und Schlagworte"))))
                .andExpect(content().string(not(containsString("Identifier"))))
                .andExpect(content().string(not(containsString("dp-preview"))))
                .andExpect(content().string(not(containsString("chart"))))
                .andExpect(content().string(not(containsString("row-disclosure"))));
    }

    @Test
    void seriesDetailRendersCurrentIssueHistoricalIssuesAndCurrentIssueDownloads() throws Exception {
        mockMvc.perform(get("/series/ch.so.abstimmungsresultate"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Abstimmungsresultate")))
                .andExpect(content().string(containsString("Kantonale und eidgenössische Abstimmungsresultate nach Gemeinde.")))
                .andExpect(content().string(containsString("<span class=\"dp-type-badge\">Datenreihe</span>")))
                .andExpect(content().string(containsString("Aktuelle Ausgabe")))
                .andExpect(content().string(containsString("Ausgabe 2026")))
                .andExpect(content().string(containsString("href=\"/series/ch.so.abstimmungsresultate/issues/current\"")))
                .andExpect(content().string(containsString("Abstimmungsresultate 2025")))
                .andExpect(content().string(containsString("Abstimmungsresultate 2024")))
                .andExpect(content().string(containsString("CSV herunterladen: Abstimmungsresultate 2026")))
                .andExpect(content().string(not(containsString("(aktuelle Ausgabe)"))))
                .andExpect(content().string(containsString("href=\"https://data.so.ch/download/ch.so.abstimmungsresultate_2026.csv\"")));
    }

    @Test
    void currentIssueDetailRendersConcreteIssueAndLinksToSeriesAndOtherIssues() throws Exception {
        mockMvc.perform(get("/series/ch.so.abstimmungsresultate/issues/current"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<so-header")))
                .andExpect(content().string(containsString("<so-breadcrumb>")))
                .andExpect(content().string(containsString("<so-breadcrumb-item href=\"/datasets\">Daten und Statistiken</so-breadcrumb-item>")))
                .andExpect(content().string(containsString("<so-breadcrumb-item href=\"/series/ch.so.abstimmungsresultate\">Abstimmungsresultate</so-breadcrumb-item>")))
                .andExpect(content().string(containsString("<so-breadcrumb-item iscurrentpage>Abstimmungsresultate 2026</so-breadcrumb-item>")))
                .andExpect(content().string(containsString("Abstimmungsresultate 2026")))
                .andExpect(content().string(containsString("Datenreihe:")))
                .andExpect(content().string(containsString("href=\"/series/ch.so.abstimmungsresultate\"")))
                .andExpect(content().string(containsString("Diese Ausgabe ist aktuell.")))
                .andExpect(content().string(containsString("Abstimmungsresultate 2025")))
                .andExpect(content().string(containsString("href=\"/series/ch.so.abstimmungsresultate/issues/ch.so.abstimmungsresultate_2025\"")))
                .andExpect(content().string(containsString("href=\"https://data.so.ch/download/ch.so.abstimmungsresultate_2026.csv\"")));
    }

    @Test
    void historicalIssueDetailRendersSelectedIssue() throws Exception {
        mockMvc.perform(get("/series/ch.so.abstimmungsresultate/issues/ch.so.abstimmungsresultate_2025"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Abstimmungsresultate 2025")))
                .andExpect(content().string(containsString("Ausgabe 2025")))
                .andExpect(content().string(containsString("href=\"https://data.so.ch/download/ch.so.abstimmungsresultate_2025.csv\"")))
                .andExpect(content().string(not(containsString("Diese Ausgabe ist aktuell."))));
    }

    @Test
    void nonOpenDatasetDetailShowsAccessBadgeAndLockInsteadOfDownloads() throws Exception {
        mockMvc.perform(get("/datasets/ch.2581.baumkataster"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Baumkataster")))
                .andExpect(content().string(containsString("dp-status-badge dp-status-badge--warning")))
                .andExpect(content().string(containsString("Oeffentlich mit Bedingungen")))
                .andExpect(content().string(containsString("class=\"bi bi-lock\"")))
                .andExpect(content().string(not(containsString("href=\"https://data.so.ch/download/ch.2581.baumkataster.csv\""))))
                .andExpect(content().string(not(containsString(">Open Data</span>"))));
    }

    @Test
    void nonOpenSeriesDetailShowsLockInCurrentIssueDownloadsAndIssueList() throws Exception {
        mockMvc.perform(get("/series/ch.so.baustellen.koordinationsplanung"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Baustellen-Koordinationsplanung")))
                .andExpect(content().string(containsString("<span class=\"dp-type-badge\">Datenreihe</span>")))
                .andExpect(content().string(containsString("Oeffentlich mit Bedingungen")))
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
                .andExpect(content().string(containsString("Oeffentlich mit Bedingungen")))
                .andExpect(content().string(containsString("class=\"bi bi-lock\"")))
                .andExpect(content().string(not(containsString("href=\"https://data.so.ch/download/ch.so.baustellen.koordinationsplanung_2026.csv\""))))
                .andExpect(content().string(not(containsString(">Open Data</span>"))));
    }

    @Test
    void detailRoutePathVariablesDeclareExplicitNames() throws NoSuchMethodException {
        assertPathVariableName("datasetDetail", new Class<?>[] {String.class, Model.class}, 0, "identifier");
        assertPathVariableName("seriesDetail", new Class<?>[] {String.class, Model.class}, 0, "seriesIdentifier");
        assertPathVariableName("currentIssueDetail", new Class<?>[] {String.class, Model.class}, 0, "seriesIdentifier");
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
    void issueIdentifierUnderWrongSeriesReturns404() throws Exception {
        mockMvc.perform(get("/series/ch.so.gemeindegrenzen/issues/ch.so.abstimmungsresultate_2026"))
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
