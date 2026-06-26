package ch.so.agi.datenportal.catalog.domain;

import java.util.Objects;
import java.util.Optional;

public record DatasetAttribute(
        String name,
        String dataType,
        Optional<String> description,
        Optional<String> unit,
        boolean mandatory) {

    public DatasetAttribute {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(dataType, "dataType must not be null");
        description = description == null ? Optional.empty() : description.filter(value -> !value.isBlank());
        unit = unit == null ? Optional.empty() : unit.filter(value -> !value.isBlank());

        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (dataType.isBlank()) {
            throw new IllegalArgumentException("dataType must not be blank");
        }
    }
}
