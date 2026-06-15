package ch.so.agi.datenportal.catalog.importxtf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ch.so.agi.datenportal.catalog.domain.AccessLevel;
import ch.so.agi.datenportal.catalog.domain.Catalog;
import ch.so.agi.datenportal.catalog.domain.DatasetEntry;
import ch.so.agi.datenportal.catalog.domain.DatasetSeriesEntry;
import ch.so.agi.datenportal.catalog.domain.DistributionFormat;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

class XtfPublishedCatalogParserTest {

    private static final PublishedCatalogParser PARSER = new XtfPublishedCatalogParser();

    @Test
    void parsesFullFixtureWithExpectedCounts() throws Exception {
        Catalog catalog = parseFixture();

        assertThat(catalog.datasetCount()).isEqualTo(23);
        assertThat(catalog.seriesCount()).isEqualTo(31);
        assertThat(catalog.issueCount()).isEqualTo(100);
        assertThat(catalog.topLevelEntries()).hasSize(54);
    }

    @Test
    void mapsDatasetsThemesOfficesKeywordsAndFormats() throws Exception {
        DatasetEntry dataset = parseFixture().datasets().stream()
                .filter(entry -> entry.identifier().equals("ch.so.bauinventar"))
                .findFirst()
                .orElseThrow();

        assertThat(dataset.title()).isEqualTo("Bauinventar");
        assertThat(dataset.creator().identifier()).isEqualTo("arp");
        assertThat(dataset.publisher().identifier()).isEqualTo("agi");
        assertThat(dataset.accessLevel()).isEqualTo(AccessLevel.OPEN);
        assertThat(dataset.themes())
                .extracting(theme -> theme.displayName())
                .containsExactly("Bau und Wohnungswesen", "Kultur Medien Informationsgesellschaft Sport");
        assertThat(dataset.keywords()).containsExactly("Bauinventar", "Kulturgüter", "Gebäude");
        assertThat(dataset.primaryDistributions())
                .extracting(distribution -> distribution.format())
                .containsExactly(DistributionFormat.CSV, DistributionFormat.XLSX, DistributionFormat.PARQUET);
        assertThat(dataset.primaryDistributions())
                .extracting(distribution -> distribution.preferredHref().toString())
                .containsExactly(
                        "https://data.so.ch/download/ch.so.bauinventar.csv",
                        "https://data.so.ch/download/ch.so.bauinventar.xlsx",
                        "https://data.so.ch/download/ch.so.bauinventar.parquet");
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
        assertThat(series.currentIssueLabelForDisplay()).isEqualTo("2026");
        assertThat(series.issuesNewestFirst())
                .extracting(issue -> issue.identifier())
                .containsExactly(
                        "ch.so.abstimmungsresultate_2026",
                        "ch.so.abstimmungsresultate_2025",
                        "ch.so.abstimmungsresultate_2024",
                        "ch.so.abstimmungsresultate_2023");
        assertThat(series.distributionsForListing())
                .extracting(distribution -> distribution.preferredHref().toString())
                .containsExactly(
                        "https://data.so.ch/download/ch.so.abstimmungsresultate_2026.csv",
                        "https://data.so.ch/download/ch.so.abstimmungsresultate_2026.xlsx",
                        "https://data.so.ch/download/ch.so.abstimmungsresultate_2026.parquet");
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
        try (var inputStream = new ClassPathResource("published_catalog_full_54_entries.xtf").getInputStream()) {
            return PARSER.parse(inputStream, "classpath:published_catalog_full_54_entries.xtf");
        }
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
}
