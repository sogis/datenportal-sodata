package ch.so.agi.datenportal.catalog.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class DatasetSeriesEntryTest {

    private static final Office OFFICE = new Office("office", "Amt für Tests", Optional.of("TEST"));
    private static final Theme THEME = new Theme("theme", "Testthema");

    @Test
    void currentIssueReturnsExplicitCurrentIssue() {
        DatasetIssueEntry explicitCurrent = issue(
                "series-2025",
                LocalDate.parse("2026-04-01"),
                true,
                "2025",
                "https://example.com/series-2025");
        DatasetIssueEntry newerNonCurrent = issue(
                "series-2026",
                LocalDate.parse("2026-05-01"),
                false,
                "2026",
                "https://example.com/series-2026");

        DatasetSeriesEntry series = series(explicitCurrent, newerNonCurrent);

        assertThat(series.currentIssue())
                .get()
                .extracting(DatasetIssueEntry::identifier)
                .isEqualTo("series-2025");
    }

    @Test
    void currentIssueFallsBackToNewestModifiedIssueWhenNoneMarked() {
        DatasetSeriesEntry series = series(
                issue("series-2024", LocalDate.parse("2025-05-01"), false, "2024", "https://example.com/2024"),
                issue("series-2025", LocalDate.parse("2026-05-01"), false, "2025", "https://example.com/2025"));

        assertThat(series.currentIssue())
                .get()
                .extracting(DatasetIssueEntry::identifier)
                .isEqualTo("series-2025");
    }

    @Test
    void currentIssueChoosesNewestModifiedWhenMultipleIssuesAreMarkedCurrent() {
        DatasetSeriesEntry series = series(
                issue("series-2025", LocalDate.parse("2026-04-01"), true, "2025", "https://example.com/2025"),
                issue("series-2026", LocalDate.parse("2026-05-01"), true, "2026", "https://example.com/2026"));

        assertThat(series.currentIssue())
                .get()
                .extracting(DatasetIssueEntry::identifier)
                .isEqualTo("series-2026");
    }

    @Test
    void distributionsForListingUseCurrentIssue() {
        DatasetSeriesEntry series = series(
                issue("series-2025", LocalDate.parse("2026-04-01"), false, "2025", "https://example.com/2025"),
                issue("series-2026", LocalDate.parse("2026-05-01"), true, "2026", "https://example.com/2026"));

        assertThat(series.distributionsForListing())
                .extracting(link -> link.preferredHref().toString())
                .containsExactly(
                        "https://example.com/2026/download.csv",
                        "https://example.com/2026/download.xlsx",
                        "https://example.com/2026/download.parquet");
    }

    private static DatasetSeriesEntry series(DatasetIssueEntry... issues) {
        return new DatasetSeriesEntry(
                "series-root",
                "Testreihe",
                "Beschreibung",
                OFFICE,
                OFFICE,
                List.of(THEME),
                List.of("Test"),
                AccessLevel.OPEN,
                List.of(issues));
    }

    private static DatasetIssueEntry issue(
            String identifier,
            LocalDate modified,
            boolean currentIssue,
            String issueLabel,
            String baseUrl) {
        return new DatasetIssueEntry(
                identifier,
                "Ausgabe " + issueLabel,
                "Beschreibung " + issueLabel,
                OFFICE,
                OFFICE,
                List.of(THEME),
                List.of(issueLabel),
                modified,
                AccessLevel.OPEN,
                List.of(
                        new DistributionLink(URI.create(baseUrl + "/access/csv"), URI.create(baseUrl + "/download.csv"), DistributionFormat.CSV),
                        new DistributionLink(URI.create(baseUrl + "/access/xlsx"), URI.create(baseUrl + "/download.xlsx"), DistributionFormat.XLSX),
                        new DistributionLink(URI.create(baseUrl + "/access/parquet"), URI.create(baseUrl + "/download.parquet"), DistributionFormat.PARQUET)),
                issueLabel,
                currentIssue);
    }
}
