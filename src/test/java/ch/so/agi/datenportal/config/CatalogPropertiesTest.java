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
    void sourceTypeIsRequired() {
        assertThatThrownBy(() -> new CatalogProperties(
                        null,
                        "catalog.xtf",
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
    void explicitClasspathSourceRequiresAndUsesClasspathLocation() {
        var properties = new CatalogProperties(
                CatalogProperties.SourceType.CLASSPATH,
                "published_catalog_full_54_entries.xtf",
                null,
                null,
                null,
                null,
                null,
                null);

        assertThat(properties.classpathLocation()).isEqualTo("published_catalog_full_54_entries.xtf");
        assertThat(properties.maxSize()).isEqualTo(DataSize.ofMegabytes(50));
        assertThat(properties.downloadUrl()).isNull();
    }

    @Test
    void classpathSourceRequiresLocation() {
        assertThatThrownBy(() -> new CatalogProperties(
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
    void explicitHttpSourceUsesHttpUrlAndTimeouts() {
        var properties = new CatalogProperties(
                CatalogProperties.SourceType.HTTP,
                null,
                null,
                URI.create("https://example.com/catalog.xtf"),
                Duration.ofSeconds(2),
                Duration.ofSeconds(3),
                DataSize.ofMegabytes(10),
                "https://download.example.org/files/");

        assertThat(properties.httpUrl()).isEqualTo(URI.create("https://example.com/catalog.xtf"));
        assertThat(properties.httpConnectTimeout()).isEqualTo(Duration.ofSeconds(2));
        assertThat(properties.httpReadTimeout()).isEqualTo(Duration.ofSeconds(3));
        assertThat(properties.downloadUrl()).isEqualTo("https://download.example.org/files");
    }

    @Test
    void httpSourceRequiresHttpUrlAndRejectsUnsupportedSchemes() {
        assertThatThrownBy(() -> new CatalogProperties(
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

        assertThatThrownBy(() -> new CatalogProperties(
                        CatalogProperties.SourceType.HTTP,
                        null,
                        null,
                        URI.create("ftp://example.com/catalog.xtf"),
                        null,
                        null,
                        null,
                        null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("http or https");
    }

    @Test
    void explicitFileSourceUsesFileLocationAndRequiresIt() {
        var properties = new CatalogProperties(
                CatalogProperties.SourceType.FILE,
                null,
                Path.of("catalog.xtf"),
                null,
                null,
                null,
                null,
                null);

        assertThat(properties.fileLocation()).isEqualTo(Path.of("catalog.xtf"));

        assertThatThrownBy(() -> new CatalogProperties(
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

    @Test
    void blankDownloadUrlRemainsUnset() {
        var properties = new CatalogProperties(
                CatalogProperties.SourceType.FILE,
                null,
                Path.of("catalog.xtf"),
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
                CatalogProperties.SourceType.FILE,
                null,
                Path.of("catalog.xtf"),
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
                        CatalogProperties.SourceType.FILE,
                        null,
                        Path.of("catalog.xtf"),
                        null,
                        null,
                        null,
                        null,
                        "downloads"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("download-url");
    }
}
