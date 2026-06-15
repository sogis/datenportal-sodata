package ch.so.agi.datenportal.web.view;

import java.util.List;

public record IssueDetailPageVm(
        PageChromeVm chrome,
        String seriesTitle,
        String seriesHref,
        String identifier,
        String title,
        String issueLabel,
        boolean currentIssue,
        String description,
        boolean openData,
        boolean structureDescribed,
        String modifiedLabel,
        String issuedLabel,
        DownloadSectionVm downloads,
        SeriesIssuesVm relatedIssues,
        List<MetadataSectionVm> metadataSections) {

    public IssueDetailPageVm {
        metadataSections = List.copyOf(metadataSections);
    }
}
