package ch.so.agi.datenportal.web.view;

import java.util.List;

public record HeaderVm(
        String activeSection,
        String logoHref,
        String siteName,
        String siteClaim,
        List<NavItemVm> primaryNav,
        List<NavItemVm> utilityNav,
        String componentConfigJson) {}
