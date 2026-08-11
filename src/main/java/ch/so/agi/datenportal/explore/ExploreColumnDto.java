package ch.so.agi.datenportal.explore;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.Optional;

public record ExploreColumnDto(
        String name,
        String type,
        @JsonInclude(JsonInclude.Include.NON_ABSENT)
        Optional<Boolean> nullable,
        @JsonInclude(JsonInclude.Include.NON_ABSENT)
        Optional<Boolean> required,
        @JsonInclude(JsonInclude.Include.NON_ABSENT)
        Optional<String> description,
        @JsonInclude(JsonInclude.Include.NON_ABSENT)
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
