package ch.so.agi.datenportal.web;

import ch.so.agi.datenportal.catalog.domain.CatalogSnapshot;
import ch.so.agi.datenportal.search.Facets;
import ch.so.agi.datenportal.search.SearchResult;
import ch.so.agi.datenportal.web.view.CatalogPageVm;
import org.springframework.stereotype.Component;

@Component
public final class HomePageVmFactory {

    private static final String PAGE_TITLE = "Daten und Statistiken";
    private static final String LEAD =
            "Finden, verstehen und nutzen Sie verlässliche Daten über Bevölkerung, Wirtschaft, Umwelt, Mobilität und mehr.";
    private static final String SECONDARY_LEAD =
            //"Alle Datensätze sind – sofern verfügbar – als Open Data mit freien Lizenzen nutzbar.";
            "";

    private final PageChromeFactory pageChromeFactory;
    private final FilterVmFactory filterVmFactory;
    private final ResultsVmFactory resultsVmFactory;

    public HomePageVmFactory(
            PageChromeFactory pageChromeFactory,
            FilterVmFactory filterVmFactory,
            ResultsVmFactory resultsVmFactory) {
        this.pageChromeFactory = pageChromeFactory;
        this.filterVmFactory = filterVmFactory;
        this.resultsVmFactory = resultsVmFactory;
    }

    public CatalogPageVm create(
            CatalogSnapshot snapshot,
            SearchResult searchResult,
            CatalogQueryParams queryParams,
            Facets facets) {
        var normalized = queryParams.normalized();
        return new CatalogPageVm(
                pageChromeFactory.catalogPage(PAGE_TITLE + " | Datenportal"),
                PAGE_TITLE,
                LEAD,
                SECONDARY_LEAD,
                normalized,
                filterVmFactory.create(normalized, facets),
                resultsVmFactory.create(searchResult, normalized));
    }
}
