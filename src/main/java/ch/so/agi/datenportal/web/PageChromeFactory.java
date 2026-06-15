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

    public PageChromeVm datasetDetailPage(DatasetEntry dataset) {
        return new PageChromeVm(
                dataset.title() + " | Datenportal",
                headerViewModelFactory.forDetailPage(),
                breadcrumbFactory.datasetDetail(dataset));
    }

    public PageChromeVm seriesDetailPage(DatasetSeriesEntry series) {
        return new PageChromeVm(
                series.title() + " | Datenportal",
                headerViewModelFactory.forDetailPage(),
                breadcrumbFactory.seriesDetail(series));
    }

    public PageChromeVm issueDetailPage(DatasetSeriesEntry series, DatasetIssueEntry issue) {
        return new PageChromeVm(
                issue.title() + " | Datenportal",
                headerViewModelFactory.forDetailPage(),
                breadcrumbFactory.issueDetail(series, issue));
    }

    public PageChromeVm notFoundPage() {
        return new PageChromeVm(
                "Seite nicht gefunden | Datenportal",
                headerViewModelFactory.forDetailPage(),
                breadcrumbFactory.notFound());
    }
}
