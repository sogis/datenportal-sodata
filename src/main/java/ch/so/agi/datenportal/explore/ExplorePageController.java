package ch.so.agi.datenportal.explore;

import ch.so.agi.datenportal.catalog.domain.DatasetEntry;
import ch.so.agi.datenportal.catalog.service.CatalogService;
import ch.so.agi.datenportal.web.CatalogNotFoundException;
import ch.so.agi.datenportal.web.CatalogUrlFactory;
import ch.so.agi.datenportal.web.PageChromeFactory;
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

    @GetMapping("/datasets/{datasetId}/explore")
    public String explorePage(@PathVariable String datasetId, Model model) {
        return catalogService.withSnapshot(snapshot -> {
            DatasetEntry dataset = findDataset(snapshot, datasetId);
            ExploreContextDto context = contextService.buildContext(datasetId);
            boolean available = !context.tables().isEmpty();
            model.addAttribute("page", new ExplorePageVm(
                    pageChromeFactory.datasetExplorePage(dataset),
                    dataset.identifier(),
                    dataset.title(),
                    "Erkunden",
                    urlFactory.datasetDetail(dataset.identifier()),
                    contextService.toEmbeddableJson(context),
                    available,
                    available ? "" : UNAVAILABLE_REASON,
                    ExploreAssetLinks.none()));
            return "pages/explore";
        });
    }

    @GetMapping(
            value = "/datasets/{datasetId}/explore/context.json",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> exploreContext(@PathVariable String datasetId) {
        String contextJson = contextService.buildContextJson(datasetId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .cacheControl(CacheControl.noCache().cachePrivate())
                .body(contextJson);
    }

    private static DatasetEntry findDataset(
            ch.so.agi.datenportal.catalog.domain.CatalogSnapshot snapshot,
            String datasetId) {
        var entry = snapshot.findAnyEntry(datasetId)
                .orElseThrow(() -> notFound(datasetId));
        if (!(entry instanceof DatasetEntry dataset)) {
            throw notFound(datasetId);
        }
        return dataset;
    }

    private static CatalogNotFoundException notFound(String identifier) {
        return new CatalogNotFoundException("Catalog entry not found: " + identifier);
    }
}
