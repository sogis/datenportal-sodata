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
    public String datasetDetail(@PathVariable("identifier") String identifier, Model model) {
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

    @GetMapping("/datasets/{identifier}/structure-quality-origin")
    public String datasetStructureQualityOrigin(@PathVariable("identifier") String identifier, Model model) {
        return catalogService.withSnapshot(snapshot -> {
            var entry = snapshot.findAnyEntry(identifier)
                    .orElseThrow(() -> notFound(identifier));
            if (!(entry instanceof DatasetEntry dataset)) {
                throw notFound(identifier);
            }

            model.addAttribute("page", detailPageVmFactory.datasetStructureQualityOrigin(dataset));
            return "pages/structureQualityOrigin";
        });
    }

    @GetMapping("/series/{seriesIdentifier}")
    public String seriesDetail(@PathVariable("seriesIdentifier") String seriesIdentifier, Model model) {
        return catalogService.withSnapshot(snapshot -> {
            DatasetSeriesEntry series = findSeries(snapshot, seriesIdentifier);
            model.addAttribute("page", detailPageVmFactory.series(series));
            return "pages/seriesDetail";
        });
    }

    @GetMapping("/series/{seriesIdentifier}/issues/current")
    public String currentIssueDetail(@PathVariable("seriesIdentifier") String seriesIdentifier, Model model) {
        return catalogService.withSnapshot(snapshot -> {
            DatasetSeriesEntry series = findSeries(snapshot, seriesIdentifier);
            model.addAttribute("page", detailPageVmFactory.issue(series, series.currentIssueOrThrow()));
            return "pages/issueDetail";
        });
    }

    @GetMapping("/series/{seriesIdentifier}/issues/current/structure-quality-origin")
    public String currentIssueStructureQualityOrigin(@PathVariable("seriesIdentifier") String seriesIdentifier, Model model) {
        return catalogService.withSnapshot(snapshot -> {
            DatasetSeriesEntry series = findSeries(snapshot, seriesIdentifier);
            model.addAttribute("page", detailPageVmFactory.issueStructureQualityOrigin(series, series.currentIssueOrThrow()));
            return "pages/structureQualityOrigin";
        });
    }

    @GetMapping("/series/{seriesIdentifier}/issues/{issueIdentifier}")
    public String issueDetail(
            @PathVariable("seriesIdentifier") String seriesIdentifier,
            @PathVariable("issueIdentifier") String issueIdentifier,
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

    @GetMapping("/series/{seriesIdentifier}/issues/{issueIdentifier}/structure-quality-origin")
    public String issueStructureQualityOrigin(
            @PathVariable("seriesIdentifier") String seriesIdentifier,
            @PathVariable("issueIdentifier") String issueIdentifier,
            Model model) {
        return catalogService.withSnapshot(snapshot -> {
            DatasetSeriesEntry series = findSeries(snapshot, seriesIdentifier);
            DatasetIssueEntry issue = findIssue(series, issueIdentifier);

            model.addAttribute("page", detailPageVmFactory.issueStructureQualityOrigin(series, issue));
            return "pages/structureQualityOrigin";
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

    private static DatasetIssueEntry findIssue(DatasetSeriesEntry series, String issueIdentifier) {
        return series.issues().stream()
                .filter(candidate -> candidate.identifier().equals(issueIdentifier))
                .findFirst()
                .orElseThrow(() -> notFound(issueIdentifier));
    }

    private static CatalogNotFoundException notFound(String identifier) {
        return new CatalogNotFoundException("Catalog entry not found: " + identifier);
    }
}
