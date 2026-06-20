package ch.so.agi.datenportal.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class CatalogControllerMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void homePageReturnsFullPageWithDefaultListViewWithoutPagination() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<html lang=\"de\">")))
                .andExpect(content().string(containsString("<so-header")))
                .andExpect(content().string(containsString("<so-breadcrumb>")))
                .andExpect(content().string(containsString("<so-breadcrumb-item")))
                .andExpect(content().string(containsString("Daten und Statistiken")))
                .andExpect(content().string(containsString("id=\"main-content\"")))
                .andExpect(content().string(containsString("aria-label=\"Breadcrumb\"")))
                .andExpect(content().string(containsString("id=\"catalog-search-form\"")))
                .andExpect(content().string(containsString("id=\"filter-toolbar\"")))
                .andExpect(content().string(containsString("id=\"filter-panel-host-theme\"")))
                .andExpect(content().string(containsString("id=\"filter-panel-host-office\"")))
                .andExpect(content().string(containsString("id=\"filter-panel-host-modified\"")))
                .andExpect(content().string(containsString("id=\"result-controls\"")))
                .andExpect(content().string(containsString("id=\"dataset-results-shell\"")))
                .andExpect(content().string(containsString("id=\"mobile-filter-button\"")))
                .andExpect(content().string(containsString("Thema / Datensatz")))
                .andExpect(content().string(not(containsString("<th scope=\"col\">Typ</th>"))))
                .andExpect(content().string(containsString("Kachelansicht")))
                .andExpect(content().string(containsString("Listenansicht")))
                .andExpect(content().string(containsString("aria-current=\"page\"")))
                .andExpect(content().string(not(containsString("id=\"pagination\""))))
                .andExpect(content().string(not(containsString("Zeilen pro Seite"))))
                .andExpect(content().string(containsString("/js/catalog-filters.js")));
    }

    @Test
    void datasetsAliasReturnsSamePage() throws Exception {
        MvcResult rootResult = mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult datasetsResult = mockMvc.perform(get("/datasets"))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(datasetsResult.getResponse().getContentAsString())
                .isEqualTo(rootResult.getResponse().getContentAsString());
    }

    @Test
    void pageContainsSearchToolbarResultsAndCurrentIssueDownloads() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("href=\"/css/app.css\"")))
                .andExpect(content().string(containsString("src=\"/js/htmx.min.js\"")))
                .andExpect(content().string(containsString("src=\"/js/catalog-filters.js\"")))
                .andExpect(content().string(containsString("src=\"/vendor/so-web-components/0.1.10/index.js\"")))
                .andExpect(content().string(containsString("href=\"/vendor/so-web-components/0.1.10/styles/reset.css\"")))
                .andExpect(content().string(containsString("href=\"/vendor/so-web-components/0.1.10/styles/fonts.css\"")))
                .andExpect(content().string(containsString("href=\"/vendor/so-web-components/0.1.10/styles/tokens.css\"")))
                .andExpect(content().string(containsString("id=\"catalog-search-form\"")))
                .andExpect(content().string(containsString("id=\"filter-trigger-theme\"")))
                .andExpect(content().string(containsString("id=\"filter-trigger-office\"")))
                .andExpect(content().string(containsString("id=\"filter-trigger-modified\"")))
                .andExpect(content().string(containsString("aria-controls=\"filter-panel-host-theme\"")))
                .andExpect(content().string(containsString("hx-target=\"#filter-panel-host-theme\"")))
                .andExpect(content().string(containsString("dp-filter-dropdown--align-end")))
                .andExpect(content().string(not(containsString("id=\"filter-trigger-resourceType\""))))
                .andExpect(content().string(containsString("class=\"bi bi-chevron-down\"")))
                .andExpect(content().string(containsString("class=\"bi bi-arrow-clockwise\"")))
                .andExpect(content().string(containsString("class=\"bi bi-grid\"")))
                .andExpect(content().string(containsString("class=\"bi bi-list-ul\"")))
                .andExpect(content().string(containsString("fill=\"currentColor\"")))
                .andExpect(content().string(containsString("id=\"dataset-results-shell\"")))
                .andExpect(content().string(containsString("id=\"dataset-loading\"")))
                .andExpect(content().string(containsString("name=\"q\"")))
                .andExpect(content().string(containsString("type=\"text\"")))
                .andExpect(content().string(containsString("placeholder=\"Suche nach Datensätzen, Themen, Fachstellen, Schlagworten ...\"")))
                .andExpect(content().string(containsString("id=\"catalog-search-clear\"")))
                .andExpect(content().string(containsString("aria-label=\"Suche zurücksetzen\"")))
                .andExpect(content().string(not(containsString("aria-label=\"Suche ausführen\""))))
                .andExpect(content().string(containsString("dp-view-toggle__link dp-view-toggle__link--list")))
                .andExpect(content().string(not(containsString("dp-view-toggle__divider"))))
                .andExpect(content().string(containsString("onchange=\"this.form.requestSubmit()\"")))
                .andExpect(content().string(not(containsString("dp-filter-trigger__chevron\" aria-hidden=\"true\">⌄"))))
                .andExpect(content().string(not(containsString(">Sortieren</button>"))))
                .andExpect(content().string(not(containsString("id=\"filter-popover-host\""))))
                .andExpect(content().string(containsString("Abstimmungsresultate")))
                .andExpect(content().string(containsString("dp-entry-row dp-entry-row--series")))
                .andExpect(content().string(containsString("class=\"bi bi-plus-lg\"")))
                .andExpect(content().string(containsString("class=\"bi bi-info-circle\"")))
                .andExpect(content().string(containsString("width=\"24\" height=\"24\"")))
                .andExpect(content().string(not(containsString(">i</a>"))))
                .andExpect(content().string(not(containsString("<span aria-hidden=\"true\">+</span>"))))
                .andExpect(content().string(not(containsString("<span aria-hidden=\"true\">-</span>"))))
                .andExpect(content().string(containsString(">CSV</a>")))
                .andExpect(content().string(containsString(">Parquet</a>")))
                .andExpect(content().string(not(containsString("(aktuelle Ausgabe)"))));
    }

    @Test
    void resourceTypeQueryParameterIsIgnoredByCatalogPage() throws Exception {
        mockMvc.perform(get("/datasets").param("resourceType", "series"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"filter-toolbar\"")))
                .andExpect(content().string(containsString("Abstimmungsresultate")))
                .andExpect(content().string(not(containsString("Ressourcentyp"))))
                .andExpect(content().string(not(containsString("resourceType=series"))));
    }

    @Test
    void catalogPageGroupsSearchAndFilterToolbarInSharedControlBand() throws Exception {
        MvcResult result = mockMvc.perform(get("/datasets"))
                .andExpect(status().isOk())
                .andReturn();

        String html = result.getResponse().getContentAsString();
        int controlBandIndex = html.indexOf("class=\"dp-control-band\"");
        int searchIndex = html.indexOf("id=\"catalog-search-form\"", controlBandIndex);
        int filterToolbarIndex = html.indexOf("id=\"filter-toolbar\"", controlBandIndex);
        int activeFiltersIndex = html.indexOf("id=\"active-filter-chips\"", controlBandIndex);
        int resultsStackIndex = html.indexOf("class=\"dp-results-stack\"", controlBandIndex);

        assertThat(controlBandIndex).isGreaterThanOrEqualTo(0);
        assertThat(searchIndex).isGreaterThan(controlBandIndex);
        assertThat(filterToolbarIndex).isGreaterThan(searchIndex);
        assertThat(activeFiltersIndex).isGreaterThan(filterToolbarIndex);
        assertThat(resultsStackIndex).isGreaterThan(activeFiltersIndex);
    }

    @Test
    void catalogPageGroupsResultControlsAndResultsInSharedResultsStack() throws Exception {
        MvcResult result = mockMvc.perform(get("/datasets"))
                .andExpect(status().isOk())
                .andReturn();

        String html = result.getResponse().getContentAsString();
        int resultsStackIndex = html.indexOf("class=\"dp-results-stack\"");
        int resultControlsIndex = html.indexOf("id=\"result-controls\"", resultsStackIndex);
        int resultsShellIndex = html.indexOf("id=\"dataset-results-shell\"", resultsStackIndex);

        assertThat(resultsStackIndex).isGreaterThanOrEqualTo(0);
        assertThat(resultControlsIndex).isGreaterThan(resultsStackIndex);
        assertThat(resultsShellIndex).isGreaterThan(resultControlsIndex);
        assertThat(html).doesNotContain("id=\"pagination\"");
    }

    @Test
    void searchQueryFiltersResults() throws Exception {
        mockMvc.perform(get("/datasets").param("q", "Bauinventar"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Bauinventar")))
                .andExpect(content().string(not(containsString("Abstimmungsresultate"))));
    }

    @Test
    void shortSearchQueryIsRenderedAsEmptyAndDoesNotFilterResults() throws Exception {
        mockMvc.perform(get("/datasets").param("q", "ab"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("value=\"\"")))
                .andExpect(content().string(not(containsString("value=\"ab\""))))
                .andExpect(content().string(containsString("Abstimmungsresultate")))
                .andExpect(content().string(containsString("Gemeindegrenzen Kanton Solothurn")));
    }

    @Test
    void cardViewRendersCardsAndOpenDataBadges() throws Exception {
        mockMvc.perform(get("/datasets").param("view", "cards"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("dp-card-grid")))
                .andExpect(content().string(containsString("dp-result-card")))
                .andExpect(content().string(containsString("Open Data")))
                .andExpect(content().string(containsString("href=\"/series/ch.so.abstimmungsresultate\"")))
                .andExpect(content().string(not(containsString("(aktuelle Ausgabe)"))))
                .andExpect(content().string(not(containsString("dp-entry-table-wrapper"))));
    }

    @Test
    void activeFilterChipsRemoveSingleValuesAndKeepRelevantParameters() throws Exception {
        mockMvc.perform(get("/datasets")
                        .param("theme", "Bau_und_Wohnungswesen")
                        .param("theme", "Kultur_Medien_Informationsgesellschaft_Sport")
                        .param("office", "arp")
                        .param("view", "cards")
                        .param("sort", "title-asc"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Aktive Filter")))
                .andExpect(content().string(containsString("Thema: Bau und Wohnungswesen")))
                .andExpect(content().string(containsString("Fachstelle / Amt: Amt für Raumplanung")))
                .andExpect(content().string(containsString("theme=Kultur_Medien_Informationsgesellschaft_Sport")))
                .andExpect(content().string(containsString("office=arp")))
                .andExpect(content().string(containsString("view=cards")))
                .andExpect(content().string(containsString("sort=title-asc")));
    }

    @Test
    void listViewOmitsTypeBadgesAndThemesWhileCardsKeepTypeBadges() throws Exception {
        var listHtml = mockMvc.perform(get("/datasets"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        var expandedHtml = mockMvc.perform(get("/datasets").param("expanded", "ch.so.abstimmungsresultate"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        var cardsHtml = mockMvc.perform(get("/datasets").param("view", "cards"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(listHtml)
                .contains("<colgroup>")
                .contains("<col class=\"dp-entry-table__col-expand\">")
                .contains("<col class=\"dp-entry-table__col-summary\">")
                .contains("<col class=\"dp-entry-table__col-date\">")
                .contains("<col class=\"dp-entry-table__col-metadata\">")
                .contains("<col class=\"dp-entry-table__col-downloads\">")
                .contains("<th scope=\"col\">Thema / Datensatz</th>")
                .contains("<th scope=\"col\">Publiziert</th>")
                .contains("<th scope=\"col\">Details</th>")
                .contains("<th scope=\"col\">Daten herunterladen</th>")
                .contains("aria-label=\"Details anzeigen: Abstimmungsresultate\"")
                .contains("class=\"bi bi-plus-lg\"")
                .contains("class=\"bi bi-info-circle\"")
                .contains("width=\"24\" height=\"24\"")
                .doesNotContain("<th scope=\"col\">Typ</th>")
                .doesNotContain("dp-type-badge")
                .doesNotContain("dp-entry-themes")
                .doesNotContain("dp-entry-icon")
                .doesNotContain(">i</a>")
                .doesNotContain("<span aria-hidden=\"true\">+</span>")
                .doesNotContain("<span aria-hidden=\"true\">-</span>");
        assertThat(expandedHtml)
                .contains("class=\"bi bi-dash-lg\"")
                .contains("class=\"bi bi-info-circle\"")
                .contains("width=\"24\" height=\"24\"")
                .contains("aria-label=\"Details anzeigen: Abstimmungsresultate 2026\"")
                .doesNotContain("dp-type-badge")
                .doesNotContain(">i</a>")
                .doesNotContain("<span aria-hidden=\"true\">+</span>")
                .doesNotContain("<span aria-hidden=\"true\">-</span>");
        assertThat(cardsHtml).contains("dp-type-badge");
    }

    @Test
    void expandedSeriesRendersIssueRowsWithoutCurrentIssueDownloadSuffix() throws Exception {
        mockMvc.perform(get("/datasets").param("expanded", "ch.so.abstimmungsresultate"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("aria-expanded=\"true\"")))
                .andExpect(content().string(containsString("href=\"/series/ch.so.abstimmungsresultate\"")))
                .andExpect(content().string(containsString("href=\"/series/ch.so.abstimmungsresultate/issues/ch.so.abstimmungsresultate_2025\"")))
                .andExpect(content().string(containsString("Abstimmungsresultate 2026")))
                .andExpect(content().string(containsString("Abstimmungsresultate 2025")))
                .andExpect(content().string(containsString("CSV herunterladen: Abstimmungsresultate")))
                .andExpect(content().string(containsString("CSV herunterladen: Abstimmungsresultate 2025")))
                .andExpect(content().string(not(containsString("(aktuelle Ausgabe)"))));
    }

    @Test
    void htmxRequestReturnsResultsFragmentWithOobAreas() throws Exception {
        mockMvc.perform(get("/datasets")
                        .header("HX-Request", "true")
                        .header("HX-Target", "dataset-results-shell")
                        .param("view", "cards"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"dataset-results-shell\"")))
                .andExpect(content().string(containsString("dp-card-grid")))
                .andExpect(content().string(containsString("hx-swap-oob=\"true\"")))
                .andExpect(content().string(containsString("id=\"filter-toolbar\"")))
                .andExpect(content().string(containsString("id=\"result-controls\"")))
                .andExpect(content().string(not(containsString("id=\"pagination\""))))
                .andExpect(content().string(not(containsString("<html"))))
                .andExpect(content().string(not(containsString("dp-site-header"))))
                .andExpect(content().string(not(containsString("Zum Inhalt springen"))));
    }

    @Test
    void sortFormKeepsQueryStateAndUsesImmediateSubmitWithoutVisibleButton() throws Exception {
        mockMvc.perform(get("/datasets")
                        .param("q", "Bauinventar")
                        .param("theme", "Geografie")
                        .param("view", "cards"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("class=\"dp-sort-form\"")))
                .andExpect(content().string(containsString("hx-get=\"/datasets\"")))
                .andExpect(content().string(containsString("hx-trigger=\"change from:#catalog-sort\"")))
                .andExpect(content().string(containsString("name=\"sort\"")))
                .andExpect(content().string(containsString("onchange=\"this.form.requestSubmit()\"")))
                .andExpect(content().string(containsString("name=\"q\" value=\"Bauinventar\"")))
                .andExpect(content().string(containsString("name=\"theme\" value=\"Geografie\"")))
                .andExpect(content().string(containsString("name=\"view\" value=\"cards\"")))
                .andExpect(content().string(not(containsString(">Sortieren</button>"))));
    }

    @Test
    void sortFormDoesNotRenderRelevanceAndFallsBackToDefaultForLegacySortParameter() throws Exception {
        mockMvc.perform(get("/datasets").param("sort", "relevance"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString(">Relevanz</option>"))))
                .andExpect(content().string(containsString("<option value=\"modified-desc\" selected>Neueste zuerst</option>")))
                .andExpect(content().string(not(containsString("<option value=\"title-asc\" selected>"))));
    }

    @Test
    void filterPopoverRouteRendersOnlyRequestedGroup() throws Exception {
        mockMvc.perform(get("/datasets/filter-popover")
                        .param("filter", "theme")
                        .param("theme", "Bau_und_Wohnungswesen"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("dp-filter-popover")))
                .andExpect(content().string(containsString("id=\"filter-panel-theme\"")))
                .andExpect(content().string(containsString("data-filter-panel")))
                .andExpect(content().string(containsString("data-filter-reset")))
                .andExpect(content().string(containsString("name=\"theme\"")))
                .andExpect(content().string(containsString("value=\"Bau_und_Wohnungswesen\"")))
                .andExpect(content().string(containsString("checked")))
                .andExpect(content().string(not(containsString("dp-filter-overlay"))))
                .andExpect(content().string(not(containsString("<html"))));
    }

    @Test
    void mobileFiltersRouteRendersFilterSheetWithAllGroups() throws Exception {
        mockMvc.perform(get("/datasets/mobile-filters")
                        .param("modified", "last30")
                        .param("resourceType", "series"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("dp-filter-sheet")))
                .andExpect(content().string(containsString("Ergebnisse anzeigen")))
                .andExpect(content().string(containsString("Alle zurücksetzen")))
                .andExpect(content().string(containsString("name=\"modified\"")))
                .andExpect(content().string(not(containsString("name=\"resourceType\""))))
                .andExpect(content().string(not(containsString("<html"))));
    }

    @Test
    void filterFragmentsAreNotAvailableOnRootAlias() throws Exception {
        mockMvc.perform(get("/filter-popover").param("filter", "theme"))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/mobile-filters"))
                .andExpect(status().isNotFound());
    }

    @Test
    void catalogPageIgnoresPageAndSizeParameters() throws Exception {
        MvcResult baseline = mockMvc.perform(get("/datasets")
                        .param("theme", "Geografie")
                        .param("view", "cards")
                        .param("sort", "title-asc"))
                .andExpect(status().isOk())
                .andReturn();

        mockMvc.perform(get("/datasets")
                        .param("theme", "Geografie")
                        .param("view", "cards")
                        .param("sort", "title-asc")
                        .param("page", "2")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(content().string(baseline.getResponse().getContentAsString()))
                .andExpect(content().string(containsString("theme=Geografie")))
                .andExpect(content().string(containsString("view=cards")))
                .andExpect(content().string(containsString("sort=title-asc")))
                .andExpect(content().string(not(containsString("page=2"))))
                .andExpect(content().string(not(containsString("size=20"))))
                .andExpect(content().string(not(containsString("name=\"size\""))));
    }

    @Test
    void webComponentAssetsAreIncludedOnceAndContainNoLocalPaths() throws Exception {
        MvcResult result = mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andReturn();

        String html = result.getResponse().getContentAsString();

        assertThat(countOccurrences(html, "/vendor/so-web-components/0.1.10/index.js")).isEqualTo(1);
        assertThat(countOccurrences(html, "/vendor/so-web-components/0.1.10/styles/reset.css")).isEqualTo(1);
        assertThat(countOccurrences(html, "/vendor/so-web-components/0.1.10/styles/fonts.css")).isEqualTo(1);
        assertThat(countOccurrences(html, "/vendor/so-web-components/0.1.10/styles/tokens.css")).isEqualTo(1);
        assertThat(countOccurrences(html, "/css/app.css")).isEqualTo(1);
        assertThat(countOccurrences(html, "/js/htmx.min.js")).isEqualTo(1);
        assertThat(countOccurrences(html, "/js/catalog-filters.js")).isEqualTo(1);
        assertThat(html)
                .doesNotContain("/Users/")
                .doesNotContain("file:")
                .doesNotContainPattern("[A-Za-z]:\\\\");
    }

    private static int countOccurrences(String value, String needle) {
        int count = 0;
        int index = 0;
        while ((index = value.indexOf(needle, index)) >= 0) {
            count++;
            index += needle.length();
        }
        return count;
    }
}
