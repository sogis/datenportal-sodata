package ch.so.agi.datenportal.catalog.importxtf;

public class CatalogSourceException extends RuntimeException {

    public CatalogSourceException(String message) {
        super(message);
    }

    public CatalogSourceException(String message, Throwable cause) {
        super(message, cause);
    }
}
