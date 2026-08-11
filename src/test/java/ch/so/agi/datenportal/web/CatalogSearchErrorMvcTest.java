package ch.so.agi.datenportal.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import ch.so.agi.datenportal.search.CatalogSearchException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.View;

class CatalogSearchErrorMvcTest {

    private MockMvc mockMvc;
    private ErrorPageVmFactory errorPageVmFactory;

    @BeforeEach
    void setUp() {
        errorPageVmFactory = mock(ErrorPageVmFactory.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new FailingSearchController())
                .setControllerAdvice(new CatalogErrorControllerAdvice(errorPageVmFactory))
                .setViewResolvers((viewName, locale) -> new View() {
                    @Override
                    public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) {}
                })
                .build();
    }

    @Test
    void returns503ForNormalSearchRequest() throws Exception {
        mockMvc.perform(get("/search"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(header().doesNotExist("HX-Refresh"));
        verify(errorPageVmFactory).serviceUnavailable(
                "Der Suchdienst ist momentan nicht verfügbar. Bitte versuchen Sie es später erneut.");
    }

    @Test
    void refreshesThePageForHtmxSearchRequest() throws Exception {
        mockMvc.perform(get("/search").header("HX-Request", "true"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(header().string("HX-Refresh", "true"));
    }

    @RestController
    static class FailingSearchController {

        @GetMapping("/search")
        String search() {
            throw new CatalogSearchException("internal Lucene detail", new IOException("I/O detail"));
        }
    }
}
