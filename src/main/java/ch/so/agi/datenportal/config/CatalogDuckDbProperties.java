package ch.so.agi.datenportal.config;

import java.net.URI;
import java.nio.file.Path;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

@ConfigurationProperties(prefix = "datenportal.catalog.duckdb")
public record CatalogDuckDbProperties(
        CatalogProperties.SourceType sourceType,
        String classpathLocation,
        Path fileLocation,
        URI httpUrl,
        Duration httpConnectTimeout,
        Duration httpReadTimeout,
        DataSize maxSize,
        String schema) {

    private static final Duration DEFAULT_HTTP_CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration DEFAULT_HTTP_READ_TIMEOUT = Duration.ofSeconds(30);
    private static final DataSize DEFAULT_MAX_SIZE = DataSize.ofMegabytes(50);
    private static final String DEFAULT_SCHEMA = "opendata";

    public CatalogDuckDbProperties {
        classpathLocation = blankToNull(classpathLocation);
        schema = blankToNull(schema);
        if (sourceType == null) {
            throw new IllegalArgumentException("datenportal.catalog.duckdb.source-type must be set");
        }
        if (httpConnectTimeout == null) {
            httpConnectTimeout = DEFAULT_HTTP_CONNECT_TIMEOUT;
        }
        if (httpReadTimeout == null) {
            httpReadTimeout = DEFAULT_HTTP_READ_TIMEOUT;
        }
        if (maxSize == null) {
            maxSize = DEFAULT_MAX_SIZE;
        }
        if (schema == null) {
            schema = DEFAULT_SCHEMA;
        }
        if (httpConnectTimeout.isZero() || httpConnectTimeout.isNegative()) {
            throw new IllegalArgumentException("datenportal.catalog.duckdb.http-connect-timeout must be greater than zero");
        }
        if (httpReadTimeout.isZero() || httpReadTimeout.isNegative()) {
            throw new IllegalArgumentException("datenportal.catalog.duckdb.http-read-timeout must be greater than zero");
        }
        if (maxSize.toBytes() <= 0) {
            throw new IllegalArgumentException("datenportal.catalog.duckdb.max-size must be greater than zero");
        }
        switch (sourceType) {
            case CLASSPATH -> requireClasspathLocation(classpathLocation);
            case FILE -> {
                if (fileLocation == null) {
                    throw new IllegalArgumentException(
                            "datenportal.catalog.duckdb.file-location must be set for file sources");
                }
            }
            case HTTP -> validateHttpUrl(httpUrl);
            case MANIFEST -> throw new IllegalArgumentException("DuckDB remains a separately configured source.");
        }
    }

    public boolean isClasspathSource() {
        return sourceType == CatalogProperties.SourceType.CLASSPATH;
    }

    public boolean isFileSource() {
        return sourceType == CatalogProperties.SourceType.FILE;
    }

    public boolean isHttpSource() {
        return sourceType == CatalogProperties.SourceType.HTTP;
    }

    public String classpathLocation() {
        if (!isClasspathSource()) {
            throw new IllegalStateException("DuckDB catalog source is not a classpath source.");
        }
        return classpathLocation.startsWith("/") ? classpathLocation.substring(1) : classpathLocation;
    }

    public Path fileLocation() {
        if (!isFileSource()) {
            throw new IllegalStateException("DuckDB catalog source is not a file source.");
        }
        return fileLocation;
    }

    public URI httpUrl() {
        if (!isHttpSource()) {
            throw new IllegalStateException("DuckDB catalog source is not an HTTP source.");
        }
        return httpUrl;
    }

    private static String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static void requireClasspathLocation(String location) {
        if (location == null) {
            throw new IllegalArgumentException(
                    "datenportal.catalog.duckdb.classpath-location must be set for classpath sources");
        }
    }

    private static void validateHttpUrl(URI uri) {
        if (uri == null) {
            throw new IllegalArgumentException("datenportal.catalog.duckdb.http-url must be set for HTTP sources");
        }
        String scheme = uri.getScheme();
        if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
            throw new IllegalArgumentException("datenportal.catalog.duckdb.http-url must use http or https");
        }
    }
}
