package ch.so.agi.datenportal.search;

import java.util.Arrays;
import java.util.Optional;

public enum SortMode {
    MODIFIED_DESC("modified-desc", "Neueste zuerst"),
    TITLE_ASC("title-asc", "Titel A-Z"),
    RELEVANCE("relevance", "Relevanz");

    private final String parameterValue;
    private final String label;

    SortMode(String parameterValue, String label) {
        this.parameterValue = parameterValue;
        this.label = label;
    }

    public String parameterValue() {
        return parameterValue;
    }

    public String label() {
        return label;
    }

    public static SortMode defaultMode() {
        return MODIFIED_DESC;
    }

    public static Optional<SortMode> fromParameterValue(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        return Arrays.stream(values())
                .filter(mode -> mode.parameterValue.equalsIgnoreCase(value.trim()))
                .findFirst();
    }
}
