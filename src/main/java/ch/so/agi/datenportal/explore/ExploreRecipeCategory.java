package ch.so.agi.datenportal.explore;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ExploreRecipeCategory {
    PREVIEW("preview"),
    PROFILE("profile"),
    QUALITY("quality"),
    CATEGORY("category"),
    NUMERIC("numeric"),
    TIME("time"),
    CUSTOM("custom");

    private final String value;

    ExploreRecipeCategory(String value) {
        this.value = value;
    }

    @JsonValue
    public String value() {
        return value;
    }
}
