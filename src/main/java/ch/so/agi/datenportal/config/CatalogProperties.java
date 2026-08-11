package ch.so.agi.datenportal.config;

import ch.so.agi.datenportal.catalog.importxtf.CatalogDownloadUrlPlaceholderResolver;
import java.net.URI;
import java.nio.file.Path;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

@ConfigurationProperties(prefix = "datenportal.catalog")
public record CatalogProperties(
        SourceType sourceType,
        String classpathLocation,
        Path fileLocation,
        URI httpUrl,
        Duration httpConnectTimeout,
        Duration httpReadTimeout,
        DataSize maxSize,
        String downloadUrl) {

    private static final Duration DEFAULT_HTTP_CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration DEFAULT_HTTP_READ_TIMEOUT = Duration.ofSeconds(30);
    private static final DataSize DEFAULT_MAX_SIZE = DataSize.ofMegabytes(50);

    public CatalogProperties {
        if (sourceType == null) {
            throw new IllegalArgumentException("datenportal.catalog.source-type must be set");
        }
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
        switch (sourceType) {
            case CLASSPATH -> requireClasspathLocation(classpathLocation);
            case FILE -> {
                if (fileLocation == null) {
                    throw new IllegalArgumentException(
                            "datenportal.catalog.file-location must be set for file catalog sources");
                }
            }
            case HTTP -> validateHttpUrl(httpUrl, "datenportal.catalog.http-url");
        }
    }

    public enum SourceType {
        CLASSPATH,
        FILE,
        HTTP
    }

    public boolean isClasspathSource() {
        return sourceType == SourceType.CLASSPATH;
    }

    public boolean isFileSource() {
        return sourceType == SourceType.FILE;
    }

    public boolean isHttpSource() {
        return sourceType == SourceType.HTTP;
    }

    public String classpathLocation() {
        if (!isClasspathSource()) {
            throw new IllegalStateException("Catalog source is not a classpath source.");
        }

        return classpathLocation.startsWith("/") ? classpathLocation.substring(1) : classpathLocation;
    }

    public Path fileLocation() {
        if (!isFileSource()) {
            throw new IllegalStateException("Catalog source is not a file source.");
        }

        return fileLocation;
    }

    public URI httpUrl() {
        if (!isHttpSource()) {
            throw new IllegalStateException("Catalog source is not an HTTP source.");
        }

        return httpUrl;
    }

    private static String normalizeDownloadUrl(String value) {
        String normalized = CatalogDownloadUrlPlaceholderResolver.normalizeDownloadUrl(value);
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

    private static void requireClasspathLocation(String location) {
        if (location == null) {
            throw new IllegalArgumentException(
                    "datenportal.catalog.classpath-location must be set for classpath catalog sources");
        }
    }

    private static void validateHttpUrl(URI uri, String propertyName) {
        if (uri == null) {
            throw new IllegalArgumentException(propertyName + " must be set for HTTP catalog sources");
        }
        String scheme = uri.getScheme();
        if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
            throw new IllegalArgumentException(propertyName + " must use http or https");
        }
    }
}
