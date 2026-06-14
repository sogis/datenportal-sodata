package ch.so.agi.datenportal.web.view;

public record CatalogPageVm(
        PageChromeVm chrome,
        String title,
        String lead,
        String secondaryLead,
        String placeholderMessage) {}
