package ch.so.agi.datenportal.web;

import ch.so.agi.datenportal.catalog.service.CatalogService;
import ch.so.agi.datenportal.search.CatalogSearchService;
import ch.so.agi.datenportal.search.FacetService;
import ch.so.agi.datenportal.web.view.CatalogPageVm;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping({"/", "/datasets"})
public class CatalogController {

    private final CatalogService catalogService;
    private final CatalogSearchService catalogSearchService;
    private final FacetService facetService;
    private final HomePageVmFactory homePageVmFactory;

    public CatalogController(
            CatalogService catalogService,
            CatalogSearchService catalogSearchService,
            FacetService facetService,
            HomePageVmFactory homePageVmFactory) {
        this.catalogService = catalogService;
        this.catalogSearchService = catalogSearchService;
        this.facetService = facetService;
        this.homePageVmFactory = homePageVmFactory;
    }

    @GetMapping
    public String index(
            @ModelAttribute CatalogQueryParams params,
            HttpServletRequest request,
            Model model) {
        var normalized = params.normalized();
        var snapshot = catalogService.currentSnapshot();
        var searchResult = catalogSearchService.search(snapshot, normalized.toSearchQuery());
        var facets = facetService.compute(snapshot);
        CatalogPageVm page = homePageVmFactory.create(snapshot, searchResult, normalized, facets);
        model.addAttribute("page", page);

        if (HtmxRequest.targetsResults(request)) {
            model.addAttribute("results", page.results());
            model.addAttribute("filterPanel", page.filterPanel());
            model.addAttribute("queryParams", page.queryParams());
            return "fragments/catalogResults";
        }

        return "pages/catalog";
    }
}
