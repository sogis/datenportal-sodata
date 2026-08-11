package ch.so.agi.datenportal.search;

public final class CatalogSearchException extends RuntimeException {

    public CatalogSearchException(String message) {
        super(message);
    }

    public CatalogSearchException(String message, Throwable cause) {
        super(message, cause);
    }
}
