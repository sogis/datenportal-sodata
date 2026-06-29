package ch.so.agi.datenportal.catalog.domain;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;

public record Office(
        String identifier,
        String displayName,
        Optional<String> abbreviation,
        Optional<URI> email,
        Optional<URI> officeAtWeb) {

    public Office(String identifier, String displayName, Optional<String> abbreviation) {
        this(identifier, displayName, abbreviation, Optional.empty(), Optional.empty());
    }

    public Office {
        Objects.requireNonNull(identifier, "identifier must not be null");
        Objects.requireNonNull(displayName, "displayName must not be null");
        abbreviation = Objects.requireNonNullElse(abbreviation, Optional.empty());
        email = Objects.requireNonNullElse(email, Optional.empty());
        officeAtWeb = Objects.requireNonNullElse(officeAtWeb, Optional.empty());
    }
}
