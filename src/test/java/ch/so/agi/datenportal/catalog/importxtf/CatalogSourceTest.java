package ch.so.agi.datenportal.catalog.importxtf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;

class CatalogSourceTest {

    @Test
    void classpathSourceLoadsFixtureFromResources() {
        CatalogSource source = new ClasspathCatalogSource(new DefaultResourceLoader(), "published_catalog_full_54_entries.xtf");

        CatalogBytes bytes = source.load();

        assertThat(bytes.sizeInBytes()).isPositive();
        assertThat(bytes.sourceDescription()).isEqualTo("classpath:published_catalog_full_54_entries.xtf");
        assertThat(new String(bytes.bytes())).contains("SO_AGI_DataCatalog_PublishedCatalog_20260602");
    }

    @Test
    void missingClasspathResourceThrowsControlledError() {
        CatalogSource source = new ClasspathCatalogSource(new DefaultResourceLoader(), "missing-catalog.xtf");

        assertThatThrownBy(source::load)
                .isInstanceOf(CatalogSourceException.class)
                .hasMessageContaining("Classpath catalog resource");
    }

    @Test
    void missingFileThrowsControlledError() {
        CatalogSource source = new FileCatalogSource(Path.of("build/does-not-exist/catalog.xtf"));

        assertThatThrownBy(source::load)
                .isInstanceOf(CatalogSourceException.class)
                .hasMessageContaining("does not exist");
    }
}
