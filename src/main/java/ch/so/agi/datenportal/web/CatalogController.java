package ch.so.agi.datenportal.web;

import ch.so.agi.datenportal.catalog.service.CatalogService;
import ch.so.agi.datenportal.web.view.CatalogPageVm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping({"/", "/datasets"})
public class CatalogController {

    private final CatalogService catalogService;
    private final CatalogPageVmFactory catalogPageVmFactory;

    public CatalogController(CatalogService catalogService, CatalogPageVmFactory catalogPageVmFactory) {
        this.catalogService = catalogService;
        this.catalogPageVmFactory = catalogPageVmFactory;
    }

    @GetMapping
    public String index(Model model) {
        CatalogPageVm page = catalogPageVmFactory.create(catalogService.visibleEntries());
        model.addAttribute("page", page);
        return "pages/catalog";
    }
}
