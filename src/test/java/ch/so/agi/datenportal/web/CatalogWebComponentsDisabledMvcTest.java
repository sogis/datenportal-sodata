package ch.so.agi.datenportal.web;

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

@SpringBootTest(properties = "datenportal.web-components.enabled=false")
@AutoConfigureMockMvc
class CatalogWebComponentsDisabledMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void rendersFallbackHeaderAndBreadcrumbWhenWebComponentsAreDisabled() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<header class=\"dp-site-header\" role=\"banner\">")))
                .andExpect(content().string(containsString("<nav class=\"dp-breadcrumb\" aria-label=\"Breadcrumb\">")))
                .andExpect(content().string(containsString("Datenportal")))
                .andExpect(content().string(containsString("Daten &amp; Statistiken")))
                .andExpect(content().string(not(containsString("<so-header"))))
                .andExpect(content().string(not(containsString("<so-breadcrumb"))))
                .andExpect(content().string(not(containsString("/vendor/so-web-components/0.1.10"))));
    }

    @Test
    void detailPageUsesFallbackChromeWhenWebComponentsAreDisabled() throws Exception {
        mockMvc.perform(get("/datasets/ch.so.bauinventar"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<header class=\"dp-site-header\" role=\"banner\">")))
                .andExpect(content().string(containsString("<nav class=\"dp-breadcrumb\" aria-label=\"Breadcrumb\">")))
                .andExpect(content().string(containsString("Bauinventar")))
                .andExpect(content().string(not(containsString("<so-header"))))
                .andExpect(content().string(not(containsString("<so-breadcrumb"))));
    }
}
