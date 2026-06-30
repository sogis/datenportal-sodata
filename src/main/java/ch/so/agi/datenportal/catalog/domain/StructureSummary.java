package ch.so.agi.datenportal.catalog.domain;

public record StructureSummary(
        int objectCount,
        int attributeCount) {

    public StructureSummary {
        if (objectCount < 0) {
            throw new IllegalArgumentException("objectCount must not be negative");
        }
        if (attributeCount < 0) {
            throw new IllegalArgumentException("attributeCount must not be negative");
        }
    }
}
