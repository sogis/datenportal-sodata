package ch.so.agi.datenportal.catalog.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class CatalogSnapshotTest {

    private static final Office OFFICE = new Office("office", "Amt für Tests", Optional.of("TEST"));
    private static final Theme THEME = new Theme("theme", "Testthema");

    @Test
    void issuesAppearOnlyInAnyEntryLookup() {
        DatasetIssueEntry issue = issue("series-issue", LocalDate.parse("2026-05-01"));
        CatalogSnapshot snapshot = CatalogSnapshot.of(
                new Catalog(
                        List.of(dataset("dataset", "Datensatz A", LocalDate.parse("2026-04-01"))),
                        List.of(series("series", "Datenreihe", issue))),
                Instant.parse("2026-06-14T08:00:00Z"),
                "test");

        assertThat(snapshot.visibleEntries())
                .extracting(CatalogEntry::identifier)
                .containsExactly("series", "dataset");
        assertThat(snapshot.findVisibleEntry("series-issue")).isEmpty();
        assertThat(snapshot.findAnyEntry("series-issue"))
                .get()
                .extracting(CatalogEntry::type)
                .isEqualTo(CatalogEntryType.DATASET_ISSUE);
        assertThat(snapshot.allEntriesByIdentifier()).containsKey("series-issue");
    }

    @Test
    void rejectsDuplicateIdentifiersAcrossDatasetsSeriesAndIssues() {
        DatasetEntry dataset = dataset("duplicate-id", "Datensatz A", LocalDate.parse("2026-04-01"));
        DatasetSeriesEntry series = series(
                "series",
                "Datenreihe",
                issue("duplicate-id", LocalDate.parse("2026-05-01")));

        assertThatThrownBy(() -> CatalogSnapshot.of(
                new Catalog(List.of(dataset), List.of(series)),
                Instant.parse("2026-06-14T08:00:00Z"),
                "test"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Duplicate catalog identifier");
    }

    @Test
    void sortsVisibleEntriesByModifiedDescendingThenTitleAscending() {
        DatasetEntry datasetB = dataset("dataset-b", "B Titel", LocalDate.parse("2026-05-01"));
        DatasetEntry datasetA = dataset("dataset-a", "A Titel", LocalDate.parse("2026-05-01"));
        DatasetSeriesEntry series = series(
                "series",
                "Reihe Titel",
                issue("series-issue", LocalDate.parse("2026-06-01")));

        CatalogSnapshot snapshot = CatalogSnapshot.of(
                new Catalog(List.of(datasetB, datasetA), List.of(series)),
                Instant.parse("2026-06-14T08:00:00Z"),
                "test");

        assertThat(snapshot.visibleEntries())
                .extracting(CatalogEntry::identifier)
                .containsExactly("series", "dataset-a", "dataset-b");
    }

    private static DatasetEntry dataset(String identifier, String title, LocalDate modified) {
        return new DatasetEntry(
                identifier,
                title,
                "Beschreibung",
                OFFICE,
                OFFICE,
                List.of(THEME),
                List.of(title),
                modified,
                AccessLevel.OPEN,
                List.of(new DistributionLink(URI.create("https://example.com/" + identifier + ".csv"), DistributionFormat.CSV)));
    }

    private static DatasetSeriesEntry series(String identifier, String title, DatasetIssueEntry issue) {
        return new DatasetSeriesEntry(
                identifier,
                title,
                "Beschreibung",
                OFFICE,
                OFFICE,
                List.of(THEME),
                List.of(title),
                AccessLevel.OPEN,
                List.of(issue));
    }

    private static DatasetIssueEntry issue(String identifier, LocalDate modified) {
        return new DatasetIssueEntry(
                identifier,
                "Ausgabe",
                "Beschreibung",
                OFFICE,
                OFFICE,
                List.of(THEME),
                List.of("Ausgabe"),
                modified,
                AccessLevel.OPEN,
                List.of(new DistributionLink(URI.create("https://example.com/" + identifier + ".csv"), DistributionFormat.CSV)),
                "2026",
                true);
    }
}
