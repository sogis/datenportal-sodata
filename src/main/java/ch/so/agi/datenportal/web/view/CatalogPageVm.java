package ch.so.agi.datenportal.web.view;

import ch.so.agi.datenportal.web.CatalogQueryParams;

public record CatalogPageVm(
        PageChromeVm chrome,
        String title,
        String lead,
        String secondaryLead,
        CatalogQueryParams queryParams,
        FilterPanelVm filterPanel,
        ResultsVm results) {}
