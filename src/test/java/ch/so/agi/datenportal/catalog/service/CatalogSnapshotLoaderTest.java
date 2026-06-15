package ch.so.agi.datenportal.catalog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
import org.junit.jupiter.api.Test;

class CatalogSnapshotLoaderTest {

    private static final Office AGI = new Office("agi", "Amt für Geoinformation", Optional.of("AGI"));
    private static final Theme REGI = new Theme("REGI", "Raum und Umwelt");
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-06-15T00:00:00Z"), ZoneOffset.UTC);

    @Test
    void loadedSnapshotContainsBuiltSearchIndex() {
        var loader = new CatalogSnapshotLoader(
                source(),
                (inputStream, sourceDescription) -> catalog(),
                new CatalogValidator(),
                new CatalogSearchIndexBuilder(new CatalogDocumentMapper()),
                CLOCK);

        var snapshot = loader.load(new CatalogBytes("<TRANSFER/>".getBytes(StandardCharsets.UTF_8), "test"));

        assertThat(snapshot.searchIndex().isEmpty()).isFalse();
        assertThat(snapshot.searchIndex().search("Bauinventar", 10))
                .extracting(hit -> hit.entryId())
                .containsExactly("bauinventar");
    }

    @Test
    void indexBuildFailurePreventsSnapshotCreation() {
        var loader = new CatalogSnapshotLoader(
                source(),
                (inputStream, sourceDescription) -> catalog(),
                new CatalogValidator(),
                new FailingSearchIndexBuilder(),
                CLOCK);

        assertThatThrownBy(() -> loader.load(new CatalogBytes("<TRANSFER/>".getBytes(StandardCharsets.UTF_8), "test")))
                .isInstanceOf(CatalogSearchIndexBuildException.class);
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
                return new CatalogBytes("<TRANSFER/>".getBytes(StandardCharsets.UTF_8), "test");
            }

            @Override
            public String description() {
                return "test";
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
