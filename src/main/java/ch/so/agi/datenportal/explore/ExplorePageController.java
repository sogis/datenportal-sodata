package ch.so.agi.datenportal.explore;

import ch.so.agi.datenportal.catalog.domain.CatalogEntry;
import ch.so.agi.datenportal.catalog.domain.CatalogSnapshot;
import ch.so.agi.datenportal.catalog.domain.DatasetEntry;
import ch.so.agi.datenportal.catalog.domain.DatasetIssueEntry;
import ch.so.agi.datenportal.catalog.domain.DatasetSeriesEntry;
import ch.so.agi.datenportal.catalog.service.CatalogService;
import ch.so.agi.datenportal.web.CatalogNotFoundException;
import ch.so.agi.datenportal.web.CatalogUrlFactory;
import ch.so.agi.datenportal.web.PageChromeFactory;
import ch.so.agi.datenportal.web.view.PageChromeVm;
import java.util.Map;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public final class ExplorePageController {

    private static final String UNAVAILABLE_REASON =
            "Erkunden ist für dieses Datenthema noch nicht verfügbar, weil keine Parquet-Datei publiziert ist.";
    private static final ExploreAssetLinks ASSET_LINKS =
            new ExploreAssetLinks("/explore/assets/explore.js", "/explore/assets/explore.css");

    private final CatalogService catalogService;
    private final ExploreContextService contextService;
    private final PageChromeFactory pageChromeFactory;
    private final CatalogUrlFactory urlFactory;

    public ExplorePageController(
            CatalogService catalogService,
            ExploreContextService contextService,
            PageChromeFactory pageChromeFactory,
            CatalogUrlFactory urlFactory) {
        this.catalogService = catalogService;
        this.contextService = contextService;
        this.pageChromeFactory = pageChromeFactory;
        this.urlFactory = urlFactory;
    }

    @GetMapping({
        "/datasets/{entryIdentifier}/explore",
        "/series/{seriesIdentifier}/issues/current/explore",
        "/series/{seriesIdentifier}/issues/{issueIdentifier}/explore"
    })
    public String explorePage(@PathVariable Map<String, String> pathVariables, Model model) {
        return catalogService.withSnapshot(snapshot -> {
            ExploreTarget target = resolveTarget(snapshot, pathVariables);
            ExploreContextDto context = contextService.buildContext(target.entry(), target.canonicalUrl());
            boolean available = !context.tables().isEmpty();
            model.addAttribute("page", new ExplorePageVm(
                    target.chrome(),
                    target.entry().identifier(),
                    target.entry().title(),
                    "Erkunden",
                    target.canonicalUrl(),
                    contextService.toEmbeddableJson(context),
                    available,
                    available ? "" : UNAVAILABLE_REASON,
                    ASSET_LINKS));
            return "pages/explore";
        });
    }

    @GetMapping(
            value = {
                "/datasets/{entryIdentifier}/explore/context.json",
                "/series/{seriesIdentifier}/issues/current/explore/context.json",
                "/series/{seriesIdentifier}/issues/{issueIdentifier}/explore/context.json"
            },
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> exploreContext(@PathVariable Map<String, String> pathVariables) {
        return catalogService.withSnapshot(snapshot -> {
            ExploreTarget target = resolveTarget(snapshot, pathVariables);
            String contextJson = contextService.buildContextJson(target.entry(), target.canonicalUrl());
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .cacheControl(CacheControl.noCache().cachePrivate())
                    .body(contextJson);
        });
    }

    private ExploreTarget resolveTarget(CatalogSnapshot snapshot, Map<String, String> pathVariables) {
        String entryIdentifier = pathVariables.get("entryIdentifier");
        if (entryIdentifier != null) {
            return datasetTarget(snapshot, entryIdentifier);
        }

        String seriesIdentifier = pathVariables.get("seriesIdentifier");
        if (seriesIdentifier == null || seriesIdentifier.isBlank()) {
            throw notFound("");
        }

        DatasetSeriesEntry series = findSeries(snapshot, seriesIdentifier);
        String issueIdentifier = pathVariables.get("issueIdentifier");
        DatasetIssueEntry issue = issueIdentifier == null || "current".equals(issueIdentifier)
                ? series.currentIssueOrThrow()
                : findIssue(series, issueIdentifier);
        boolean currentIssue = issue.identifier().equals(series.currentIssueOrThrow().identifier());
        String canonicalUrl = currentIssue
                ? urlFactory.currentIssueDetail(series.identifier())
                : urlFactory.issueDetail(series.identifier(), issue.identifier());
        return new ExploreTarget(
                issue,
                canonicalUrl,
                pageChromeFactory.issueExplorePage(series, issue));
    }

    private ExploreTarget datasetTarget(CatalogSnapshot snapshot, String datasetId) {
        var entry = snapshot.findAnyEntry(datasetId)
                .orElseThrow(() -> notFound(datasetId));
        if (!(entry instanceof DatasetEntry dataset)) {
            throw notFound(datasetId);
        }
        return new ExploreTarget(
                dataset,
                urlFactory.datasetDetail(dataset.identifier()),
                pageChromeFactory.datasetExplorePage(dataset));
    }

    private static DatasetSeriesEntry findSeries(CatalogSnapshot snapshot, String seriesIdentifier) {
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

    private record ExploreTarget(
            CatalogEntry entry,
            String canonicalUrl,
            PageChromeVm chrome) {
    }
}
