package ch.so.agi.datenportal.catalog.importxtf;

import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import org.springframework.util.unit.DataSize;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** Resolves one immutable publication generation per load, without requiring an S3 SDK. */
public final class ManifestCatalogSource implements CatalogSource {
    private final URI uri;
    private final Duration connectTimeout;
    private final Duration readTimeout;
    private final DataSize maxSize;
    private final Clock clock;

    public ManifestCatalogSource(URI uri, Duration connectTimeout, Duration readTimeout, DataSize maxSize, Clock clock) {
        if (uri.getUserInfo() != null || uri.getQuery() != null || uri.getFragment() != null) {
            throw new IllegalArgumentException("Manifest URL must not contain credentials, query or fragment.");
        }
        this.uri = uri;
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        this.maxSize = maxSize;
        this.clock = clock;
    }

    @Override
    public CatalogBytes load() {
        return loadPublished(resolve(false));
    }

    /** Resolves the pointer once, then downloads both immutable files from that generation. */
    public CatalogInputs loadInputs(Duration duckDbConnectTimeout, Duration duckDbReadTimeout, DataSize duckDbMaxSize) {
        var publication = resolve(true);
        var published = loadPublished(publication);
        var duckDb = new HttpCatalogSource(uri.resolve(publication.duckdb()), duckDbConnectTimeout,
                duckDbReadTimeout, duckDbMaxSize, clock).load();
        return new CatalogInputs(published, duckDb);
    }

    private CatalogBytes loadPublished(Publication publication) {
        return publication.catalog() == null ? CatalogBytes.absent(publication.manifest())
                : new HttpCatalogSource(uri.resolve(publication.catalog()), connectTimeout, readTimeout, maxSize, clock).load();
    }

    private record Publication(CatalogBytes manifest, String catalog, String duckdb) {}

    private Publication resolve(boolean requireDuckDb) {
        CatalogBytes manifest = new HttpCatalogSource(uri, connectTimeout, readTimeout, DataSize.ofKilobytes(64), clock).load();
        try {
            JsonNode root = JsonMapper.builder().build().readTree(manifest.bytes());
            String release = root.path("releaseId").asString("");
            JsonNode catalogNode = root.get("catalog");
            if (!root.path("schemaVersion").isIntegralNumber() || root.path("schemaVersion").asInt() != 1
                    || !root.path("releaseId").isString()
                    || !release.matches("[A-Za-z0-9][A-Za-z0-9_-]{0,127}")
                    || !root.path("datasheets").asString("").equals("datasheets-" + release + ".xtf")
                    || catalogNode == null
                    || !(catalogNode.isNull() || catalogNode.isString()
                        && catalogNode.asString().equals("published-catalog-" + release + ".xtf"))) {
                throw new CatalogSourceException("Invalid publication manifest: " + description());
            }
            JsonNode duckDbNode = root.get("duckdb");
            if ((requireDuckDb && duckDbNode == null) || (duckDbNode != null &&
                    (!duckDbNode.isString() || !duckDbNode.asString().equals("catalog-" + release + ".duckdb")))) {
                throw new CatalogSourceException("Invalid or missing DuckDB reference in publication manifest: " + description());
            }
            return new Publication(manifest, catalogNode.isNull() ? null : catalogNode.asString(),
                    duckDbNode == null ? null : duckDbNode.asString());
        } catch (CatalogSourceException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw new CatalogSourceException("Cannot parse publication manifest: " + description(), ex);
        }
    }

    @Override
    public String description() { return uri.toString(); }
}
