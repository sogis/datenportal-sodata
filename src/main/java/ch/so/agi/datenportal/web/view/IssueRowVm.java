package ch.so.agi.datenportal.web.view;

import java.util.List;

public record IssueRowVm(
        String id,
        String title,
        String description,
        String typeLabel,
        String publicationDateLabel,
        String detailHref,
        AccessStateVm accessState,
        List<DownloadLinkVm> downloads) {

    public IssueRowVm {
        downloads = List.copyOf(downloads);
    }
}
