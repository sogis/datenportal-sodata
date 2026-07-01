package ch.so.agi.datenportal.explore;

public enum ExploreSnippetLanguage {
    SQL("sql"),
    PYTHON("python"),
    R("r"),
    BASH("bash");

    private final String value;

    ExploreSnippetLanguage(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
