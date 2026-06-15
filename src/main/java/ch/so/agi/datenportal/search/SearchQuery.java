package ch.so.agi.datenportal.search;

import java.util.Objects;

public record SearchQuery(String q, SearchFilters filters, SortMode sortMode, PageRequest pageRequest) {

    public SearchQuery {
        q = q == null ? "" : q.trim();
        filters = Objects.requireNonNullElse(filters, SearchFilters.empty());
        sortMode = Objects.requireNonNullElse(sortMode, SortMode.defaultMode());
        pageRequest = Objects.requireNonNullElse(pageRequest, PageRequest.unpaged());
    }

    public SearchQuery(String q, SearchFilters filters, SortMode sortMode) {
        this(q, filters, sortMode, PageRequest.unpaged());
    }

    public boolean hasTextQuery() {
        return !q.isBlank();
    }
}
