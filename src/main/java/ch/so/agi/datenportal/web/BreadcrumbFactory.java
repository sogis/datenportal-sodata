package ch.so.agi.datenportal.web;

import ch.so.agi.datenportal.web.view.BreadcrumbItemVm;
import ch.so.agi.datenportal.web.view.BreadcrumbVm;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class BreadcrumbFactory {

    public BreadcrumbVm catalog() {
        return new BreadcrumbVm(
                List.of(
                        new BreadcrumbItemVm("so.ch", Optional.of("https://so.ch"), false),
                        new BreadcrumbItemVm("Datenportal", Optional.of("/datasets"), false),
                        new BreadcrumbItemVm("Daten & Statistiken", Optional.empty(), true)));
    }
}
