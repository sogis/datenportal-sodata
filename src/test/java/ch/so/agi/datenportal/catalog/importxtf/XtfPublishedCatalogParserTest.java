package ch.so.agi.datenportal.catalog.importxtf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ch.so.agi.datenportal.catalog.domain.AccessLevel;
import ch.so.agi.datenportal.catalog.domain.Catalog;
import ch.so.agi.datenportal.catalog.domain.DatasetEntry;
import ch.so.agi.datenportal.catalog.domain.DatasetSeriesEntry;
import ch.so.agi.datenportal.catalog.domain.DistributionFormat;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.time.LocalDate;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

class XtfPublishedCatalogParserTest {

    private static final PublishedCatalogParser PARSER = new XtfPublishedCatalogParser();

    @Test
    void parsesFullFixtureWithExpectedCounts() throws Exception {
        Catalog catalog = parseFixture();

        assertThat(catalog.datasetCount()).isEqualTo(27);
        assertThat(catalog.seriesCount()).isEqualTo(35);
        assertThat(catalog.issueCount()).isEqualTo(111);
        assertThat(catalog.topLevelEntries()).hasSize(62);
    }

    @Test
    void mapsDatasetsThemesOfficesKeywordsFormatsAndStructureMetadata() throws Exception {
        DatasetEntry dataset = parseFixture().datasets().stream()
                .filter(entry -> entry.identifier().equals("ch.so.wasserqualitaet_grundwasser"))
                .findFirst()
                .orElseThrow();

        assertThat(dataset.title()).isEqualTo("Wasserqualität Grundwasser");
        assertThat(dataset.creator().identifier()).isEqualTo("afu");
        assertThat(dataset.creator().email()).contains(URI.create("mailto:afu@bd.so.ch"));
        assertThat(dataset.creator().officeAtWeb())
                .contains(URI.create("https://afu.so.ch"));
        assertThat(dataset.publisher().identifier()).isEqualTo("agi");
        assertThat(dataset.publisher().email()).contains(URI.create("mailto:agi@bd.so.ch"));
        assertThat(dataset.publisher().officeAtWeb())
                .contains(URI.create("https://agi.so.ch"));
        assertThat(dataset.accessLevel()).isEqualTo(AccessLevel.OPEN);
        assertThat(dataset.themes())
                .extracting(theme -> theme.identifier())
                .containsExactly("Raum_und_Umwelt");
        assertThat(dataset.themes())
                .extracting(theme -> theme.displayName())
                .containsExactly("Raum und Umwelt");
        assertThat(dataset.keywords()).contains("Wasserqualität", "Grundwasser", "NAQUA");
        assertThat(dataset.metadata().issued()).contains(LocalDate.parse("2026-06-24"));
        assertThat(dataset.metadata().licenseUri()).contains(URI.create("https://creativecommons.org/licenses/by/4.0/"));
        assertThat(dataset.metadata().landingPage()).contains(URI.create("https://data.so.ch/dataset/ch.so.wasserqualitaet_grundwasser"));
        assertThat(dataset.metadata().publicationStatus()).contains("published");
        assertThat(dataset.metadata().origin()).contains("cantonal");
        assertThat(dataset.metadata().accrualPeriodicity()).contains("annually");
        assertThat(dataset.metadata().contactPoint()).get()
                .satisfies(contact -> {
                    assertThat(contact.name()).isEqualTo("Anna Keller");
                    assertThat(contact.organizationUnit()).contains("Fachstelle für Wasserqualität Grundwasser");
                    assertThat(contact.email()).contains(URI.create("mailto:afu@bd.so.ch"));
                });
        assertThat(dataset.metadata().surveyMethod())
                .contains("Die Daten zu Wasserqualität Grundwasser entstehen durch fachliche Erfassung, anschliessende Qualitätskontrolle und periodische Harmonisierung.");
        assertThat(dataset.metadata().dataAvailableFrom())
                .contains("Zeitreihen sind ab 2011 weitgehend vollständig verfügbar.");
        assertThat(dataset.metadata().furtherUses())
                .contains("Geeignet für Übersichten, Plausibilitätsvergleiche, Kennzahlen und vorbereitende Analysen.");
        assertThat(dataset.metadata().auxiliaryData())
                .contains("Ergänzend werden Referenztabellen, Geocodierungen und technische Prüflisten verwendet.");
        assertThat(dataset.metadata().qualitySummary()).isEmpty();
        assertThat(dataset.metadata().structureSummary()).get()
                .satisfies(summary -> {
                    assertThat(summary.objectCount()).isEqualTo(36176);
                    assertThat(summary.attributeCount()).isEqualTo(7);
                });
        assertThat(dataset.metadata().attributes())
                .extracting(attribute -> attribute.name())
                .containsExactly(
                        "jahr",
                        "messstelle_code",
                        "gemeinde",
                        "parameter",
                        "messwert",
                        "einheit",
                        "messwert_status");
        assertThat(dataset.metadata().attributes().get(0))
                .satisfies(attribute -> {
                    assertThat(attribute.dataType()).isEqualTo("INTEGER");
                    assertThat(attribute.description()).contains("Jahr der Messung.");
                    assertThat(attribute.unit()).contains("Jahr");
                    assertThat(attribute.mandatory()).isTrue();
                });
        assertThat(dataset.metadata().model()).isEmpty();
        assertThat(dataset.metadata().hasStructureInformation()).isTrue();
        assertThat(dataset.primaryDistributions())
                .extracting(distribution -> distribution.format())
                .containsExactly(DistributionFormat.CSV, DistributionFormat.XLSX, DistributionFormat.PARQUET);
        assertThat(dataset.primaryDistributions())
                .extracting(distribution -> distribution.preferredHref().toString())
                .containsExactly(
                        "http://localhost:8081/ch.so.datenportal/downloads/ch.so.wasserqualitaet_grundwasser.csv",
                        "http://localhost:8081/ch.so.datenportal/downloads/ch.so.wasserqualitaet_grundwasser.xlsx",
                        "http://localhost:8081/ch.so.datenportal/downloads/ch.so.wasserqualitaet_grundwasser.parquet");
    }

