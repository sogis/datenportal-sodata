package ch.so.agi.datenportal.config;

import static org.assertj.core.api.Assertions.*;
import java.net.URI;
import java.time.Clock;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;

class CatalogInputsConfigurationTest {
    @Test void manifestDuckDbRequiresTheSharedManifestSource() {
        var xml = new CatalogProperties(CatalogProperties.SourceType.CLASSPATH,
                "catalog.xtf", null, null, null, null, null, null);
        var db = new CatalogDuckDbProperties(CatalogProperties.SourceType.MANIFEST,
                null, null, null, null, null, null, null);
        assertThatThrownBy(() -> new CatalogImportConfiguration().catalogInputsSource(
                xml, db, new DefaultResourceLoader(), Clock.systemUTC()))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("source-type=manifest");
    }

    @Test void sharedManifestConfigurationDoesNotEagerlyDownload() {
        var xml = new CatalogProperties(CatalogProperties.SourceType.MANIFEST,
                null, null, URI.create("https://example.invalid/current.json"), null, null, null, null);
        var db = new CatalogDuckDbProperties(CatalogProperties.SourceType.MANIFEST,
                null, null, null, null, null, null, null);
        assertThat(new CatalogImportConfiguration().catalogInputsSource(
                xml, db, new DefaultResourceLoader(), Clock.systemUTC())).isNotNull();
    }
}
