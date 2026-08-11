package ch.so.agi.datenportal.explore;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record ExploreContextDto(
        int version,
        String datasetId,
        String title,
        @JsonInclude(JsonInclude.Include.NON_ABSENT)
        Optional<String> description,
        String canonicalUrl,
        @JsonInclude(JsonInclude.Include.NON_ABSENT)
        Optional<String> updatedAt,
        @JsonInclude(JsonInclude.Include.NON_ABSENT)
        Optional<String> license,
        ExploreExecutionDto execution,
        ExploreCatalogDatabaseDto catalogDatabase,
        List<ExploreTableDto> tables,
        List<ExploreRecipeDto> recipes,
        boolean chartsEnabled,
        boolean webREnabled,
        ExploreRLaboratoryDto rLaboratory) {

    public ExploreContextDto {
        description = description == null ? Optional.empty() : description.filter(value -> !value.isBlank());
        updatedAt = updatedAt == null ? Optional.empty() : updatedAt.filter(value -> !value.isBlank());
        license = license == null ? Optional.empty() : license.filter(value -> !value.isBlank());
        catalogDatabase = Objects.requireNonNull(catalogDatabase, "catalogDatabase must not be null");
        tables = tables == null ? List.of() : List.copyOf(tables);
        recipes = recipes == null ? List.of() : List.copyOf(recipes);
    }
}
