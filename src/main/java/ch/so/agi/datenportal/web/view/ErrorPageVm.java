package ch.so.agi.datenportal.web.view;

public record ErrorPageVm(
        PageChromeVm chrome,
        int statusCode,
        String title,
        String message,
        String actionHref,
        String actionLabel) {}
