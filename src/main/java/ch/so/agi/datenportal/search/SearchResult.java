package ch.so.agi.datenportal.search;

import ch.so.agi.datenportal.catalog.domain.CatalogEntry;
import java.util.List;
import java.util.Objects;

public record SearchResult(
        List<CatalogEntry> entries,
        List<SearchHit> hits,
        int totalElements,
        int page,
        int size,
        int totalPages,
        SearchQuery query) {

    public SearchResult {
        entries = List.copyOf(entries);
        hits = List.copyOf(hits);
        Objects.requireNonNull(query, "query must not be null");
    }

    public SearchResult(List<CatalogEntry> entries, int totalElements, SearchQuery query) {
        this(
                entries,
                entries.stream()
                        .map(entry -> SearchHit.withoutHighlight(entry.identifier(), 0.0f))
                        .toList(),
                totalElements,
                1,
                totalElements,
                1,
                query);
    }
}
