package ch.so.agi.datenportal.catalog.service;

import static org.assertj.core.api.Assertions.assertThat;

import ch.so.agi.datenportal.admin.reload.ReloadFailureType;
import ch.so.agi.datenportal.catalog.CatalogTestArtifacts;
import ch.so.agi.datenportal.catalog.domain.AccessLevel;
import ch.so.agi.datenportal.catalog.domain.Catalog;
import ch.so.agi.datenportal.catalog.domain.CatalogEntry;
import ch.so.agi.datenportal.catalog.domain.CatalogSnapshot;
import ch.so.agi.datenportal.catalog.domain.DatasetEntry;
import ch.so.agi.datenportal.catalog.domain.DistributionFormat;
import ch.so.agi.datenportal.catalog.domain.DistributionLink;
import ch.so.agi.datenportal.catalog.domain.Office;
import ch.so.agi.datenportal.catalog.domain.Theme;
import ch.so.agi.datenportal.catalog.importxtf.CatalogBytes;
import ch.so.agi.datenportal.catalog.importxtf.CatalogSource;
import ch.so.agi.datenportal.catalog.importxtf.CatalogSourceException;
import ch.so.agi.datenportal.catalog.importxtf.CatalogValidator;
import ch.so.agi.datenportal.catalog.importxtf.PublishedCatalogParser;
import ch.so.agi.datenportal.catalog.importxtf.XtfElementPath;
import ch.so.agi.datenportal.catalog.importxtf.XtfParseException;
import ch.so.agi.datenportal.search.CatalogDocumentMapper;
import ch.so.agi.datenportal.search.CatalogSearchIndexBuildException;
import ch.so.agi.datenportal.search.CatalogSearchIndexBuilder;
import ch.so.agi.datenportal.search.SearchHit;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

class CatalogReloadServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-06-15T00:00:00Z"), ZoneOffset.UTC);
    private static final Office OFFICE = new Office("agi", "Amt für Geoinformation", Optional.of("AGI"));
    private static final Theme THEME = new Theme("REGI", "Raum und Umwelt");

    @Test
    void successfulReloadReplacesSnapshotAndSearchIndex() {
        CatalogService catalogService = new CatalogService(snapshot(catalog("initial", "Initial"), "initial", "initial-hash"));
        var service = reloadService(
                catalogService,
                source(() -> bytes("replacement")),
                (inputStream, sourceDescription) -> catalog("replacement", "Replacement"));

        List<SearchHit> initialHits = catalogService.withSnapshot(snapshot -> snapshot.searchIndex().search("Replacement"));
        assertThat(initialHits).isEmpty();

        var result = service.reload();

        assertThat(result.success()).isTrue();
        assertThat(result.contentHash()).isEqualTo(bytes("replacement").contentHash());
        assertThat(catalogService.findVisibleEntry("replacement")).isPresent();
        assertThat(catalogService.findVisibleEntry("initial")).isEmpty();
        List<SearchHit> replacementHits = catalogService.withSnapshot(snapshot -> snapshot.searchIndex().search("Replacement"));
        assertThat(replacementHits)
                .extracting(SearchHit::entryId)
                .containsExactly("replacement");
    }

    @Test
    void failedDownloadKeepsOldSnapshotActive() {
        CatalogService catalogService = new CatalogService(snapshot(catalog("initial", "Initial"), "initial", "initial-hash"));
        var service = reloadService(
                catalogService,
                source(() -> {
                    throw new CatalogSourceException("boom");
                }),
                (inputStream, sourceDescription) -> catalog("replacement", "Replacement"));

        var result = service.reload();

        assertThat(result.success()).isFalse();
        assertThat(result.failureType()).isEqualTo(ReloadFailureType.SOURCE);
        assertThat(result.httpStatus()).isEqualTo(502);
        assertThat(catalogService.findVisibleEntry("initial")).isPresent();
        assertThat(catalogService.findVisibleEntry("replacement")).isEmpty();
    }

    @Test
    void invalidXmlKeepsOldSnapshotActive() {
        CatalogService catalogService = new CatalogService(snapshot(catalog("initial", "Initial"), "initial", "initial-hash"));
        var service = reloadService(
                catalogService,
                source(() -> bytes("invalid")),
                (inputStream, sourceDescription) -> {
                    throw new XtfParseException(sourceDescription, XtfElementPath.root(), "invalid xml");
                });

        var result = service.reload();

        assertThat(result.success()).isFalse();
        assertThat(result.failureType()).isEqualTo(ReloadFailureType.PARSE);
        assertThat(result.httpStatus()).isEqualTo(422);
        assertThat(catalogService.findVisibleEntry("initial")).isPresent();
    }

    @Test
    void validationFailureKeepsOldSnapshotActive() {
        CatalogService catalogService = new CatalogService(snapshot(catalog("initial", "Initial"), "initial", "initial-hash"));
        var service = reloadService(
                catalogService,
                source(() -> bytes("empty")),
                (inputStream, sourceDescription) -> new Catalog(List.of(), List.of()));

        var result = service.reload();

        assertThat(result.success()).isFalse();
        assertThat(result.failureType()).isEqualTo(ReloadFailureType.VALIDATION);
        assertThat(result.httpStatus()).isEqualTo(422);
        assertThat(catalogService.findVisibleEntry("initial")).isPresent();
    }

    @Test
    void duckDbFailureKeepsOldSnapshotAndDuckDbActive() {
        CatalogService catalogService = new CatalogService(snapshot(catalog("initial", "Initial"), "initial", "initial-hash"));
        var service = reloadService(
                catalogService,
                source(() -> bytes("replacement")),
                source(() -> new CatalogBytes(new byte[12], "invalid-duckdb")),
                (inputStream, sourceDescription) -> catalog("replacement", "Replacement"));

        var result = service.reload();

        assertThat(result.success()).isFalse();
        assertThat(result.failureType()).isEqualTo(ReloadFailureType.SOURCE);
        assertThat(catalogService.findVisibleEntry("initial")).isPresent();
        String activeDuckDb = catalogService.withSnapshot(snapshot -> snapshot.duckDbCatalog().sourceDescription());
        assertThat(activeDuckDb).isEqualTo("initial-duckdb");
    }

    @Test
    void indexFailureKeepsOldSnapshotAndDuckDbActive() {
        CatalogService catalogService = new CatalogService(snapshot(catalog("initial", "Initial"), "initial", "initial-hash"));
        var service = reloadService(
                catalogService,
                source(() -> bytes("replacement")),
                source(() -> CatalogTestArtifacts.duckDb("replacement-duckdb")),
                (inputStream, sourceDescription) -> catalog("replacement", "Replacement"),
                new FailingSearchIndexBuilder());

        var result = service.reload();

        assertThat(result.success()).isFalse();
        assertThat(result.failureType()).isEqualTo(ReloadFailureType.INDEX);
        assertThat(catalogService.findVisibleEntry("initial")).isPresent();
        String activeDuckDb = catalogService.withSnapshot(snapshot -> snapshot.duckDbCatalog().sourceDescription());
        assertThat(activeDuckDb).isEqualTo("initial-duckdb");
    }

    @Test
    void parallelReloadIsRejectedAndStatusTracksSuccessAndFailure() throws Exception {
        CatalogService catalogService = new CatalogService(snapshot(catalog("initial", "Initial"), "initial", "initial-hash"));
        CountDownLatch sourceEntered = new CountDownLatch(1);
        CountDownLatch releaseSource = new CountDownLatch(1);
        var service = reloadService(
                catalogService,
                source(() -> {
                    sourceEntered.countDown();
                    try {
                        releaseSource.await(2, TimeUnit.SECONDS);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                    return bytes("replacement");
                }),
                (inputStream, sourceDescription) -> catalog("replacement", "Replacement"));

        var executor = Executors.newSingleThreadExecutor();
        try {
            CompletableFuture<?> firstReload = CompletableFuture.supplyAsync(service::reload, executor);
            assertThat(sourceEntered.await(2, TimeUnit.SECONDS)).isTrue();

            var conflict = service.reload();
            assertThat(conflict.success()).isFalse();
            assertThat(conflict.failureType()).isEqualTo(ReloadFailureType.CONFLICT);
            assertThat(conflict.httpStatus()).isEqualTo(409);

            releaseSource.countDown();
            firstReload.get(2, TimeUnit.SECONDS);

            var status = service.status();
            assertThat(status.lastSuccessfulReload()).isNotNull();
            assertThat(status.lastFailedReload()).isNotNull();
            assertThat(status.loadedAt()).isEqualTo(CLOCK.instant());
        } finally {
            executor.shutdownNow();
        }
    }

    private static CatalogReloadService reloadService(
            CatalogService catalogService,
            CatalogSource source,
            PublishedCatalogParser parser) {
        return reloadService(
                catalogService,
                source,
                source(() -> CatalogTestArtifacts.duckDb("duckdb")),
                parser,
                new CatalogSearchIndexBuilder(new CatalogDocumentMapper()));
    }

    private static CatalogReloadService reloadService(
            CatalogService catalogService,
            CatalogSource source,
            CatalogSource duckDbSource,
            PublishedCatalogParser parser) {
        return reloadService(
                catalogService,
                source,
                duckDbSource,
                parser,
                new CatalogSearchIndexBuilder(new CatalogDocumentMapper()));
    }

    private static CatalogReloadService reloadService(
            CatalogService catalogService,
            CatalogSource source,
            CatalogSource duckDbSource,
            PublishedCatalogParser parser,
            CatalogSearchIndexBuilder indexBuilder) {
        return new CatalogReloadService(
                source,
                duckDbSource,
                new CatalogSnapshotBuilder(
                        parser,
                        new CatalogValidator(),
                        indexBuilder,
                        CLOCK),
                catalogService,
                CLOCK);
    }

    private static CatalogSource source(Supplier<CatalogBytes> supplier) {
        return new CatalogSource() {
            @Override
            public CatalogBytes load() throws CatalogSourceException {
                return supplier.get();
            }

            @Override
            public String description() {
                return "test";
            }
        };
    }

    private static CatalogSnapshot snapshot(Catalog catalog, String sourceDescription, String contentHash) {
        var index = new CatalogSearchIndexBuilder(new CatalogDocumentMapper()).build(catalog.topLevelEntries());
        return CatalogSnapshot.of(
                catalog,
                CLOCK.instant(),
                java.time.Duration.ZERO,
                CatalogTestArtifacts.published(sourceDescription, contentHash),
                CatalogTestArtifacts.duckDb(sourceDescription + "-duckdb"),
                index);
    }

    private static Catalog catalog(String identifier, String title) {
        return new Catalog(
                List.of(new DatasetEntry(
                        identifier,
                        title,
                        "Beschreibung",
                        OFFICE,
                        OFFICE,
                        List.of(THEME),
                        List.of(title),
                        LocalDate.parse("2026-06-01"),
                        AccessLevel.OPEN,
                        List.of(new DistributionLink(URI.create("https://example.com/" + identifier + ".csv"), DistributionFormat.CSV)))),
                List.of());
    }

    private static CatalogBytes bytes(String value) {
        return new CatalogBytes(
                value.getBytes(StandardCharsets.UTF_8),
                "test",
                Integer.toHexString(value.hashCode()),
                CLOCK.instant());
    }

    private static final class FailingSearchIndexBuilder extends CatalogSearchIndexBuilder {
        private FailingSearchIndexBuilder() {
            super(new CatalogDocumentMapper());
        }

        @Override
        public ch.so.agi.datenportal.search.CatalogSearchIndex build(List<CatalogEntry> visibleEntries) {
            throw new CatalogSearchIndexBuildException("boom", new IllegalStateException("boom"));
        }
    }
}
