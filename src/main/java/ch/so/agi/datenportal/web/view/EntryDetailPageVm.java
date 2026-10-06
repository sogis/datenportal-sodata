package ch.so.agi.datenportal.web.view;

import ch.so.agi.datenportal.support.metadata.MetadataTextRenderer.Html;
import java.util.List;
import java.util.Optional;

public record EntryDetailPageVm(
        PageChromeVm chrome,
        Optional<String> seriesTitle,
        Optional<String> seriesHref,
        String title,
        Html description,
        AccessStateVm accessState,
        String structureQualityOriginHref,
        String exploreHref,
        String usageHref,
        boolean currentIssue,
        List<DetailFeatureVm> features,
        List<DownloadLinkVm> downloads,
        MetadataSectionVm overview,
        MetadataSectionVm temporalCoverage,
        MetadataSectionVm topics,
        MetadataSectionVm responsibilitiesContact,
        List<RelatedIssueVm> relatedIssues) {

    public EntryDetailPageVm {
        seriesTitle = seriesTitle == null ? Optional.empty() : seriesTitle;
        seriesHref = seriesHref == null ? Optional.empty() : seriesHref;
        features = List.copyOf(features);
        downloads = List.copyOf(downloads);
        relatedIssues = List.copyOf(relatedIssues);
    }
}
