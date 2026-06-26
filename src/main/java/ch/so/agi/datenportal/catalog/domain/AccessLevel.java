package ch.so.agi.datenportal.catalog.domain;

public enum AccessLevel {
    OPEN("Open Data"),
    PUBLIC_WITH_CONDITIONS("Öffentlich mit Bedingungen"),
    RESTRICTED("Eingeschränkt"),
    INTERNAL("Intern"),
    CONFIDENTIAL("Vertraulich");

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
            case "public_with_conditions" -> PUBLIC_WITH_CONDITIONS;
            case "restricted" -> RESTRICTED;
            case "internal" -> INTERNAL;
            case "confidential" -> CONFIDENTIAL;
            default -> throw new IllegalArgumentException("Unsupported access level: " + value);
        };
    }
}
