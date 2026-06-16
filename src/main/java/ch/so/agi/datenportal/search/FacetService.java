package ch.so.agi.datenportal.search;

import ch.so.agi.datenportal.catalog.domain.CatalogEntry;
import ch.so.agi.datenportal.catalog.domain.CatalogEntryType;
import ch.so.agi.datenportal.catalog.domain.CatalogSnapshot;
import java.time.Clock;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public final class FacetService {

    private static final Comparator<FacetValue> BY_LABEL =
            Comparator.comparing(FacetValue::label, String.CASE_INSENSITIVE_ORDER);

    private final Clock clock;

    public FacetService(Clock clock) {
        this.clock = clock;
    }

    public Facets compute(CatalogSnapshot snapshot) {
        var themes = new LinkedHashMap<String, MutableFacet>();
        var offices = new LinkedHashMap<String, MutableFacet>();

        for (CatalogEntry entry : snapshot.visibleEntries()) {
            entry.themes().forEach(theme -> increment(themes, theme.identifier(), theme.displayName()));
            increment(offices, entry.creator().identifier(), entry.creator().displayName());
        }

        var modifiedRanges = java.util.Arrays.stream(ModifiedDateRange.values())
                .map(range -> new FacetValue(
                        range.parameterValue(),
                        range.label(),
                        snapshot.visibleEntries().stream()
                                .filter(entry -> range.matches(entry.modified(), clock))
                                .count()))
                .toList();

        var resourceTypes = java.util.List.of(
                new FacetValue(
                        "dataset",
                        CatalogEntryType.DATASET.label(),
                        snapshot.visibleEntries().stream()
                                .filter(entry -> entry.type() == CatalogEntryType.DATASET)
                                .count()),
                new FacetValue(
                        "series",
                        CatalogEntryType.DATASET_SERIES.label(),
                        snapshot.visibleEntries().stream()
                                .filter(entry -> entry.type() == CatalogEntryType.DATASET_SERIES)
                                .count()));

        return new Facets(
                toSortedFacetValues(themes),
                toSortedFacetValues(offices),
                modifiedRanges,
                resourceTypes);
    }

    private static void increment(Map<String, MutableFacet> facets, String value, String label) {
        facets.computeIfAbsent(value, ignored -> new MutableFacet(value, label)).increment();
    }

    private static java.util.List<FacetValue> toSortedFacetValues(Map<String, MutableFacet> facets) {
        return facets.values().stream()
                .map(MutableFacet::toFacetValue)
                .sorted(BY_LABEL)
                .toList();
    }

    private static final class MutableFacet {
        private final String value;
        private final String label;
        private long count;

        private MutableFacet(String value, String label) {
            this.value = value;
            this.label = label;
        }

        private void increment() {
            count++;
        }

        private FacetValue toFacetValue() {
            return new FacetValue(value, label, count);
        }
    }
}
