package ch.so.agi.datenportal.catalog.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class DistributionLinkTest {

    @Test
    void preferredHrefUsesDownloadUrlBeforeAccessUrl() {
        DistributionLink withDownloadUrl = new DistributionLink(
                URI.create("https://example.com/access.csv"),
                URI.create("https://example.com/download.csv"),
                DistributionFormat.CSV);
        DistributionLink withoutDownloadUrl = new DistributionLink(
                URI.create("https://example.com/access.xlsx"),
                Optional.empty(),
                DistributionFormat.XLSX);

        assertThat(withDownloadUrl.preferredHref()).isEqualTo(URI.create("https://example.com/download.csv"));
        assertThat(withoutDownloadUrl.preferredHref()).isEqualTo(URI.create("https://example.com/access.xlsx"));
    }

    @Test
    void displayLabelMapsPrimaryFormats() {
        assertThat(new DistributionLink(URI.create("https://example.com/csv"), DistributionFormat.CSV).displayLabel())
                .isEqualTo("CSV");
        assertThat(new DistributionLink(URI.create("https://example.com/xlsx"), DistributionFormat.XLSX).displayLabel())
                .isEqualTo("XLSX");
        assertThat(new DistributionLink(URI.create("https://example.com/parquet"), DistributionFormat.PARQUET).displayLabel())
                .isEqualTo("Parquet");
        assertThat(new DistributionLink(URI.create("https://example.com/other"), DistributionFormat.OTHER).isPrimaryFormat())
                .isFalse();
    }
}
