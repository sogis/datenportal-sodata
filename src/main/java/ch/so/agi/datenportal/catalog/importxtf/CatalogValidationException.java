package ch.so.agi.datenportal.catalog.importxtf;

import java.util.List;

public class CatalogValidationException extends RuntimeException {

    private final List<String> errors;
    private final List<String> warnings;

    public CatalogValidationException(List<String> errors) {
        this(errors, List.of());
    }

    public CatalogValidationException(List<String> errors, List<String> warnings) {
        super(String.join(System.lineSeparator(), errors));
        this.errors = List.copyOf(errors);
        this.warnings = List.copyOf(warnings);
    }

    public List<String> errors() {
        return errors;
    }

    public List<String> warnings() {
        return warnings;
    }
}
