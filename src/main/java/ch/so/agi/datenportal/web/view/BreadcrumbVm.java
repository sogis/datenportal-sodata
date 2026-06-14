package ch.so.agi.datenportal.web.view;

import java.util.List;

public record BreadcrumbVm(List<BreadcrumbItemVm> items) {

    public boolean isEmpty() {
        return items == null || items.isEmpty();
    }
}
