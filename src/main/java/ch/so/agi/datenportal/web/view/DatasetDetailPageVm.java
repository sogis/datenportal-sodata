package ch.so.agi.datenportal.web.view;

import java.util.List;

public record DatasetDetailPageVm(
        PageChromeVm chrome,
        String identifier,
        String title,
        String description,
        String typeLabel,
        AccessStateVm accessState,
        boolean structureDescribed,
        String structureQualityOriginHref,
        String modifiedLabel,
        String issuedLabel,
        List<DetailFeatureVm> features,
        DownloadSectionVm downloads,
        MetadataSectionVm overview,
        MetadataSectionVm temporalCoverage,
        MetadataSectionVm topics,
        ContactMetadataSectionVm responsibilitiesContact,
        List<MetadataSectionVm> metadataSections) {

    public DatasetDetailPageVm {
        features = List.copyOf(features);
        metadataSections = List.copyOf(metadataSections);
    }
}
