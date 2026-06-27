package ch.so.agi.datenportal.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import ch.so.agi.datenportal.catalog.domain.AccessLevel;
import ch.so.agi.datenportal.catalog.domain.CatalogEntryMetadata;
import ch.so.agi.datenportal.catalog.domain.DatasetAttribute;
import ch.so.agi.datenportal.catalog.domain.DatasetEntry;
import ch.so.agi.datenportal.catalog.domain.DatasetIssueEntry;
import ch.so.agi.datenportal.catalog.domain.DatasetSeriesEntry;
import ch.so.agi.datenportal.catalog.domain.DistributionFormat;
import ch.so.agi.datenportal.catalog.domain.DistributionLink;
import ch.so.agi.datenportal.catalog.domain.Office;
import ch.so.agi.datenportal.catalog.domain.Theme;
import ch.so.agi.datenportal.config.WebComponentsProperties;
import ch.so.agi.datenportal.support.JsonAttributeEncoder;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.boot.info.BuildProperties;
import org.springframework.core.io.DefaultResourceLoader;

class DetailPageVmFactoryTest {

    private final DetailPageVmFactory factory = new DetailPageVmFactory(
            new PageChromeFactory(
                    new HeaderViewModelFactory(new JsonAttributeEncoder()),
                    new BreadcrumbFactory(),
                    new WebAssetsVmFactory(
                            new WebComponentsProperties(true, "0.1.10", null, false),
                            new DefaultResourceLoader()),
                    new FooterViewModelFactory(new StaticListableBeanFactory().getBeanProvider(BuildProperties.class))),
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
    void datasetOverviewContainsTranslatedCoreMetadataAndLicense() {
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
                metadataForOverview(),
                List.of(distribution(DistributionFormat.CSV)));

        var page = factory.dataset(dataset);

        assertThat(page.overview().title()).isEqualTo("Übersicht");
        assertThat(page.overview().items())
                .extracting(item -> item.label(), item -> item.value())
                .containsExactly(
                        tuple("Identifier", "dataset"),
                        tuple("Typ", "Datensatz"),
                        tuple("Zugriff", "Open Data"),
                        tuple("Herkunft", "Kanton"),
                        tuple("Publikationsstatus", "veröffentlicht"),
                        tuple("Publiziert", "01.05.2026"),
                        tuple("Aktualisiert", "19.05.2026"),
                        tuple("Aktualisierungsintervall", "bei Bedarf"),
                        tuple("Lizenz", "https://creativecommons.org/licenses/by/4.0/"));
        assertThat(page.overview().items().getLast().href())
                .contains("https://creativecommons.org/licenses/by/4.0/");
    }

    @Test
    void datasetFeaturesReflectAccessAttributesAndModel() {
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
                metadataWithStructure(true, true),
                List.of(distribution(DistributionFormat.CSV)));

        var page = factory.dataset(dataset);

