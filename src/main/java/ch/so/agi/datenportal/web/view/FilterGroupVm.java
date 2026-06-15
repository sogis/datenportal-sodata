package ch.so.agi.datenportal.web.view;

import java.util.List;

public record FilterGroupVm(
        String id,
        String label,
        String parameterName,
        String collapsedLabel,
        int selectedCount,
        List<FilterOptionVm> options) {

    public FilterGroupVm {
        options = List.copyOf(options);
    }
}
