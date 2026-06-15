package ch.so.agi.datenportal.catalog.domain;

import java.util.Objects;

public record Theme(String identifier, String displayName) {

    public Theme {
        Objects.requireNonNull(identifier, "identifier must not be null");
        Objects.requireNonNull(displayName, "displayName must not be null");
    }
}
