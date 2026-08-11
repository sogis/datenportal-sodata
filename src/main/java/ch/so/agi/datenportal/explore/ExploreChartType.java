package ch.so.agi.datenportal.explore;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ExploreChartType {
    BAR("bar"),
    LINE("line"),
    SCATTER("scatter"),
    HISTOGRAM("histogram"),
    PIE("pie"),
    DONUT("donut");

    private final String value;

    ExploreChartType(String value) {
        this.value = value;
    }

    @JsonValue
    public String value() {
        return value;
    }
}
