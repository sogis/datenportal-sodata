package ch.so.agi.datenportal.web;

import java.util.Arrays;
import java.util.Optional;

public enum ViewMode {
    LIST("list", "Listenansicht"),
    CARDS("cards", "Kartenansicht");

    private final String parameterValue;
    private final String label;

    ViewMode(String parameterValue, String label) {
        this.parameterValue = parameterValue;
        this.label = label;
    }

    public String parameterValue() {
        return parameterValue;
    }

    public String label() {
        return label;
    }

    public static ViewMode defaultMode() {
        return LIST;
    }

    public static Optional<ViewMode> fromParameterValue(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        return Arrays.stream(values())
                .filter(mode -> mode.parameterValue.equalsIgnoreCase(value.trim()))
                .findFirst();
    }
}
