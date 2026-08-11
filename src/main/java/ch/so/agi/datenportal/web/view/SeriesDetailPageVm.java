package ch.so.agi.datenportal.web.view;

import java.util.List;

public record SeriesDetailPageVm(
        PageChromeVm chrome,
        String title,
        String description,
        List<SeriesIssueVm> issues) {

    public SeriesDetailPageVm {
        issues = List.copyOf(issues);
    }
}
