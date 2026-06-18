package ch.so.agi.datenportal.search;

import java.util.List;

public record Facets(
        List<FacetValue> themes,
        List<FacetValue> offices,
        List<FacetValue> modifiedRanges) {

    public Facets {
        themes = List.copyOf(themes);
        offices = List.copyOf(offices);
        modifiedRanges = List.copyOf(modifiedRanges);
    }
}
