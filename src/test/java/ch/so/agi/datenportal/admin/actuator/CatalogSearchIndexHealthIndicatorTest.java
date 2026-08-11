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
import ch.so.agi.datenportal.search.CatalogSearchException;
import ch.so.agi.datenportal.search.CatalogSearchIndex;
import ch.so.agi.datenportal.search.SearchHit;
import java.net.URI;
import java.time.Instant;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.health.contributor.Status;

class CatalogSearchIndexHealthIndicatorTest {

    @Test
    void reportsDownWhenIndexedAndVisibleDocumentCountsDiffer() {
        var health = new CatalogSearchIndexHealthIndicator(new CatalogService(snapshot(indexWithCount(0)))).health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails())
                .containsEntry("expectedDocuments", 1)
                .containsEntry("indexedDocuments", 0);
    }

    @Test
    void reportsDownWithAnErrorWhenDocumentCountFails() {
        var health = new CatalogSearchIndexHealthIndicator(new CatalogService(snapshot(new FailingIndex()))).health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsKey("error");
    }

    @Test
    void reportsUpWhenCountsMatch() {
        var health = new CatalogSearchIndexHealthIndicator(new CatalogService(snapshot(indexWithCount(1)))).health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails())
                .containsEntry("expectedDocuments", 1)
                .containsEntry("indexedDocuments", 1);
    }

    private static CatalogSnapshot snapshot(CatalogSearchIndex index) {
        return CatalogSnapshot.of(
                new Catalog(List.of(entry()), List.of()),
                Instant.parse("2026-06-15T00:00:00Z"),
                Duration.ZERO,
                CatalogTestArtifacts.published("health-test"),
                CatalogTestArtifacts.duckDb("health-test-duckdb"),
                index);
    }

    private static CatalogSearchIndex indexWithCount(int count) {
        return new CatalogSearchIndex() {
            @Override
            public List<SearchHit> search(String userQuery) {
                return List.of();
            }

            @Override
            public int documentCount() {
                return count;
            }

            @Override
            public void close() {}
        };
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

    private static final class FailingIndex implements CatalogSearchIndex {

        @Override
        public List<SearchHit> search(String userQuery) {
            return List.of();
        }

        @Override
        public int documentCount() {
            throw new CatalogSearchException("count failed");
        }

        @Override
        public void close() {}
    }
}
