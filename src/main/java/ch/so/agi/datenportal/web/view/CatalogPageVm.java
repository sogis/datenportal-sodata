package ch.so.agi.datenportal.web.view;

import java.util.List;

public record CatalogPageVm(
        PageChromeVm chrome,
        String title,
        String lead,
        String secondaryLead,
        List<CatalogEntrySummaryVm> entries) {

    public CatalogPageVm {
        entries = List.copyOf(entries);
    }

    public boolean hasEntries() {
        return !entries.isEmpty();
    }
}
