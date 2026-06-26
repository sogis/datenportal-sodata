package ch.so.agi.datenportal.admin.actuator;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class CatalogActuatorMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthEndpointContainsCatalogComponents() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components.catalogSnapshot.status").value("UP"))
                .andExpect(jsonPath("$.components.catalogSnapshot.details.visibleEntries").value(62))
                .andExpect(jsonPath("$.components.catalogSnapshot.details.loadedAt", not(emptyOrNullString())))
                .andExpect(jsonPath("$.components.catalogReload.status").value("UP"))
                .andExpect(jsonPath("$.components.catalogReload.details.running").value(false))
                .andExpect(jsonPath("$.components.catalogReload.details.lastSuccessfulReloadAt", not(emptyOrNullString())))
                .andExpect(jsonPath("$.components.catalogSearchIndex.status").value("UP"))
                .andExpect(jsonPath("$.components.catalogSearchIndex.details.available").value(true))
                .andExpect(jsonPath("$.components.diskSpace").doesNotExist());
    }

    @Test
    void infoEndpointContainsAppBasics() throws Exception {
        mockMvc.perform(get("/actuator/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.app.name").value("datenportal-sodata"))
                .andExpect(jsonPath("$.app.packageBase").value("ch.so.agi.datenportal"))
                .andExpect(jsonPath("$.app.javaVersion", not(emptyOrNullString())));
    }
}
