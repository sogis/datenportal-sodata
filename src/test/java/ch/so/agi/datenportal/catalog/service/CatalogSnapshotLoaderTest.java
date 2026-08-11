package ch.so.agi.datenportal.catalog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ch.so.agi.datenportal.catalog.CatalogTestArtifacts;
import ch.so.agi.datenportal.catalog.domain.AccessLevel;
import ch.so.agi.datenportal.catalog.domain.Catalog;
import ch.so.agi.datenportal.catalog.domain.CatalogEntry;
import ch.so.agi.datenportal.catalog.domain.DatasetEntry;
import ch.so.agi.datenportal.catalog.domain.DistributionFormat;
import ch.so.agi.datenportal.catalog.domain.DistributionLink;
import ch.so.agi.datenportal.catalog.domain.Office;
import ch.so.agi.datenportal.catalog.domain.Theme;
import ch.so.agi.datenportal.catalog.importxtf.CatalogBytes;
import ch.so.agi.datenportal.catalog.importxtf.CatalogSource;
import ch.so.agi.datenportal.catalog.importxtf.CatalogSourceException;
import ch.so.agi.datenportal.catalog.importxtf.CatalogValidator;
import ch.so.agi.datenportal.search.CatalogDocumentMapper;
import ch.so.agi.datenportal.search.CatalogSearchIndexBuildException;
import ch.so.agi.datenportal.search.CatalogSearchIndexBuilder;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class CatalogSnapshotLoaderTest {

    private static final Office AGI = new Office("agi", "Amt für Geoinformation", Optional.of("AGI"));
    private static final Theme REGI = new Theme("REGI", "Raum und Umwelt");
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-06-15T00:00:00Z"), ZoneOffset.UTC);

    @Test
    void loadedSnapshotContainsBuiltSearchIndex() {
        var loader = new CatalogSnapshotLoader(
                source(),
                duckSource(),
                new CatalogSnapshotBuilder(
                        (inputStream, sourceDescription) -> catalog(),
                        new CatalogValidator(),
                        new CatalogSearchIndexBuilder(new CatalogDocumentMapper()),
                        CLOCK));

        var snapshot = loader.load(CatalogTestArtifacts.published("test"), CatalogTestArtifacts.duckDb("duckdb"));

        assertThat(snapshot.searchIndex().documentCount()).isEqualTo(1);
        assertThat(snapshot.searchIndex().search("Bauinventar"))
                .extracting(hit -> hit.entryId())
                .containsExactly("bauinventar");
    }

    @Test
    void indexBuildFailurePreventsSnapshotCreation() {
        var loader = new CatalogSnapshotLoader(
                source(),
                duckSource(),
                new CatalogSnapshotBuilder(
                        (inputStream, sourceDescription) -> catalog(),
                        new CatalogValidator(),
                        new FailingSearchIndexBuilder(),
                        CLOCK));

        assertThatThrownBy(() -> loader.load(
                CatalogTestArtifacts.published("test"), CatalogTestArtifacts.duckDb("duckdb")))
                .isInstanceOf(CatalogSearchIndexBuildException.class);
    }

    @Test
    void startupLoadsPublishedCatalogAndDuckDbExactlyOnce() {
        AtomicInteger publishedLoads = new AtomicInteger();
        AtomicInteger duckDbLoads = new AtomicInteger();
        var loader = new CatalogSnapshotLoader(
                countingSource(publishedLoads, CatalogTestArtifacts.published("published")),
                countingSource(duckDbLoads, CatalogTestArtifacts.duckDb("duckdb")),
                new CatalogSnapshotBuilder(
                        (inputStream, sourceDescription) -> catalog(),
                        new CatalogValidator(),
                        new CatalogSearchIndexBuilder(new CatalogDocumentMapper()),
                        CLOCK));

        var snapshot = loader.load();

        assertThat(snapshot.publishedCatalog().sourceDescription()).isEqualTo("published");
        assertThat(snapshot.duckDbCatalog().sourceDescription()).isEqualTo("duckdb");
        assertThat(publishedLoads).hasValue(1);
        assertThat(duckDbLoads).hasValue(1);
    }

    @Test
    void shortDuckDbArtifactPreventsSnapshotCreation() {
        var builder = new CatalogSnapshotBuilder(
                (inputStream, sourceDescription) -> catalog(),
                new CatalogValidator(),
                new CatalogSearchIndexBuilder(new CatalogDocumentMapper()),
                CLOCK);

        assertThatThrownBy(() -> builder.build(
                CatalogTestArtifacts.published("published"),
                new CatalogBytes(new byte[11], "short-duckdb")))
                .isInstanceOf(CatalogSourceException.class)
                .hasMessageContaining("at least 12 bytes");
    }

    @Test
    void missingDuckMarkerPreventsSnapshotCreation() {
        var invalid = new byte[12];
        var builder = new CatalogSnapshotBuilder(
                (inputStream, sourceDescription) -> catalog(),
                new CatalogValidator(),
                new CatalogSearchIndexBuilder(new CatalogDocumentMapper()),
                CLOCK);

        assertThatThrownBy(() -> builder.build(
                CatalogTestArtifacts.published("published"),
                new CatalogBytes(invalid, "invalid-duckdb")))
                .isInstanceOf(CatalogSourceException.class)
                .hasMessageContaining("DUCK marker");
    }

    private static Catalog catalog() {
        return new Catalog(
                List.of(new DatasetEntry(
                        "bauinventar",
                        "Bauinventar",
                        "Inventarisierte Bauten im Kanton Solothurn.",
                        AGI,
                        AGI,
                        List.of(REGI),
                        List.of("Bauen"),
                        LocalDate.parse("2026-05-14"),
                        AccessLevel.OPEN,
                        List.of(new DistributionLink(URI.create("https://example.com/bauinventar.csv"), DistributionFormat.CSV)))),
                List.of());
    }

    private static CatalogSource source() {
        return new CatalogSource() {
            @Override
            public CatalogBytes load() throws CatalogSourceException {
                return CatalogTestArtifacts.published("test");
            }

            @Override
            public String description() {
                return "test";
            }
        };
    }

    private static CatalogSource duckSource() {
        return new CatalogSource() {
            @Override
            public CatalogBytes load() throws CatalogSourceException {
                return CatalogTestArtifacts.duckDb("duckdb");
            }

            @Override
            public String description() {
                return "duckdb";
            }
        };
    }

    private static CatalogSource countingSource(AtomicInteger counter, CatalogBytes bytes) {
        return new CatalogSource() {
            @Override
            public CatalogBytes load() throws CatalogSourceException {
                counter.incrementAndGet();
                return bytes;
            }

            @Override
            public String description() {
                return bytes.sourceDescription();
            }
        };
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
