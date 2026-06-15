package ch.so.agi.datenportal.search;

import java.util.Objects;

public record SearchQuery(String q, SearchFilters filters, SortMode sortMode) {

    public SearchQuery {
        q = q == null ? "" : q.trim();
        filters = Objects.requireNonNullElse(filters, SearchFilters.empty());
        sortMode = Objects.requireNonNullElse(sortMode, SortMode.defaultMode());
    }

    public boolean hasTextQuery() {
        return !q.isBlank();
    }
}
