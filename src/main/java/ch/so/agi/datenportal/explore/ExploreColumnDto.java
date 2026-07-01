package ch.so.agi.datenportal.explore;

import java.util.List;
import java.util.Optional;

public record ExploreColumnDto(
        String name,
        String type,
        Optional<Boolean> nullable,
        Optional<Boolean> required,
        Optional<String> description,
        Optional<String> example,
        List<ExploreColumnRole> roles) {

    public ExploreColumnDto {
        nullable = nullable == null ? Optional.empty() : nullable;
        required = required == null ? Optional.empty() : required;
        description = description == null ? Optional.empty() : description.filter(value -> !value.isBlank());
        example = example == null ? Optional.empty() : example.filter(value -> !value.isBlank());
        roles = roles == null || roles.isEmpty() ? List.of(ExploreColumnRole.UNKNOWN) : List.copyOf(roles);
    }
}
