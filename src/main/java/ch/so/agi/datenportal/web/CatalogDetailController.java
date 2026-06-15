package ch.so.agi.datenportal.web;

import ch.so.agi.datenportal.catalog.domain.DatasetEntry;
import ch.so.agi.datenportal.catalog.domain.DatasetIssueEntry;
import ch.so.agi.datenportal.catalog.domain.DatasetSeriesEntry;
import ch.so.agi.datenportal.catalog.service.CatalogService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public final class CatalogDetailController {

    private final CatalogService catalogService;
    private final DetailPageVmFactory detailPageVmFactory;

    public CatalogDetailController(
            CatalogService catalogService,
            DetailPageVmFactory detailPageVmFactory) {
        this.catalogService = catalogService;
        this.detailPageVmFactory = detailPageVmFactory;
    }

    @GetMapping("/datasets/{identifier}")
    public String datasetDetail(@PathVariable String identifier, Model model) {
        return catalogService.withSnapshot(snapshot -> {
            var entry = snapshot.findAnyEntry(identifier)
                    .orElseThrow(() -> notFound(identifier));
            if (!(entry instanceof DatasetEntry dataset)) {
                throw notFound(identifier);
            }

            model.addAttribute("page", detailPageVmFactory.dataset(dataset));
            return "pages/datasetDetail";
        });
    }

    @GetMapping("/series/{seriesIdentifier}")
    public String seriesDetail(@PathVariable String seriesIdentifier, Model model) {
        return catalogService.withSnapshot(snapshot -> {
            DatasetSeriesEntry series = findSeries(snapshot, seriesIdentifier);
            model.addAttribute("page", detailPageVmFactory.series(series));
            return "pages/seriesDetail";
        });
    }

    @GetMapping("/series/{seriesIdentifier}/issues/current")
    public String currentIssueDetail(@PathVariable String seriesIdentifier, Model model) {
        return catalogService.withSnapshot(snapshot -> {
            DatasetSeriesEntry series = findSeries(snapshot, seriesIdentifier);
            model.addAttribute("page", detailPageVmFactory.issue(series, series.currentIssueOrThrow()));
            return "pages/issueDetail";
        });
    }

    @GetMapping("/series/{seriesIdentifier}/issues/{issueIdentifier}")
    public String issueDetail(
            @PathVariable String seriesIdentifier,
            @PathVariable String issueIdentifier,
            Model model) {
        return catalogService.withSnapshot(snapshot -> {
            DatasetSeriesEntry series = findSeries(snapshot, seriesIdentifier);
            DatasetIssueEntry issue = series.issues().stream()
                    .filter(candidate -> candidate.identifier().equals(issueIdentifier))
                    .findFirst()
                    .orElseThrow(() -> notFound(issueIdentifier));

            model.addAttribute("page", detailPageVmFactory.issue(series, issue));
            return "pages/issueDetail";
        });
    }

    private static DatasetSeriesEntry findSeries(ch.so.agi.datenportal.catalog.domain.CatalogSnapshot snapshot, String seriesIdentifier) {
        var entry = snapshot.findAnyEntry(seriesIdentifier)
                .orElseThrow(() -> notFound(seriesIdentifier));
        if (!(entry instanceof DatasetSeriesEntry series)) {
            throw notFound(seriesIdentifier);
        }
        return series;
    }

    private static CatalogNotFoundException notFound(String identifier) {
        return new CatalogNotFoundException("Catalog entry not found: " + identifier);
    }
}
