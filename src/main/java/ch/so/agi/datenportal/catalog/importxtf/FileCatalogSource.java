package ch.so.agi.datenportal.catalog.importxtf;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public final class FileCatalogSource implements CatalogSource {

    private final Path fileLocation;

    public FileCatalogSource(Path fileLocation) {
        this.fileLocation = Objects.requireNonNull(fileLocation, "fileLocation must not be null")
                .toAbsolutePath()
                .normalize();
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

        try {
            return new CatalogBytes(Files.readAllBytes(fileLocation), description());
        } catch (IOException ex) {
            throw new CatalogSourceException("Failed to load catalog file: " + description(), ex);
        }
    }

    @Override
    public String description() {
        return "file:" + fileLocation;
    }
}
