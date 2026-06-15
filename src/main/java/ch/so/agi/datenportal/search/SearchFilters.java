package ch.so.agi.datenportal.search;

import ch.so.agi.datenportal.catalog.domain.DistributionFormat;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public record SearchFilters(
        Set<String> themes,
        Set<String> offices,
        Set<ModifiedDateRange> modifiedRanges,
        Set<DistributionFormat> resourceTypes) {

    public SearchFilters {
        themes = copyStringSet(themes);
        offices = copyStringSet(offices);
        modifiedRanges = Set.copyOf(Objects.requireNonNullElse(modifiedRanges, Set.of()));
        resourceTypes = Set.copyOf(Objects.requireNonNullElse(resourceTypes, Set.of()));
    }

    public static SearchFilters empty() {
        return new SearchFilters(Set.of(), Set.of(), Set.of(), Set.of());
    }

    public boolean isEmpty() {
        return themes.isEmpty() && offices.isEmpty() && modifiedRanges.isEmpty() && resourceTypes.isEmpty();
    }

    private static Set<String> copyStringSet(Set<String> values) {
        var copy = new LinkedHashSet<String>();
        Objects.requireNonNullElse(values, Set.<String>of()).stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .forEach(copy::add);
        return Set.copyOf(copy);
    }
}
