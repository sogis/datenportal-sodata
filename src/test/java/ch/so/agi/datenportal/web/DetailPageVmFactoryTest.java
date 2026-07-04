package ch.so.agi.datenportal.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import ch.so.agi.datenportal.catalog.domain.AccessLevel;
import ch.so.agi.datenportal.catalog.domain.CatalogEntryMetadata;
import ch.so.agi.datenportal.catalog.domain.ContactPoint;
import ch.so.agi.datenportal.catalog.domain.DatasetAttribute;
import ch.so.agi.datenportal.catalog.domain.DatasetEntry;
import ch.so.agi.datenportal.catalog.domain.DatasetIssueEntry;
import ch.so.agi.datenportal.catalog.domain.DatasetSeriesEntry;
import ch.so.agi.datenportal.catalog.domain.DistributionFormat;
import ch.so.agi.datenportal.catalog.domain.DistributionLink;
import ch.so.agi.datenportal.catalog.domain.Office;
import ch.so.agi.datenportal.catalog.domain.QualitySummary;
import ch.so.agi.datenportal.catalog.domain.StructureSummary;
import ch.so.agi.datenportal.catalog.domain.TemporalCoverage;
import ch.so.agi.datenportal.catalog.domain.Theme;
import ch.so.agi.datenportal.config.WebComponentsProperties;
import ch.so.agi.datenportal.support.JsonAttributeEncoder;
import java.net.URI;
import java.time.LocalDate;
import java.time.OffsetDateTime;
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
    void datasetTemporalCoverageContainsReferenceDate() {
        var page = factory.dataset(datasetWithMetadata(metadataWithTemporalCoverage(new TemporalCoverage(
                Optional.empty(),
                Optional.empty(),
                Optional.of(LocalDate.parse("2026-05-19"))))));

        assertThat(page.temporalCoverage().title()).isEqualTo("Zeitliche Abdeckung");
        assertThat(page.temporalCoverage().items())
                .extracting(item -> item.label(), item -> item.value())
                .containsExactly(tuple("Stichtag", "19.05.2026"));
    }

    @Test
    void datasetTemporalCoverageContainsClosedPeriod() {
        var page = factory.dataset(datasetWithMetadata(metadataWithTemporalCoverage(new TemporalCoverage(
                Optional.of(LocalDate.parse("2018-01-01")),
                Optional.of(LocalDate.parse("2025-12-31")),
                Optional.empty()))));

        assertThat(page.temporalCoverage().items())
                .extracting(item -> item.label(), item -> item.value())
                .containsExactly(tuple("Zeitraum", "01.01.2018 bis 31.12.2025"));
    }

    @Test
    void datasetTemporalCoverageContainsOpenPeriodStart() {
        var page = factory.dataset(datasetWithMetadata(metadataWithTemporalCoverage(new TemporalCoverage(
                Optional.of(LocalDate.parse("2018-01-01")),
                Optional.empty(),
                Optional.empty()))));

        assertThat(page.temporalCoverage().items())
                .extracting(item -> item.label(), item -> item.value())
                .containsExactly(tuple("Zeitraum", "ab 01.01.2018"));
    }

    @Test
    void datasetTemporalCoverageContainsOpenPeriodEnd() {
        var page = factory.dataset(datasetWithMetadata(metadataWithTemporalCoverage(new TemporalCoverage(
                Optional.empty(),
                Optional.of(LocalDate.parse("2025-12-31")),
                Optional.empty()))));

        assertThat(page.temporalCoverage().items())
                .extracting(item -> item.label(), item -> item.value())
                .containsExactly(tuple("Zeitraum", "bis 31.12.2025"));
    }

    @Test
    void datasetTemporalCoverageIsEmptyWhenNoTemporalCoverageExists() {
        var page = factory.dataset(datasetWithMetadata(CatalogEntryMetadata.empty()));

        assertThat(page.temporalCoverage().title()).isEqualTo("Zeitliche Abdeckung");
        assertThat(page.temporalCoverage().items()).isEmpty();
    }

    @Test
    void datasetTopicsContainTranslatedThemesAndCommaSeparatedKeywords() {
        DatasetEntry dataset = new DatasetEntry(
                "dataset",
                "Datensatz",
                "Beschreibung",
                office(),
                office(),
                List.of(
                        new Theme("Bevoelkerung", "Bevölkerung"),
                        new Theme("Mobilitaet_und_Verkehr", "Mobilität und Verkehr")),
                List.of("ÖV", "Pendler", "ÖV"),
                LocalDate.parse("2026-05-19"),
                AccessLevel.OPEN,
                CatalogEntryMetadata.empty(),
                List.of(distribution(DistributionFormat.CSV)));

        var page = factory.dataset(dataset);

        assertThat(page.topics().title()).isEqualTo("Themen und Schlagworte");
        assertThat(page.topics().items())
                .extracting(item -> item.label(), item -> item.value())
                .containsExactly(
                        tuple("Thema", "Bevölkerung, Mobilität und Verkehr"),
                        tuple("Schlagworte", "ÖV, Pendler"));
    }

    @Test
    void datasetResponsibilitiesContactContainsProducerContactAndPublisherLines() {
        Office creator = new Office(
                "arp",
                "Amt für Raumplanung",
                Optional.of("ARP"),
                Optional.of(URI.create("mailto:arp@bd.so.ch")),
                Optional.of(URI.create("https://so.ch/arp/")));
        Office publisher = new Office(
                "agi",
                "Amt für Geoinformation",
                Optional.of("AGI"),
                Optional.of(URI.create("mailto:agi@bd.so.ch")),
                Optional.of(URI.create("https://so.ch/agi/")));
        CatalogEntryMetadata metadata = new CatalogEntryMetadata(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(new ContactPoint(
                        "Amt für Raumplanung",
                        Optional.of("Nutzungsplanung"),
                        Optional.of(URI.create("mailto:planung@bd.so.ch")),
                        Optional.empty(),
                        Optional.empty())),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                List.of(),
                Optional.empty());
        DatasetEntry dataset = new DatasetEntry(
                "dataset",
                "Datensatz",
                "Beschreibung",
                publisher,
                creator,
                List.of(theme()),
                List.of(),
                LocalDate.parse("2026-05-19"),
                AccessLevel.OPEN,
                metadata,
                List.of(distribution(DistributionFormat.CSV)));

        var section = factory.dataset(dataset).responsibilitiesContact();

        assertThat(section.title()).isEqualTo("Zuständigkeiten und Kontakt");
        assertThat(section.items())
                .extracting(item -> item.label())
                .containsExactly("Datenproduzent", "Kontakt", "Herausgeber");
        assertThat(section.items().get(0).lines())
                .extracting(line -> line.value(), line -> line.href())
                .containsExactly(
                        tuple("Amt für Raumplanung", Optional.empty()),
                        tuple("https://so.ch/arp/", Optional.of("https://so.ch/arp/")));
        assertThat(section.items().get(1).lines())
                .extracting(line -> line.value(), line -> line.href())
                .containsExactly(
                        tuple("Amt für Raumplanung", Optional.empty()),
                        tuple("Nutzungsplanung", Optional.empty()),
                        tuple("planung@bd.so.ch", Optional.of("mailto:planung@bd.so.ch")));
        assertThat(section.items().get(2).lines())
                .extracting(line -> line.value(), line -> line.href())
                .containsExactly(
                        tuple("Amt für Geoinformation", Optional.empty()),
                        tuple("https://so.ch/agi/", Optional.of("https://so.ch/agi/")),
                        tuple("agi@bd.so.ch", Optional.of("mailto:agi@bd.so.ch")));
    }

    @Test
    void structureQualityOriginGroupsOriginAndUsageValuesInOneCard() {
        CatalogEntryMetadata metadata = new CatalogEntryMetadata(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                List.of(),
                Optional.empty(),
                Optional.of("Fachliche Erhebung und Qualitätskontrolle"),
                Optional.of("ab 2011"),
                Optional.of("Übersichten und Kennzahlen"),
                Optional.of("Referenztabellen und Prüflisten"));

        var page = factory.datasetStructureQualityOrigin(datasetWithMetadata(metadata));

        assertThat(page.originUsage()).isPresent();
        assertThat(page.originUsage().get().title()).isEqualTo("Herkunft & Verwendung");
        assertThat(page.originUsage().get().items())
                .extracting(item -> item.label(), item -> item.value())
                .containsExactly(
                        tuple("Erhebungs- / Messmethode", "Fachliche Erhebung und Qualitätskontrolle"),
                        tuple("Hilfsdaten", "Referenztabellen und Prüflisten"),
                        tuple("Weitere Verwendungen", "Übersichten und Kennzahlen"),
                        tuple("Verfügbare Daten ab", "ab 2011"));
    }

    @Test
    void structureQualityOriginOmitsEmptyOriginUsageCard() {
        var page = factory.datasetStructureQualityOrigin(datasetWithMetadata(CatalogEntryMetadata.empty()));

        assertThat(page.originUsage()).isEmpty();
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
    void datasetDetailLinksStructureQualityPage() {
        var page = factory.dataset(datasetWithMetadata(CatalogEntryMetadata.empty()));

        assertThat(page.structureQualityOriginHref()).isEqualTo("/datasets/dataset/structure-quality-origin");
        assertThat(page.exploreHref()).isEqualTo("/datasets/dataset/explore");
    }

    @Test
    void datasetStructureQualityOriginMapsAttributesAndQualityWithModel() {
        CatalogEntryMetadata metadata = new CatalogEntryMetadata(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                List.of(
                        new DatasetAttribute(
                                "identifier",
                                "TEXT",
                                Optional.of("Fachlicher Identifikator"),
                                Optional.empty(),
                                true),
                        new DatasetAttribute(
                                "flaeche_m2",
                                "DECIMAL",
                                Optional.empty(),
                                Optional.of("m2"),
                                false)),
                Optional.of("SO_AGI_TestModel"));

        var page = factory.datasetStructureQualityOrigin(datasetWithMetadata(metadata));

        assertThat(page.title()).isEqualTo("Struktur, Qualität und Herkunft");
        assertThat(page.emptyAttributesText()).isEqualTo("Für dieses Datenthema sind keine Attribute beschrieben.");
        assertThat(page.attributes())
                .extracting(
                        attribute -> attribute.name(),
                        attribute -> attribute.dataType(),
                        attribute -> attribute.mandatoryLabel(),
                        attribute -> attribute.unit(),
                        attribute -> attribute.description())
                .containsExactly(
                        tuple("identifier", "TEXT", "Ja", "–", "Fachlicher Identifikator"),
                        tuple("flaeche_m2", "DECIMAL", "Nein", "m2", "–"));
        assertThat(page.quality().modelName()).contains("SO_AGI_TestModel");
        assertThat(page.quality().modelHref()).isEqualTo("#");
        assertThat(page.quality().validationReportName()).isEqualTo("ilivalidator.log");
        assertThat(page.quality().validationReportHref()).isEqualTo("#");
        assertThat(page.quality().missingModelMessage()).isEmpty();
    }

    @Test
    void structureQualityOriginMapsKpisWithSummaries() {
        CatalogEntryMetadata metadata = new CatalogEntryMetadata(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                List.of(),
                Optional.of("SO_AGI_TestModel"),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(new QualitySummary(
                        "success",
                        0,
                        OffsetDateTime.parse("2026-06-24T02:28:00+02:00"),
                        URI.create("https://data.so.ch/validation/test/ilivalidator.log"))),
                Optional.of(new StructureSummary(26349, 6)));

        var page = factory.datasetStructureQualityOrigin(datasetWithMetadata(metadata));

        assertThat(page.kpis())
                .extracting(kpi -> kpi.title(), kpi -> kpi.value(), kpi -> kpi.detail(), kpi -> kpi.iconName())
                .containsExactly(
                        tuple("Validierung", "Erfolgreich", Optional.of("0 Fehler"), "shield-check"),
                        tuple("Objekte", "26349", Optional.empty(), "database"),
                        tuple("Attribute", "6", Optional.empty(), "table"));
    }

    @Test
    void structureQualityOriginShowsQualityMessageWhenModelIsMissing() {
        var page = factory.datasetStructureQualityOrigin(datasetWithMetadata(CatalogEntryMetadata.empty()));

        assertThat(page.attributes()).isEmpty();
        assertThat(page.kpis())
                .extracting(kpi -> kpi.title(), kpi -> kpi.value(), kpi -> kpi.detail(), kpi -> kpi.iconName())
                .containsExactly(
                        tuple("Validierung", "Nicht prüfbar", Optional.of("Kein Datenmodell"), "shield-check"),
                        tuple("Objekte", "–", Optional.empty(), "database"),
                        tuple("Attribute", "–", Optional.empty(), "table"));
        assertThat(page.quality().modelName()).isEmpty();
        assertThat(page.quality().missingModelMessage())
                .contains("Für dieses Datenthema ist kein Datenmodell hinterlegt. Ohne Datenmodell kann die Struktur nicht automatisiert geprüft oder validiert werden.");
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
    void issuePageBuildsDatasetStyleCardsFromConcreteIssueMetadata() {
        DatasetIssueEntry oldIssue = issue(
                "series-2025",
                "2025",
                false,
                LocalDate.parse("2025-12-31"),
                AccessLevel.OPEN,
                metadataWithIssued(LocalDate.parse("2025-02-03")));
        CatalogEntryMetadata issueMetadata = new CatalogEntryMetadata(
                Optional.empty(),
                Optional.of(LocalDate.parse("2026-01-15")),
                Optional.of(URI.create("https://creativecommons.org/licenses/by/4.0/")),
                Optional.of(new ContactPoint(
                        "Ausgabe Kontakt",
                        Optional.of("Zeitreihen-Team"),
                        Optional.of(URI.create("mailto:zeitreihe@example.test")),
                        Optional.empty(),
                        Optional.empty())),
                Optional.of("annually"),
                Optional.of("published"),
                Optional.of("cantonal"),
                Optional.of(new TemporalCoverage(
                        Optional.of(LocalDate.parse("2026-01-01")),
                        Optional.of(LocalDate.parse("2026-12-31")),
                        Optional.empty())),
                List.of(new DatasetAttribute(
                        "bfs_nr",
                        "INTEGER",
                        Optional.of("Gemeindenummer"),
                        Optional.empty(),
                        true)),
                Optional.of("SO_AGI_IssueModel"),
                Optional.of("Ausgabenspezifische Erhebung"),
                Optional.of("ab 2026"),
                Optional.of("Jahresvergleich"),
                Optional.of("Gemeindeliste"));
        DatasetIssueEntry currentIssue = issue(
                "series-2026",
                "2026",
                true,
                LocalDate.parse("2026-12-31"),
                AccessLevel.OPEN,
                issueMetadata);
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

        assertThat(page.currentIssue()).isTrue();
        assertThat(page.features())
                .extracting("label", "available")
                .containsExactly(
                        tuple("Open Data", true),
                        tuple("Attribute beschrieben", true),
                        tuple("Daten validiert", true));
        assertThat(page.overview().items())
                .extracting(item -> item.label(), item -> item.value())
                .contains(
                        tuple("Identifier", "series-2026"),
                        tuple("Typ", "Ausgabe"),
                        tuple("Publiziert", "15.01.2026"),
                        tuple("Aktualisiert", "31.12.2026"));
        assertThat(page.temporalCoverage().items())
                .extracting(item -> item.label(), item -> item.value())
                .containsExactly(tuple("Zeitraum", "01.01.2026 bis 31.12.2026"));
        assertThat(page.topics().items())
                .extracting(item -> item.label(), item -> item.value())
                .containsExactly(
                        tuple("Thema", "Geografie"),
                        tuple("Schlagworte", "2026"));
        assertThat(page.responsibilitiesContact().items().get(1).lines())
                .extracting(line -> line.value(), line -> line.href())
                .containsExactly(
                        tuple("Ausgabe Kontakt", Optional.empty()),
                        tuple("Zeitreihen-Team", Optional.empty()),
                        tuple("zeitreihe@example.test", Optional.of("mailto:zeitreihe@example.test")));
        assertThat(page.structureQualityOriginHref()).isEqualTo("/series/series/issues/current/structure-quality-origin");
        assertThat(page.exploreHref()).isEqualTo("/series/series/issues/current/explore");
        assertThat(page.relatedIssues().issues())
                .extracting(
                        issue -> issue.issueLabel(),
                        issue -> issue.title(),
                        issue -> issue.publicationDateLabel(),
                        issue -> issue.detailHref(),
                        issue -> issue.current())
                .containsExactly(tuple("2025", "Ausgabe 2025", "03.02.2025", "/series/series/issues/series-2025", false));
    }

    @Test
    void historicalIssueDetailLinksConcreteStructureQualityPage() {
        DatasetIssueEntry historicalIssue = issue("series-2025", "foo 2025", false, LocalDate.parse("2025-12-31"));
        DatasetIssueEntry currentIssue = issue("series-2026", "2026", true, LocalDate.parse("2026-12-31"));
        DatasetIssueEntry olderIssue = issue("series-2024", "foo 2024", false, LocalDate.parse("2024-12-31"));
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
                List.of(historicalIssue, currentIssue, olderIssue));

        var page = factory.issue(series, historicalIssue);

        assertThat(page.currentIssue()).isFalse();
        assertThat(page.structureQualityOriginHref()).isEqualTo("/series/series/issues/series-2025/structure-quality-origin");
        assertThat(page.exploreHref()).isEqualTo("/series/series/issues/series-2025/explore");
        assertThat(page.relatedIssues().issues())
                .extracting(issue -> issue.issueLabel(), issue -> issue.detailHref(), issue -> issue.current())
                .containsExactly(
                        tuple("2026", "/series/series/issues/current", true),
                        tuple("foo 2024", "/series/series/issues/series-2024", false));
    }

    @Test
    void singleIssueSeriesCarriesEmptyRelatedIssues() {
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
                List.of(currentIssue));

        var page = factory.issue(series, currentIssue);

        assertThat(page.currentIssue()).isTrue();
        assertThat(page.relatedIssues().issues()).isEmpty();
    }

    @Test
    void issueStructureQualityOriginUsesConcreteIssueMetadata() {
        CatalogEntryMetadata metadata = new CatalogEntryMetadata(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                List.of(new DatasetAttribute(
                        "bfs_nr",
                        "INTEGER",
                        Optional.of("BFS-Gemeindenummer"),
                        Optional.empty(),
                        true)),
                Optional.of("SO_AGI_IssueModel"));
        DatasetIssueEntry issue = issue(
                "series-2026",
                "2026",
                true,
                LocalDate.parse("2026-12-31"),
                AccessLevel.OPEN,
                metadata);
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
                List.of(issue));

        var page = factory.issueStructureQualityOrigin(series, issue);

        assertThat(page.attributes())
                .extracting(
                        attribute -> attribute.name(),
                        attribute -> attribute.dataType(),
                        attribute -> attribute.mandatoryLabel(),
                        attribute -> attribute.unit(),
                        attribute -> attribute.description())
                .containsExactly(tuple("bfs_nr", "INTEGER", "Ja", "–", "BFS-Gemeindenummer"));
        assertThat(page.quality().modelName()).contains("SO_AGI_IssueModel");
    }

    @Test
    void seriesIssuesPlaceCurrentIssueFirstAndSortHistoricalIssuesDescendingByTitle() {
        DatasetIssueEntry historical2023 = issue("series-2023", "foo 2023", false, LocalDate.parse("2026-01-01"));
        DatasetIssueEntry historical2025 = issue("series-2025", "foo 2025", false, LocalDate.parse("2024-01-01"));
        DatasetIssueEntry current2024 = issue("series-2024", "foo 2024", true, LocalDate.parse("2025-01-01"));
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
                List.of(historical2023, historical2025, current2024));

        var page = factory.series(series);

        assertThat(page.issues().issues())
                .extracting(issue -> issue.issueLabel())
                .containsExactly("foo 2024", "foo 2025", "foo 2023");
        assertThat(page.issues().issues().getFirst().current()).isTrue();
        assertThat(page.issues().issues().getFirst().detailHref()).isEqualTo("/series/series/issues/current");
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
        assertThat(page.issues().issues())
                .allSatisfy(issue -> {
                    assertThat(issue.accessState().openData()).isFalse();
                    assertThat(issue.accessState().label()).isEqualTo("Öffentlich mit Bedingungen");
                });
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
        return issue(identifier, label, current, modified, accessLevel, CatalogEntryMetadata.empty());
    }

    private static DatasetIssueEntry issue(
            String identifier,
            String label,
            boolean current,
            LocalDate modified,
            AccessLevel accessLevel,
            CatalogEntryMetadata metadata) {
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
                metadata,
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

    private static CatalogEntryMetadata metadataWithIssued(LocalDate issued) {
        return new CatalogEntryMetadata(
                Optional.empty(),
                Optional.of(issued),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                List.of(),
                Optional.empty());
    }

    private static DatasetEntry datasetWithMetadata(CatalogEntryMetadata metadata) {
        return new DatasetEntry(
                "dataset",
                "Datensatz",
                "Beschreibung",
                office(),
                office(),
                List.of(theme()),
                List.of(),
                LocalDate.parse("2026-05-19"),
                AccessLevel.OPEN,
                metadata,
                List.of(distribution(DistributionFormat.CSV)));
    }

    private static CatalogEntryMetadata metadataWithTemporalCoverage(TemporalCoverage temporalCoverage) {
        return new CatalogEntryMetadata(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(temporalCoverage),
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
