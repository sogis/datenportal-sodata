package ch.so.agi.datenportal.web.view;

import java.util.Optional;

public record BreadcrumbItemVm(
        String label,
        Optional<String> href,
        boolean currentPage) {}
