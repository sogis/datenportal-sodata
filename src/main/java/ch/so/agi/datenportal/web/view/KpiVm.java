package ch.so.agi.datenportal.web.view;

import java.util.Optional;

public record KpiVm(
        String title,
        String value,
        Optional<String> detail,
        String iconName) {

    public KpiVm {
        detail = detail == null ? Optional.empty() : detail;
    }
}
