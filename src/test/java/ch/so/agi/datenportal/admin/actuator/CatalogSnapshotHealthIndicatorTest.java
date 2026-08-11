package ch.so.agi.datenportal.admin.actuator;

import static org.assertj.core.api.Assertions.assertThat;

import ch.so.agi.datenportal.catalog.CatalogTestArtifacts;
import ch.so.agi.datenportal.catalog.domain.AccessLevel;
import ch.so.agi.datenportal.catalog.domain.Catalog;
import ch.so.agi.datenportal.catalog.domain.CatalogSnapshot;
import ch.so.agi.datenportal.catalog.domain.DatasetEntry;
import ch.so.agi.datenportal.catalog.domain.DistributionFormat;
import ch.so.agi.datenportal.catalog.domain.DistributionLink;
import ch.so.agi.datenportal.catalog.domain.Office;
import ch.so.agi.datenportal.catalog.domain.Theme;
import ch.so.agi.datenportal.catalog.service.CatalogService;
import java.net.URI;
import java.time.Instant;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.health.contributor.Status;

class CatalogSnapshotHealthIndicatorTest {

    @Test
    void exposesOperationalCountsAndTimeWithoutSourcePath() {
        var snapshot = CatalogSnapshot.of(
                new Catalog(List.of(entry()), List.of()),
                Instant.parse("2026-06-15T00:00:00Z"),
                Duration.ofMillis(42),
                CatalogTestArtifacts.published("file:/secret/catalog.xtf", "xtf-hash"),
                CatalogTestArtifacts.duckDb("file:/secret/catalog.duckdb"),
                ch.so.agi.datenportal.search.CatalogSearchIndex.empty());

        var health = new CatalogSnapshotHealthIndicator(new CatalogService(snapshot)).health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails())
                .containsEntry("visibleEntries", 1)
                .containsEntry("visibleDatasets", 1)
                .containsEntry("publishedCatalogHash", "xtf-hash")
                .containsEntry("duckDbCatalogSizeBytes", 12)
                .containsKey("duckDbCatalogHash")
                .containsEntry("loadDurationMs", 42L)
                .doesNotContainKey("sourceDescription")
                .containsKey("loadedAt");
    }

    private static DatasetEntry entry() {
        var office = new Office("agi", "Amt für Geoinformation", Optional.of("AGI"));
        return new DatasetEntry(
                "health-entry",
                "Health entry",
                "Health test entry.",
                office,
                office,
                List.of(new Theme("theme", "Thema")),
                List.of("Health"),
                LocalDate.parse("2026-06-01"),
                AccessLevel.OPEN,
                List.of(new DistributionLink(URI.create("https://example.com/health.csv"), DistributionFormat.CSV)));
    }
}
