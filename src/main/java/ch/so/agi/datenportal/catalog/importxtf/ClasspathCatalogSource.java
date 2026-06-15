package ch.so.agi.datenportal.catalog.importxtf;

import java.io.IOException;
import java.time.Clock;
import java.util.Objects;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.unit.DataSize;

public final class ClasspathCatalogSource implements CatalogSource {

    private final ResourceLoader resourceLoader;
    private final String classpathLocation;
    private final DataSize maxSize;
    private final Clock clock;

    public ClasspathCatalogSource(ResourceLoader resourceLoader, String classpathLocation) {
        this(resourceLoader, classpathLocation, DataSize.ofMegabytes(50), Clock.systemUTC());
    }

    public ClasspathCatalogSource(
            ResourceLoader resourceLoader,
            String classpathLocation,
            DataSize maxSize,
            Clock clock) {
        this.resourceLoader = Objects.requireNonNull(resourceLoader, "resourceLoader must not be null");
        this.classpathLocation = normalize(classpathLocation);
        this.maxSize = Objects.requireNonNull(maxSize, "maxSize must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    @Override
    public CatalogBytes load() {
        Resource resource = resourceLoader.getResource("classpath:" + classpathLocation);
        if (!resource.exists() || !resource.isReadable()) {
            throw new CatalogSourceException("Classpath catalog resource is not readable: " + description());
        }

        try (var inputStream = resource.getInputStream()) {
            return CatalogBytesReader.read(inputStream, description(), maxSize, clock);
        } catch (IOException ex) {
            throw new CatalogSourceException("Failed to load classpath catalog resource: " + description(), ex);
        }
    }

    @Override
    public String description() {
        return "classpath:" + classpathLocation;
    }

    private static String normalize(String location) {
        Objects.requireNonNull(location, "classpathLocation must not be null");
        String normalized = location.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("classpathLocation must not be blank");
        }
        if (normalized.startsWith("classpath:")) {
            normalized = normalized.substring("classpath:".length());
        }
        return normalized.startsWith("/") ? normalized.substring(1) : normalized;
    }
}
