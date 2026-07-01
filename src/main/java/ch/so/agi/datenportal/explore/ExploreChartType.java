package ch.so.agi.datenportal.explore;

public enum ExploreChartType {
    BAR("bar"),
    LINE("line"),
    SCATTER("scatter"),
    HISTOGRAM("histogram");

    private final String value;

    ExploreChartType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
