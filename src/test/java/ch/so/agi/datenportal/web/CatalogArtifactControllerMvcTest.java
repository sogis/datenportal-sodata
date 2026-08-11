package ch.so.agi.datenportal.web;

import static org.hamcrest.Matchers.containsString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.so.agi.datenportal.catalog.service.CatalogService;
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

    @Autowired
    private CatalogService catalogService;

    @Test
    void publishedCatalogEndpointServesResolvedXtfArtifact() throws Exception {
        mockMvc.perform(get("/catalog/published-catalog.xtf"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, containsString("no-cache")))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, containsString("private")))
                .andExpect(header().string(HttpHeaders.ETAG, containsString("\"")))
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
                .andExpect(header().string(HttpHeaders.ETAG, containsString("\"")))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("catalog.duckdb")))
                .andExpect(result -> assertThat(result.getResponse().getContentLengthLong()).isGreaterThan(0));
    }

    @Test
    void versionedDuckDbArtifactIsPublicImmutableAndUsesSnapshotLength() throws Exception {
        String hash = catalogService.withSnapshot(snapshot -> snapshot.duckDbCatalog().contentHash());
        long size = catalogService.withSnapshot(snapshot -> (long) snapshot.duckDbCatalog().sizeInBytes());

        mockMvc.perform(get("/catalog/catalog.duckdb").param("v", hash))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, containsString("public")))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, containsString("immutable")))
                .andExpect(header().string(HttpHeaders.ETAG, "\"" + hash + "\""))
                .andExpect(header().longValue(HttpHeaders.CONTENT_LENGTH, size));
    }

    @Test
    void staleDuckDbVersionIsRejected() throws Exception {
        mockMvc.perform(get("/catalog/catalog.duckdb").param("v", "stale-version"))
                .andExpect(status().isConflict());
    }

    @Test
    void matchingIfNoneMatchReturnsNotModifiedForBothArtifacts() throws Exception {
        String xtfHash = catalogService.withSnapshot(snapshot -> snapshot.publishedCatalog().contentHash());
        String duckDbHash = catalogService.withSnapshot(snapshot -> snapshot.duckDbCatalog().contentHash());

        mockMvc.perform(get("/catalog/published-catalog.xtf")
                        .header(HttpHeaders.IF_NONE_MATCH, "\"" + xtfHash + "\""))
                .andExpect(status().isNotModified())
                .andExpect(header().string(HttpHeaders.ETAG, "\"" + xtfHash + "\""));
        mockMvc.perform(get("/catalog/catalog.duckdb")
                        .param("v", duckDbHash)
                        .header(HttpHeaders.IF_NONE_MATCH, "\"" + duckDbHash + "\""))
                .andExpect(status().isNotModified())
                .andExpect(header().string(HttpHeaders.ETAG, "\"" + duckDbHash + "\""));
    }
}
