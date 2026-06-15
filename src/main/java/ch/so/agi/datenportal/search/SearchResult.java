package ch.so.agi.datenportal.search;

import ch.so.agi.datenportal.catalog.domain.CatalogEntry;
import java.util.List;
import java.util.Objects;

public record SearchResult(
        List<CatalogEntry> entries,
        int totalElements,
        SearchQuery query) {

    public SearchResult {
        entries = List.copyOf(entries);
        Objects.requireNonNull(query, "query must not be null");
    }
}
