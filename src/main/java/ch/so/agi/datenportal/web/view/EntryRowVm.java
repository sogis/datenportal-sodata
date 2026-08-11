package ch.so.agi.datenportal.web.view;

import java.util.List;

public record EntryRowVm(
        String id,
        String title,
        String description,
        String typeLabel,
        String themeLabel,
        String publicationDateLabel,
        String detailHref,
        AccessStateVm accessState,
        List<DownloadLinkVm> downloads,
        boolean series,
        boolean expanded,
        String expandHref,
        String expandLabel,
        List<IssueRowVm> issueRows) {

    public EntryRowVm {
        downloads = List.copyOf(downloads);
        issueRows = List.copyOf(issueRows);
    }
}
