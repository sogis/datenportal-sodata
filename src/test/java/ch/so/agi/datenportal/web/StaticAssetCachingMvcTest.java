package ch.so.agi.datenportal.web;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class StaticAssetCachingMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void cssAssetUsesShortCacheHeader() throws Exception {
        mockMvc.perform(get("/css/app.css"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", allOf(containsString("max-age=3600"), containsString("public"))));
    }

    @Test
    void htmxAssetUsesMediumCacheHeader() throws Exception {
        mockMvc.perform(get("/js/htmx.min.js"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", allOf(containsString("max-age=2592000"), containsString("public"))));
    }

    @Test
    void appJsAssetUsesMediumCacheHeader() throws Exception {
        mockMvc.perform(get("/js/catalog-filters.js"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", allOf(containsString("max-age=2592000"), containsString("public"))));
    }

    @Test
    void webComponentAssetUsesLongCacheHeaderAndStillServesContent() throws Exception {
        mockMvc.perform(get("/vendor/so-web-components/0.1.9/index.js"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", allOf(containsString("max-age=31536000"), containsString("public"))))
                .andExpect(content().string(containsString("customElements.define")))
                .andExpect(content().string(containsString("so-header")));
    }

    @Test
    void vendoredFontsCssServesLocalFontReferencesWithoutEmbeddedBase64() throws Exception {
        mockMvc.perform(get("/vendor/so-web-components/0.1.9/styles/fonts.css"))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        containsString("/assets/fonts/FrutigerLTW05-55Roman.woff2"),
                        containsString("/assets/fonts/FrutigerLTW05-75Black.woff2"),
                        not(containsString("data:")),
                        not(containsString("base64")))));
    }

    @Test
    void vendoredFrutigerWoff2AssetsAreServed() throws Exception {
        mockMvc.perform(get("/assets/fonts/FrutigerLTW05-55Roman.woff2"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/assets/fonts/FrutigerLTW05-75Black.woff2"))
                .andExpect(status().isOk());
    }

    @Test
    void normalPagesIncludeSecurityHeaders() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("Referrer-Policy", "strict-origin-when-cross-origin"))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("Permissions-Policy", containsString("geolocation=()")))
                .andExpect(header().string("Content-Security-Policy", containsString("default-src 'self'")));
    }
}
