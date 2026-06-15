package ch.so.agi.datenportal.web;

import ch.so.agi.datenportal.support.JsonAttributeEncoder;
import ch.so.agi.datenportal.web.view.HeaderVm;
import ch.so.agi.datenportal.web.view.NavItemVm;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class HeaderViewModelFactory {

    private final JsonAttributeEncoder json;

    public HeaderViewModelFactory(JsonAttributeEncoder json) {
        this.json = json;
    }

    public HeaderVm forCatalogPage() {
        return create("Daten");
    }

    public HeaderVm forDetailPage() {
        return create("Daten");
    }

    public HeaderVm create(String activeSection) {
        List<NavItemVm> primaryNav = primaryNav(activeSection);
        List<NavItemVm> utilityNav = utilityNav();
        return new HeaderVm(
                activeSection,
                "/",
                "Datenportal",
                "Kanton Solothurn",
                primaryNav,
                utilityNav,
                json.toJson(primaryNav),
                json.toJson(utilityNav),
                json.toJson(Map.of("siteClaim", "Kanton Solothurn")));
    }

    private static List<NavItemVm> primaryNav(String activeSection) {
        return List.of(
                navItem("Daten", "/datasets", activeSection),
                navItem("Themen", "/themes", activeSection),
                navItem("Statistiken", "/statistics", activeSection),
                navItem("Karten", "/maps", activeSection),
                navItem("APIs", "/apis", activeSection),
                navItem("Über uns", "/about", activeSection));
    }

    private static List<NavItemVm> utilityNav() {
        return List.of(
                new NavItemVm("Services", "https://so.ch/services/", false),
                new NavItemVm("Verwaltung", "https://so.ch/verwaltung/", false),
                new NavItemVm("my.so.ch", "https://my.so.ch/", false));
    }

    private static NavItemVm navItem(String label, String href, String activeSection) {
        return new NavItemVm(label, href, label.equals(activeSection));
    }
}
