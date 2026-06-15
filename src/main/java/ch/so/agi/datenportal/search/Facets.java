package ch.so.agi.datenportal.search;

import java.util.List;

public record Facets(
        List<FacetValue> themes,
        List<FacetValue> offices,
        List<FacetValue> modifiedRanges,
        List<FacetValue> resourceTypes) {

    public Facets {
        themes = List.copyOf(themes);
        offices = List.copyOf(offices);
        modifiedRanges = List.copyOf(modifiedRanges);
        resourceTypes = List.copyOf(resourceTypes);
    }
}
