package ch.so.agi.datenportal.explore;

import ch.so.agi.datenportal.catalog.domain.DatasetEntry;
import ch.so.agi.datenportal.catalog.service.CatalogService;
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
    private final CatalogUrlFactory urlFactory;
    private final ExploreContextJsonWriter jsonWriter;

    public ExploreContextService(
            CatalogService catalogService,
            ExploreTableService tableService,
            ExploreRecipeService recipeService,
            ExploreCodeSnippetService codeSnippetService,
            ExploreProperties properties,
            CatalogUrlFactory urlFactory,
            ExploreContextJsonWriter jsonWriter) {
        this.catalogService = catalogService;
        this.tableService = tableService;
        this.recipeService = recipeService;
        this.codeSnippetService = codeSnippetService;
        this.properties = properties;
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
            return buildContext(dataset);
        });
    }

    public String buildContextJson(String datasetId) {
        return toEmbeddableJson(buildContext(datasetId));
    }

    public String toEmbeddableJson(ExploreContextDto context) {
        return jsonWriter.write(context)
                .replace("</", "<\\/")
                .replace("<!--", "\\u003C!--")
                .replace("-->", "--\\u003E");
    }

    private ExploreContextDto buildContext(DatasetEntry dataset) {
        List<ExploreTableDto> tables = properties.enabled() ? tableService.buildTables(dataset) : List.of();
        var source = new ExploreContextSource(
                dataset.identifier(),
                dataset.title(),
                urlFactory.datasetDetail(dataset.identifier()));
        return new ExploreContextDto(
                1,
                dataset.identifier(),
                dataset.title(),
                Optional.of(dataset.description()),
                source.canonicalUrl(),
                Optional.of(dataset.modified().toString()),
                dataset.metadata().licenseUri().map(Object::toString),
                properties.execution(),
                tables,
                recipeService.generateRecipes(tables),
                codeSnippetService.generateSnippets(source, tables),
                properties.featureFlags());
    }

    private static CatalogNotFoundException notFound(String identifier) {
        return new CatalogNotFoundException("Catalog entry not found: " + identifier);
    }
}
