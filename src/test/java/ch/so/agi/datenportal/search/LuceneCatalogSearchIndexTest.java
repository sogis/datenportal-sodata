package ch.so.agi.datenportal.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ch.so.agi.datenportal.catalog.domain.AccessLevel;
import ch.so.agi.datenportal.catalog.domain.CatalogEntry;
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
import org.junit.jupiter.api.Test;

class LuceneCatalogSearchIndexTest {

    private static final Office AGI = new Office("agi", "Amt für Geoinformation", Optional.of("AGI"));
    private static final Office AFIN = new Office("afin", "Amt für Finanzen", Optional.of("AFIN"));
    private static final Office STAT = new Office("stat", "Fachstelle Statistik", Optional.of("STAT"));
    private static final Theme REGI = new Theme("REGI", "Raum und Umwelt");
    private static final Theme ECON = new Theme("ECON", "Wirtschaft und Finanzen");
    private static final Theme GOVE = new Theme("GOVE", "Regierung und Verwaltung");

    private final CatalogSearchIndexBuilder builder = new CatalogSearchIndexBuilder(new CatalogDocumentMapper());

    @Test
    void exactIdentifierRanksAheadOfOtherMatches() {
        try (CatalogSearchIndex index = index()) {
            assertThat(index.search("gemeindegrenzen"))
                    .extracting(SearchHit::entryId)
                    .first()
                    .isEqualTo("gemeindegrenzen");
        }
    }

    @Test
    void guaranteedSubstringFieldsMatchPartialTokens() {
        try (CatalogSearchIndex index = index()) {
            assertThat(index.search("zaehl"))
                    .extracting(SearchHit::entryId)
                    .containsExactly("verkehrszaehlstellen");
            assertThat(index.search("steue"))
                    .extracting(SearchHit::entryId)
                    .containsExactly("steuerfuss-gemeinden");
            assertThat(index.search("apri"))
                    .extracting(SearchHit::entryId)
                    .containsExactly("gemeindegrenzen");
        }
    }

    @Test
    void queryUsesAndAcrossTokensAndOrAcrossFields() {
        try (CatalogSearchIndex index = index()) {
            assertThat(index.search("grenzen april"))
                    .extracting(SearchHit::entryId)
                    .containsExactly("gemeindegrenzen");
            assertThat(index.search("steuerfuss finanzen"))
                    .extracting(SearchHit::entryId)
                    .containsExactly("steuerfuss-gemeinden");
            assertThat(index.search("grenzen statistik")).isEmpty();
        }
    }

    @Test
    void secondaryFieldsRemainSearchable() {
        try (CatalogSearchIndex index = index()) {
            assertThat(index.search("verwaltungsdaten"))
                    .extracting(SearchHit::entryId)
                    .containsExactly("archiv");
            assertThat(index.search("raum umwelt"))
                    .extracting(SearchHit::entryId)
                    .contains("gemeindegrenzen", "verkehrszaehlstellen");
            assertThat(index.search("amt finanzen"))
                    .extracting(SearchHit::entryId)
                    .containsExactly("steuerfuss-gemeinden");
        }
    }

    @Test
    void specialCharactersDoNotCrashAndPureDescriptionSubstringIsNotGuaranteed() {
        try (CatalogSearchIndex index = index()) {
            assertThatCode(() -> index.search("+:/( wasser")).doesNotThrowAnyException();
            assertThat(index.search("waltungsdat")).isEmpty();
        }
    }

    @Test
    void closedIndexReportsSearchFailureInsteadOfReturningNoResults() {
        CatalogSearchIndex index = index();
        index.close();

        assertThatThrownBy(() -> index.search("gemeindegrenzen"))
                .isInstanceOf(CatalogSearchException.class);
        assertThatThrownBy(index::documentCount)
                .isInstanceOf(CatalogSearchException.class);
    }

    private CatalogSearchIndex index() {
        return builder.build(List.of(
                dataset(
                        "steuerfuss-gemeinden",
                        "Steuerfuss Gemeinden",
                        "Steuersätze der Solothurner Gemeinden.",
                        AFIN,
                        ECON,
                        LocalDate.parse("2026-05-20"),
                        List.of(DistributionFormat.CSV),
                        List.of("Gemeinden", "Steuern")),
                dataset(
                        "verkehrszaehlstellen",
                        "Verkehrszählstellen",
                        "Standorte und Angaben zu Zählstellen.",
                        AGI,
                        REGI,
                        LocalDate.parse("2026-05-14"),
                        List.of(DistributionFormat.CSV, DistributionFormat.XLSX, DistributionFormat.PARQUET),
                        List.of("Verkehr", "Zählstellen")),
                dataset(
                        "archiv",
                        "Archiv",
                        "Historische Verwaltungsdaten.",
                        STAT,
                        GOVE,
                        LocalDate.parse("2024-03-01"),
                        List.of(DistributionFormat.CSV),
                        List.of("Archiv")),
                series()));
    }

    private static CatalogEntry dataset(
            String identifier,
            String title,
            String description,
            Office office,
            Theme theme,
            LocalDate modified,
            List<DistributionFormat> formats,
            List<String> keywords) {
        return new DatasetEntry(
                identifier,
                title,
                description,
                office,
                office,
                List.of(theme),
                keywords,
                modified,
                AccessLevel.OPEN,
                distributions(identifier, formats));
    }

    private static CatalogEntry series() {
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
                        issue("gemeindegrenzen-2026", "Gemeindegrenzen Mai 2026", "Mai 2026", LocalDate.parse("2026-05-18"), true),
                        issue("gemeindegrenzen-2025", "Gemeindegrenzen April 2025", "April 2025", LocalDate.parse("2025-04-18"), false)));
    }

    private static DatasetIssueEntry issue(
            String identifier,
            String title,
            String label,
            LocalDate modified,
            boolean current) {
        return new DatasetIssueEntry(
                identifier,
                title,
                "Ausgabe " + label,
                AGI,
                AGI,
                List.of(REGI),
                List.of(label),
                modified,
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
