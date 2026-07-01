package ch.so.agi.datenportal.explore;

import static org.hamcrest.Matchers.containsString;
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
                .andExpect(content().string(containsString("<h1 class=\"dp-detail-title\">Bauinventar erkunden</h1>")))
                .andExpect(content().string(containsString("Läuft lokal im Browser mit DuckDB-Wasm direkt auf den Parquet-Dateien.")))
                .andExpect(content().string(containsString("id=\"datenportal-explore-root\"")))
                .andExpect(content().string(containsString("id=\"datenportal-explore-context\" type=\"application/json\"")))
                .andExpect(content().string(containsString("\"datasetId\":\"ch.so.bauinventar\"")))
                .andExpect(content().string(containsString("\"engine\":\"duckdb-wasm\"")))
                .andExpect(content().string(containsString("\"mode\":\"browser-local\"")))
                .andExpect(content().string(containsString("\"charts\":true")))
                .andExpect(content().string(containsString("\"aiAssistant\":false")))
                .andExpect(content().string(containsString("SQL-Labor wird vorbereitet")))
                .andExpect(content().string(containsString("rel=\"stylesheet\" href=\"/explore/assets/explore.css\"")))
                .andExpect(content().string(containsString("type=\"module\" src=\"/explore/assets/explore.js\"")));
    }

    @Test
    void contextJsonReturnsExpectedShape() throws Exception {
        mockMvc.perform(get("/datasets/ch.so.bauinventar/explore/context.json"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, containsString("no-cache")))
                .andExpect(jsonPath("$.version").value(1))
                .andExpect(jsonPath("$.datasetId").value("ch.so.bauinventar"))
                .andExpect(jsonPath("$.title").value("Bauinventar"))
                .andExpect(jsonPath("$.canonicalUrl").value("/datasets/ch.so.bauinventar"))
                .andExpect(jsonPath("$.execution.engine").value("duckdb-wasm"))
                .andExpect(jsonPath("$.execution.mode").value("browser-local"))
                .andExpect(jsonPath("$.execution.maxPreviewRows").value(100))
                .andExpect(jsonPath("$.tables[0].id").value("ch_so_bauinventar"))
                .andExpect(jsonPath("$.tables[0].name").value("ch_so_bauinventar"))
                .andExpect(jsonPath("$.tables[0].primary").value(true))
                .andExpect(jsonPath("$.tables[0].columns[0].roles[0]").isString())
                .andExpect(jsonPath("$.recipes[0].category").value("preview"))
                .andExpect(jsonPath("$.codeSnippets[0].language").value("sql"))
                .andExpect(jsonPath("$.featureFlags.charts").value(true))
                .andExpect(jsonPath("$.featureFlags.localHistory").value(true))
                .andExpect(jsonPath("$.featureFlags.aiAssistant").value(false))
                .andExpect(jsonPath("$.featureFlags.webR").value(false));
    }

    @Test
    void missingDatasetReturnsNotFound() throws Exception {
        mockMvc.perform(get("/datasets/does-not-exist/explore"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Seite nicht gefunden")));
        mockMvc.perform(get("/datasets/does-not-exist/explore/context.json"))
                .andExpect(status().isNotFound());
    }
}
