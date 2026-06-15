package ch.so.agi.datenportal.catalog.importxtf;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.util.Objects;
import org.springframework.util.unit.DataSize;

public final class FileCatalogSource implements CatalogSource {

    private final Path fileLocation;
    private final DataSize maxSize;
    private final Clock clock;

    public FileCatalogSource(Path fileLocation) {
        this(fileLocation, DataSize.ofMegabytes(50), Clock.systemUTC());
    }

    public FileCatalogSource(Path fileLocation, DataSize maxSize, Clock clock) {
        this.fileLocation = Objects.requireNonNull(fileLocation, "fileLocation must not be null")
                .toAbsolutePath()
                .normalize();
        this.maxSize = Objects.requireNonNull(maxSize, "maxSize must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    @Override
    public CatalogBytes load() {
        if (!Files.exists(fileLocation)) {
            throw new CatalogSourceException("Catalog file does not exist: " + description());
        }
        if (!Files.isRegularFile(fileLocation)) {
            throw new CatalogSourceException("Catalog file is not a regular file: " + description());
        }
        if (!Files.isReadable(fileLocation)) {
            throw new CatalogSourceException("Catalog file is not readable: " + description());
        }

        try (var inputStream = Files.newInputStream(fileLocation)) {
            return CatalogBytesReader.read(inputStream, description(), maxSize, clock);
        } catch (IOException ex) {
            throw new CatalogSourceException("Failed to load catalog file: " + description(), ex);
        }
    }

    @Override
    public String description() {
        return "file:" + fileLocation;
    }
}
