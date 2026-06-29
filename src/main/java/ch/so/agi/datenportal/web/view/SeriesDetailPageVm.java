package ch.so.agi.datenportal.web.view;

public record SeriesDetailPageVm(
        PageChromeVm chrome,
        String identifier,
        String title,
        String description,
        String typeLabel,
        AccessStateVm accessState,
        boolean structureDescribed,
        String modifiedLabel,
        String issuedLabel,
        SeriesIssuesVm issues) {}