    @Test
    void mapsQualityAndStructureSummaries() throws Exception {
        DatasetEntry dataset = parseFixture().datasets().stream()
                .filter(entry -> entry.identifier().equals("ch.so.bauinventar"))
                .findFirst()
                .orElseThrow();

        assertThat(dataset.metadata().qualitySummary()).get()
                .satisfies(summary -> {
                    assertThat(summary.status()).isEqualTo("success");
                    assertThat(summary.errors()).isEqualTo(0);
                    assertThat(summary.validatedAt().toString()).isEqualTo("2026-06-24T02:28+02:00");
                    assertThat(summary.reportUrl()).isEqualTo(URI.create("https://data.so.ch/validation/ch.so.bauinventar/ilivalidator.log"));
                });
        assertThat(dataset.metadata().structureSummary()).get()
                .satisfies(summary -> {
                    assertThat(summary.objectCount()).isEqualTo(26349);
                    assertThat(summary.attributeCount()).isEqualTo(6);
                });
    }

    @Test
    void mapsSeriesRootWithoutStructureMetadataWhileKeepingIssueStructureMetadata() throws Exception {
        DatasetSeriesEntry series = parseFixture().datasetSeries().stream()
                .filter(entry -> entry.identifier().equals("ch.so.bevoelkerung.altersstruktur"))
                .findFirst()
                .orElseThrow();

        assertThat(series.metadata().attributes()).isEmpty();
        assertThat(series.metadata().model()).isEmpty();
        assertThat(series.metadata().hasStructureInformation()).isFalse();
        assertThat(series.currentIssueOrThrow().metadata().attributes()).isNotEmpty();
        assertThat(series.currentIssueOrThrow().metadata().model())
                .contains("SO_AFIN_Bevoelkerung_Statistik_Publikation_20260624");
    }

