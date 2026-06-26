package ch.so.agi.datenportal.web.view;

import java.util.List;

public record SeriesDetailPageVm(
        PageChromeVm chrome,
        String identifier,
        String title,
        String description,
        String typeLabel,
        AccessStateVm accessState,
        boolean structureDescribed,
        String modifiedLabel,
        String issuedLabel,
        String currentIssueLabel,
        String currentIssueHref,
        DownloadSectionVm currentIssueDownloads,
        SeriesIssuesVm issues,
        List<MetadataSectionVm> metadataSections) {

    public SeriesDetailPageVm {
        metadataSections = List.copyOf(metadataSections);
    }
}
