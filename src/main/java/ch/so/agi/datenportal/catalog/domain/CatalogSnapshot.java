package ch.so.agi.datenportal.catalog.domain;

import ch.so.agi.datenportal.search.CatalogSearchIndex;
import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public record CatalogSnapshot(
        Catalog catalog,
        List<CatalogEntry> visibleEntries,
        Map<String, CatalogEntry> visibleEntriesByIdentifier,
        Map<String, CatalogEntry> allEntriesByIdentifier,
        Instant loadedAt,
        String sourceDescription,
        CatalogSearchIndex searchIndex) implements AutoCloseable {

    private static final Comparator<CatalogEntry> VISIBLE_ENTRY_ORDER =
            Comparator.comparing(CatalogEntry::modified)
                    .reversed()
                    .thenComparing(CatalogEntry::title);

    public CatalogSnapshot {
        Objects.requireNonNull(catalog, "catalog must not be null");
        visibleEntries = List.copyOf(visibleEntries);
        visibleEntriesByIdentifier = Map.copyOf(visibleEntriesByIdentifier);
        allEntriesByIdentifier = Map.copyOf(allEntriesByIdentifier);
        Objects.requireNonNull(loadedAt, "loadedAt must not be null");
        Objects.requireNonNull(sourceDescription, "sourceDescription must not be null");
        Objects.requireNonNull(searchIndex, "searchIndex must not be null");
    }

    public static CatalogSnapshot of(Catalog catalog, Instant loadedAt, String sourceDescription) {
        return of(catalog, loadedAt, sourceDescription, CatalogSearchIndex.empty());
    }

    public static CatalogSnapshot of(
            Catalog catalog,
            Instant loadedAt,
            String sourceDescription,
            CatalogSearchIndex searchIndex) {
        Objects.requireNonNull(catalog, "catalog must not be null");
        Objects.requireNonNull(loadedAt, "loadedAt must not be null");
        Objects.requireNonNull(sourceDescription, "sourceDescription must not be null");
        Objects.requireNonNull(searchIndex, "searchIndex must not be null");

        var visibleEntries = catalog.topLevelEntries().stream()
                .sorted(VISIBLE_ENTRY_ORDER)
                .toList();

        var visibleEntriesByIdentifier = new LinkedHashMap<String, CatalogEntry>();
        visibleEntries.forEach(entry -> register(visibleEntriesByIdentifier, entry));

        var allEntriesByIdentifier = new LinkedHashMap<String, CatalogEntry>();
        catalog.datasets().forEach(entry -> register(allEntriesByIdentifier, entry));
        catalog.datasetSeries().forEach(entry -> {
            register(allEntriesByIdentifier, entry);
            entry.issuesNewestFirst().forEach(issue -> register(allEntriesByIdentifier, issue));
        });

        return new CatalogSnapshot(
                catalog,
                visibleEntries,
                visibleEntriesByIdentifier,
                allEntriesByIdentifier,
                loadedAt,
                sourceDescription,
                searchIndex);
    }

    public Optional<CatalogEntry> findVisibleEntry(String identifier) {
        return Optional.ofNullable(visibleEntriesByIdentifier.get(identifier));
    }

    public Optional<CatalogEntry> findAnyEntry(String identifier) {
        return Optional.ofNullable(allEntriesByIdentifier.get(identifier));
    }

    public boolean isEmpty() {
        return visibleEntries.isEmpty();
    }

    @Override
    public void close() {
        searchIndex.close();
    }

    private static void register(Map<String, CatalogEntry> entries, CatalogEntry entry) {
        var previous = entries.putIfAbsent(entry.identifier(), entry);
        if (previous != null) {
            throw new IllegalArgumentException("Duplicate catalog identifier: " + entry.identifier());
        }
    }
}
