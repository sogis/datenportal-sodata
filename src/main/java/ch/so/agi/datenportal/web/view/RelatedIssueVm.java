package ch.so.agi.datenportal.web.view;

public record RelatedIssueVm(
        String issueLabel,
        String title,
        String publicationDateLabel,
        String detailHref,
        boolean current) {}
