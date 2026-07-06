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
    void exploreIslandAssetsUseShortCacheHeader() throws Exception {
        mockMvc.perform(get("/explore/assets/explore.js"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", allOf(containsString("max-age=3600"), containsString("public"))))
                .andExpect(content().string(containsString("datenportal-explore-root")));

        mockMvc.perform(get("/explore/assets/explore.css"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", allOf(containsString("max-age=3600"), containsString("public"))))
                .andExpect(content().string(containsString(".dp-explore-workbench")));
    }

    @Test
    void exploreDuckDbExtensionsUseLongCacheHeader() throws Exception {
        mockMvc.perform(get("/explore-extensions/v1.5.4/wasm_mvp/parquet.duckdb_extension.wasm"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", allOf(containsString("max-age=31536000"), containsString("public"))));
    }

    @Test
    void webRRuntimeAndPackageMirrorUseLongCacheHeader() throws Exception {
        mockMvc.perform(get("/webr/0.6.0/webr.mjs"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", allOf(containsString("max-age=31536000"), containsString("public"))))
                .andExpect(content().string(containsString("WebR")));

        mockMvc.perform(get("/webr-packages/bin/emscripten/contrib/4.6/PACKAGES"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", allOf(containsString("max-age=31536000"), containsString("public"))))
                .andExpect(content().string(containsString("Package: ggplot2")));
    }

    @Test
    void exploreDuckDbWasmUsesBrotliWhenAccepted() throws Exception {
        mockMvc.perform(get("/explore/assets/duckdb-mvp.wasm").header("Accept-Encoding", "br, gzip"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Encoding", "br"))
                .andExpect(header().string("Vary", "Accept-Encoding"));
    }

    @Test
    void exploreDuckDbWasmUsesGzipWhenBrotliIsNotAccepted() throws Exception {
        mockMvc.perform(get("/explore/assets/duckdb-mvp.wasm").header("Accept-Encoding", "gzip"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Encoding", "gzip"))
                .andExpect(header().string("Vary", "Accept-Encoding"));
    }

    @Test
    void exploreDuckDbWasmFallsBackToOriginalWhenNoEncodingIsAccepted() throws Exception {
        mockMvc.perform(get("/explore/assets/duckdb-mvp.wasm"))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist("Content-Encoding"));
    }

    @Test
    void exploreDuckDbExtensionUsesBrotliWhenAccepted() throws Exception {
        mockMvc.perform(get("/explore-extensions/v1.5.4/wasm_mvp/parquet.duckdb_extension.wasm")
                        .header("Accept-Encoding", "br, gzip"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Encoding", "br"))
                .andExpect(header().string("Vary", "Accept-Encoding"));
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
    void imageAssetUsesMediumCacheHeader() throws Exception {
        mockMvc.perform(get("/images/usage-recipes/excel.png"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", allOf(containsString("max-age=2592000"), containsString("public"))));
    }

    @Test
    void webComponentAssetUsesLongCacheHeaderAndStillServesContent() throws Exception {
        mockMvc.perform(get("/vendor/so-web-components/0.1.10/index.js"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", allOf(containsString("max-age=31536000"), containsString("public"))))
                .andExpect(content().string(containsString("customElements.define")))
                .andExpect(content().string(containsString("so-header")));
    }

    @Test
    void vendoredFontsCssServesRelativeWoff2ReferencesWithoutEmbeddedBase64() throws Exception {
        mockMvc.perform(get("/vendor/so-web-components/0.1.10/styles/fonts.css"))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        containsString("./FrutigerLTW05-55Roman.woff2"),
                        containsString("./FrutigerLTW05-75Black.woff2"),
                        not(containsString("/assets/fonts/")),
                        not(containsString("data:")),
                        not(containsString("base64")))));
    }

    @Test
    void vendoredFrutigerWoff2AssetsAreServedWithLongCacheHeaders() throws Exception {
        mockMvc.perform(get("/vendor/so-web-components/0.1.10/styles/FrutigerLTW05-55Roman.woff2"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", allOf(containsString("max-age=31536000"), containsString("public"))));

        mockMvc.perform(get("/vendor/so-web-components/0.1.10/styles/FrutigerLTW05-75Black.woff2"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", allOf(containsString("max-age=31536000"), containsString("public"))));
    }

    @Test
    void vendoredJetBrainsMonoFontsCssServesRelativeWoff2ReferenceWithoutEmbeddedBase64() throws Exception {
        mockMvc.perform(get("/vendor/jetbrains-mono/2.304/fonts.css"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", allOf(containsString("max-age=31536000"), containsString("public"))))
                .andExpect(content().string(allOf(
                        containsString("./JetBrainsMono-Regular.woff2"),
                        not(containsString("http")),
                        not(containsString("data:")),
                        not(containsString("base64")))));
    }

    @Test
    void vendoredJetBrainsMonoWoff2AssetIsServedWithLongCacheHeader() throws Exception {
        mockMvc.perform(get("/vendor/jetbrains-mono/2.304/JetBrainsMono-Regular.woff2"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", allOf(containsString("max-age=31536000"), containsString("public"))));
    }

    @Test
    void normalPagesIncludeSecurityHeaders() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("Referrer-Policy", "strict-origin-when-cross-origin"))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("Permissions-Policy", containsString("geolocation=()")))
                .andExpect(header().string("Content-Security-Policy", containsString("default-src 'self'")))
                .andExpect(header().string("Content-Security-Policy", containsString("script-src 'self' 'wasm-unsafe-eval'")))
                .andExpect(header().string(
                        "Content-Security-Policy",
                        containsString("connect-src 'self' https://data.so.ch http://localhost:8081")))
                .andExpect(header().string("Content-Security-Policy", containsString("worker-src 'self' blob:")));
    }
}
