package ch.so.agi.datenportal.web;

import static org.assertj.core.api.Assertions.assertThat;

import ch.so.agi.datenportal.search.ModifiedDateRange;
import org.junit.jupiter.api.Test;

class CatalogQueryParamsTest {

    @Test
    void normalizedTreatsQueriesShorterThanThreeCharactersAsEmpty() {
        var shortQuery = new CatalogQueryParams();
        shortQuery.setQ(" ab ");

        var validQuery = new CatalogQueryParams();
        validQuery.setQ(" abc ");

        assertThat(shortQuery.normalized().q()).isEmpty();
        assertThat(validQuery.normalized().q()).isEqualTo("abc");
    }

    @Test
    void normalizedUsesFirstValidModifiedValue() {
        var params = new CatalogQueryParams();
        params.setModified(java.util.List.of("invalid", "last30", "older"));

        var normalized = params.normalized();

        assertThat(normalized.modified()).containsExactly("last30");
        assertThat(normalized.selectedModifiedRange()).contains(ModifiedDateRange.LAST_30_DAYS);
    }

    @Test
    void toSearchQueryIgnoresPaginationValuesForCatalogFlow() {
        var params = new CatalogQueryParams();
        params.setPage(3);
        params.setSize(25);

        var query = params.normalized().toSearchQuery();

        assertThat(query.pageRequest().paged()).isFalse();
        assertThat(query.pageRequest().page()).isEqualTo(1);
        assertThat(query.pageRequest().size()).isEqualTo(0);
        assertThat(query.filters().resourceTypes()).isEmpty();
    }

    @Test
    void hasActiveFiltersTracksCurrentSelections() {
        var params = new CatalogQueryParams();
        assertThat(params.hasActiveFilters()).isFalse();

        params.setOffice(java.util.List.of("agi"));

        assertThat(params.hasActiveFilters()).isTrue();
    }
}
