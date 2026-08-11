package ch.so.agi.datenportal.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import ch.so.agi.datenportal.catalog.CatalogTestArtifacts;
import ch.so.agi.datenportal.catalog.domain.AccessLevel;
import ch.so.agi.datenportal.catalog.domain.Catalog;
import ch.so.agi.datenportal.catalog.domain.CatalogEntry;
import ch.so.agi.datenportal.catalog.domain.CatalogEntryType;
import ch.so.agi.datenportal.catalog.domain.CatalogSnapshot;
import ch.so.agi.datenportal.catalog.domain.DatasetEntry;
import ch.so.agi.datenportal.catalog.domain.DatasetIssueEntry;
import ch.so.agi.datenportal.catalog.domain.DatasetSeriesEntry;
import ch.so.agi.datenportal.catalog.domain.DistributionFormat;
import ch.so.agi.datenportal.catalog.domain.DistributionLink;
import ch.so.agi.datenportal.catalog.domain.Office;
import ch.so.agi.datenportal.catalog.domain.Theme;
import ch.so.agi.datenportal.config.SearchProperties;
import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

class CatalogSearchServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-06-15T00:00:00Z"), ZoneOffset.UTC);
    private static final Office AGI = new Office("agi", "Amt für Geoinformation", java.util.Optional.of("AGI"));
    private static final Office AFIN = new Office("afin", "Amt für Finanzen", java.util.Optional.of("AFIN"));
    private static final Office STAT = new Office("stat", "Fachstelle Statistik", java.util.Optional.of("STAT"));
    private static final Theme REGI = new Theme("REGI", "Raum und Umwelt");
    private static final Theme ECON = new Theme("ECON", "Wirtschaft und Finanzen");
    private static final Theme GOVE = new Theme("GOVE", "Regierung und Verwaltung");

    private final CatalogSearchService service = new CatalogSearchService(new SearchProperties(10, 100), CLOCK);

    @Test
    void emptyQueryReturnsAllEntriesSortedByModifiedDescending() {
        SearchResult result = service.search(snapshot(), new SearchQuery("", SearchFilters.empty(), SortMode.MODIFIED_DESC));

        assertThat(result.entries())
                .extracting(CatalogEntry::identifier)
                .containsExactly("steuerfuss-gemeinden", "gemeindegrenzen", "verkehrszaehlstellen", "archiv");
        assertThat(result.totalElements()).isEqualTo(4);
        assertThat(result.page()).isEqualTo(1);
        assertThat(result.totalPages()).isEqualTo(1);
    }

    @Test
    void findsDatasetsAndSeriesByIndexedTextFields() {
        assertThat(search("Steuerfuss")).containsExactly("steuerfuss-gemeinden");
        assertThat(search("gemeindegrenzen")).first().isEqualTo("gemeindegrenzen");
        assertThat(search("Gemeinden")).contains("steuerfuss-gemeinden", "gemeindegrenzen");
        assertThat(search("Amt für Finanzen")).containsExactly("steuerfuss-gemeinden");
        assertThat(search("Raum Umwelt")).contains("gemeindegrenzen", "verkehrszaehlstellen");
        assertThat(search("zaehl")).containsExactly("verkehrszaehlstellen");
        assertThat(search("steue")).containsExactly("steuerfuss-gemeinden");
        assertThat(search("grenz april")).containsExactly("gemeindegrenzen");
    }

    @Test
    void exactIdentifierRanksFirst() {
        SearchResult result = service.search(
                snapshot(),
                new SearchQuery("gemeindegrenzen", SearchFilters.empty(), SortMode.MODIFIED_DESC));

        assertThat(result.entries())
                .extracting(CatalogEntry::identifier)
                .first()
                .isEqualTo("gemeindegrenzen");
        assertThat(result.hits().getFirst().score()).isGreaterThan(0.0f);
    }

    @Test
    void seriesIssuesInfluenceSearchabilityWithoutReturningIssuesAsTopLevelHits() {
        SearchResult result = service.search(
                snapshot(),
                new SearchQuery("April 2025", SearchFilters.empty(), SortMode.MODIFIED_DESC));

        assertThat(result.entries())
                .extracting(CatalogEntry::identifier)
                .containsExactly("gemeindegrenzen");
    }

    @Test
    void multiWordQueriesUseAndAcrossTokensAndOrAcrossFields() {
        assertThat(search("steuerfuss finanzen")).containsExactly("steuerfuss-gemeinden");
        assertThat(search("grenzen april")).containsExactly("gemeindegrenzen");
        assertThat(search("grenzen statistik")).isEmpty();
    }

    @Test
    void filtersUseOrWithinCategoryAndAndAcrossCategories() {
        var filters = new SearchFilters(Set.of("REGI", "GOVE"), Set.of("agi"), Optional.empty(), Set.of());

        SearchResult result = service.search(snapshot(), new SearchQuery("", filters, SortMode.TITLE_ASC));

        assertThat(result.entries())
                .extracting(CatalogEntry::identifier)
                .containsExactly("gemeindegrenzen", "verkehrszaehlstellen");
    }

    @Test
    void filtersByModifiedDateRanges() {
        var recent = new SearchFilters(Set.of(), Set.of(), Optional.of(ModifiedDateRange.LAST_30_DAYS), Set.of());
        var older = new SearchFilters(Set.of(), Set.of(), Optional.of(ModifiedDateRange.OLDER), Set.of());

        assertThat(service.search(snapshot(), new SearchQuery("", recent, SortMode.MODIFIED_DESC)).entries())
                .extracting(CatalogEntry::identifier)
                .containsExactly("steuerfuss-gemeinden", "gemeindegrenzen");
        assertThat(service.search(snapshot(), new SearchQuery("", older, SortMode.MODIFIED_DESC)).entries())
                .extracting(CatalogEntry::identifier)
                .containsExactly("archiv");
    }

    @Test
    void filtersByResourceType() {
        var filters = new SearchFilters(Set.of(), Set.of(), Optional.empty(), Set.of(CatalogEntryType.DATASET_SERIES));

        SearchResult result = service.search(snapshot(), new SearchQuery("", filters, SortMode.TITLE_ASC));

        assertThat(result.entries())
                .extracting(CatalogEntry::identifier)
                .containsExactly("gemeindegrenzen");
    }

    @Test
    void sortsByTitleWhenRequestedAfterTextSearch() {
        SearchResult result = service.search(snapshot(), new SearchQuery("Gemeinden", SearchFilters.empty(), SortMode.TITLE_ASC));

        assertThat(result.entries())
                .extracting(CatalogEntry::identifier)
                .containsExactly("gemeindegrenzen", "steuerfuss-gemeinden");
    }

    @Test
    void textSearchUsesModifiedDescendingWhenNoAlternateSortIsRequested() {
        SearchResult result = service.search(snapshot(), new SearchQuery("Gemeinden", SearchFilters.empty(), SortMode.MODIFIED_DESC));

        assertThat(result.entries())
                .extracting(CatalogEntry::identifier)
                .containsExactly("steuerfuss-gemeinden", "gemeindegrenzen");
    }

    @Test
    void serviceSidePaginationReturnsRequestedSliceWithoutUiContractChanges() {
        SearchResult result = service.search(
                snapshot(),
                new SearchQuery("", SearchFilters.empty(), SortMode.TITLE_ASC, PageRequest.of(2, 2)));

        assertThat(result.entries())
                .extracting(CatalogEntry::identifier)
                .containsExactly("steuerfuss-gemeinden", "verkehrszaehlstellen");
        assertThat(result.totalElements()).isEqualTo(4);
        assertThat(result.page()).isEqualTo(2);
        assertThat(result.size()).isEqualTo(2);
        assertThat(result.totalPages()).isEqualTo(2);
    }

    @Test
    void invalidQuerySyntaxDoesNotCrashSearch() {
        assertThatCode(() -> service.search(
                        snapshot(),
                        new SearchQuery("+:/( wasser", SearchFilters.empty(), SortMode.MODIFIED_DESC)))
                .doesNotThrowAnyException();
    }

    @Test
    void secondaryFieldsRemainSearchableWithoutGuaranteedSubstring() {
        assertThat(search("verwaltungsdaten")).containsExactly("archiv");
        assertThat(search("amt finanzen")).containsExactly("steuerfuss-gemeinden");
    }

    @Test
    void appliesJavaFiltersAfterTheCompleteLuceneHitSet() {
        List<DatasetEntry> entries = java.util.stream.IntStream.range(0, 501)
                .mapToObj(index -> new DatasetEntry(
                        "target-" + index,
                        "Target entry " + index,
                        "Searchable target entry.",
                        AGI,
                        AGI,
                        List.of(new Theme(index == 500 ? "wanted" : "other", index == 500 ? "Wanted" : "Other")),
                        List.of("Target"),
                        LocalDate.parse("2026-06-01"),
                        AccessLevel.OPEN,
                        List.of(new DistributionLink(URI.create("https://example.com/target-" + index), DistributionFormat.CSV))))
                .toList();
        var catalog = new Catalog(entries, List.of());
        var index = new CatalogSearchIndexBuilder(new CatalogDocumentMapper()).build(catalog.topLevelEntries());
        var snapshot = CatalogSnapshot.of(
                catalog,
                Instant.parse("2026-06-15T00:00:00Z"),
                Duration.ZERO,
                CatalogTestArtifacts.published("many-hits"),
                CatalogTestArtifacts.duckDb("many-hits-duckdb"),
                index);
        var filters = new SearchFilters(Set.of("wanted"), Set.of(), Optional.empty(), Set.of());

        assertThat(service.search(snapshot, new SearchQuery("Target", filters, SortMode.MODIFIED_DESC)).entries())
                .extracting(CatalogEntry::identifier)
                .containsExactly("target-500");
    }

    private List<String> search(String query) {
        return service.search(snapshot(), new SearchQuery(query, SearchFilters.empty(), SortMode.MODIFIED_DESC))
                .entries().stream()
                .map(CatalogEntry::identifier)
                .toList();
    }

    private static CatalogSnapshot snapshot() {
        Catalog catalog = catalog();
        var index = new CatalogSearchIndexBuilder(new CatalogDocumentMapper()).build(catalog.topLevelEntries());
        return CatalogSnapshot.of(
                catalog,
                Instant.parse("2026-06-15T00:00:00Z"),
                Duration.ZERO,
                CatalogTestArtifacts.published("test"),
                CatalogTestArtifacts.duckDb("test-duckdb"),
                index);
    }

    private static Catalog catalog() {
        return new Catalog(
                List.of(
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
                                List.of("Archiv"))),
                List.of(new DatasetSeriesEntry(
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
                                issue("gemeindegrenzen-2025", "Gemeindegrenzen April 2025", "April 2025", LocalDate.parse("2025-04-18"), false)))));
    }

    private static DatasetEntry dataset(
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
