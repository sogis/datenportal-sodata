package ch.so.agi.datenportal.search;

import ch.so.agi.datenportal.catalog.domain.CatalogEntry;
import ch.so.agi.datenportal.catalog.domain.CatalogSnapshot;
import ch.so.agi.datenportal.config.SearchProperties;
import java.time.Clock;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public final class CatalogSearchService {

    private static final Comparator<CatalogEntry> MODIFIED_DESC =
            Comparator.comparing(CatalogEntry::modified)
                    .reversed()
                    .thenComparing(CatalogEntry::title, String.CASE_INSENSITIVE_ORDER);

    private static final Comparator<CatalogEntry> TITLE_ASC =
            Comparator.comparing(CatalogEntry::title, String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(CatalogEntry::modified, Comparator.reverseOrder());

    private final SearchProperties properties;
    private final Clock clock;

    public CatalogSearchService(SearchProperties properties, Clock clock) {
        this.properties = Objects.requireNonNull(properties, "properties must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    public SearchResult search(CatalogSnapshot snapshot, SearchQuery query) {
        Objects.requireNonNull(snapshot, "snapshot must not be null");
        Objects.requireNonNull(query, "query must not be null");

        var effectiveQuery = new SearchQuery(
                query.q(),
                query.filters(),
                query.sortMode(),
                properties.normalize(query.pageRequest()));

        Map<String, SearchHit> hitsById = new LinkedHashMap<>();
        List<CatalogEntry> entries = effectiveQuery.hasTextQuery()
                ? luceneEntries(snapshot, effectiveQuery, hitsById)
                : snapshot.visibleEntries();

        var filteredAndSorted = applySort(
                entries.stream()
                        .filter(entry -> matchesFilters(entry, effectiveQuery.filters()))
                        .toList(),
                effectiveQuery);
        return paginate(filteredAndSorted, hitsById, effectiveQuery);
    }

    private List<CatalogEntry> luceneEntries(
            CatalogSnapshot snapshot,
            SearchQuery query,
            Map<String, SearchHit> hitsById) {
        var hits = snapshot.searchIndex().search(query.q(), properties.maxResults());
        hits.forEach(hit -> hitsById.put(hit.entryId(), hit));
        return hits.stream()
                .map(hit -> snapshot.findVisibleEntry(hit.entryId()))
                .flatMap(java.util.Optional::stream)
                .toList();
    }

    private boolean matchesFilters(CatalogEntry entry, SearchFilters filters) {
        return matchesThemes(entry, filters)
                && matchesOffices(entry, filters)
                && matchesModifiedRanges(entry, filters)
                && matchesResourceTypes(entry, filters);
    }

    private boolean matchesThemes(CatalogEntry entry, SearchFilters filters) {
        return filters.themes().isEmpty()
                || entry.themes().stream().anyMatch(theme -> filters.themes().contains(theme.identifier()));
    }

    private boolean matchesOffices(CatalogEntry entry, SearchFilters filters) {
        return filters.offices().isEmpty() || filters.offices().contains(entry.creator().identifier());
    }

    private boolean matchesModifiedRanges(CatalogEntry entry, SearchFilters filters) {
        return filters.modifiedRange().isEmpty()
                || filters.modifiedRange().orElseThrow().matches(entry.modified(), clock);
    }

    private boolean matchesResourceTypes(CatalogEntry entry, SearchFilters filters) {
        return filters.resourceTypes().isEmpty()
                || filters.resourceTypes().contains(entry.type());
    }

    private List<CatalogEntry> applySort(List<CatalogEntry> entries, SearchQuery query) {
        if (query.sortMode() == SortMode.TITLE_ASC) {
            return entries.stream().sorted(TITLE_ASC).toList();
        }
        return entries.stream().sorted(MODIFIED_DESC).toList();
    }

    private SearchResult paginate(
            List<CatalogEntry> entries,
            Map<String, SearchHit> hitsById,
            SearchQuery query) {
        int totalElements = entries.size();
        var pageRequest = query.pageRequest();

        List<CatalogEntry> pageEntries;
        int page;
        int size;
        int totalPages;

        if (pageRequest.paged()) {
            int offset = Math.min(pageRequest.offset(), totalElements);
            int end = Math.min(offset + pageRequest.size(), totalElements);
            pageEntries = entries.subList(offset, end);
            page = pageRequest.page();
            size = pageRequest.size();
            totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        } else {
            pageEntries = entries;
            page = 1;
            size = totalElements;
            totalPages = 1;
        }

        var pageHits = pageEntries.stream()
                .map(entry -> hitsById.getOrDefault(entry.identifier(), SearchHit.withoutHighlight(entry.identifier(), 0.0f)))
                .toList();

        return new SearchResult(pageEntries, pageHits, totalElements, page, size, totalPages, query);
    }
}
