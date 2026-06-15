package ch.so.agi.datenportal.catalog.service;

import ch.so.agi.datenportal.catalog.domain.CatalogSnapshot;
import java.util.List;
import java.util.Objects;

public record CatalogBuildResult(CatalogSnapshot snapshot, List<String> warnings) {

    public CatalogBuildResult {
        Objects.requireNonNull(snapshot, "snapshot must not be null");
        warnings = List.copyOf(warnings);
    }
}
