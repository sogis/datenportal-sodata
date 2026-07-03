package ch.so.agi.datenportal.catalog.importxtf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class CatalogDownloadUrlPlaceholderResolverTest {

    private static final Instant FETCHED_AT = Instant.parse("2026-07-02T08:00:00Z");

    private final CatalogDownloadUrlPlaceholderResolver resolver = new CatalogDownloadUrlPlaceholderResolver();

    @Test
    void replacesDownloadUrlPlaceholderAndRecomputesHash() {
        CatalogBytes original = bytes("<downloadURL>${DOWNLOAD_URL}/file.parquet</downloadURL>");

        CatalogBytes resolved = resolver.resolve(original, "http://localhost:8081/ch.so.datenportal/downloads");

        assertThat(text(resolved))
                .isEqualTo("<downloadURL>http://localhost:8081/ch.so.datenportal/downloads/file.parquet</downloadURL>");
        assertThat(resolved.sourceDescription()).isEqualTo(original.sourceDescription());
        assertThat(resolved.fetchedAt()).isEqualTo(FETCHED_AT);
        assertThat(resolved.contentHash()).isNotEqualTo(original.contentHash());
    }

    @Test
    void collapsesDuplicateSlashesAtPlaceholderJoin() {
        CatalogBytes original = bytes("<downloadURL>${DOWNLOAD_URL}//file.parquet</downloadURL>");

        CatalogBytes resolved = resolver.resolve(original, "http://localhost:8081/ch.so.datenportal/downloads/");

        assertThat(text(resolved))
                .isEqualTo("<downloadURL>http://localhost:8081/ch.so.datenportal/downloads/file.parquet</downloadURL>");
    }

    @Test
    void leavesBytesUnchangedWhenPlaceholderIsAbsent() {
        CatalogBytes original = bytes("<downloadURL>https://example.org/file.parquet</downloadURL>");

        CatalogBytes resolved = resolver.resolve(original, null);

        assertThat(resolved).isSameAs(original);
    }

    @Test
    void failsClearlyWhenPlaceholderIsPresentButDownloadUrlIsBlank() {
        CatalogBytes original = bytes("<downloadURL>${DOWNLOAD_URL}/file.parquet</downloadURL>");

        assertThatThrownBy(() -> resolver.resolve(original, " "))
                .isInstanceOf(CatalogSourceException.class)
                .hasMessageContaining("datenportal.catalog.download-url");
    }

    @Test
    void supportsRootRelativeDownloadUrl() {
        CatalogBytes original = bytes("<downloadURL>${DOWNLOAD_URL}/file.parquet</downloadURL>");

        CatalogBytes resolved = resolver.resolve(original, "/downloads/");

        assertThat(text(resolved)).isEqualTo("<downloadURL>/downloads/file.parquet</downloadURL>");
    }

    private static CatalogBytes bytes(String value) {
        return new CatalogBytes(value.getBytes(StandardCharsets.UTF_8), "test-source", FETCHED_AT);
    }

    private static String text(CatalogBytes bytes) {
        return new String(bytes.bytes(), StandardCharsets.UTF_8);
    }
}
