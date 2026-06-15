package ch.so.agi.datenportal.catalog.importxtf;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Objects;

public record CatalogBytes(byte[] bytes, String sourceDescription, String contentHash, Instant fetchedAt) {

    public CatalogBytes(byte[] bytes, String sourceDescription) {
        this(bytes, sourceDescription, sha256(bytes), Instant.EPOCH);
    }

    public CatalogBytes {
        Objects.requireNonNull(bytes, "bytes must not be null");
        Objects.requireNonNull(sourceDescription, "sourceDescription must not be null");
        Objects.requireNonNull(contentHash, "contentHash must not be null");
        Objects.requireNonNull(fetchedAt, "fetchedAt must not be null");
        bytes = Arrays.copyOf(bytes, bytes.length);
    }

    @Override
    public byte[] bytes() {
        return Arrays.copyOf(bytes, bytes.length);
    }

    public InputStream inputStream() {
        return new ByteArrayInputStream(bytes);
    }

    public int sizeInBytes() {
        return bytes.length;
    }

    private static String sha256(byte[] value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available.", ex);
        }
    }
}
