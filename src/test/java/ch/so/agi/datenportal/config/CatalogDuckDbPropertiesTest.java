package ch.so.agi.datenportal.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.URI;
import java.nio.file.Path;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.util.unit.DataSize;

class CatalogDuckDbPropertiesTest {

    @Test
    void sourceTypeIsRequired() {
        assertThatThrownBy(() -> new CatalogDuckDbProperties(
                        null,
                        "catalog.duckdb",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("source-type");
    }

    @Test
    void explicitClasspathSourceUsesLocationAndTechnicalDefaults() {
        var properties = new CatalogDuckDbProperties(
                CatalogProperties.SourceType.CLASSPATH,
                "catalog.duckdb",
                null,
                null,
                null,
                null,
                null,
                null);

        assertThat(properties.classpathLocation()).isEqualTo("catalog.duckdb");
        assertThat(properties.maxSize()).isEqualTo(DataSize.ofMegabytes(50));
        assertThat(properties.httpConnectTimeout()).isEqualTo(Duration.ofSeconds(5));
        assertThat(properties.httpReadTimeout()).isEqualTo(Duration.ofSeconds(30));
        assertThat(properties.schema()).isEqualTo("opendata");
    }

    @Test
    void classpathSourceRequiresLocation() {
        assertThatThrownBy(() -> new CatalogDuckDbProperties(
                        CatalogProperties.SourceType.CLASSPATH,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("classpath-location");
    }

    @Test
    void explicitHttpSourceUsesHttpUrlAndRejectsInvalidValues() {
        var properties = new CatalogDuckDbProperties(
                CatalogProperties.SourceType.HTTP,
                null,
                null,
                URI.create("https://example.com/catalog.duckdb"),
                Duration.ofSeconds(2),
                Duration.ofSeconds(3),
                DataSize.ofMegabytes(20),
                "opendata");

        assertThat(properties.httpUrl()).isEqualTo(URI.create("https://example.com/catalog.duckdb"));
        assertThat(properties.httpConnectTimeout()).isEqualTo(Duration.ofSeconds(2));
        assertThat(properties.httpReadTimeout()).isEqualTo(Duration.ofSeconds(3));
        assertThat(properties.maxSize()).isEqualTo(DataSize.ofMegabytes(20));

        assertThatThrownBy(() -> new CatalogDuckDbProperties(
                        CatalogProperties.SourceType.HTTP,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("http-url");

        assertThatThrownBy(() -> new CatalogDuckDbProperties(
                        CatalogProperties.SourceType.HTTP,
                        null,
                        null,
                        URI.create("ftp://example.com/catalog.duckdb"),
                        null,
                        null,
                        null,
                        null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("http or https");
    }

    @Test
    void explicitFileSourceUsesFileLocationAndRequiresIt() {
        var properties = new CatalogDuckDbProperties(
                CatalogProperties.SourceType.FILE,
                null,
                Path.of("build/catalog.duckdb"),
                null,
                null,
                null,
                null,
                null);

        assertThat(properties.fileLocation()).isEqualTo(Path.of("build/catalog.duckdb"));

        assertThatThrownBy(() -> new CatalogDuckDbProperties(
                        CatalogProperties.SourceType.FILE,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("file-location");
    }
}
