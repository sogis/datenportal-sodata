package ch.so.agi.datenportal.explore;

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

    public String value() {
        return value;
    }
}
