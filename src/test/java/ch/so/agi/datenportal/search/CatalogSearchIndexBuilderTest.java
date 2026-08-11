package ch.so.agi.datenportal.search;

import static org.assertj.core.api.Assertions.assertThat;

import ch.so.agi.datenportal.catalog.domain.AccessLevel;
import ch.so.agi.datenportal.catalog.domain.CatalogEntry;
import ch.so.agi.datenportal.catalog.domain.DatasetEntry;
import ch.so.agi.datenportal.catalog.domain.DistributionFormat;
import ch.so.agi.datenportal.catalog.domain.DistributionLink;
import ch.so.agi.datenportal.catalog.domain.Office;
import ch.so.agi.datenportal.catalog.domain.Theme;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class CatalogSearchIndexBuilderTest {

    private static final Office AGI = new Office("agi", "Amt für Geoinformation", Optional.of("AGI"));
    private static final Theme REGI = new Theme("REGI", "Raum und Umwelt");

    private final CatalogSearchIndexBuilder builder = new CatalogSearchIndexBuilder(new CatalogDocumentMapper());

    @Test
    void reindexingCreatesIndependentIndexesWithoutOldDocuments() {
        CatalogSearchIndex first = builder.build(List.of(dataset("old-entry", "Alte Grenzen")));
        CatalogSearchIndex second = builder.build(List.of(dataset("new-entry", "Neue Zonen")));

        assertThat(first.search("Alte"))
                .extracting(SearchHit::entryId)
                .containsExactly("old-entry");
        assertThat(second.search("Alte")).isEmpty();
        assertThat(second.search("Neue"))
                .extracting(SearchHit::entryId)
                .containsExactly("new-entry");

        first.close();
        first.close();
        second.close();
    }

    private static CatalogEntry dataset(String identifier, String title) {
        return new DatasetEntry(
                identifier,
                title,
                "Beschreibung",
                AGI,
                AGI,
                List.of(REGI),
                List.of("Test"),
                LocalDate.parse("2026-05-14"),
                AccessLevel.OPEN,
                List.of(new DistributionLink(URI.create("https://example.com/" + identifier + "/csv"), DistributionFormat.CSV)));
    }
}
