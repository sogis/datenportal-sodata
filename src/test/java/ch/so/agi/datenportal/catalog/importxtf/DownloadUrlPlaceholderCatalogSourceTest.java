package ch.so.agi.datenportal.catalog.importxtf;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class DownloadUrlPlaceholderCatalogSourceTest {

    @Test
    void resolvesPlaceholdersWhenLoadingFromDelegate() {
        var source = new DownloadUrlPlaceholderCatalogSource(
                delegate("<downloadURL>${DOWNLOAD_URL}/file.parquet</downloadURL>"),
                new CatalogDownloadUrlPlaceholderResolver(),
                "http://localhost:8081/ch.so.datenportal/downloads/");

        CatalogBytes bytes = source.load();

        assertThat(text(bytes))
                .isEqualTo("<downloadURL>http://localhost:8081/ch.so.datenportal/downloads/file.parquet</downloadURL>");
        assertThat(source.description()).isEqualTo("delegate-source");
    }

    private static CatalogSource delegate(String content) {
        return new CatalogSource() {
            @Override
            public CatalogBytes load() throws CatalogSourceException {
                return new CatalogBytes(content.getBytes(StandardCharsets.UTF_8), description());
            }

            @Override
            public String description() {
                return "delegate-source";
            }
        };
    }

    private static String text(CatalogBytes bytes) {
        return new String(bytes.bytes(), StandardCharsets.UTF_8);
    }
}
