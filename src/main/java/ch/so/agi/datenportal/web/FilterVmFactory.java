package ch.so.agi.datenportal.web;

import ch.so.agi.datenportal.search.FacetValue;
import ch.so.agi.datenportal.search.Facets;
import ch.so.agi.datenportal.web.view.FilterChipVm;
import ch.so.agi.datenportal.web.view.FilterGroupVm;
import ch.so.agi.datenportal.web.view.FilterGroupType;
import ch.so.agi.datenportal.web.view.FilterOptionVm;
import ch.so.agi.datenportal.web.view.FilterPanelVm;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public final class FilterVmFactory {

    private final CatalogUrlFactory urlFactory;

    public FilterVmFactory(CatalogUrlFactory urlFactory) {
        this.urlFactory = urlFactory;
    }

    public FilterPanelVm create(CatalogQueryParams params, Facets facets) {
        var normalized = params.normalized();
        var groups = List.of(
                group(normalized, "theme", "Thema", "theme", "Alle Themen", FilterGroupType.MULTI_SELECT, normalized.theme(), facets.themes()),
                group(normalized, "office", "Fachstelle / Amt", "office", "Alle Fachstellen", FilterGroupType.MULTI_SELECT, normalized.office(), facets.offices()),
                group(normalized, "modified", "Publikationsdatum", "modified", "Alle Zeiträume", FilterGroupType.SINGLE_SELECT, normalized.modified(), facets.modifiedRanges()));

        var chips = new ArrayList<FilterChipVm>();
        chips.addAll(chipsFor(normalized, "Thema", "theme", normalized.theme(), facets.themes()));
        chips.addAll(chipsFor(normalized, "Fachstelle / Amt", "office", normalized.office(), facets.offices()));
        chips.addAll(chipsFor(normalized, "Publikationsdatum", "modified", normalized.modified(), facets.modifiedRanges()));

        return new FilterPanelVm(
                groups,
                chips,
                urlFactory.resetFilters(normalized),
                !chips.isEmpty(),
                chips.size(),
                urlFactory.mobileFilters(normalized));
    }

    private FilterGroupVm group(
            CatalogQueryParams params,
            String id,
            String label,
            String parameterName,
            String emptyLabel,
            FilterGroupType type,
            List<String> selected,
            List<FacetValue> values) {
        var selectedSet = Set.copyOf(selected);
        var options = values.stream()
                .map(value -> new FilterOptionVm(
                        value.value(),
                        value.label(),
                        value.count(),
                        selectedSet.contains(value.value()),
                        false))
                .toList();

        return new FilterGroupVm(
                id,
                label,
                parameterName,
                collapsedLabel(emptyLabel, selected, values),
                selected.size(),
                type,
                "modified".equals(id),
                urlFactory.filterPopover(params, id),
                urlFactory.resetFilterGroup(params, parameterName),
                options);
    }

    private List<FilterChipVm> chipsFor(
            CatalogQueryParams params,
            String groupLabel,
            String parameterName,
            List<String> selected,
            List<FacetValue> values) {
        Map<String, FacetValue> labelsByValue = values.stream()
                .collect(Collectors.toMap(FacetValue::value, Function.identity(), (left, right) -> left));

        return selected.stream()
                .map(value -> new FilterChipVm(
                        groupLabel,
                        labelsByValue.getOrDefault(value, new FacetValue(value, value, 0)).label(),
                        urlFactory.removeFilter(params, parameterName, value)))
                .toList();
    }

    private static String collapsedLabel(String emptyLabel, List<String> selected, List<FacetValue> values) {
        if (selected.isEmpty()) {
            return emptyLabel;
        }

        Map<String, String> labelsByValue = values.stream()
                .collect(Collectors.toMap(FacetValue::value, FacetValue::label, (left, right) -> left));
        var first = labelsByValue.getOrDefault(selected.getFirst(), selected.getFirst());
        if (selected.size() == 1) {
            return first;
        }
        return first + " +" + (selected.size() - 1);
    }
}
