package ch.so.agi.datenportal.catalog.importxtf;

import java.util.List;

public record CatalogValidationResult(List<String> warnings, List<String> errors) {

    public CatalogValidationResult {
        warnings = List.copyOf(warnings);
        errors = List.copyOf(errors);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public void throwIfInvalid() {
        if (hasErrors()) {
            throw new CatalogValidationException(errors, warnings);
        }
    }
}
