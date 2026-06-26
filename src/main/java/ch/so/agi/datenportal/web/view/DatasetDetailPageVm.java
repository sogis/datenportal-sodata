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
        String modifiedLabel,
        String issuedLabel,
        DownloadSectionVm downloads,
        List<MetadataSectionVm> metadataSections) {

    public DatasetDetailPageVm {
        metadataSections = List.copyOf(metadataSections);
    }
}
