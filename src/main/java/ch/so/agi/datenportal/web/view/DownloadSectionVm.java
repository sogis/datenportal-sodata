package ch.so.agi.datenportal.web.view;

import java.util.List;

public record DownloadSectionVm(
        String title,
        String lead,
        AccessStateVm accessState,
        List<DownloadLinkVm> links) {

    public DownloadSectionVm {
        links = List.copyOf(links);
    }
}
