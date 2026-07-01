package ch.so.agi.datenportal.explore;

import java.util.Optional;

public record ExploreChartConfigDto(
        ExploreChartType type,
        Optional<String> x,
        Optional<String> y,
        Optional<String> color,
        Optional<String> title) {

    public ExploreChartConfigDto {
        x = x == null ? Optional.empty() : x.filter(value -> !value.isBlank());
        y = y == null ? Optional.empty() : y.filter(value -> !value.isBlank());
        color = color == null ? Optional.empty() : color.filter(value -> !value.isBlank());
        title = title == null ? Optional.empty() : title.filter(value -> !value.isBlank());
    }
}
