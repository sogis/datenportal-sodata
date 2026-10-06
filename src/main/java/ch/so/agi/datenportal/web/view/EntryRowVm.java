package ch.so.agi.datenportal.web.view;

import ch.so.agi.datenportal.support.metadata.MetadataTextRenderer.Html;
import java.util.List;

public record EntryRowVm(
        String id,
        String title,
        Html description,
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
