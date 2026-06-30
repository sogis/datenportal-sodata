package ch.so.agi.datenportal.catalog.domain;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Objects;

public record QualitySummary(
        String status,
        int errors,
        OffsetDateTime validatedAt,
        URI reportUrl) {

    public QualitySummary {
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("status must not be blank");
        }
        if (errors < 0) {
            throw new IllegalArgumentException("errors must not be negative");
        }
        Objects.requireNonNull(validatedAt, "validatedAt must not be null");
        Objects.requireNonNull(reportUrl, "reportUrl must not be null");
        status = status.trim();
    }
}
