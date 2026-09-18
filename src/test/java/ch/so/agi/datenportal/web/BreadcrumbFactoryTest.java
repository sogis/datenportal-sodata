package ch.so.agi.datenportal.web;

import static org.assertj.core.api.Assertions.assertThat;

import ch.so.agi.datenportal.catalog.domain.AccessLevel;
import ch.so.agi.datenportal.catalog.domain.CatalogEntryMetadata;
import ch.so.agi.datenportal.catalog.domain.DatasetEntry;
import ch.so.agi.datenportal.catalog.domain.DatasetIssueEntry;
import ch.so.agi.datenportal.catalog.domain.DatasetSeriesEntry;
import ch.so.agi.datenportal.catalog.domain.Office;
import ch.so.agi.datenportal.catalog.domain.Theme;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class BreadcrumbFactoryTest {

    private final BreadcrumbFactory factory = new BreadcrumbFactory();

    @Test
    void catalogBreadcrumbMarksCatalogAsCurrent() {
        var breadcrumb = factory.catalog();

        assertThat(breadcrumb.items()).extracting("label")
                .containsExactly("so.ch", "Datenportal", "Daten und Statistiken");
        assertThat(breadcrumb.items().getLast().currentPage()).isTrue();
        assertThat(breadcrumb.items().getLast().href()).isEmpty();
    }

    @Test
    void datasetBreadcrumbAddsDatasetTitle() {
        var breadcrumb = factory.datasetDetail(dataset("dataset-1", "Bauinventar"));

        assertThat(breadcrumb.items()).extracting("label")
                .containsExactly("so.ch", "Datenportal", "Daten und Statistiken", "Bauinventar");
        assertThat(breadcrumb.items().get(2).href()).contains("/datasets");
        assertThat(breadcrumb.items().getLast().currentPage()).isTrue();
    }

    @Test
    void issueBreadcrumbAddsSeriesAndIssue() {
        DatasetIssueEntry issue = issue("series-2026", "Ausgabe 2026");
        DatasetSeriesEntry series = series("series", "Datenreihe", issue);

        var breadcrumb = factory.issueDetail(series, issue);

        assertThat(breadcrumb.items()).extracting("label")
                .containsExactly("so.ch", "Datenportal", "Daten und Statistiken", "Datenreihe", "2026");
        assertThat(breadcrumb.items().get(3).href()).contains("/series/series");
        assertThat(breadcrumb.items().getLast().currentPage()).isTrue();
    }

    @Test
    void seriesDetailBreadcrumbKeepsFullSeriesTitle() {
        DatasetIssueEntry issue = issue("series-2026", "Ausgabe 2026");
        DatasetSeriesEntry series = series("series", "Datenreihe", issue);

        var breadcrumb = factory.seriesDetail(series);

        assertThat(breadcrumb.items()).extracting("label")
                .containsExactly("so.ch", "Datenportal", "Daten und Statistiken", "Datenreihe");
        assertThat(breadcrumb.items().getLast().currentPage()).isTrue();
    }

    @Test
    void datasetStructureQualityOriginBreadcrumbLinksBackToDataset() {
        var breadcrumb = factory.datasetStructureQualityOrigin(dataset("dataset-1", "Bauinventar"));

        assertThat(breadcrumb.items()).extracting("label")
                .containsExactly(
                        "so.ch",
                        "Datenportal",
                        "Daten und Statistiken",
                        "Bauinventar",
                        "Struktur, Qualität und Herkunft");
        assertThat(breadcrumb.items().get(3).href()).contains("/datasets/dataset-1");
        assertThat(breadcrumb.items().getLast().currentPage()).isTrue();
    }

    @Test
    void issueStructureQualityOriginBreadcrumbLinksBackToCurrentIssue() {
        DatasetIssueEntry issue = issue("series-2026", "Ausgabe 2026");
        DatasetSeriesEntry series = series("series", "Datenreihe", issue);

        var breadcrumb = factory.issueStructureQualityOrigin(series, issue);

        assertThat(breadcrumb.items()).extracting("label")
                .containsExactly(
                        "so.ch",
                        "Datenportal",
                        "Daten und Statistiken",
                        "Datenreihe",
                        "2026",
                        "Struktur, Qualität und Herkunft");
        assertThat(breadcrumb.items().get(3).href()).contains("/series/series");
        assertThat(breadcrumb.items().get(4).href()).contains("/series/series/issues/current");
        assertThat(breadcrumb.items().getLast().currentPage()).isTrue();
    }

    @Test
    void issueUsageBreadcrumbUsesIssueLabel() {
        DatasetIssueEntry issue = issue("series-2026", "Ausgabe 2026");
        DatasetSeriesEntry series = series("series", "Datenreihe", issue);

        var breadcrumb = factory.issueUsage(series, issue);

        assertThat(breadcrumb.items()).extracting("label")
                .containsExactly("so.ch", "Datenportal", "Daten und Statistiken", "Datenreihe", "2026", "Daten verwenden");
        assertThat(breadcrumb.items().get(4).href()).contains("/series/series/issues/current");
        assertThat(breadcrumb.items().getLast().currentPage()).isTrue();
    }

    @Test
    void issueExploreBreadcrumbLinksBackToCurrentIssue() {
        DatasetIssueEntry issue = issue("series-2026", "Ausgabe 2026");
        DatasetSeriesEntry series = series("series", "Datenreihe", issue);

        var breadcrumb = factory.issueExplore(series, issue);

        assertThat(breadcrumb.items()).extracting("label")
                .containsExactly(
                        "so.ch",
                        "Datenportal",
                        "Daten und Statistiken",
                        "Datenreihe",
                        "2026",
                        "Erkunden");
        assertThat(breadcrumb.items().get(3).href()).contains("/series/series");
        assertThat(breadcrumb.items().get(4).href()).contains("/series/series/issues/current");
        assertThat(breadcrumb.items().getLast().currentPage()).isTrue();
    }

    private static DatasetEntry dataset(String identifier, String title) {
        return new DatasetEntry(
                identifier,
                title,
                "Beschreibung",
                office(),
                office(),
                List.of(theme()),
                List.of(),
                LocalDate.parse("2026-05-19"),
                AccessLevel.OPEN,
                CatalogEntryMetadata.empty(),
                List.of());
    }

    private static DatasetSeriesEntry series(String identifier, String title, DatasetIssueEntry issue) {
        return new DatasetSeriesEntry(
                identifier,
                title,
                "Beschreibung",
                office(),
                office(),
                List.of(theme()),
                List.of(),
                AccessLevel.OPEN,
                CatalogEntryMetadata.empty(),
                List.of(issue));
    }

    private static DatasetIssueEntry issue(String identifier, String title) {
        return new DatasetIssueEntry(
                identifier,
                title,
                "Beschreibung",
                office(),
                office(),
                List.of(theme()),
                List.of(),
                LocalDate.parse("2026-05-19"),
                AccessLevel.OPEN,
                CatalogEntryMetadata.empty(),
                List.of(),
                "2026",
                true);
    }

    private static Office office() {
        return new Office("agi", "Amt für Geoinformation", Optional.of("AGI"));
    }

    private static Theme theme() {
        return new Theme("geografie", "Geografie");
    }
}