    @Test
    void mapsSeriesIssuesAndCurrentIssueSelection() throws Exception {
        DatasetSeriesEntry series = parseFixture().datasetSeries().stream()
                .filter(entry -> entry.identifier().equals("ch.so.abstimmungsresultate"))
                .findFirst()
                .orElseThrow();

        assertThat(series.currentIssue()).get()
                .extracting(issue -> issue.identifier())
                .isEqualTo("ch.so.abstimmungsresultate_2026");
        assertThat(series.metadata().accrualPeriodicity()).contains("asNeeded");
        assertThat(series.metadata().temporalCoverage()).get()
                .satisfies(coverage -> {
                    assertThat(coverage.startDate()).contains(LocalDate.parse("2023-01-01"));
                    assertThat(coverage.endDate()).contains(LocalDate.parse("2026-12-31"));
                });
        assertThat(series.currentIssueLabelForDisplay()).isEqualTo("2026");
        assertThat(series.issuesNewestFirst())
                .extracting(issue -> issue.identifier())
                .containsExactlyInAnyOrder(
                        "ch.so.abstimmungsresultate_2026",
                        "ch.so.abstimmungsresultate_2025",
                        "ch.so.abstimmungsresultate_2024",
                        "ch.so.abstimmungsresultate_2023");
        assertThat(series.distributionsForListing())
                .extracting(distribution -> distribution.preferredHref().toString())
                .containsExactly(
                        "http://localhost:8081/ch.so.datenportal/downloads/ch.so.abstimmungsresultate_2026.csv",
                        "http://localhost:8081/ch.so.datenportal/downloads/ch.so.abstimmungsresultate_2026.xlsx",
                        "http://localhost:8081/ch.so.datenportal/downloads/ch.so.abstimmungsresultate_2026.parquet");
    }

    @Test
    void mapsNewAccessRightsExactlyAndKeepsOpenDataStrict() throws Exception {
        Catalog catalog = parseFixture();

        DatasetEntry publicWithConditions = catalog.datasets().stream()
                .filter(entry -> entry.identifier().equals("ch.2581.baumkataster"))
                .findFirst()
                .orElseThrow();
        DatasetEntry confidential = catalog.datasets().stream()
                .filter(entry -> entry.identifier().equals("ch.so.polizei.einsatzlagen.laufend"))
                .findFirst()
                .orElseThrow();

        assertThat(publicWithConditions.accessLevel()).isEqualTo(AccessLevel.PUBLIC_WITH_CONDITIONS);
        assertThat(publicWithConditions.isOpenData()).isFalse();
        assertThat(confidential.accessLevel()).isEqualTo(AccessLevel.CONFIDENTIAL);
        assertThat(confidential.isOpenData()).isFalse();
    }

    @Test
    void parsesAllSupportedAccessLevels() {
        assertThat(parseSingleDatasetWithAccessLevel("open").accessLevel()).isEqualTo(AccessLevel.OPEN);
        assertThat(parseSingleDatasetWithAccessLevel("public_with_conditions").accessLevel())
                .isEqualTo(AccessLevel.PUBLIC_WITH_CONDITIONS);
        assertThat(parseSingleDatasetWithAccessLevel("restricted").accessLevel()).isEqualTo(AccessLevel.RESTRICTED);
        assertThat(parseSingleDatasetWithAccessLevel("internal").accessLevel()).isEqualTo(AccessLevel.INTERNAL);
        assertThat(parseSingleDatasetWithAccessLevel("confidential").accessLevel()).isEqualTo(AccessLevel.CONFIDENTIAL);
    }

