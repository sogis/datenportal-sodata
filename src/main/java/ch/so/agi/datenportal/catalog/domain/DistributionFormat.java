package ch.so.agi.datenportal.catalog.domain;

public enum DistributionFormat {
    CSV("CSV", 0),
    XLSX("XLSX", 1),
    PARQUET("Parquet", 2),
    OTHER("Weitere", 99);

    private final String label;
    private final int displayOrder;

    DistributionFormat(String label, int displayOrder) {
        this.label = label;
        this.displayOrder = displayOrder;
    }

    public String label() {
        return label;
    }

    public int displayOrder() {
        return displayOrder;
    }

    public boolean isPrimary() {
        return this != OTHER;
    }

    public static DistributionFormat fromModelValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Distribution format must not be null");
        }

        return switch (value.trim().toLowerCase()) {
            case "csv" -> CSV;
            case "xlsx" -> XLSX;
            case "parquet" -> PARQUET;
            case "other" -> OTHER;
            default -> throw new IllegalArgumentException("Unsupported distribution format: " + value);
        };
    }
}
