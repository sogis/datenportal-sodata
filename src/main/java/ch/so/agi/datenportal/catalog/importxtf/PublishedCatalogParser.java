package ch.so.agi.datenportal.catalog.importxtf;

import ch.so.agi.datenportal.catalog.domain.Catalog;
import java.io.InputStream;

public interface PublishedCatalogParser {
    Catalog parse(InputStream inputStream, String sourceDescription) throws XtfParseException, CatalogValidationException;
}
