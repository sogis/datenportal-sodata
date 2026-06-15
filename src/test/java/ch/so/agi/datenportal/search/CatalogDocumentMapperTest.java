package ch.so.agi.datenportal.search;

import static org.assertj.core.api.Assertions.assertThat;

import ch.so.agi.datenportal.catalog.domain.AccessLevel;
import ch.so.agi.datenportal.catalog.domain.DatasetEntry;
import ch.so.agi.datenportal.catalog.domain.DatasetIssueEntry;
import ch.so.agi.datenportal.catalog.domain.DatasetSeriesEntry;
import ch.so.agi.datenportal.catalog.domain.DistributionFormat;
import ch.so.agi.datenportal.catalog.domain.DistributionLink;
import ch.so.agi.datenportal.catalog.domain.Office;
import ch.so.agi.datenportal.catalog.domain.Theme;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.apache.lucene.document.Document;
import org.junit.jupiter.api.Test;

class CatalogDocumentMapperTest {

    private static final Office AGI = new Office("agi", "Amt für Geoinformation", Optional.of("AGI"));
    private static final Theme REGI = new Theme("REGI", "Raum und Umwelt");
    private final CatalogDocumentMapper mapper = new CatalogDocumentMapper();

    @Test
    void mapsDatasetToLuceneDocument() {
        Document document = mapper.toDocument(dataset());

        assertThat(document.get(CatalogSearchFields.ENTRY_ID)).isEqualTo("verkehrszaehlstellen");
        assertThat(document.get(CatalogSearchFields.ENTRY_TYPE)).isEqualTo("dataset");
        assertThat(document.get(CatalogSearchFields.IDENTIFIER_EXACT)).isEqualTo("verkehrszaehlstellen");
        assertThat(document.get(CatalogSearchFields.TITLE)).contains("Verkehrszählstellen");
        assertThat(document.get(CatalogSearchFields.DESCRIPTION)).contains("Standorte");
        assertThat(hasField(document, CatalogSearchFields.KEYWORDS)).isTrue();
        assertThat(hasField(document, CatalogSearchFields.THEME_TEXT)).isTrue();
        assertThat(hasField(document, CatalogSearchFields.OFFICE_TEXT)).isTrue();
        assertThat(hasField(document, CatalogSearchFields.FORMATS)).isTrue();
        assertThat(hasField(document, CatalogSearchFields.MODIFIED_DATE_EPOCH_DAY)).isTrue();
        assertThat(document.get(CatalogSearchFields.OPEN_DATA)).isEqualTo("true");
        assertThat(document.get(CatalogSearchFields.STRUCTURE_DESCRIBED)).isEqualTo("false");
    }

    @Test
    void mapsSeriesAsSingleTopLevelDocumentWithIssueMetadata() {
        Document document = mapper.toDocument(series());

        assertThat(document.get(CatalogSearchFields.ENTRY_ID)).isEqualTo("gemeindegrenzen");
        assertThat(document.get(CatalogSearchFields.ENTRY_TYPE)).isEqualTo("series");
        assertThat(hasField(document, CatalogSearchFields.ISSUE_YEARS)).isTrue();
        assertThat(hasField(document, CatalogSearchFields.ISSUE_TEXT)).isTrue();
        assertThat(document.getFields(CatalogSearchFields.ENTRY_ID)).hasSize(1);
    }

    private static boolean hasField(Document document, String fieldName) {
        return document.getFields(fieldName).length > 0;
    }

    private static DatasetEntry dataset() {
        return new DatasetEntry(
                "verkehrszaehlstellen",
                "Verkehrszählstellen",
                "Standorte und Angaben zu Zählstellen.",
                AGI,
                AGI,
                List.of(REGI),
                List.of("Verkehr", "Zählstellen"),
                LocalDate.parse("2026-05-14"),
                AccessLevel.OPEN,
                distributions("verkehrszaehlstellen", List.of(DistributionFormat.CSV, DistributionFormat.PARQUET)));
    }

    private static DatasetSeriesEntry series() {
        return new DatasetSeriesEntry(
                "gemeindegrenzen",
                "Gemeindegrenzen",
                "Digitale Abgrenzung der Solothurner Gemeinden.",
                AGI,
                AGI,
                List.of(REGI),
                List.of("Grenzen", "Gemeinden"),
                AccessLevel.OPEN,
                List.of(
                        issue("gemeindegrenzen-2026", "Gemeindegrenzen Mai 2026", "Mai 2026", true),
                        issue("gemeindegrenzen-2025", "Gemeindegrenzen April 2025", "April 2025", false)));
    }

    private static DatasetIssueEntry issue(String identifier, String title, String label, boolean current) {
        return new DatasetIssueEntry(
                identifier,
                title,
                "Ausgabe " + label,
                AGI,
                AGI,
                List.of(REGI),
                List.of(label),
                current ? LocalDate.parse("2026-05-18") : LocalDate.parse("2025-04-18"),
                AccessLevel.OPEN,
                distributions(identifier, List.of(DistributionFormat.CSV, DistributionFormat.XLSX, DistributionFormat.PARQUET)),
                label,
                current);
    }

    private static List<DistributionLink> distributions(String identifier, List<DistributionFormat> formats) {
        return formats.stream()
                .map(format -> new DistributionLink(
                        URI.create("https://example.com/" + identifier + "/" + format.name().toLowerCase()),
                        format))
                .toList();
    }
}
