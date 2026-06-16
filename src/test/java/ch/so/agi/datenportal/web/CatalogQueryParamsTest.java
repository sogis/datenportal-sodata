package ch.so.agi.datenportal.web;

import static org.assertj.core.api.Assertions.assertThat;

import ch.so.agi.datenportal.catalog.domain.CatalogEntryType;
import ch.so.agi.datenportal.search.ModifiedDateRange;
import org.junit.jupiter.api.Test;

class CatalogQueryParamsTest {

    @Test
    void normalizedUsesFirstValidModifiedValueAndOnlyCanonicalResourceTypes() {
        var params = new CatalogQueryParams();
        params.setModified(java.util.List.of("invalid", "last30", "older"));
        params.setResourceType(java.util.List.of("dataset", "csv", "series", "dataset"));

        var normalized = params.normalized();

        assertThat(normalized.modified()).containsExactly("last30");
        assertThat(normalized.resourceType()).containsExactly("dataset", "series");
        assertThat(normalized.selectedModifiedRange()).contains(ModifiedDateRange.LAST_30_DAYS);
        assertThat(normalized.selectedResourceTypes()).containsExactlyInAnyOrder(
                CatalogEntryType.DATASET,
                CatalogEntryType.DATASET_SERIES);
    }

    @Test
    void toSearchQueryCarriesPaginationValues() {
        var params = new CatalogQueryParams();
        params.setPage(3);
        params.setSize(25);

        var query = params.normalized().toSearchQuery();

        assertThat(query.pageRequest().paged()).isTrue();
        assertThat(query.pageRequest().page()).isEqualTo(3);
        assertThat(query.pageRequest().size()).isEqualTo(25);
    }

    @Test
    void hasActiveFiltersTracksCurrentSelections() {
        var params = new CatalogQueryParams();
        assertThat(params.hasActiveFilters()).isFalse();

        params.setOffice(java.util.List.of("agi"));

        assertThat(params.hasActiveFilters()).isTrue();
    }
}
