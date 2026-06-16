package ch.so.agi.datenportal.web;

import ch.so.agi.datenportal.catalog.domain.CatalogEntryType;
import ch.so.agi.datenportal.search.ModifiedDateRange;
import ch.so.agi.datenportal.search.SearchFilters;
import ch.so.agi.datenportal.search.SearchQuery;
import ch.so.agi.datenportal.search.SortMode;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class CatalogQueryParams {

    private String q = "";
    private List<String> theme = List.of();
    private List<String> office = List.of();
    private List<String> modified = List.of();
    private List<String> resourceType = List.of();
    private String sort = SortMode.defaultMode().parameterValue();
    private String view = ViewMode.defaultMode().parameterValue();
    private int page = 1;
    private int size = 0;
    private List<String> expanded = List.of();

    public String getQ() {
        return q;
    }

    public void setQ(String q) {
        this.q = q == null ? "" : q;
    }

    public List<String> getTheme() {
        return theme;
    }

    public void setTheme(List<String> theme) {
        this.theme = copyList(theme);
    }

    public List<String> getOffice() {
        return office;
    }

    public void setOffice(List<String> office) {
        this.office = copyList(office);
    }

    public List<String> getModified() {
        return modified;
    }

    public void setModified(List<String> modified) {
        this.modified = copyList(modified);
    }

    public List<String> getResourceType() {
        return resourceType;
    }

    public void setResourceType(List<String> resourceType) {
        this.resourceType = copyList(resourceType);
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort == null ? "" : sort;
    }

    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view == null ? "" : view;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public List<String> getExpanded() {
        return expanded;
    }

    public void setExpanded(List<String> expanded) {
        this.expanded = copyList(expanded);
    }

    public String q() {
        return q == null ? "" : q;
    }

    public List<String> theme() {
        return theme;
    }

    public List<String> office() {
        return office;
    }

    public List<String> modified() {
        return modified;
    }

    public List<String> resourceType() {
        return resourceType;
    }

    public List<String> expanded() {
        return expanded;
    }

    public SortMode sortMode() {
        return SortMode.fromParameterValue(sort).orElse(SortMode.defaultMode());
    }

    public ViewMode viewMode() {
        return ViewMode.fromParameterValue(view).orElse(ViewMode.defaultMode());
    }

    public String sortValue() {
        return sortMode().parameterValue();
    }

    public String viewValue() {
        return viewMode().parameterValue();
    }

    public int page() {
        return page;
    }

    public int size() {
        return size;
    }

    public Set<String> selectedThemes() {
        return new LinkedHashSet<>(theme);
    }

    public Set<String> selectedOffices() {
        return new LinkedHashSet<>(office);
    }

    public Optional<ModifiedDateRange> selectedModifiedRange() {
        return modified.stream()
                .map(ModifiedDateRange::fromParameterValue)
                .flatMap(Optional::stream)
                .findFirst();
    }

    public Set<CatalogEntryType> selectedResourceTypes() {
        var types = new LinkedHashSet<CatalogEntryType>();
        for (String value : resourceType) {
            toResourceType(value).ifPresent(types::add);
        }
        return types;
    }

    public SearchQuery toSearchQuery() {
        return new SearchQuery(
                q(),
                new SearchFilters(
                        selectedThemes(),
                        selectedOffices(),
                        selectedModifiedRange(),
                        selectedResourceTypes()),
                sortMode(),
                ch.so.agi.datenportal.search.PageRequest.of(Math.max(1, page()), Math.max(0, size())));
    }

    public CatalogQueryParams normalized() {
        var normalized = new CatalogQueryParams();
        normalized.setQ(q().trim());
        normalized.setTheme(copyDistinct(theme));
        normalized.setOffice(copyDistinct(office));
        normalized.setModified(selectedModifiedRange()
                .map(range -> List.of(range.parameterValue()))
                .orElseGet(List::of));
        normalized.setResourceType(selectedResourceTypes().stream()
                .map(CatalogQueryParams::toResourceTypeValue)
                .toList());
        normalized.setSort(sortValue());
        normalized.setView(viewValue());
        normalized.setPage(Math.max(1, page()));
        normalized.setSize(Math.max(0, size()));
        normalized.setExpanded(copyDistinct(expanded));
        return normalized;
    }

    public boolean hasActiveFilters() {
        return !theme.isEmpty() || !office.isEmpty() || !modified.isEmpty() || !resourceType.isEmpty();
    }

    private static List<String> copyList(List<String> values) {
        return List.copyOf(Objects.requireNonNullElse(values, List.<String>of()));
    }

    private static List<String> copyDistinct(List<String> values) {
        var distinct = new LinkedHashSet<String>();
        Objects.requireNonNullElse(values, List.<String>of()).stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .forEach(distinct::add);
        return new ArrayList<>(distinct);
    }

    private static Optional<CatalogEntryType> toResourceType(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }

        return switch (value.trim().toLowerCase()) {
            case "dataset" -> Optional.of(CatalogEntryType.DATASET);
            case "series" -> Optional.of(CatalogEntryType.DATASET_SERIES);
            default -> Optional.empty();
        };
    }

    private static String toResourceTypeValue(CatalogEntryType type) {
        return switch (type) {
            case DATASET -> "dataset";
            case DATASET_SERIES -> "series";
            case DATASET_ISSUE -> "issue";
        };
    }
}
