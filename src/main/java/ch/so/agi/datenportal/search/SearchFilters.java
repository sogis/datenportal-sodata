package ch.so.agi.datenportal.search;

import ch.so.agi.datenportal.catalog.domain.CatalogEntryType;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public record SearchFilters(
        Set<String> themes,
        Set<String> offices,
        Optional<ModifiedDateRange> modifiedRange,
        Set<CatalogEntryType> resourceTypes) {

    public SearchFilters {
        themes = copyStringSet(themes);
        offices = copyStringSet(offices);
        modifiedRange = Objects.requireNonNullElse(modifiedRange, Optional.empty());
        resourceTypes = Set.copyOf(Objects.requireNonNullElse(resourceTypes, Set.of()));
    }

    public static SearchFilters empty() {
        return new SearchFilters(Set.of(), Set.of(), Optional.empty(), Set.of());
    }

    public boolean isEmpty() {
        return themes.isEmpty() && offices.isEmpty() && modifiedRange.isEmpty() && resourceTypes.isEmpty();
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
