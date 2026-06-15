package ch.so.agi.datenportal.catalog.service;

import ch.so.agi.datenportal.catalog.domain.Catalog;
import ch.so.agi.datenportal.catalog.domain.CatalogSnapshot;
import ch.so.agi.datenportal.catalog.importxtf.CatalogBytes;
import ch.so.agi.datenportal.catalog.importxtf.CatalogValidator;
import ch.so.agi.datenportal.catalog.importxtf.PublishedCatalogParser;
import ch.so.agi.datenportal.search.CatalogSearchIndex;
import ch.so.agi.datenportal.search.CatalogSearchIndexBuilder;
import java.time.Clock;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public final class CatalogSnapshotBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(CatalogSnapshotBuilder.class);

    private final PublishedCatalogParser parser;
    private final CatalogValidator validator;
    private final CatalogSearchIndexBuilder searchIndexBuilder;
    private final Clock clock;

    public CatalogSnapshotBuilder(
            PublishedCatalogParser parser,
            CatalogValidator validator,
            CatalogSearchIndexBuilder searchIndexBuilder,
            Clock clock) {
        this.parser = Objects.requireNonNull(parser, "parser must not be null");
        this.validator = Objects.requireNonNull(validator, "validator must not be null");
        this.searchIndexBuilder = Objects.requireNonNull(searchIndexBuilder, "searchIndexBuilder must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    public CatalogBuildResult build(CatalogBytes bytes) {
        Objects.requireNonNull(bytes, "bytes must not be null");

        LOGGER.info("Parsing catalog source {}.", bytes.sourceDescription());
        Catalog catalog = parser.parse(bytes.inputStream(), bytes.sourceDescription());
        LOGGER.info(
                "Parsed catalog source {} with {} datasets and {} dataset series.",
                bytes.sourceDescription(),
                catalog.datasetCount(),
                catalog.seriesCount());

        var validation = validator.validate(catalog);
        validation.throwIfInvalid();
        LOGGER.info(
                "Validated catalog source {} with {} warnings.",
                bytes.sourceDescription(),
                validation.warnings().size());

        CatalogSearchIndex searchIndex = searchIndexBuilder.build(catalog.topLevelEntries());
        try {
            var snapshot = CatalogSnapshot.of(
                    catalog,
                    clock.instant(),
                    bytes.sourceDescription(),
                    bytes.contentHash(),
                    searchIndex);
            return new CatalogBuildResult(snapshot, validation.warnings());
        } catch (RuntimeException ex) {
            searchIndex.close();
            throw ex;
        }
    }
}
