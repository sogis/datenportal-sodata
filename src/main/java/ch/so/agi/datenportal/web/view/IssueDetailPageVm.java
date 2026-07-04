package ch.so.agi.datenportal.web.view;

import java.util.List;

public record IssueDetailPageVm(
        PageChromeVm chrome,
        String seriesTitle,
        String seriesHref,
        String identifier,
        String title,
        String description,
        AccessStateVm accessState,
        boolean structureDescribed,
        String structureQualityOriginHref,
        String exploreHref,
        String usageHref,
        boolean currentIssue,
        String modifiedLabel,
        String issuedLabel,
        List<DetailFeatureVm> features,
        DownloadSectionVm downloads,
        MetadataSectionVm overview,
        MetadataSectionVm temporalCoverage,
        MetadataSectionVm topics,
        ContactMetadataSectionVm responsibilitiesContact,
        RelatedIssuesVm relatedIssues) {

    public IssueDetailPageVm {
        features = List.copyOf(features);
    }
}
