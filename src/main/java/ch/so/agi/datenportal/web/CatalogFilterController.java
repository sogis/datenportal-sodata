package ch.so.agi.datenportal.web;

import ch.so.agi.datenportal.catalog.service.CatalogService;
import ch.so.agi.datenportal.search.FacetService;
import ch.so.agi.datenportal.web.view.FilterGroupVm;
import ch.so.agi.datenportal.web.view.FilterPanelVm;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/datasets")
public class CatalogFilterController {

    private final CatalogService catalogService;
    private final FacetService facetService;
    private final FilterVmFactory filterVmFactory;

    public CatalogFilterController(
            CatalogService catalogService,
            FacetService facetService,
            FilterVmFactory filterVmFactory) {
        this.catalogService = catalogService;
        this.facetService = facetService;
        this.filterVmFactory = filterVmFactory;
    }

    @GetMapping("/filter-popover")
    public String filterPopover(
            @RequestParam("filter") String filterId,
            @ModelAttribute CatalogQueryParams params,
            Model model) {
        var normalized = params.normalized();
        return catalogService.withSnapshot(snapshot -> {
            var panel = filterVmFactory.create(normalized, facetService.compute(snapshot));
            var group = findGroup(panel, filterId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown filter: " + filterId));
            model.addAttribute("group", group);
            model.addAttribute("queryParams", normalized);
            return "fragments/filterPopover";
        });
    }

    @GetMapping("/mobile-filters")
    public String mobileFilters(
            @ModelAttribute CatalogQueryParams params,
            Model model) {
        var normalized = params.normalized();
        return catalogService.withSnapshot(snapshot -> {
            model.addAttribute("filterPanel", filterVmFactory.create(normalized, facetService.compute(snapshot)));
            model.addAttribute("queryParams", normalized);
            return "fragments/mobileFilters";
        });
    }

    private Optional<FilterGroupVm> findGroup(FilterPanelVm panel, String filterId) {
        return panel.groups().stream()
                .filter(group -> group.id().equals(filterId))
                .findFirst();
    }
}
