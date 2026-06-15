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

    public String withView(CatalogQueryParams params, ViewMode viewMode) {
        var query = baseQuery(params);
        query.put("view", List.of(viewMode.parameterValue()));
        return buildUrl(query);
    }

    public String withSort(CatalogQueryParams params, SortMode sortMode) {
        var query = baseQuery(params);
        query.put("sort", List.of(sortMode.parameterValue()));
        return buildUrl(query);
    }

    public String removeFilter(CatalogQueryParams params, String parameterName, String value) {
        var query = baseQuery(params);
        var values = new ArrayList<>(query.getOrDefault(parameterName, List.of()));
        values.removeIf(existing -> existing.equals(value));
        if (values.isEmpty()) {
            query.remove(parameterName);
        } else {
            query.put(parameterName, values);
        }
        return buildUrl(query);
    }

    public String resetFilters(CatalogQueryParams params) {
        var normalized = params.normalized();
        var query = new LinkedHashMap<String, List<String>>();
        query.put("view", List.of(normalized.viewValue()));
        query.put("sort", List.of(normalized.sortValue()));
        return buildUrl(query);
    }

    public String toggleExpanded(CatalogQueryParams params, String seriesId) {
        var query = baseQuery(params);
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

    private static LinkedHashMap<String, List<String>> baseQuery(CatalogQueryParams params) {
        var normalized = params.normalized();
        var query = new LinkedHashMap<String, List<String>>();
        putIfPresent(query, "q", normalized.q());
        putAllIfPresent(query, "theme", normalized.theme());
        putAllIfPresent(query, "office", normalized.office());
        putAllIfPresent(query, "modified", normalized.modified());
        putAllIfPresent(query, "resourceType", normalized.resourceType());
        query.put("view", List.of(normalized.viewValue()));
        query.put("sort", List.of(normalized.sortValue()));
        putAllIfPresent(query, "expanded", normalized.expanded());
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
        if (query.isEmpty()) {
            return DATASETS_PATH;
        }

        var builder = new StringBuilder(DATASETS_PATH);
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
