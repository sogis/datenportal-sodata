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

    private static final String DEFAULT_CLASSPATH_LOCATION = "catalog.duckdb";
    private static final Duration DEFAULT_HTTP_CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration DEFAULT_HTTP_READ_TIMEOUT = Duration.ofSeconds(30);
    private static final DataSize DEFAULT_MAX_SIZE = DataSize.ofMegabytes(50);
    private static final String DEFAULT_SCHEMA = "opendata";

    public CatalogDuckDbProperties {
        classpathLocation = blankToNull(classpathLocation);
        schema = blankToNull(schema);
        if (sourceType == null) {
            sourceType = CatalogProperties.SourceType.CLASSPATH;
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
    }

    public CatalogProperties.SourceType effectiveSourceType() {
        return sourceType;
    }

    public boolean isClasspathSource() {
        return effectiveSourceType() == CatalogProperties.SourceType.CLASSPATH;
    }

    public boolean isFileSource() {
        return effectiveSourceType() == CatalogProperties.SourceType.FILE;
    }

    public boolean isHttpSource() {
        return effectiveSourceType() == CatalogProperties.SourceType.HTTP;
    }

    public String classpathLocation() {
        if (!isClasspathSource()) {
            throw new IllegalStateException("DuckDB catalog source is not a classpath source.");
        }
        String location = classpathLocation == null ? DEFAULT_CLASSPATH_LOCATION : classpathLocation;
        return location.startsWith("/") ? location.substring(1) : location;
    }

    public Path fileLocation() {
        if (!isFileSource()) {
            throw new IllegalStateException("DuckDB catalog source is not a file source.");
        }
        if (fileLocation == null) {
            throw new IllegalArgumentException("datenportal.catalog.duckdb.file-location must be set for file sources");
        }
        return fileLocation;
    }

    public URI httpUrl() {
        if (!isHttpSource()) {
            throw new IllegalStateException("DuckDB catalog source is not an HTTP source.");
        }
        if (httpUrl == null) {
            throw new IllegalArgumentException("datenportal.catalog.duckdb.http-url must be set for HTTP sources");
        }
        String scheme = httpUrl.getScheme();
        if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
            throw new IllegalArgumentException("datenportal.catalog.duckdb.http-url must use http or https");
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
}
