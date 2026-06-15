package ch.so.agi.datenportal.catalog.domain;

import java.util.Objects;
import java.util.Optional;

public record Office(
        String identifier,
        String displayName,
        Optional<String> abbreviation) {

    public Office {
        Objects.requireNonNull(identifier, "identifier must not be null");
        Objects.requireNonNull(displayName, "displayName must not be null");
        abbreviation = Objects.requireNonNullElse(abbreviation, Optional.empty());
    }
}
