package ch.so.agi.datenportal.config;

import ch.so.agi.datenportal.catalog.importxtf.CatalogDownloadUrlPlaceholderResolver;
import java.net.URI;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Objects;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

@ConfigurationProperties(prefix = "datenportal.catalog")
public record CatalogProperties(
        String source,
        SourceType sourceType,
        String classpathLocation,
        Path fileLocation,
        URI httpUrl,
        Duration httpConnectTimeout,
        Duration httpReadTimeout,
        DataSize maxSize,
        String downloadUrl) {

    private static final String DEFAULT_CLASSPATH_LOCATION = "published_catalog_full_62_entries.xtf";
    private static final Duration DEFAULT_HTTP_CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration DEFAULT_HTTP_READ_TIMEOUT = Duration.ofSeconds(30);
    private static final DataSize DEFAULT_MAX_SIZE = DataSize.ofMegabytes(50);
    private static final String DEFAULT_DOWNLOAD_URL = "http://localhost:8081/ch.so.datenportal/downloads";

    public CatalogProperties {
        source = blankToNull(source);
        classpathLocation = blankToNull(classpathLocation);
        downloadUrl = normalizeDownloadUrl(downloadUrl);
        if (httpConnectTimeout == null) {
            httpConnectTimeout = DEFAULT_HTTP_CONNECT_TIMEOUT;
        }
        if (httpReadTimeout == null) {
            httpReadTimeout = DEFAULT_HTTP_READ_TIMEOUT;
        }
        if (maxSize == null) {
            maxSize = DEFAULT_MAX_SIZE;
        }
        if (httpConnectTimeout.isZero() || httpConnectTimeout.isNegative()) {
            throw new IllegalArgumentException("datenportal.catalog.http-connect-timeout must be greater than zero");
        }
        if (httpReadTimeout.isZero() || httpReadTimeout.isNegative()) {
            throw new IllegalArgumentException("datenportal.catalog.http-read-timeout must be greater than zero");
        }
        if (maxSize.toBytes() <= 0) {
            throw new IllegalArgumentException("datenportal.catalog.max-size must be greater than zero");
        }
    }

    public enum SourceType {
        CLASSPATH,
        FILE,
        HTTP
    }

    public SourceType effectiveSourceType() {
        if (sourceType != null) {
            return sourceType;
        }
        if (source == null) {
            return SourceType.CLASSPATH;
        }
        if (source.startsWith("classpath:") || !source.contains(":")) {
            return SourceType.CLASSPATH;
        }
        if (source.startsWith("file:")) {
            return SourceType.FILE;
        }
        if (source.startsWith("http://") || source.startsWith("https://")) {
            return SourceType.HTTP;
        }
        throw new IllegalStateException("Unsupported catalog source: " + source);
    }

    public boolean isClasspathSource() {
        return effectiveSourceType() == SourceType.CLASSPATH;
    }

    public boolean isFileSource() {
        return effectiveSourceType() == SourceType.FILE;
    }

    public boolean isHttpSource() {
        return effectiveSourceType() == SourceType.HTTP;
    }

    public String classpathLocation() {
        if (!isClasspathSource()) {
            throw new IllegalStateException("Catalog source is not a classpath source.");
        }

        String location = sourceType == null && source != null
                ? (source.startsWith("classpath:") ? source.substring("classpath:".length()) : source)
                : classpathLocation;
        if (location == null) {
            location = DEFAULT_CLASSPATH_LOCATION;
        }
        return location.startsWith("/") ? location.substring(1) : location;
    }

    public Path fileLocation() {
        if (!isFileSource()) {
            throw new IllegalStateException("Catalog source is not a file source.");
        }

        if (sourceType == null && source != null && source.startsWith("file:")) {
            String location = source.substring("file:".length()).trim();
            if (location.isEmpty()) {
                throw new IllegalArgumentException("datenportal.catalog.source file location must not be blank");
            }
            return Path.of(location);
        }
        if (fileLocation == null) {
            throw new IllegalArgumentException("datenportal.catalog.file-location must be set for file catalog sources");
        }
        return fileLocation;
    }

    public URI httpUrl() {
        if (!isHttpSource()) {
            throw new IllegalStateException("Catalog source is not an HTTP source.");
        }

        URI uri = sourceType == null && source != null ? URI.create(source) : httpUrl;
        if (uri == null) {
            throw new IllegalArgumentException("datenportal.catalog.http-url must be set for HTTP catalog sources");
        }
        String scheme = uri.getScheme();
        if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
            throw new IllegalArgumentException("datenportal.catalog.http-url must use http or https");
        }
        return uri;
    }

    private static String normalizeDownloadUrl(String value) {
        String normalized = value == null
                ? DEFAULT_DOWNLOAD_URL
                : CatalogDownloadUrlPlaceholderResolver.normalizeDownloadUrl(value);
        if (normalized == null) {
            return null;
        }
        if (normalized.startsWith("/")) {
            return normalized;
        }
        URI uri = URI.create(normalized);
        String scheme = uri.getScheme();
        if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
            throw new IllegalArgumentException(
                    "datenportal.catalog.download-url must be an absolute http(s) URL or a root-relative path");
        }
        if (uri.getHost() == null || uri.getHost().isBlank()) {
            throw new IllegalArgumentException("datenportal.catalog.download-url must contain a host");
        }
        return normalized;
    }

    private static String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
