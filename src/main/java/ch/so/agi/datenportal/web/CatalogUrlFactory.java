package ch.so.agi.datenportal.web;

import ch.so.agi.datenportal.search.SortMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public final class CatalogUrlFactory {

    private static final String DATASETS_PATH = "/datasets";
    private static final String FILTER_POPOVER_PATH = "/datasets/filter-popover";
    private static final String MOBILE_FILTERS_PATH = "/datasets/mobile-filters";

    public String withView(CatalogQueryParams params, ViewMode viewMode) {
        var query = baseQuery(params, false);
        query.put("view", List.of(viewMode.parameterValue()));
        query.remove("expanded");
        return buildUrl(query);
    }

    public String withSort(CatalogQueryParams params, SortMode sortMode) {
        var query = baseQuery(params, false);
        query.put("sort", List.of(sortMode.parameterValue()));
        query.remove("expanded");
        return buildUrl(query);
    }

    public String removeFilter(CatalogQueryParams params, String parameterName, String value) {
        var query = baseQuery(params, false);
        var values = new ArrayList<>(query.getOrDefault(parameterName, List.of()));
        values.removeIf(existing -> existing.equals(value));
        if (values.isEmpty()) {
            query.remove(parameterName);
        } else {
            query.put(parameterName, values);
        }
        query.remove("expanded");
        return buildUrl(query);
    }

    public String resetFilters(CatalogQueryParams params) {
        var normalized = params.normalized();
        var query = new LinkedHashMap<String, List<String>>();
        putIfPresent(query, "q", normalized.q());
        query.put("view", List.of(normalized.viewValue()));
        query.put("sort", List.of(normalized.sortValue()));
        return buildUrl(query);
    }

    public String resetFilterGroup(CatalogQueryParams params, String parameterName) {
        var query = baseQuery(params, false);
        query.remove(parameterName);
        query.remove("expanded");
        return buildUrl(query);
    }

    public String filterPopover(CatalogQueryParams params, String filterId) {
        var query = baseQuery(params, true);
        query.put("filter", List.of(filterId));
        return buildPath(FILTER_POPOVER_PATH, query);
    }

    public String mobileFilters(CatalogQueryParams params) {
        return buildPath(MOBILE_FILTERS_PATH, baseQuery(params, true));
    }

    public String toggleExpanded(CatalogQueryParams params, String seriesId) {
        var query = baseQuery(params, true);
        var expanded = new ArrayList<>(query.getOrDefault("expanded", List.of()));
        if (expanded.contains(seriesId)) {
            expanded.removeIf(seriesId::equals);
        } else {
            expanded.add(seriesId);
        }
        if (expanded.isEmpty()) {
            query.remove("expanded");
        } else {
            query.put("expanded", expanded);
        }
        return buildUrl(query);
    }

    public String pathSegment(String value) {
        return encode(value);
    }

    public String datasetDetail(String identifier) {
        return "/datasets/" + pathSegment(identifier);
    }

    public String seriesDetail(String seriesIdentifier) {
        return "/series/" + pathSegment(seriesIdentifier);
    }

    public String currentIssueDetail(String seriesIdentifier) {
        return seriesDetail(seriesIdentifier) + "/issues/current";
    }

    public String issueDetail(String seriesIdentifier, String issueIdentifier) {
        return seriesDetail(seriesIdentifier) + "/issues/" + pathSegment(issueIdentifier);
    }

    private LinkedHashMap<String, List<String>> baseQuery(CatalogQueryParams params, boolean includeExpanded) {
        var normalized = params.normalized();
        var query = new LinkedHashMap<String, List<String>>();
        putIfPresent(query, "q", normalized.q());
        putAllIfPresent(query, "theme", normalized.theme());
        putAllIfPresent(query, "office", normalized.office());
        putAllIfPresent(query, "modified", normalized.modified());
        query.put("view", List.of(normalized.viewValue()));
        query.put("sort", List.of(normalized.sortValue()));
        if (includeExpanded) {
            putAllIfPresent(query, "expanded", normalized.expanded());
        }
        return query;
    }

    private static void putIfPresent(Map<String, List<String>> query, String name, String value) {
        if (value != null && !value.isBlank()) {
            query.put(name, List.of(value.trim()));
        }
    }

    private static void putAllIfPresent(Map<String, List<String>> query, String name, List<String> values) {
        if (!values.isEmpty()) {
            query.put(name, List.copyOf(values));
        }
    }

    private static String buildUrl(Map<String, List<String>> query) {
        return buildPath(DATASETS_PATH, query);
    }

    private static String buildPath(String path, Map<String, List<String>> query) {
        if (query.isEmpty()) {
            return path;
        }

        var builder = new StringBuilder(path);
        var separator = "?";
        for (var entry : query.entrySet()) {
            for (String value : entry.getValue()) {
                builder.append(separator)
                        .append(encode(entry.getKey()))
                        .append("=")
                        .append(encode(value));
                separator = "&";
            }
        }
        return builder.toString();
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
