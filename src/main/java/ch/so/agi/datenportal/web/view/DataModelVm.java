package ch.so.agi.datenportal.web.view;

public record DataModelVm(
        String modelName,
        String modelHref,
        String validationReportName,
        String validationReportHref) {}
