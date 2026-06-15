package ch.so.agi.datenportal.search;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Year;
import java.util.Arrays;
import java.util.Optional;

public enum ModifiedDateRange {
    LAST_30_DAYS("last30", "Letzte 30 Tage"),
    LAST_6_MONTHS("last6months", "Letzte 6 Monate"),
    THIS_YEAR("thisYear", "Dieses Jahr"),
    LAST_YEAR("lastYear", "Letztes Jahr"),
    OLDER("older", "Älter");

    private final String parameterValue;
    private final String label;

    ModifiedDateRange(String parameterValue, String label) {
        this.parameterValue = parameterValue;
        this.label = label;
    }

    public String parameterValue() {
        return parameterValue;
    }

    public String label() {
        return label;
    }

    public boolean matches(LocalDate date, Clock clock) {
        var today = LocalDate.now(clock);
        var thisYearStart = Year.from(today).atDay(1);
        var lastYearStart = thisYearStart.minusYears(1);

        return switch (this) {
            case LAST_30_DAYS -> !date.isBefore(today.minusDays(30)) && !date.isAfter(today);
            case LAST_6_MONTHS -> !date.isBefore(today.minusMonths(6)) && !date.isAfter(today);
            case THIS_YEAR -> !date.isBefore(thisYearStart) && !date.isAfter(today);
            case LAST_YEAR -> !date.isBefore(lastYearStart) && date.isBefore(thisYearStart);
            case OLDER -> date.isBefore(lastYearStart);
        };
    }

    public static Optional<ModifiedDateRange> fromParameterValue(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        return Arrays.stream(values())
                .filter(range -> range.parameterValue.equalsIgnoreCase(value.trim()))
                .findFirst();
    }
}
