package ch.so.agi.datenportal.explore;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Optional;

public record ExploreRecipeDto(
        String id,
        String title,
        String description,
        String tableId,
        ExploreRecipeCategory category,
        String sql,
        @JsonInclude(JsonInclude.Include.NON_ABSENT)
        Optional<ExploreChartConfigDto> preferredChart) {

    public ExploreRecipeDto {
        preferredChart = preferredChart == null ? Optional.empty() : preferredChart;
    }
}
