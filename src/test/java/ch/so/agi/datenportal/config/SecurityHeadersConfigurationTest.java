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
