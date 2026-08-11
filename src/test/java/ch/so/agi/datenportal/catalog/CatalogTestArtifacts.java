package ch.so.agi.datenportal.catalog;

import ch.so.agi.datenportal.catalog.importxtf.CatalogBytes;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

public final class CatalogTestArtifacts {

    private CatalogTestArtifacts() {
    }

    public static CatalogBytes published(String description) {
        return new CatalogBytes("<TRANSFER/>".getBytes(StandardCharsets.UTF_8), description, Instant.EPOCH);
    }

    public static CatalogBytes published(String description, String contentHash) {
        return new CatalogBytes(
                "<TRANSFER/>".getBytes(StandardCharsets.UTF_8), description, contentHash, Instant.EPOCH);
    }

    public static CatalogBytes duckDb(String description) {
        byte[] bytes = new byte[12];
        bytes[8] = 'D';
        bytes[9] = 'U';
        bytes[10] = 'C';
        bytes[11] = 'K';
        return new CatalogBytes(bytes, description, Instant.EPOCH);
    }
}
