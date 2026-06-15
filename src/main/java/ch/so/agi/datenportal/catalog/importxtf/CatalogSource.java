package ch.so.agi.datenportal.catalog.importxtf;

public interface CatalogSource {
    CatalogBytes load() throws CatalogSourceException;

    String description();
}
