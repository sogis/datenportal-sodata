package ch.so.agi.datenportal.catalog.domain;

public enum AccessLevel {
    OPEN("Open Data"),
    RESTRICTED("Eingeschränkt");

    private final String displayLabel;

    AccessLevel(String displayLabel) {
        this.displayLabel = displayLabel;
    }

    public String displayLabel() {
        return displayLabel;
    }

    public boolean isOpen() {
        return this == OPEN;
    }
}
