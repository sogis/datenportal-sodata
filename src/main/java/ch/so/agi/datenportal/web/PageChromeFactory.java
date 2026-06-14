package ch.so.agi.datenportal.web;

import ch.so.agi.datenportal.web.view.PageChromeVm;
import org.springframework.stereotype.Component;

@Component
public class PageChromeFactory {

    private final HeaderViewModelFactory headerViewModelFactory;
    private final BreadcrumbFactory breadcrumbFactory;

    public PageChromeFactory(
            HeaderViewModelFactory headerViewModelFactory,
            BreadcrumbFactory breadcrumbFactory) {
        this.headerViewModelFactory = headerViewModelFactory;
        this.breadcrumbFactory = breadcrumbFactory;
    }

    public PageChromeVm catalogPage(String pageTitle) {
        return new PageChromeVm(
                pageTitle,
                headerViewModelFactory.forCatalogPage(),
                breadcrumbFactory.catalog());
    }
}
