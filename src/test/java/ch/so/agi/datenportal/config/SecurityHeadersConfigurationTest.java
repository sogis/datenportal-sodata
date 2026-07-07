package ch.so.agi.datenportal.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class SecurityHeadersConfigurationTest {

    @Test
    void cspConnectSrcUsesDefaultsAndCatalogDownloadOrigin() {
        var configuration = new SecurityHeadersConfiguration(
                new SecurityCspProperties(null, true),
                catalogProperties("http://localhost:8081/ch.so.datenportal/downloads"));

        assertThat(configuration.csp())
                .contains("connect-src 'self' https://data.so.ch http://localhost:8081;");
    }

    @Test
    void explicitConnectSrcCanDisableCatalogDownloadOrigin() {
        var configuration = new SecurityHeadersConfiguration(
                new SecurityCspProperties(List.of("'self'", "https://example.org"), false),
                catalogProperties("http://localhost:8081/ch.so.datenportal/downloads"));

        assertThat(configuration.csp())
                .contains("connect-src 'self' https://example.org;")
                .doesNotContain("https://data.so.ch")
                .doesNotContain("http://localhost:8081");
    }

    @Test
    void rootRelativeCatalogDownloadUrlDoesNotAddExternalOrigin() {
        var configuration = new SecurityHeadersConfiguration(
                new SecurityCspProperties(List.of("'self'"), true),
                catalogProperties("/downloads"));

        assertThat(configuration.csp()).contains("connect-src 'self';");
    }

    @Test
    void normalPagesKeepUnsafeEvalDisabled() {
        var configuration = new SecurityHeadersConfiguration(
                new SecurityCspProperties(null, true),
                catalogProperties("/downloads"));

        assertThat(configuration.csp("/"))
                .contains("script-src 'self' 'wasm-unsafe-eval';")
                .doesNotContain("'unsafe-eval'");
    }

    @Test
    void exploreAndWebRRuntimePathsAllowUnsafeEvalForBrowserRuntimes() {
        var configuration = new SecurityHeadersConfiguration(
                new SecurityCspProperties(null, true),
                catalogProperties("/downloads"));

        assertThat(configuration.csp("/datasets/ch.so.bauinventar/explore"))
                .contains("script-src 'self' 'wasm-unsafe-eval' 'unsafe-eval';");
        assertThat(configuration.csp("/series/ch.so.foo/issues/current/explore/context.json"))
                .contains("script-src 'self' 'wasm-unsafe-eval' 'unsafe-eval';");
        assertThat(configuration.csp("/explore/assets/explore.js"))
                .contains("script-src 'self' 'wasm-unsafe-eval' 'unsafe-eval';");
        assertThat(configuration.csp("/webr/0.6.0/webr-worker.js"))
                .contains("script-src 'self' 'wasm-unsafe-eval' 'unsafe-eval';");
        assertThat(configuration.csp("/webr-packages/bin/emscripten/contrib/4.6/PACKAGES"))
                .contains("script-src 'self' 'wasm-unsafe-eval' 'unsafe-eval';");
    }

    private static CatalogProperties catalogProperties(String downloadUrl) {
        return new CatalogProperties(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                downloadUrl);
    }
}