    @Test
    void rejectsInvalidXml() {
        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <ili:transfer xmlns="http://www.interlis.ch/xtf/2.4/SO_AGI_DataCatalog_PublishedCatalog_20260602"
                              xmlns:ili="http://www.interlis.ch/xtf/2.4/INTERLIS">
                  <ili:headersection>
                    <ili:models>
                      <ili:model>SO_AGI_DataCatalog_PublishedCatalog_20260602</ili:model>
                    </ili:models>
                  </ili:headersection>
                  <ili:datasection>
                """;

        assertThatThrownBy(() -> parseXml(xml))
                .isInstanceOf(XtfParseException.class)
                .hasMessageContaining("XML stream is invalid or incomplete");
    }

    @Test
    void rejectsMissingRequiredFieldWithControlledValidationError() {
        String xml = transferXml("""
                  <datasets>
                    <Dataset>
                      <title>Fehlender Identifier</title>
                      <description>Beschreibung</description>
                      <publisher>
                        <Office>
                          <officeUri>https://data.so.ch/agent/agi</officeUri>
                          <name>Amt für Geoinformation</name>
                        </Office>
                      </publisher>
                      <creator>
                        <Office>
                          <officeUri>https://data.so.ch/agent/agi</officeUri>
                          <name>Amt für Geoinformation</name>
                        </Office>
                      </creator>
                      <modified>2026-05-19</modified>
                      <accessRights>
                        <AccessRights>
                          <localAccessLevel>open</localAccessLevel>
                        </AccessRights>
                      </accessRights>
                      <publicationStatus>published</publicationStatus>
                      <distributions>
                        <Distribution>
                          <accessURL>https://data.so.ch/dataset/missing-id</accessURL>
                          <format>csv</format>
                        </Distribution>
                      </distributions>
                    </Dataset>
                  </datasets>
                """);

        assertThatThrownBy(() -> parseXml(xml))
                .isInstanceOf(CatalogValidationException.class)
                .hasMessageContaining("/Publication/Catalog/datasets/Dataset/identifier");
    }

    @Test
    void rejectsUnknownAccessLevelWithControlledValidationError() {
        String xml = datasetTransferXml("""
                <accessRights>
                  <AccessRights>
                    <localAccessLevel>secret</localAccessLevel>
                    <accessRightsUri>https://example.com/access-right</accessRightsUri>
                  </AccessRights>
                </accessRights>
                """);

        assertThatThrownBy(() -> parseXml(xml))
                .isInstanceOf(CatalogValidationException.class)
                .hasMessageContaining("/Publication/Catalog/datasets/Dataset/accessRights/AccessRights/localAccessLevel");
    }

    @Test
    void rejectsDatasetAttributeWithoutName() {
        String xml = datasetTransferXml("""
                <accessRights>
                  <AccessRights>
                    <localAccessLevel>open</localAccessLevel>
                    <accessRightsUri>https://example.com/access-right</accessRightsUri>
                  </AccessRights>
                </accessRights>
                <attributes>
                  <base:DatasetAttribute>
                    <base:dataType>TEXT</base:dataType>
                    <base:mandatory>true</base:mandatory>
                  </base:DatasetAttribute>
                </attributes>
                """);

        assertThatThrownBy(() -> parseXml(xml))
                .isInstanceOf(CatalogValidationException.class)
                .hasMessageContaining("/Publication/Catalog/datasets/Dataset/attributes/DatasetAttribute/name");
    }

    @Test
    void rejectsDatasetAttributeWithInvalidMandatoryValue() {
        String xml = datasetTransferXml("""
                <accessRights>
                  <AccessRights>
                    <localAccessLevel>open</localAccessLevel>
                    <accessRightsUri>https://example.com/access-right</accessRightsUri>
                  </AccessRights>
                </accessRights>
                <attributes>
                  <base:DatasetAttribute>
                    <base:name>gemeinde</base:name>
                    <base:dataType>TEXT</base:dataType>
                    <base:mandatory>yes</base:mandatory>
                  </base:DatasetAttribute>
                </attributes>
                """);

        assertThatThrownBy(() -> parseXml(xml))
                .isInstanceOf(CatalogValidationException.class)
                .hasMessageContaining("/Publication/Catalog/datasets/Dataset/attributes/DatasetAttribute/mandatory");
    }

    @Test
    void disablesExternalEntities() {
        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <!DOCTYPE foo [<!ENTITY xxe SYSTEM "file:///etc/passwd">]>
                <ili:transfer xmlns="http://www.interlis.ch/xtf/2.4/SO_AGI_DataCatalog_PublishedCatalog_20260602"
                              xmlns:ili="http://www.interlis.ch/xtf/2.4/INTERLIS">
                  <ili:headersection>
                    <ili:models>
                      <ili:model>SO_AGI_DataCatalog_PublishedCatalog_20260602</ili:model>
                    </ili:models>
                  </ili:headersection>
                  <ili:datasection>
                    <Publication ili:bid="b1">
                      <Catalog ili:tid="catalog">
                        <datasets>
                          <Dataset>
                            <identifier>xxe</identifier>
                            <title>&xxe;</title>
                            <description>Beschreibung</description>
                            <publisher>
                              <Office>
                                <officeUri>https://data.so.ch/agent/agi</officeUri>
                                <name>Amt für Geoinformation</name>
                              </Office>
                            </publisher>
                            <creator>
                              <Office>
                                <officeUri>https://data.so.ch/agent/agi</officeUri>
                                <name>Amt für Geoinformation</name>
                              </Office>
                            </creator>
                            <modified>2026-05-19</modified>
                            <accessRights>
                              <AccessRights>
                                <localAccessLevel>open</localAccessLevel>
                              </AccessRights>
                            </accessRights>
                            <publicationStatus>published</publicationStatus>
                            <distributions>
                              <Distribution>
                                <accessURL>https://data.so.ch/dataset/xxe</accessURL>
                                <format>csv</format>
                              </Distribution>
                            </distributions>
                          </Dataset>
                        </datasets>
                      </Catalog>
                    </Publication>
                  </ili:datasection>
                </ili:transfer>
                """;

        assertThatThrownBy(() -> parseXml(xml))
                .isInstanceOf(XtfParseException.class);
    }

