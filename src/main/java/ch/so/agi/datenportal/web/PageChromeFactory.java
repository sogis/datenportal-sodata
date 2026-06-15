package ch.so.agi.datenportal.web;

import ch.so.agi.datenportal.catalog.domain.DatasetEntry;
import ch.so.agi.datenportal.catalog.domain.DatasetIssueEntry;
import ch.so.agi.datenportal.catalog.domain.DatasetSeriesEntry;
import ch.so.agi.datenportal.web.view.PageChromeVm;
import org.springframework.stereotype.Component;

@Component
public class PageChromeFactory {

    private final HeaderViewModelFactory headerViewModelFactory;
    private final BreadcrumbFactory breadcrumbFactory;
    private final WebAssetsVmFactory webAssetsVmFactory;

    public PageChromeFactory(
            HeaderViewModelFactory headerViewModelFactory,
            BreadcrumbFactory breadcrumbFactory,
            WebAssetsVmFactory webAssetsVmFactory) {
        this.headerViewModelFactory = headerViewModelFactory;
        this.breadcrumbFactory = breadcrumbFactory;
        this.webAssetsVmFactory = webAssetsVmFactory;
    }

    public PageChromeVm catalogPage(String pageTitle) {
        return new PageChromeVm(
                pageTitle,
                headerViewModelFactory.forCatalogPage(),
                breadcrumbFactory.catalog(),
                webAssetsVmFactory.create());
    }

    public PageChromeVm datasetDetailPage(DatasetEntry dataset) {
        return new PageChromeVm(
                dataset.title() + " | Datenportal",
                headerViewModelFactory.forDetailPage(),
                breadcrumbFactory.datasetDetail(dataset),
                webAssetsVmFactory.create());
    }

    public PageChromeVm seriesDetailPage(DatasetSeriesEntry series) {
        return new PageChromeVm(
                series.title() + " | Datenportal",
                headerViewModelFactory.forDetailPage(),
                breadcrumbFactory.seriesDetail(series),
                webAssetsVmFactory.create());
    }

    public PageChromeVm issueDetailPage(DatasetSeriesEntry series, DatasetIssueEntry issue) {
        return new PageChromeVm(
                issue.title() + " | Datenportal",
                headerViewModelFactory.forDetailPage(),
                breadcrumbFactory.issueDetail(series, issue),
                webAssetsVmFactory.create());
    }

    public PageChromeVm notFoundPage() {
        return errorPage("Seite nicht gefunden | Datenportal", "Seite nicht gefunden");
    }

    public PageChromeVm errorPage(String pageTitle, String breadcrumbLabel) {
        return new PageChromeVm(
                pageTitle,
                headerViewModelFactory.forDetailPage(),
                breadcrumbFactory.error(breadcrumbLabel),
                webAssetsVmFactory.create());
    }
}
