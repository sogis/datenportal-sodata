package ch.so.agi.datenportal.web.view;

import ch.so.agi.datenportal.web.CatalogQueryParams;
import ch.so.agi.datenportal.support.metadata.MetadataTextRenderer.Html;

public record CatalogPageVm(
        PageChromeVm chrome,
        String title,
        Html lead,
        String secondaryLead,
        CatalogQueryParams queryParams,
        FilterPanelVm filterPanel,
        ResultsVm results) {}
