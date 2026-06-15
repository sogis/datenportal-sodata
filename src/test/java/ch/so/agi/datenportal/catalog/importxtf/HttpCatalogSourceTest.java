package ch.so.agi.datenportal.catalog.importxtf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.unit.DataSize;

class HttpCatalogSourceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-06-15T00:00:00Z"), ZoneOffset.UTC);

    private HttpServer server;
    private ExecutorService executor;

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        executor = Executors.newCachedThreadPool();
        server.setExecutor(executor);
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
        executor.shutdownNow();
    }

    @Test
    void downloadsCatalogBytesAndSanitizesDescription() {
        byte[] body = "<TRANSFER/>".getBytes(StandardCharsets.UTF_8);
        server.createContext("/catalog.xtf", exchange -> {
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });

        var source = new HttpCatalogSource(
                URI.create(baseUrl() + "/catalog.xtf?token=redacted"),
                Duration.ofSeconds(1),
                Duration.ofSeconds(5),
                DataSize.ofKilobytes(64),
                CLOCK);

        CatalogBytes bytes = source.load();

        assertThat(bytes.bytes()).isEqualTo(body);
        assertThat(bytes.sourceDescription()).isEqualTo(baseUrl() + "/catalog.xtf");
        assertThat(bytes.contentHash()).hasSize(64);
        assertThat(bytes.fetchedAt()).isEqualTo(CLOCK.instant());
        assertThat(source.description()).doesNotContain("token=redacted");
    }

    @Test
    void nonSuccessStatusThrowsControlledError() {
        server.createContext("/missing.xtf", exchange -> {
            exchange.sendResponseHeaders(503, -1);
            exchange.close();
        });

        var source = new HttpCatalogSource(
                URI.create(baseUrl() + "/missing.xtf"),
                Duration.ofSeconds(1),
                Duration.ofSeconds(5),
                DataSize.ofKilobytes(64),
                CLOCK);

        assertThatThrownBy(source::load)
                .isInstanceOf(CatalogSourceException.class)
                .hasMessageContaining("status 503");
    }

    @Test
    void readTimeoutThrowsControlledError() {
        server.createContext("/slow.xtf", exchange -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            byte[] body = "<TRANSFER/>".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });

        var source = new HttpCatalogSource(
                URI.create(baseUrl() + "/slow.xtf"),
                Duration.ofMillis(50),
                Duration.ofMillis(100),
                DataSize.ofKilobytes(64),
                CLOCK);

        assertThatThrownBy(source::load)
                .isInstanceOf(CatalogSourceException.class);
    }

    @Test
    void maxSizeIsEnforced() {
        byte[] body = "abcdef".getBytes(StandardCharsets.UTF_8);
        server.createContext("/large.xtf", exchange -> {
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });

        var source = new HttpCatalogSource(
                URI.create(baseUrl() + "/large.xtf"),
                Duration.ofSeconds(1),
                Duration.ofSeconds(5),
                DataSize.ofBytes(5),
                CLOCK);

        assertThatThrownBy(source::load)
                .isInstanceOf(CatalogSourceException.class)
                .hasMessageContaining("exceeds configured maximum size");
    }

    private String baseUrl() {
        return "http://127.0.0.1:" + server.getAddress().getPort();
    }
}
