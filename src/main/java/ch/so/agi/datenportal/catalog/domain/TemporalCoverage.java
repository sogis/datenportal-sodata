package ch.so.agi.datenportal.catalog.domain;

import java.time.LocalDate;
import java.util.Optional;

public record TemporalCoverage(
        Optional<LocalDate> startDate,
        Optional<LocalDate> endDate,
        Optional<LocalDate> referenceDate) {

    public TemporalCoverage {
        startDate = startDate == null ? Optional.empty() : startDate;
        endDate = endDate == null ? Optional.empty() : endDate;
        referenceDate = referenceDate == null ? Optional.empty() : referenceDate;
    }

    public boolean isEmpty() {
        return startDate.isEmpty() && endDate.isEmpty() && referenceDate.isEmpty();
    }
}