        assertThat(page.features())
                .extracting("label", "available")
                .containsExactly(
                        tuple("Open Data", true),
                        tuple("Attribute beschrieben", true),
                        tuple("Daten validiert", true));
    }

    @Test
    void datasetFeaturesExposeUnavailableStates() {
        DatasetEntry dataset = new DatasetEntry(
                "dataset",
                "Datensatz",
                "Beschreibung",
                office(),
                office(),
                List.of(theme()),
                List.of(),
                LocalDate.parse("2026-05-19"),
                AccessLevel.PUBLIC_WITH_CONDITIONS,
                metadataWithStructure(false, false),
                List.of(distribution(DistributionFormat.CSV)));

        var page = factory.dataset(dataset);

        assertThat(page.features())
                .extracting("label", "available")
                .containsExactly(
                        tuple("Open Data", false),
                        tuple("Attribute beschrieben", false),
                        tuple("Daten validiert", false));
    }

    @Test
    void nonOpenDatasetCarriesAccessLabelAndSuppressesResourceLinksInMetadata() {
        DatasetEntry dataset = new DatasetEntry(
                "dataset",
                "Datensatz",
                "Beschreibung",
                office(),
                office(),
                List.of(theme()),
                List.of(),
                LocalDate.parse("2026-05-19"),
                AccessLevel.PUBLIC_WITH_CONDITIONS,
                CatalogEntryMetadata.empty(),
                List.of(distribution(DistributionFormat.CSV), distribution(DistributionFormat.XLSX)));

        var page = factory.dataset(dataset);

        assertThat(page.accessState().openData()).isFalse();
        assertThat(page.accessState().label()).isEqualTo("Öffentlich mit Bedingungen");
        assertThat(page.downloads().accessState().openData()).isFalse();
        assertThat(page.downloads().lead()).contains("keine Open-Data-Downloads");
        assertThat(page.metadataSections())
                .filteredOn(section -> section.id().equals("resources"))
                .singleElement()
                .satisfies(section -> assertThat(section.items())
                        .extracting(item -> item.label())
                        .contains("Formate")
                        .doesNotContain("CSV", "XLSX"));
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

    @Test
    void seriesAndIssuesCarrySeparateAccessStatesForBadgesAndDownloads() {
        DatasetIssueEntry oldIssue = issue(
                "series-2025",
                "2025",
                false,
                LocalDate.parse("2025-12-31"),
                AccessLevel.PUBLIC_WITH_CONDITIONS);
        DatasetIssueEntry currentIssue = issue(
                "series-2026",
                "2026",
                true,
                LocalDate.parse("2026-12-31"),
                AccessLevel.PUBLIC_WITH_CONDITIONS);
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

        var page = factory.series(series);

        assertThat(page.accessState().openData()).isTrue();
        assertThat(page.currentIssueDownloads().accessState().openData()).isFalse();
        assertThat(page.currentIssueDownloads().lead()).contains("keine Open-Data-Downloads");
        assertThat(page.issues().issues())
                .allSatisfy(issue -> {
                    assertThat(issue.accessState().openData()).isFalse();
                    assertThat(issue.accessState().label()).isEqualTo("Öffentlich mit Bedingungen");
                });
        assertThat(page.metadataSections())
                .filteredOn(section -> section.id().equals("resources"))
                .singleElement()
                .satisfies(section -> assertThat(section.items())
                        .extracting(item -> item.label())
                        .contains("Formate")
                        .doesNotContain("CSV", "XLSX", "Parquet"));
    }

    private static DatasetIssueEntry issue(String identifier, String label, boolean current, LocalDate modified) {
        return issue(identifier, label, current, modified, AccessLevel.OPEN);
    }

    private static DatasetIssueEntry issue(
            String identifier,
            String label,
            boolean current,
            LocalDate modified,
            AccessLevel accessLevel) {
        return new DatasetIssueEntry(
                identifier,
                "Ausgabe " + label,
                "Beschreibung " + label,
                office(),
                office(),
                List.of(theme()),
                List.of(label),
                modified,
                accessLevel,
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

    private static CatalogEntryMetadata metadataWithStructure(boolean attributes, boolean model) {
        return new CatalogEntryMetadata(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                attributes
                        ? List.of(new DatasetAttribute(
                                "identifier",
                                "TEXT",
                                Optional.of("Fachlicher Identifikator"),
                                Optional.empty(),
                                true))
                        : List.of(),
                model ? Optional.of("SO_AGI_TestModel") : Optional.empty());
    }

    private static CatalogEntryMetadata metadataForOverview() {
        return new CatalogEntryMetadata(
                Optional.empty(),
                Optional.of(LocalDate.parse("2026-05-01")),
                Optional.of(URI.create("https://creativecommons.org/licenses/by/4.0/")),
                Optional.empty(),
                Optional.of("asNeeded"),
                Optional.of("published"),
                Optional.of("cantonal"),
                Optional.empty(),
                List.of(),
                Optional.empty());
    }

    private static Office office() {
        return new Office("agi", "Amt für Geoinformation", Optional.of("AGI"));
    }

    private static Theme theme() {
        return new Theme("geografie", "Geografie");
    }
}