    private static Catalog parseFixture() throws Exception {
        String sourceDescription = "classpath:published_catalog_full_62_entries.xtf";
        try (var inputStream = new ClassPathResource("published_catalog_full_62_entries.xtf").getInputStream()) {
            var bytes = new CatalogBytes(inputStream.readAllBytes(), sourceDescription);
            var resolvedBytes = new CatalogDownloadUrlPlaceholderResolver()
                    .resolve(bytes, "http://localhost:8081/ch.so.datenportal/downloads");
            return PARSER.parse(resolvedBytes.inputStream(), sourceDescription);
        }
    }

    private static DatasetEntry parseSingleDatasetWithAccessLevel(String accessLevel) {
        return parseXml(datasetTransferXml("""
                <accessRights>
                  <AccessRights>
                    <localAccessLevel>%s</localAccessLevel>
                    <accessRightsUri>https://example.com/access-right</accessRightsUri>
                  </AccessRights>
                </accessRights>
                """.formatted(accessLevel))).datasets().getFirst();
    }

    @Test
    void acceptsModelStatusesButExcludesThemFromPublicView() throws Exception {
        String xml = new ClassPathResource("published_catalog_full_62_entries.xtf")
                .getContentAsString(StandardCharsets.UTF_8).replace("${DOWNLOAD_URL}", "https://example.test/downloads");
        for (String status : List.of("draft", "in_review", "archived")) {
            Catalog full = parseXml(xml.replace("<publicationStatus>published</publicationStatus>",
                    "<publicationStatus>" + status + "</publicationStatus>"));
            new CatalogValidator().validateOrThrow(full);
            assertThat(full.publishedView().topLevelEntries()).isEmpty();
            try (var snapshot = buildSnapshot(xml.replace("<publicationStatus>published</publicationStatus>",
                    "<publicationStatus>" + status + "</publicationStatus>"))) {
                assertThat(snapshot.isEmpty()).isTrue();
                assertThat(snapshot.searchIndex().documentCount()).isZero();
                assertThat(snapshot.allEntriesByIdentifier()).isEmpty();
                assertThat(snapshot.publishedCatalog().sizeInBytes()).isPositive();
            }
        }
        assertThatThrownBy(() -> parseXml(xml.replaceFirst("<publicationStatus>published</publicationStatus>",
                "<publicationStatus>invalid</publicationStatus>")))
                .hasMessageContaining("Unknown publicationStatus");
    }

