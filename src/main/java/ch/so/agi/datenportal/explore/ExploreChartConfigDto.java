package ch.so.agi.datenportal.explore;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Optional;

public record ExploreChartConfigDto(
        ExploreChartType type,
        @JsonInclude(JsonInclude.Include.NON_ABSENT)
        Optional<String> x,
        @JsonInclude(JsonInclude.Include.NON_ABSENT)
        Optional<String> y,
        @JsonInclude(JsonInclude.Include.NON_ABSENT)
        Optional<String> color,
        @JsonInclude(JsonInclude.Include.NON_ABSENT)
        Optional<String> title) {

    public ExploreChartConfigDto {
        x = x == null ? Optional.empty() : x.filter(value -> !value.isBlank());
        y = y == null ? Optional.empty() : y.filter(value -> !value.isBlank());
        color = color == null ? Optional.empty() : color.filter(value -> !value.isBlank());
        title = title == null ? Optional.empty() : title.filter(value -> !value.isBlank());
    }
}
