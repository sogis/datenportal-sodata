package ch.so.agi.datenportal.web;

import static org.assertj.core.api.Assertions.assertThat;
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
    void homePageReturns200() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Daten &amp; Statistiken")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Zum Inhalt springen")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("id=\"main-content\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("aria-label=\"Breadcrumb\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Hauptnavigation")));
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
    void pageContainsLocalAssetsAndResultsPlaceholder() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("href=\"/css/app.css\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("src=\"/js/htmx.min.js\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("id=\"dataset-results\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Phase 0: Das Projektgerüst läuft.")));
    }
}
