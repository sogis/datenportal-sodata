package ch.so.agi.datenportal.catalog.service;

import ch.so.agi.datenportal.catalog.domain.Catalog;
import ch.so.agi.datenportal.catalog.domain.CatalogSnapshot;
import ch.so.agi.datenportal.catalog.importxtf.CatalogBytes;
import ch.so.agi.datenportal.catalog.importxtf.CatalogSourceException;
import ch.so.agi.datenportal.catalog.importxtf.CatalogValidator;
import ch.so.agi.datenportal.catalog.importxtf.PublishedCatalogParser;
import ch.so.agi.datenportal.search.CatalogSearchIndex;
import ch.so.agi.datenportal.search.CatalogSearchIndexBuilder;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.io.IOException;
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

    public CatalogBuildResult build(CatalogBytes publishedCatalog, CatalogBytes duckDbCatalog) {
        Objects.requireNonNull(publishedCatalog, "publishedCatalog must not be null");
        Objects.requireNonNull(duckDbCatalog, "duckDbCatalog must not be null");
        Instant startedAt = clock.instant();
        validateDuckDb(duckDbCatalog);

        LOGGER.info("Parsing catalog source {}.", publishedCatalog.sourceDescription());
        Catalog catalog = publishedCatalog.absent() ? new Catalog(java.util.List.of(), java.util.List.of())
                : parser.parse(publishedCatalog.inputStream(), publishedCatalog.sourceDescription());
        LOGGER.info(
                "Parsed catalog source {} with {} datasets and {} dataset series.",
                publishedCatalog.sourceDescription(),
                catalog.datasetCount(),
                catalog.seriesCount());

        var validation = publishedCatalog.absent()
                ? new ch.so.agi.datenportal.catalog.importxtf.CatalogValidationResult(java.util.List.of(), java.util.List.of())
                : validator.validate(catalog);
        validation.throwIfInvalid();
        LOGGER.info(
                "Validated catalog source {} with {} warnings.",
                publishedCatalog.sourceDescription(),
                validation.warnings().size());

        catalog = catalog.publishedView();
        CatalogSearchIndex searchIndex = searchIndexBuilder.build(catalog.topLevelEntries());
        try {
            Instant loadedAt = clock.instant();
            var snapshot = CatalogSnapshot.of(
                    catalog,
                    loadedAt,
                    Duration.between(startedAt, loadedAt),
                    publishedCatalog,
                    duckDbCatalog,
                    searchIndex);
            return new CatalogBuildResult(snapshot, validation.warnings());
        } catch (RuntimeException ex) {
            searchIndex.close();
            throw ex;
        }
    }

    private static void validateDuckDb(CatalogBytes duckDbCatalog) {
        if (duckDbCatalog.sizeInBytes() < 12) {
            throw new CatalogSourceException(
                    "DuckDB catalog source " + duckDbCatalog.sourceDescription() + " must contain at least 12 bytes.");
        }

        byte[] header = new byte[12];
        try (var inputStream = duckDbCatalog.inputStream()) {
            int offset = 0;
            while (offset < header.length) {
                int read = inputStream.read(header, offset, header.length - offset);
                if (read < 0) {
                    throw invalidDuckDb(duckDbCatalog);
                }
                offset += read;
            }
        } catch (IOException ex) {
            throw new CatalogSourceException(
                    "Failed to inspect DuckDB catalog source " + duckDbCatalog.sourceDescription() + ".", ex);
        }

        if (header[8] != 'D' || header[9] != 'U' || header[10] != 'C' || header[11] != 'K') {
            throw invalidDuckDb(duckDbCatalog);
        }
    }

    private static CatalogSourceException invalidDuckDb(CatalogBytes duckDbCatalog) {
        return new CatalogSourceException(
                "DuckDB catalog source " + duckDbCatalog.sourceDescription() + " has no DUCK marker at byte 8.");
    }
}
