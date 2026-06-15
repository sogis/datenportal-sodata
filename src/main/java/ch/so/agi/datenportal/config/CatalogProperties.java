package ch.so.agi.datenportal.config;

import java.nio.file.Path;
import java.util.Objects;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "datenportal.catalog")
public record CatalogProperties(String source) {

    public CatalogProperties {
        Objects.requireNonNull(source, "datenportal.catalog.source must not be null");
        source = source.trim();
        if (source.isEmpty()) {
            throw new IllegalArgumentException("datenportal.catalog.source must not be blank");
        }
    }

    public boolean isClasspathSource() {
        return source.startsWith("classpath:") || !source.contains(":");
    }

    public boolean isFileSource() {
        return source.startsWith("file:");
    }

    public String classpathLocation() {
        if (!isClasspathSource()) {
            throw new IllegalStateException("Catalog source is not a classpath source: " + source);
        }

        String location = source.startsWith("classpath:") ? source.substring("classpath:".length()) : source;
        return location.startsWith("/") ? location.substring(1) : location;
    }

    public Path fileLocation() {
        if (!isFileSource()) {
            throw new IllegalStateException("Catalog source is not a file source: " + source);
        }

        String location = source.substring("file:".length()).trim();
        if (location.isEmpty()) {
            throw new IllegalArgumentException("datenportal.catalog.source file location must not be blank");
        }
        return Path.of(location);
    }
}