    @Test
    void filtersIssuesBeforeSelectingCurrentIssueAndDoesNotChangeSource() throws Exception {
        String xml = new ClassPathResource("published_catalog_full_62_entries.xtf")
                .getContentAsString(StandardCharsets.UTF_8).replace("${DOWNLOAD_URL}", "https://example.test/downloads");
        // Hide every issue marked current; the public view must select among the remaining issues.
        var document = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        var nodes = document.getElementsByTagName("DatasetIssue");
        var hidden = new java.util.HashSet<String>();
        for (int i = 0; i < nodes.getLength(); i++) {
            var issue = (org.w3c.dom.Element) nodes.item(i);
            if ("true".equals(issue.getElementsByTagName("isCurrentIssue").item(0).getTextContent())) {
                hidden.add(issue.getElementsByTagName("identifier").item(0).getTextContent());
                issue.getElementsByTagName("publicationStatus").item(0).setTextContent("in_review");
            }
        }
        var output = new java.io.StringWriter();
        javax.xml.transform.TransformerFactory.newInstance().newTransformer().transform(
                new javax.xml.transform.dom.DOMSource(document), new javax.xml.transform.stream.StreamResult(output));
        Catalog full = parseXml(output.toString());
        new CatalogValidator().validateOrThrow(full);
        Catalog visible = full.publishedView();
        assertThat(hidden).isNotEmpty();
        assertThat(full.issueCount()).isEqualTo(111);
        assertThat(visible.issueCount()).isEqualTo(111 - hidden.size());
        try (var snapshot = buildSnapshot(output.toString())) {
            for (String identifier : hidden) {
                assertThat(snapshot.findAnyEntry(identifier)).isEmpty();
                assertThat(snapshot.findVisibleEntry(identifier)).isEmpty();
                assertThat(snapshot.searchIndex().search(identifier)).noneMatch(hit -> hidden.contains(hit.entryId()));
            }
            assertThat(snapshot.catalog().issueCount()).isEqualTo(visible.issueCount());
        }
        for (var series : visible.datasetSeries()) {
            assertThat(series.issues()).allMatch(issue -> !hidden.contains(issue.identifier()));
            assertThat(hidden).doesNotContain(series.currentIssueOrThrow().identifier());
        }
    }

    @Test
    void importsRealGretlDeliveryWithRetainedAndReleasedIssues() throws Exception {
        // Exported by the offline GRETL integration test with model revision 2bd8046b03ea04d202056b05c62fa953e3ddfff5.
        String xml = new ClassPathResource("published_catalog_gretl_delivery.xtf")
                .getContentAsString(StandardCharsets.UTF_8);
        Catalog full = parseXml(xml);
        new CatalogValidator().validateOrThrow(full);
        assertThat(full.seriesCount()).isEqualTo(1);
        assertThat(full.issueCount()).isEqualTo(3);
        try (var snapshot = buildSnapshot(xml)) {
            assertThat(snapshot.catalog().issueCount()).isEqualTo(1);
            assertThat(snapshot.catalog().datasetSeries().getFirst().currentIssueOrThrow().identifier())
                    .isEqualTo("ch.so.bevoelkerung.altersstruktur_2030");
            assertThat(snapshot.findAnyEntry("ch.so.bevoelkerung.altersstruktur_2025")).isEmpty();
        }
    }

