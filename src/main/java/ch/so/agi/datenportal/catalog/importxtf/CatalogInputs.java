package ch.so.agi.datenportal.catalog.importxtf;

import java.util.Objects;

/** The complete inputs of one load attempt, before snapshot activation. */
public record CatalogInputs(CatalogBytes publishedCatalog, CatalogBytes duckDbCatalog) {
    public CatalogInputs {
        Objects.requireNonNull(publishedCatalog);
        Objects.requireNonNull(duckDbCatalog);
    }
}
