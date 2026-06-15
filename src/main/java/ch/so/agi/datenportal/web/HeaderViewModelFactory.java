package ch.so.agi.datenportal.web;

import ch.so.agi.datenportal.web.view.HeaderVm;
import ch.so.agi.datenportal.web.view.NavItemVm;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class HeaderViewModelFactory {

    public HeaderVm forCatalogPage() {
        return forDataPage();
    }

    public HeaderVm forDetailPage() {
        return forDataPage();
    }

    private HeaderVm forDataPage() {
        return new HeaderVm(
                "Daten",
                "/",
                "Datenportal",
                "Kanton Solothurn",
                List.of(
                        new NavItemVm("Daten", "/datasets", true),
                        new NavItemVm("Themen", "/themes", false),
                        new NavItemVm("Statistiken", "/statistics", false),
                        new NavItemVm("Karten", "/maps", false),
                        new NavItemVm("APIs", "/apis", false),
                        new NavItemVm("Über uns", "/about", false)),
                List.of(
                        new NavItemVm("Services", "https://so.ch/services/", false),
                        new NavItemVm("Verwaltung", "https://so.ch/verwaltung/", false),
                        new NavItemVm("my.so.ch", "https://my.so.ch/", false)),
                "{}");
    }
}
