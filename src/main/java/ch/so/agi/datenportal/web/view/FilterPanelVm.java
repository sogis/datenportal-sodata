package ch.so.agi.datenportal.web.view;

import java.util.List;

public record FilterPanelVm(
        List<FilterGroupVm> groups,
        List<FilterChipVm> chips,
        String resetHref,
        boolean hasActiveFilters,
        int activeValueCount,
        String mobileFilterHref) {

    public FilterPanelVm {
        groups = List.copyOf(groups);
        chips = List.copyOf(chips);
    }
}
