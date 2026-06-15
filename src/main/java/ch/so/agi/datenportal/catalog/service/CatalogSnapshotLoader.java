package ch.so.agi.datenportal.catalog.service;

import ch.so.agi.datenportal.catalog.domain.Catalog;
import ch.so.agi.datenportal.catalog.domain.CatalogSnapshot;
import ch.so.agi.datenportal.catalog.importxtf.CatalogBytes;
import ch.so.agi.datenportal.catalog.importxtf.CatalogSource;
import ch.so.agi.datenportal.catalog.importxtf.CatalogValidator;
import ch.so.agi.datenportal.catalog.importxtf.PublishedCatalogParser;
import java.time.Clock;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public final class CatalogSnapshotLoader {

    private final CatalogSource catalogSource;
    private final PublishedCatalogParser parser;
    private final CatalogValidator validator;
    private final Clock clock;

    public CatalogSnapshotLoader(
            CatalogSource catalogSource,
            PublishedCatalogParser parser,
            CatalogValidator validator,
            Clock clock) {
        this.catalogSource = Objects.requireNonNull(catalogSource, "catalogSource must not be null");
        this.parser = Objects.requireNonNull(parser, "parser must not be null");
        this.validator = Objects.requireNonNull(validator, "validator must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    public CatalogSnapshot load() {
        return load(catalogSource.load());
    }

    CatalogSnapshot load(CatalogBytes bytes) {
        Catalog catalog = parser.parse(bytes.inputStream(), bytes.sourceDescription());
        validator.validateOrThrow(catalog);
        return CatalogSnapshot.of(catalog, clock.instant(), bytes.sourceDescription());
    }
}
