package ch.so.agi.datenportal.web;

import static org.assertj.core.api.Assertions.assertThat;

import ch.so.agi.datenportal.config.WebComponentsProperties;
import ch.so.agi.datenportal.catalog.domain.AccessLevel;
import ch.so.agi.datenportal.catalog.domain.CatalogEntryMetadata;
import ch.so.agi.datenportal.catalog.domain.DatasetEntry;
import ch.so.agi.datenportal.catalog.domain.DatasetIssueEntry;
import ch.so.agi.datenportal.catalog.domain.DatasetSeriesEntry;
import ch.so.agi.datenportal.catalog.domain.DistributionFormat;
import ch.so.agi.datenportal.catalog.domain.DistributionLink;
import ch.so.agi.datenportal.catalog.domain.Office;
import ch.so.agi.datenportal.catalog.domain.Theme;
import ch.so.agi.datenportal.support.JsonAttributeEncoder;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;

class DetailPageVmFactoryTest {

    private final DetailPageVmFactory factory = new DetailPageVmFactory(
            new PageChromeFactory(
                    new HeaderViewModelFactory(new JsonAttributeEncoder()),
                    new BreadcrumbFactory(),
                    new WebAssetsVmFactory(
                            new WebComponentsProperties(true, "0.1.9", null, false),
                            new DefaultResourceLoader())),
            new CatalogUrlFactory());

    @Test
    void datasetMetadataSectionsContainOnlyAvailableValues() {
        DatasetEntry dataset = new DatasetEntry(
                "dataset",
                "Datensatz",
                "Beschreibung",
                office(),
                office(),
                List.of(theme()),
                List.of("Keyword"),
                LocalDate.parse("2026-05-19"),
                AccessLevel.OPEN,
                CatalogEntryMetadata.empty(),
                List.of(distribution(DistributionFormat.CSV)));

        var page = factory.dataset(dataset);

        assertThat(page.metadataSections())
                .flatExtracting(section -> section.items())
                .extracting(item -> item.label())
                .contains("Identifier", "Typ", "Zugriff", "Aktualisiert", "Thema", "Keywords", "Fachstelle / Amt")
                .doesNotContain("Lizenz", "Landing Page", "Kontakt", "Zeitlicher Bezug");
    }

    @Test
    void datasetDownloadsAreOrderedByPrimaryFormatsAndThenOther() {
        DatasetEntry dataset = new DatasetEntry(
                "dataset",
                "Datensatz",
                "Beschreibung",
                office(),
                office(),
                List.of(theme()),
                List.of(),
                LocalDate.parse("2026-05-19"),
                AccessLevel.OPEN,
                CatalogEntryMetadata.empty(),
                List.of(
                        distribution(DistributionFormat.OTHER),
                        distribution(DistributionFormat.PARQUET),
                        distribution(DistributionFormat.CSV),
                        distribution(DistributionFormat.XLSX)));

        var page = factory.dataset(dataset);

        assertThat(page.downloads().links())
                .extracting(link -> link.label())
                .containsExactly("CSV", "XLSX", "Parquet", "Weitere");
    }

    @Test
    void issuePageMarksRelatedCurrentIssue() {
        DatasetIssueEntry oldIssue = issue("series-2025", "2025", false, LocalDate.parse("2025-12-31"));
        DatasetIssueEntry currentIssue = issue("series-2026", "2026", true, LocalDate.parse("2026-12-31"));
        DatasetSeriesEntry series = new DatasetSeriesEntry(
                "series",
                "Datenreihe",
                "Beschreibung",
                office(),
                office(),
                List.of(theme()),
                List.of(),
                AccessLevel.OPEN,
                CatalogEntryMetadata.empty(),
                List.of(oldIssue, currentIssue));

        var page = factory.issue(series, currentIssue);

        assertThat(page.relatedIssues().issues())
                .filteredOn(issue -> issue.current())
                .singleElement()
                .satisfies(issue -> {
                    assertThat(issue.issueLabel()).isEqualTo("2026");
                    assertThat(issue.detailHref()).isEqualTo("/series/series/issues/current");
                });
    }

    private static DatasetIssueEntry issue(String identifier, String label, boolean current, LocalDate modified) {
        return new DatasetIssueEntry(
                identifier,
                "Ausgabe " + label,
                "Beschreibung " + label,
                office(),
                office(),
                List.of(theme()),
                List.of(label),
                modified,
                AccessLevel.OPEN,
                CatalogEntryMetadata.empty(),
                List.of(distribution(DistributionFormat.CSV)),
                label,
                current);
    }

    private static DistributionLink distribution(DistributionFormat format) {
        String suffix = format.name().toLowerCase();
        return new DistributionLink(
                URI.create("https://data.so.ch/access/" + suffix),
                URI.create("https://data.so.ch/download." + suffix),
                format);
    }

    private static Office office() {
        return new Office("agi", "Amt für Geoinformation", Optional.of("AGI"));
    }

    private static Theme theme() {
        return new Theme("geografie", "Geografie");
    }
}
