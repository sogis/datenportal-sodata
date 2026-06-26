package ch.so.agi.datenportal.admin.reload;

import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "datenportal.admin.reload-token=test-token")
@AutoConfigureMockMvc
class AdminCatalogControllerMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void reloadWithoutTokenIsRejected() throws Exception {
        mockMvc.perform(post("/admin/catalog/reload"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Unauthorized."));
    }

    @Test
    void reloadWithWrongTokenIsRejected() throws Exception {
        mockMvc.perform(post("/admin/catalog/reload")
                        .header(ReloadTokenVerifier.HEADER_NAME, "wrong"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Unauthorized."));
    }

    @Test
    void reloadWithValidTokenWorks() throws Exception {
        mockMvc.perform(post("/admin/catalog/reload")
                        .header(ReloadTokenVerifier.HEADER_NAME, "test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.visibleEntries").value(62))
                .andExpect(jsonPath("$.contentHash", not(emptyOrNullString())));
    }

    @Test
    void statusWithValidTokenShowsActiveCatalog() throws Exception {
        mockMvc.perform(get("/admin/catalog/status")
                        .header(ReloadTokenVerifier.HEADER_NAME, "test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.visibleEntries").value(62))
                .andExpect(jsonPath("$.sourceDescription").value("classpath:published_catalog_full_62_entries.xtf"))
                .andExpect(jsonPath("$.contentHash", not(emptyOrNullString())));
    }

    @Test
    void getReloadDoesNotMutate() throws Exception {
        mockMvc.perform(get("/admin/catalog/reload")
                        .header(ReloadTokenVerifier.HEADER_NAME, "test-token"))
                .andExpect(status().isMethodNotAllowed());
    }
}

@SpringBootTest(properties = "datenportal.admin.reload-token=")
@AutoConfigureMockMvc
class AdminCatalogControllerDisabledMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void reloadEndpointIsDisabledWhenTokenIsBlank() throws Exception {
        mockMvc.perform(post("/admin/catalog/reload")
                        .header(ReloadTokenVerifier.HEADER_NAME, "test-token"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Reload endpoint is disabled."));
    }

    @Test
    void statusEndpointIsDisabledWhenTokenIsBlank() throws Exception {
        mockMvc.perform(get("/admin/catalog/status")
                        .header(ReloadTokenVerifier.HEADER_NAME, "test-token"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.enabled").value(false));
    }
}
