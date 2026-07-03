package ch.so.agi.datenportal.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.URI;
import java.nio.file.Path;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.util.unit.DataSize;

class CatalogPropertiesTest {

    @Test
    void legacyClasspathSourceRemainsSupported() {
        var properties = new CatalogProperties(
                "classpath:published_catalog_full_54_entries.xtf",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);

        assertThat(properties.effectiveSourceType()).isEqualTo(CatalogProperties.SourceType.CLASSPATH);
        assertThat(properties.classpathLocation()).isEqualTo("published_catalog_full_54_entries.xtf");
        assertThat(properties.maxSize()).isEqualTo(DataSize.ofMegabytes(50));
        assertThat(properties.downloadUrl()).isEqualTo("http://localhost:8081/ch.so.datenportal/downloads");
    }

    @Test
    void explicitSourceTypeWinsOverLegacySource() {
        var properties = new CatalogProperties(
                "classpath:legacy.xtf",
                CatalogProperties.SourceType.HTTP,
                "ignored.xtf",
                null,
                URI.create("https://example.com/catalog.xtf"),
                Duration.ofSeconds(2),
                Duration.ofSeconds(3),
                DataSize.ofMegabytes(10),
                "https://download.example.org/files/");

        assertThat(properties.effectiveSourceType()).isEqualTo(CatalogProperties.SourceType.HTTP);
        assertThat(properties.httpUrl()).isEqualTo(URI.create("https://example.com/catalog.xtf"));
        assertThat(properties.httpConnectTimeout()).isEqualTo(Duration.ofSeconds(2));
        assertThat(properties.httpReadTimeout()).isEqualTo(Duration.ofSeconds(3));
        assertThat(properties.downloadUrl()).isEqualTo("https://download.example.org/files");
    }

    @Test
    void fileSourceRequiresLocationWhenExplicitlySelected() {
        var properties = new CatalogProperties(
                null,
                CatalogProperties.SourceType.FILE,
                null,
                null,
                null,
                null,
                null,
                null,
                null);

        assertThatThrownBy(properties::fileLocation)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("file-location");
    }

    @Test
    void explicitFileSourceUsesFileLocation() {
        var properties = new CatalogProperties(
                null,
                CatalogProperties.SourceType.FILE,
                null,
                Path.of("catalog.xtf"),
                null,
                null,
                null,
                null,
                null);

        assertThat(properties.fileLocation()).isEqualTo(Path.of("catalog.xtf"));
    }

    @Test
    void blankDownloadUrlRemainsUnsetForExplicitBlankConfiguration() {
        var properties = new CatalogProperties(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                " ");

        assertThat(properties.downloadUrl()).isNull();
    }

    @Test
    void rootRelativeDownloadUrlIsSupportedAndNormalized() {
        var properties = new CatalogProperties(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "/downloads/");

        assertThat(properties.downloadUrl()).isEqualTo("/downloads");
    }

    @Test
    void downloadUrlMustBeAbsoluteHttpUrlOrRootRelativePath() {
        assertThatThrownBy(() -> new CatalogProperties(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        "downloads"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("download-url");
    }
}
