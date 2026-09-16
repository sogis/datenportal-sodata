package ch.so.agi.datenportal.catalog.importxtf;

import static org.assertj.core.api.Assertions.*;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.util.unit.DataSize;

class ManifestCatalogSourceTest {
    private HttpServer server;
    private final AtomicInteger reads = new AtomicInteger();
    private String manifest;

    @BeforeEach void start() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/current.json", exchange -> {
            reads.incrementAndGet();
            assertThat(exchange.getRequestHeaders().getFirst("Cache-Control")).contains("no-cache");
            byte[] body = manifest.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.createContext("/missing-current.json", exchange -> {
            exchange.sendResponseHeaders(404, -1);
            exchange.close();
        });
        server.createContext("/published-catalog-a.xtf", exchange -> {
            byte[] body = "<example/>".getBytes(StandardCharsets.UTF_8);
            manifest = "{}"; // A changed pointer must not trigger another lookup during this load.
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
    }
    @AfterEach void stop() { server.stop(0); }
    private ManifestCatalogSource source() {
        return source("current.json");
    }
    private ManifestCatalogSource source(String path) {
        return new ManifestCatalogSource(URI.create("http://127.0.0.1:"+server.getAddress().getPort()+"/"+path),
                Duration.ofSeconds(1), Duration.ofSeconds(2), DataSize.ofMegabytes(1), Clock.systemUTC());
    }
    private String manifest(String catalog) {
        return "{\"schemaVersion\":1,\"releaseId\":\"a\",\"datasheets\":\"datasheets-a.xtf\",\"catalog\":"+catalog+"}";
    }
    @Test void resolvesExactlyOnce() {
        manifest = manifest("\"published-catalog-a.xtf\"");
        assertThat(new String(source().load().bytes(), StandardCharsets.UTF_8)).isEqualTo("<example/>");
        assertThat(reads.get()).isEqualTo(1);
    }
    @Test void explicitlyAbsentCatalogHasNoSyntheticXml() {
        manifest = manifest("null");
        var bytes = source().load();
        assertThat(bytes.absent()).isTrue();
        assertThat(bytes.bytes()).isEmpty();
        assertThat(bytes.contentHash()).hasSize(64);
        assertThat(reads.get()).isEqualTo(1);
    }
    @Test void missingManifestIsNotAnEmptyCatalog() {
        assertThatThrownBy(() -> source("missing-current.json").load())
                .isInstanceOf(CatalogSourceException.class)
                .hasMessageContaining("404");
    }
    @ParameterizedTest @ValueSource(strings={"{}", "broken", "{\"schemaVersion\":2}",
            "{\"schemaVersion\":1,\"releaseId\":\"a\",\"datasheets\":\"../evil.xtf\",\"catalog\":null}"})
    void rejectsInvalidManifests(String input) {
        manifest=input;
        assertThatThrownBy(() -> source().load()).isInstanceOf(CatalogSourceException.class);
    }
    @ParameterizedTest @ValueSource(strings={"\"../catalog.xtf\"", "\"https://example.org/catalog.xtf\"", "\"published-catalog-b.xtf\"", "42"})
    void rejectsUnrelatedOrUnsafeReferences(String value) {
        manifest=manifest(value);
        assertThatThrownBy(() -> source().load()).isInstanceOf(CatalogSourceException.class);
    }
    @Test void releaseIdMustBeAString() {
        manifest="{\"schemaVersion\":1,\"releaseId\":1,\"datasheets\":\"datasheets-1.xtf\",\"catalog\":null}";
        assertThatThrownBy(() -> source().load()).isInstanceOf(CatalogSourceException.class);
    }
    @Test void missingReferencedFileIsNotAnEmptyCatalog() {
        manifest="{\"schemaVersion\":1,\"releaseId\":\"missing\",\"datasheets\":\"datasheets-missing.xtf\",\"catalog\":\"published-catalog-missing.xtf\"}";
        assertThatThrownBy(() -> source().load()).isInstanceOf(CatalogSourceException.class).hasMessageContaining("404");
    }
}
