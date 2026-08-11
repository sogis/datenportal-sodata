package ch.so.agi.datenportal.explore;

import ch.so.agi.datenportal.catalog.domain.CatalogEntry;
import ch.so.agi.datenportal.catalog.domain.CatalogSnapshot;
import ch.so.agi.datenportal.catalog.domain.DatasetEntry;
import ch.so.agi.datenportal.catalog.domain.DatasetIssueEntry;
import ch.so.agi.datenportal.catalog.service.CatalogService;
import ch.so.agi.datenportal.config.CatalogDuckDbProperties;
import ch.so.agi.datenportal.web.CatalogNotFoundException;
import ch.so.agi.datenportal.web.CatalogUrlFactory;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public final class ExploreContextService {

    private final CatalogService catalogService;
    private final ExploreTableService tableService;
    private final ExploreRecipeService recipeService;
    private final ExploreProperties properties;
    private final CatalogDuckDbProperties catalogDuckDbProperties;
    private final CatalogUrlFactory urlFactory;
    private final ObjectMapper objectMapper;

    public ExploreContextService(
            CatalogService catalogService,
            ExploreTableService tableService,
            ExploreRecipeService recipeService,
            ExploreProperties properties,
            CatalogDuckDbProperties catalogDuckDbProperties,
            CatalogUrlFactory urlFactory,
            ObjectMapper objectMapper) {
        this.catalogService = catalogService;
        this.tableService = tableService;
        this.recipeService = recipeService;
        this.properties = properties;
        this.catalogDuckDbProperties = catalogDuckDbProperties;
        this.urlFactory = urlFactory;
        this.objectMapper = objectMapper;
    }

    public ExploreContextDto buildContext(String datasetId) {
        return catalogService.withSnapshot(snapshot -> {
            var entry = snapshot.findAnyEntry(datasetId)
                    .orElseThrow(() -> notFound(datasetId));
            if (!(entry instanceof DatasetEntry dataset)) {
                throw notFound(datasetId);
            }
            return buildContext(snapshot, dataset, urlFactory.datasetDetail(dataset.identifier()));
        });
    }

    public String buildContextJson(String datasetId) {
        return toEmbeddableJson(buildContext(datasetId));
    }

    public ExploreContextDto buildContext(CatalogSnapshot snapshot, CatalogEntry entry, String canonicalUrl) {
        if (snapshot == null) {
            throw new IllegalArgumentException("snapshot must not be null");
        }
        if (!(entry instanceof DatasetEntry || entry instanceof DatasetIssueEntry)) {
            throw notFound(entry.identifier());
        }
        return buildExplorableContext(snapshot, entry, canonicalUrl);
    }

    public String buildContextJson(CatalogSnapshot snapshot, CatalogEntry entry, String canonicalUrl) {
        return toEmbeddableJson(buildContext(snapshot, entry, canonicalUrl));
    }

    public String toEmbeddableJson(ExploreContextDto context) {
        try {
            return objectMapper.writeValueAsString(context)
                    .replace("</script>", "<\\/script>")
                    .replace("<!--", "\\u003C!--")
                    .replace("-->", "--\\u003E");
        } catch (JacksonException exception) {
            throw new IllegalStateException("Explore-Kontext konnte nicht serialisiert werden.", exception);
        }
    }

    private ExploreContextDto buildExplorableContext(
            CatalogSnapshot snapshot,
            CatalogEntry entry,
        String canonicalUrl) {
        List<ExploreTableDto> tables = properties.enabled() ? tableService.buildTables(entry) : List.of();
        return new ExploreContextDto(
                4,
                entry.identifier(),
                entry.title(),
                Optional.of(entry.description()),
                canonicalUrl,
                Optional.of(entry.modified().toString()),
                entry.metadata().licenseUri().map(Object::toString),
                properties.execution(),
                new ExploreCatalogDatabaseDto(
                        "/catalog/catalog.duckdb?v=" + snapshot.duckDbCatalog().contentHash(),
                        "catalog",
                        catalogDuckDbProperties.schema()),
                tables,
                recipeService.generateRecipes(tables),
                properties.chartsEnabled(),
                properties.webrEnabled(),
                properties.rLaboratory());
    }

    private static CatalogNotFoundException notFound(String identifier) {
        return new CatalogNotFoundException("Catalog entry not found: " + identifier);
    }
}
