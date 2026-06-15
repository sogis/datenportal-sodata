package ch.so.agi.datenportal.catalog.domain;

public enum CatalogEntryType {
    DATASET("Datensatz"),
    DATASET_SERIES("Datenreihe"),
    DATASET_ISSUE("Ausgabe");

    private final String label;

    CatalogEntryType(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
