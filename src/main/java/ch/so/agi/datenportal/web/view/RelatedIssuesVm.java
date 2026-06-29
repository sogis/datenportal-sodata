package ch.so.agi.datenportal.web.view;

import java.util.List;

public record RelatedIssuesVm(
        List<RelatedIssueVm> issues) {

    public RelatedIssuesVm {
        issues = List.copyOf(issues);
    }
}
