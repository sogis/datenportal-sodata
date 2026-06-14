package ch.so.agi.datenportal.web;

import ch.so.agi.datenportal.web.view.CatalogPageVm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping({"/", "/datasets"})
public class CatalogController {

    private static final String PAGE_TITLE = "Daten & Statistiken";
    private static final String LEAD =
            "Finden und nutzen Sie offene Daten, Geodaten und Statistiken des Kantons Solothurn.";
    private static final String SECONDARY_LEAD =
            "Alle Datensätze sind – sofern verfügbar – als Open Data mit freien Lizenzen nutzbar.";
    private static final String PLACEHOLDER_MESSAGE =
            "Phase 0: Das Projektgerüst läuft. Katalogresultate folgen in den nächsten Phasen.";

    private final PageChromeFactory pageChromeFactory;

    public CatalogController(PageChromeFactory pageChromeFactory) {
        this.pageChromeFactory = pageChromeFactory;
    }

    @GetMapping
    public String index(Model model) {
        model.addAttribute(
                "page",
                new CatalogPageVm(
                        pageChromeFactory.catalogPage(PAGE_TITLE + " | Datenportal"),
                        PAGE_TITLE,
                        LEAD,
                        SECONDARY_LEAD,
                        PLACEHOLDER_MESSAGE));
        return "pages/catalog";
    }
}
