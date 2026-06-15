package ch.so.agi.datenportal.catalog.importxtf;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Clock;
import java.time.Duration;
import java.util.Objects;
import org.springframework.util.unit.DataSize;

public final class HttpCatalogSource implements CatalogSource {

    private final HttpClient httpClient;
    private final URI uri;
    private final Duration readTimeout;
    private final DataSize maxSize;
    private final Clock clock;
    private final String description;

    public HttpCatalogSource(
            URI uri,
            Duration connectTimeout,
            Duration readTimeout,
            DataSize maxSize,
            Clock clock) {
        this(
                HttpClient.newBuilder()
                        .connectTimeout(requirePositive(connectTimeout, "connectTimeout"))
                        .followRedirects(HttpClient.Redirect.NORMAL)
                        .build(),
                uri,
                readTimeout,
                maxSize,
                clock);
    }

    HttpCatalogSource(
            HttpClient httpClient,
            URI uri,
            Duration readTimeout,
            DataSize maxSize,
            Clock clock) {
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient must not be null");
        this.uri = validateUri(uri);
        this.readTimeout = requirePositive(readTimeout, "readTimeout");
        this.maxSize = Objects.requireNonNull(maxSize, "maxSize must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
        this.description = safeDescription(this.uri);
    }

    @Override
    public CatalogBytes load() {
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(readTimeout)
                .header("Accept", "application/xml,text/xml,*/*")
                .GET()
                .build();

        try {
            HttpResponse<java.io.InputStream> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            try (var body = response.body()) {
                int statusCode = response.statusCode();
                if (statusCode < 200 || statusCode >= 300) {
                    throw new CatalogSourceException(
                            "HTTP catalog source returned status " + statusCode + ": " + description);
                }
                return CatalogBytesReader.read(body, description, maxSize, clock);
            }
        } catch (IOException ex) {
            throw new CatalogSourceException("Failed to download HTTP catalog source: " + description, ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new CatalogSourceException("Interrupted while downloading HTTP catalog source: " + description, ex);
        }
    }

    @Override
    public String description() {
        return description;
    }

    private static URI validateUri(URI uri) {
        Objects.requireNonNull(uri, "uri must not be null");
        String scheme = uri.getScheme();
        if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
            throw new IllegalArgumentException("HTTP catalog source URI must use http or https.");
        }
        if (uri.getHost() == null || uri.getHost().isBlank()) {
            throw new IllegalArgumentException("HTTP catalog source URI must contain a host.");
        }
        return uri;
    }

    private static Duration requirePositive(Duration duration, String name) {
        Objects.requireNonNull(duration, name + " must not be null");
        if (duration.isZero() || duration.isNegative()) {
            throw new IllegalArgumentException(name + " must be greater than zero.");
        }
        return duration;
    }

    private static String safeDescription(URI uri) {
        try {
            String path = uri.getRawPath();
            return new URI(
                    uri.getScheme(),
                    null,
                    uri.getHost(),
                    uri.getPort(),
                    path == null || path.isBlank() ? "/" : path,
                    null,
                    null).toString();
        } catch (URISyntaxException ex) {
            return uri.getScheme() + "://" + uri.getHost();
        }
    }
}
