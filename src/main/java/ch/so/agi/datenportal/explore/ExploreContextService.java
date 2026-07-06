package ch.so.agi.datenportal.explore;

import ch.so.agi.datenportal.catalog.domain.CatalogEntry;
import ch.so.agi.datenportal.catalog.domain.DatasetEntry;
import ch.so.agi.datenportal.catalog.domain.DatasetIssueEntry;
import ch.so.agi.datenportal.catalog.service.CatalogService;
import ch.so.agi.datenportal.config.CatalogDuckDbProperties;
import ch.so.agi.datenportal.web.CatalogNotFoundException;
import ch.so.agi.datenportal.web.CatalogUrlFactory;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public final class ExploreContextService {

    private final CatalogService catalogService;
    private final ExploreTableService tableService;
    private final ExploreRecipeService recipeService;
    private final ExploreCodeSnippetService codeSnippetService;
    private final ExploreProperties properties;
    private final CatalogDuckDbProperties catalogDuckDbProperties;
    private final CatalogUrlFactory urlFactory;
    private final ExploreContextJsonWriter jsonWriter;

    public ExploreContextService(
            CatalogService catalogService,
            ExploreTableService tableService,
            ExploreRecipeService recipeService,
            ExploreCodeSnippetService codeSnippetService,
            ExploreProperties properties,
            CatalogDuckDbProperties catalogDuckDbProperties,
            CatalogUrlFactory urlFactory,
            ExploreContextJsonWriter jsonWriter) {
        this.catalogService = catalogService;
        this.tableService = tableService;
        this.recipeService = recipeService;
        this.codeSnippetService = codeSnippetService;
        this.properties = properties;
        this.catalogDuckDbProperties = catalogDuckDbProperties;
        this.urlFactory = urlFactory;
        this.jsonWriter = jsonWriter;
    }

    public ExploreContextDto buildContext(String datasetId) {
        return catalogService.withSnapshot(snapshot -> {
            var entry = snapshot.findAnyEntry(datasetId)
                    .orElseThrow(() -> notFound(datasetId));
            if (!(entry instanceof DatasetEntry dataset)) {
                throw notFound(datasetId);
            }
            return buildContext(dataset, urlFactory.datasetDetail(dataset.identifier()));
        });
    }

    public String buildContextJson(String datasetId) {
        return toEmbeddableJson(buildContext(datasetId));
    }

    public ExploreContextDto buildContext(CatalogEntry entry, String canonicalUrl) {
        if (!(entry instanceof DatasetEntry || entry instanceof DatasetIssueEntry)) {
            throw notFound(entry.identifier());
        }
        return buildExplorableContext(entry, canonicalUrl);
    }

    public String buildContextJson(CatalogEntry entry, String canonicalUrl) {
        return toEmbeddableJson(buildContext(entry, canonicalUrl));
    }

    public String toEmbeddableJson(ExploreContextDto context) {
        return jsonWriter.write(context)
                .replace("</", "<\\/")
                .replace("<!--", "\\u003C!--")
                .replace("-->", "--\\u003E");
    }

    private ExploreContextDto buildExplorableContext(CatalogEntry entry, String canonicalUrl) {
        List<ExploreTableDto> tables = properties.enabled() ? tableService.buildTables(entry) : List.of();
        var source = new ExploreContextSource(
                entry.identifier(),
                entry.title(),
                canonicalUrl);
        return new ExploreContextDto(
                3,
                entry.identifier(),
                entry.title(),
                Optional.of(entry.description()),
                source.canonicalUrl(),
                Optional.of(entry.modified().toString()),
                entry.metadata().licenseUri().map(Object::toString),
                properties.execution(),
                new ExploreCatalogDatabaseDto(
                        "/catalog/catalog.duckdb",
                        "catalog",
                        catalogDuckDbProperties.schema()),
                tables,
                recipeService.generateRecipes(tables),
                codeSnippetService.generateSnippets(source, tables),
                properties.featureFlags(),
                properties.rLaboratory());
    }

    private static CatalogNotFoundException notFound(String identifier) {
        return new CatalogNotFoundException("Catalog entry not found: " + identifier);
    }
}
