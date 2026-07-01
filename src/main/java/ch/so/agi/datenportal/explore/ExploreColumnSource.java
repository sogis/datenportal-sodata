package ch.so.agi.datenportal.explore;

import java.util.Optional;

public record ExploreColumnSource(
        String name,
        String type,
        Optional<String> description) {

    public ExploreColumnSource {
        description = description == null ? Optional.empty() : description.filter(value -> !value.isBlank());
    }
}
