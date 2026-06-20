package ch.so.agi.datenportal.web.view;

public record PageChromeVm(
        String pageTitle,
        HeaderVm header,
        BreadcrumbVm breadcrumb,
        WebAssetsVm assets,
        FooterVm footer) {}
