package ch.so.agi.datenportal.web;

import static org.hamcrest.Matchers.containsString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class CatalogArtifactControllerMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void publishedCatalogEndpointServesResolvedXtfArtifact() throws Exception {
        mockMvc.perform(get("/catalog/published-catalog.xtf"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, containsString("no-cache")))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, containsString("private")))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("published-catalog.xtf")))
                .andExpect(content().string(containsString("SO_AGI_DataCatalog_PublishedCatalog_20260602")))
                .andExpect(content().string(containsString("http://localhost:8081/ch.so.datenportal/downloads")));
    }

    @Test
    void duckDbCatalogEndpointServesBinaryCatalogArtifact() throws Exception {
        mockMvc.perform(get("/catalog/catalog.duckdb"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, containsString("no-cache")))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, containsString("private")))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("catalog.duckdb")))
                .andExpect(result -> assertThat(result.getResponse().getContentLengthLong()).isGreaterThan(0));
    }
}