    @Test
    void publishedIssueOfWithheldSeriesIsNotAccessible() throws Exception {
        String xml = new ClassPathResource("published_catalog_gretl_delivery.xtf")
                .getContentAsString(StandardCharsets.UTF_8)
                .replaceFirst("<publicationStatus>published</publicationStatus>",
                        "<publicationStatus>draft</publicationStatus>");
        assertThat(parseXml(xml).datasetSeries().getFirst().issues())
                .anyMatch(issue -> issue.metadata().publicationStatus().orElse("").equals("published"));
        try (var snapshot = buildSnapshot(xml)) {
            assertThat(snapshot.isEmpty()).isTrue();
            assertThat(snapshot.allEntriesByIdentifier()).isEmpty();
            assertThat(snapshot.searchIndex().documentCount()).isZero();
        }
    }

    private static ch.so.agi.datenportal.catalog.domain.CatalogSnapshot buildSnapshot(String xml) {
        var builder = new ch.so.agi.datenportal.catalog.service.CatalogSnapshotBuilder(
                PARSER, new CatalogValidator(),
                new ch.so.agi.datenportal.search.CatalogSearchIndexBuilder(
                        new ch.so.agi.datenportal.search.CatalogDocumentMapper()), java.time.Clock.systemUTC());
        return builder.build(new CatalogBytes(xml.getBytes(StandardCharsets.UTF_8), "statuses-test"),
                ch.so.agi.datenportal.catalog.CatalogTestArtifacts.duckDb("test")).snapshot();
    }

    private static Catalog parseXml(String xml) {
        return PARSER.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)), "inline-test.xml");
    }

    private static String transferXml(String catalogBody) {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <ili:transfer xmlns="http://www.interlis.ch/xtf/2.4/SO_AGI_DataCatalog_PublishedCatalog_20260602"
                              xmlns:base="http://www.interlis.ch/xtf/2.4/SO_AGI_DataCatalog_Base_20260529"
                              xmlns:ili="http://www.interlis.ch/xtf/2.4/INTERLIS">
                  <ili:headersection>
                    <ili:models>
                      <ili:model>SO_AGI_DataCatalog_PublishedCatalog_20260602</ili:model>
                      <ili:model>SO_AGI_DataCatalog_Base_20260529</ili:model>
                    </ili:models>
                  </ili:headersection>
                  <ili:datasection>
                    <Publication ili:bid="b1">
                      <Catalog ili:tid="catalog">
                """ + catalogBody + """
                      </Catalog>
                    </Publication>
                  </ili:datasection>
                </ili:transfer>
                """;
    }

    private static String datasetTransferXml(String extraBody) {
        return transferXml("""
                  <datasets>
                    <Dataset>
                      <identifier>dataset-1</identifier>
                      <title>Testdatensatz</title>
                      <description>Beschreibung</description>
                      <publisher>
                        <Office>
                          <officeUri>https://data.so.ch/agent/agi</officeUri>
                          <name>Amt für Geoinformation</name>
                        </Office>
                      </publisher>
                      <creator>
                        <Office>
                          <officeUri>https://data.so.ch/agent/agi</officeUri>
                          <name>Amt für Geoinformation</name>
                        </Office>
                      </creator>
                      <modified>2026-05-19</modified>
                """ + extraBody + """
                      <publicationStatus>published</publicationStatus>
                      <distributions>
                        <Distribution>
                          <distributionUri>https://data.so.ch/dataset/test/distribution/csv</distributionUri>
                          <accessURL>https://data.so.ch/dataset/test</accessURL>
                          <downloadURL>http://localhost:8081/ch.so.datenportal/downloads/test.csv</downloadURL>
                          <format>csv</format>
                        </Distribution>
                      </distributions>
                    </Dataset>
                  </datasets>
                """);
    }
}
