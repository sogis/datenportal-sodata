package ch.so.agi.datenportal.explore;

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

    public String value() {
        return value;
    }
}
