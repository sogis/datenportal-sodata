package ch.so.agi.datenportal.web;

import ch.so.agi.datenportal.search.ModifiedDateRange;
import ch.so.agi.datenportal.search.PageRequest;
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

    private static final int MIN_SEARCH_QUERY_LENGTH = 3;

    private String q = "";
    private List<String> theme = List.of();
    private List<String> office = List.of();
    private List<String> modified = List.of();
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

    public SearchQuery toSearchQuery() {
        return new SearchQuery(
                q(),
                new SearchFilters(
                        selectedThemes(),
                        selectedOffices(),
                        selectedModifiedRange(),
                        Set.of()),
                sortMode(),
                PageRequest.unpaged());
    }

    public CatalogQueryParams normalized() {
        var normalized = new CatalogQueryParams();
        normalized.setQ(normalizeQuery(q()));
        normalized.setTheme(copyDistinct(theme));
        normalized.setOffice(copyDistinct(office));
        normalized.setModified(selectedModifiedRange()
                .map(range -> List.of(range.parameterValue()))
                .orElseGet(List::of));
        normalized.setSort(sortValue());
        normalized.setView(viewValue());
        normalized.setPage(1);
        normalized.setSize(0);
        normalized.setExpanded(copyDistinct(expanded));
        return normalized;
    }

    public boolean hasActiveFilters() {
        return !theme.isEmpty() || !office.isEmpty() || !modified.isEmpty();
    }

    private static String normalizeQuery(String value) {
        var trimmed = value == null ? "" : value.trim();
        return trimmed.length() >= MIN_SEARCH_QUERY_LENGTH ? trimmed : "";
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
}
