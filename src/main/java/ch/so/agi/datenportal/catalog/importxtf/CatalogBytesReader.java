package ch.so.agi.datenportal.catalog.importxtf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.util.HexFormat;
import java.util.Objects;
import org.springframework.util.unit.DataSize;

final class CatalogBytesReader {

    private static final int BUFFER_SIZE = 8192;

    private CatalogBytesReader() {}

    static CatalogBytes read(
            InputStream inputStream,
            String sourceDescription,
            DataSize maxSize,
            Clock clock) throws IOException {
        Objects.requireNonNull(inputStream, "inputStream must not be null");
        Objects.requireNonNull(sourceDescription, "sourceDescription must not be null");
        Objects.requireNonNull(maxSize, "maxSize must not be null");
        Objects.requireNonNull(clock, "clock must not be null");

        long maxBytes = maxSize.toBytes();
        if (maxBytes <= 0) {
            throw new IllegalArgumentException("maxSize must be greater than zero");
        }

        MessageDigest digest = sha256();
        try (var digestInput = new DigestInputStream(inputStream, digest);
             var output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[BUFFER_SIZE];
            long totalBytes = 0;
            int read;
            while ((read = digestInput.read(buffer)) >= 0) {
                totalBytes += read;
                if (totalBytes > maxBytes) {
                    throw new CatalogSourceException(
                            "Catalog source exceeds configured maximum size of " + maxBytes + " bytes: " + sourceDescription);
                }
                output.write(buffer, 0, read);
            }

            return new CatalogBytes(
                    output.toByteArray(),
                    sourceDescription,
                    HexFormat.of().formatHex(digest.digest()),
                    clock.instant());
        }
    }

    private static MessageDigest sha256() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available.", ex);
        }
    }
}
