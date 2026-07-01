package ch.so.agi.datenportal.explore;

import java.util.List;
import java.util.Optional;

public record ExploreContextDto(
        int version,
        String datasetId,
        String title,
        Optional<String> description,
        String canonicalUrl,
        Optional<String> updatedAt,
        Optional<String> license,
        ExploreExecutionDto execution,
        List<ExploreTableDto> tables,
        List<ExploreRecipeDto> recipes,
        List<ExploreCodeSnippetDto> codeSnippets,
        ExploreFeatureFlagsDto featureFlags) {

    public ExploreContextDto {
        description = description == null ? Optional.empty() : description.filter(value -> !value.isBlank());
        updatedAt = updatedAt == null ? Optional.empty() : updatedAt.filter(value -> !value.isBlank());
        license = license == null ? Optional.empty() : license.filter(value -> !value.isBlank());
        tables = tables == null ? List.of() : List.copyOf(tables);
        recipes = recipes == null ? List.of() : List.copyOf(recipes);
        codeSnippets = codeSnippets == null ? List.of() : List.copyOf(codeSnippets);
    }
}
