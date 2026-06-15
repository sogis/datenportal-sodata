package ch.so.agi.datenportal.web.view;

import java.util.List;

public record SeriesIssuesVm(
        String title,
        List<SeriesIssueVm> issues) {

    public SeriesIssuesVm {
        issues = List.copyOf(issues);
    }
}
