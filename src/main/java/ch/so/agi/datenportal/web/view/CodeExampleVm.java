package ch.so.agi.datenportal.web.view;

public record CodeExampleVm(
        String id,
        String label,
        String code,
        boolean selected) {}
