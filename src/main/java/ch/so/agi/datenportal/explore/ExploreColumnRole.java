package ch.so.agi.datenportal.explore;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ExploreColumnRole {
    IDENTIFIER("identifier"),
    LABEL("label"),
    CATEGORY("category"),
    MEASURE("measure"),
    DATE("date"),
    YEAR("year"),
    GEOMETRY("geometry"),
    MUNICIPALITY("municipality"),
    UNKNOWN("unknown");

    private final String value;

    ExploreColumnRole(String value) {
        this.value = value;
    }

    @JsonValue
    public String value() {
        return value;
    }
}
