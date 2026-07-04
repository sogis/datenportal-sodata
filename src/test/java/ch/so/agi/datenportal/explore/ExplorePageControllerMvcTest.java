package ch.so.agi.datenportal.explore;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ExplorePageControllerMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void explorePageRendersPortalHostAndEmbeddedContext() throws Exception {
        mockMvc.perform(get("/datasets/ch.so.bauinventar/explore"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<so-header")))
                .andExpect(content().string(containsString("<so-breadcrumb>")))
                .andExpect(content().string(containsString("<so-breadcrumb-item href=\"/datasets/ch.so.bauinventar\">Bauinventar</so-breadcrumb-item>")))
                .andExpect(content().string(containsString("<so-breadcrumb-item iscurrentpage>Erkunden</so-breadcrumb-item>")))
                .andExpect(content().string(containsString("class=\"dp-page dp-page--explore\"")))
                .andExpect(content().string(containsString("class=\"dp-main dp-main--explore\"")))
                .andExpect(content().string(containsString("class=\"dp-explore-host\"")))
                .andExpect(content().string(containsString("id=\"datenportal-explore-root\"")))
                .andExpect(content().string(containsString("id=\"datenportal-explore-context\" type=\"application/json\"")))
                .andExpect(content().string(containsString("\"datasetId\":\"ch.so.bauinventar\"")))
                .andExpect(content().string(containsString("\"engine\":\"duckdb-wasm\"")))
                .andExpect(content().string(containsString("\"mode\":\"browser-local\"")))
                .andExpect(content().string(containsString("\"charts\":true")))
                .andExpect(content().string(containsString("\"aiAssistant\":false")))
                .andExpect(content().string(containsString("\"webR\":false")))
                .andExpect(content().string(containsString("\"vega\":false")))
                .andExpect(content().string(containsString("\"mosaic\":false")))
                .andExpect(content().string(containsString("\"geospatial\":false")))
                .andExpect(content().string(containsString("SQL-Labor lädt")))
                .andExpect(content().string(containsString("Das SQL-Labor läuft lokal im Browser mit DuckDB-Wasm.")))
                .andExpect(content().string(containsString("rel=\"stylesheet\" href=\"/explore/assets/explore.css\"")))
                .andExpect(content().string(containsString("type=\"module\" src=\"/explore/assets/explore.js\"")))
                .andExpect(content().string(not(containsString("<footer"))))
                .andExpect(content().string(not(containsString("dp-detail-page"))));
    }

    @Test
    void contextJsonReturnsExpectedShape() throws Exception {
        mockMvc.perform(get("/datasets/ch.so.bauinventar/explore/context.json"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, containsString("no-cache")))
                .andExpect(jsonPath("$.version").value(2))
                .andExpect(jsonPath("$.datasetId").value("ch.so.bauinventar"))
                .andExpect(jsonPath("$.title").value("Bauinventar"))
                .andExpect(jsonPath("$.canonicalUrl").value("/datasets/ch.so.bauinventar"))
                .andExpect(jsonPath("$.execution.engine").value("duckdb-wasm"))
                .andExpect(jsonPath("$.execution.mode").value("browser-local"))
                .andExpect(jsonPath("$.execution.maxPreviewRows").value(100))
                .andExpect(jsonPath("$.catalogDatabase.url").value("/catalog/catalog.duckdb"))
                .andExpect(jsonPath("$.catalogDatabase.database").value("catalog"))
                .andExpect(jsonPath("$.catalogDatabase.schema").value("opendata"))
                .andExpect(jsonPath("$.tables[0].id").value("ch_so_bauinventar"))
                .andExpect(jsonPath("$.tables[0].name").value("ch_so_bauinventar"))
                .andExpect(jsonPath("$.tables[0].primary").value(true))
                .andExpect(jsonPath("$.tables[0].columns[0].roles[0]").isString())
                .andExpect(jsonPath("$.recipes[0].category").value("preview"))
                .andExpect(jsonPath("$.codeSnippets[0].language").value("sql"))
                .andExpect(jsonPath("$.featureFlags.charts").value(true))
                .andExpect(jsonPath("$.featureFlags.localHistory").value(false))
                .andExpect(jsonPath("$.featureFlags.aiAssistant").value(false))
                .andExpect(jsonPath("$.featureFlags.webR").value(false))
                .andExpect(jsonPath("$.featureFlags.vega").value(false))
                .andExpect(jsonPath("$.featureFlags.mosaic").value(false))
                .andExpect(jsonPath("$.featureFlags.geospatial").value(false));
    }

    @Test
    void currentIssueExplorePageRendersPortalHostAndEmbeddedContext() throws Exception {
        mockMvc.perform(get("/series/ch.so.abstimmungsresultate/issues/current/explore"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<so-header")))
                .andExpect(content().string(containsString("<so-breadcrumb>")))
                .andExpect(content().string(containsString("<so-breadcrumb-item href=\"/series/ch.so.abstimmungsresultate\">Abstimmungsresultate</so-breadcrumb-item>")))
                .andExpect(content().string(containsString("<so-breadcrumb-item href=\"/series/ch.so.abstimmungsresultate/issues/current\">Abstimmungsresultate 2026</so-breadcrumb-item>")))
                .andExpect(content().string(containsString("<so-breadcrumb-item iscurrentpage>Erkunden</so-breadcrumb-item>")))
                .andExpect(content().string(containsString("class=\"dp-page dp-page--explore\"")))
                .andExpect(content().string(containsString("id=\"datenportal-explore-root\"")))
                .andExpect(content().string(containsString("id=\"datenportal-explore-context\" type=\"application/json\"")))
                .andExpect(content().string(containsString("\"datasetId\":\"ch.so.abstimmungsresultate_2026\"")))
                .andExpect(content().string(containsString("\"canonicalUrl\":\"/series/ch.so.abstimmungsresultate/issues/current\"")))
                .andExpect(content().string(containsString("\"parquetUrl\":\"http://localhost:8081/ch.so.datenportal/downloads/ch.so.abstimmungsresultate_2026.parquet\"")))
                .andExpect(content().string(containsString("SQL-Labor lädt")))
                .andExpect(content().string(containsString("type=\"module\" src=\"/explore/assets/explore.js\"")));
    }

    @Test
    void issueContextJsonReturnsConcreteIssueShape() throws Exception {
        mockMvc.perform(get("/series/ch.so.abstimmungsresultate/issues/current/explore/context.json"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, containsString("no-cache")))
                .andExpect(jsonPath("$.version").value(2))
                .andExpect(jsonPath("$.datasetId").value("ch.so.abstimmungsresultate_2026"))
                .andExpect(jsonPath("$.title").value("Abstimmungsresultate 2026"))
                .andExpect(jsonPath("$.canonicalUrl").value("/series/ch.so.abstimmungsresultate/issues/current"))
                .andExpect(jsonPath("$.catalogDatabase.url").value("/catalog/catalog.duckdb"))
                .andExpect(jsonPath("$.catalogDatabase.database").value("catalog"))
                .andExpect(jsonPath("$.catalogDatabase.schema").value("opendata"))
                .andExpect(jsonPath("$.tables[0].id").value("ch_so_abstimmungsresultate_2026"))
                .andExpect(jsonPath("$.tables[0].name").value("ch_so_abstimmungsresultate_2026"))
                .andExpect(jsonPath("$.tables[0].parquetUrl").value("http://localhost:8081/ch.so.datenportal/downloads/ch.so.abstimmungsresultate_2026.parquet"))
                .andExpect(jsonPath("$.recipes[0].category").value("preview"))
                .andExpect(jsonPath("$.codeSnippets[0].language").value("sql"));
    }

    @Test
    void historicalIssueContextJsonUsesSelectedIssue() throws Exception {
        mockMvc.perform(get("/series/ch.so.abstimmungsresultate/issues/ch.so.abstimmungsresultate_2025/explore/context.json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datasetId").value("ch.so.abstimmungsresultate_2025"))
                .andExpect(jsonPath("$.title").value("Abstimmungsresultate 2025"))
                .andExpect(jsonPath("$.canonicalUrl").value("/series/ch.so.abstimmungsresultate/issues/ch.so.abstimmungsresultate_2025"))
                .andExpect(jsonPath("$.tables[0].id").value("ch_so_abstimmungsresultate_2025"))
                .andExpect(jsonPath("$.tables[0].parquetUrl").value("http://localhost:8081/ch.so.datenportal/downloads/ch.so.abstimmungsresultate_2025.parquet"));
    }

    @Test
    void missingDatasetReturnsNotFound() throws Exception {
        mockMvc.perform(get("/datasets/does-not-exist/explore"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Seite nicht gefunden")));
        mockMvc.perform(get("/datasets/does-not-exist/explore/context.json"))
                .andExpect(status().isNotFound());
    }

    @Test
    void issueIdentifierOnDatasetExploreRouteReturnsNotFound() throws Exception {
        mockMvc.perform(get("/datasets/ch.so.abstimmungsresultate_2026/explore"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Seite nicht gefunden")));
        mockMvc.perform(get("/datasets/ch.so.abstimmungsresultate_2026/explore/context.json"))
                .andExpect(status().isNotFound());
    }

    @Test
    void issueIdentifierUnderWrongSeriesReturnsNotFound() throws Exception {
        mockMvc.perform(get("/series/ch.so.gemeindegrenzen/issues/ch.so.abstimmungsresultate_2026/explore"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Seite nicht gefunden")));
        mockMvc.perform(get("/series/ch.so.gemeindegrenzen/issues/ch.so.abstimmungsresultate_2026/explore/context.json"))
                .andExpect(status().isNotFound());
    }
}
