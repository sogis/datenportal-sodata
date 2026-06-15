package ch.so.agi.datenportal.catalog.domain;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;

public record ContactPoint(
        String name,
        Optional<String> organizationUnit,
        Optional<URI> email,
        Optional<String> phone,
        Optional<URI> url) {

    public ContactPoint {
        Objects.requireNonNull(name, "name must not be null");
        organizationUnit = organizationUnit == null ? Optional.empty() : organizationUnit
                .filter(value -> !value.isBlank());
        email = email == null ? Optional.empty() : email;
        phone = phone == null ? Optional.empty() : phone
                .filter(value -> !value.isBlank());
        url = url == null ? Optional.empty() : url;
    }
}
