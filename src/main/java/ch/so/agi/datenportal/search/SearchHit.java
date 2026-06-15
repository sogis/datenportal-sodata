package ch.so.agi.datenportal.search;

import java.util.List;
import java.util.Objects;

public record SearchHit(String entryId, float score, List<String> highlights) {

    public SearchHit {
        Objects.requireNonNull(entryId, "entryId must not be null");
        highlights = List.copyOf(Objects.requireNonNullElse(highlights, List.of()));
    }

    public static SearchHit withoutHighlight(String entryId, float score) {
        return new SearchHit(entryId, score, List.of());
    }
}
