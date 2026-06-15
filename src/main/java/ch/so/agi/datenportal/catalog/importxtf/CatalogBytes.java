package ch.so.agi.datenportal.catalog.importxtf;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Objects;

public record CatalogBytes(byte[] bytes, String sourceDescription) {

    public CatalogBytes {
        Objects.requireNonNull(bytes, "bytes must not be null");
        Objects.requireNonNull(sourceDescription, "sourceDescription must not be null");
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
}
