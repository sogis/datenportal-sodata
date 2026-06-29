package ch.so.agi.datenportal.web.view;

public record AttributeRowVm(
        String name,
        String dataType,
        String mandatoryLabel,
        String unit,
        String description) {}
