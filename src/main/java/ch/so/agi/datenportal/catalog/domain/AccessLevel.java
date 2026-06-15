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

    public static AccessLevel fromModelValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Access level must not be null");
        }

        return switch (value.trim().toLowerCase()) {
            case "open" -> OPEN;
            case "restricted" -> RESTRICTED;
            default -> throw new IllegalArgumentException("Unsupported access level: " + value);
        };
    }
}
