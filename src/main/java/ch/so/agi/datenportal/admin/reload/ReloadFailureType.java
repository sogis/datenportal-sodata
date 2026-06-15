package ch.so.agi.datenportal.admin.reload;

public enum ReloadFailureType {
    NONE,
    SOURCE,
    PARSE,
    VALIDATION,
    INDEX,
    CONFLICT,
    UNEXPECTED
}
