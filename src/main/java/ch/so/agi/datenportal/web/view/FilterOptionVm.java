package ch.so.agi.datenportal.web.view;

public record FilterOptionVm(
        String value,
        String label,
        long resultCount,
        boolean selected) {}
