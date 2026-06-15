package ch.so.agi.datenportal.catalog.service;

import static org.assertj.core.api.Assertions.assertThat;

import ch.so.agi.datenportal.catalog.domain.AccessLevel;
import ch.so.agi.datenportal.catalog.domain.Catalog;
import ch.so.agi.datenportal.catalog.domain.CatalogSnapshot;
import ch.so.agi.datenportal.catalog.domain.DatasetEntry;
import ch.so.agi.datenportal.catalog.domain.DistributionFormat;
import ch.so.agi.datenportal.catalog.domain.DistributionLink;
import ch.so.agi.datenportal.catalog.domain.Office;
import ch.so.agi.datenportal.catalog.domain.Theme;
import ch.so.agi.datenportal.search.CatalogSearchIndex;
import ch.so.agi.datenportal.search.SearchHit;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

class CatalogServiceTest {

    @Test
    void returnsInitialSnapshotAndReadAccessors() {
        CatalogService catalogService = new CatalogService(createInitialSnapshot());

        assertThat(catalogService.currentSnapshot().sourceDescription()).isEqualTo("initial");
        assertThat(catalogService.visibleEntries())
                .extracting(entry -> entry.identifier())
                .containsExactly(
                        "replacement-series",
                        "replacement-dataset");
        assertThat(catalogService.findVisibleEntry("replacement-series")).isPresent();
        assertThat(catalogService.findAnyEntry("replacement-series-2026")).isPresent();
    }

    @Test
    void replaceSnapshotSwapsReadableState() {
        CatalogService catalogService = new CatalogService(createInitialSnapshot());
        CatalogSnapshot replacement = CatalogSnapshot.of(
                new Catalog(
                        List.of(new DatasetEntry(
                                "replacement-dataset",
                                "Replacement",
                                "Beschreibung",
                                new Office("office", "Amt", Optional.empty()),
                                new Office("office", "Amt", Optional.empty()),
                                List.of(new Theme("theme", "Thema")),
                                List.of("Replacement"),
                                LocalDate.parse("2026-06-01"),
                                AccessLevel.OPEN,
                                List.of(new DistributionLink(URI.create("https://example.com/replacement.csv"), DistributionFormat.CSV)))),
                        List.of()),
                Instant.parse("2026-06-14T09:00:00Z"),
                "replacement");

        catalogService.replaceSnapshot(replacement);

        assertThat(catalogService.currentSnapshot().sourceDescription()).isEqualTo("replacement");
        assertThat(catalogService.visibleEntries())
                .extracting(entry -> entry.identifier())
                .containsExactly("replacement-dataset");
    }

    @Test
    void replaceSnapshotWaitsForActiveSnapshotReadersBeforeClosingOldIndex() throws Exception {
        var oldIndex = new CloseTrackingSearchIndex();
        CatalogSnapshot initial = CatalogSnapshot.of(
                createInitialSnapshot().catalog(),
                Instant.parse("2026-06-14T08:00:00Z"),
                "initial",
                "initial-hash",
                oldIndex);
        CatalogService catalogService = new CatalogService(initial);
        CatalogSnapshot replacement = CatalogSnapshot.of(
                new Catalog(
                        List.of(new DatasetEntry(
                                "new-dataset",
                                "New",
                                "Beschreibung",
                                new Office("office", "Amt", Optional.empty()),
                                new Office("office", "Amt", Optional.empty()),
                                List.of(new Theme("theme", "Thema")),
                                List.of("New"),
                                LocalDate.parse("2026-06-01"),
                                AccessLevel.OPEN,
                                List.of(new DistributionLink(URI.create("https://example.com/new.csv"), DistributionFormat.CSV)))),
                        List.of()),
                Instant.parse("2026-06-14T09:00:00Z"),
                "replacement");

        CountDownLatch readerStarted = new CountDownLatch(1);
        CountDownLatch releaseReader = new CountDownLatch(1);
        var executor = Executors.newFixedThreadPool(2);
        try {
            CompletableFuture<String> reader = CompletableFuture.supplyAsync(() -> catalogService.withSnapshot(snapshot -> {
                readerStarted.countDown();
                try {
                    releaseReader.await(2, TimeUnit.SECONDS);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                return snapshot.sourceDescription();
            }), executor);
            assertThat(readerStarted.await(2, TimeUnit.SECONDS)).isTrue();

            CompletableFuture<Void> writer = CompletableFuture.runAsync(() -> catalogService.replaceSnapshot(replacement), executor);
            Thread.sleep(100);

            assertThat(writer.isDone()).isFalse();
            assertThat(oldIndex.closed()).isFalse();

            releaseReader.countDown();

            assertThat(reader.get(2, TimeUnit.SECONDS)).isEqualTo("initial");
            writer.get(2, TimeUnit.SECONDS);
            assertThat(oldIndex.closed()).isTrue();
            assertThat(catalogService.currentSnapshot().sourceDescription()).isEqualTo("replacement");
        } finally {
            executor.shutdownNow();
        }
    }

    private static CatalogSnapshot createInitialSnapshot() {
        return CatalogSnapshot.of(
                new Catalog(
                        List.of(new DatasetEntry(
                                "replacement-dataset",
                                "Replacement",
                                "Beschreibung",
                                new Office("office", "Amt", Optional.empty()),
                                new Office("office", "Amt", Optional.empty()),
                                List.of(new Theme("theme", "Thema")),
                                List.of("Replacement"),
                                LocalDate.parse("2026-05-01"),
                                AccessLevel.OPEN,
                                List.of(new DistributionLink(URI.create("https://example.com/replacement.csv"), DistributionFormat.CSV)))),
                        List.of(new ch.so.agi.datenportal.catalog.domain.DatasetSeriesEntry(
                                "replacement-series",
                                "Replacement Series",
                                "Beschreibung",
                                new Office("office", "Amt", Optional.empty()),
                                new Office("office", "Amt", Optional.empty()),
                                List.of(new Theme("theme", "Thema")),
                                List.of("Series"),
                                AccessLevel.OPEN,
                                List.of(new ch.so.agi.datenportal.catalog.domain.DatasetIssueEntry(
                                        "replacement-series-2026",
                                        "Replacement Series 2026",
                                        "Beschreibung",
                                        new Office("office", "Amt", Optional.empty()),
                                        new Office("office", "Amt", Optional.empty()),
                                        List.of(new Theme("theme", "Thema")),
                                        List.of("Series"),
                                        LocalDate.parse("2026-06-01"),
                                        AccessLevel.OPEN,
                                        List.of(new DistributionLink(URI.create("https://example.com/replacement-series.csv"), DistributionFormat.CSV)),
                                        "2026",
                                        true))))),
                Instant.parse("2026-06-14T08:00:00Z"),
                "initial");
    }

    private static final class CloseTrackingSearchIndex implements CatalogSearchIndex {

        private final AtomicBoolean closed = new AtomicBoolean();

        @Override
        public List<SearchHit> search(String userQuery, int maxResults) {
            return List.of();
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public void close() {
            closed.set(true);
        }

        boolean closed() {
            return closed.get();
        }
    }
}
