package ch.so.agi.datenportal.web.view;

import ch.so.agi.datenportal.support.metadata.MetadataTextRenderer.Html;
import java.util.List;

public record IssueRowVm(
        String id,
        String title,
        Html description,
        String typeLabel,
        String publicationDateLabel,
        String detailHref,
        AccessStateVm accessState,
        List<DownloadLinkVm> downloads) {

    public IssueRowVm {
        downloads = List.copyOf(downloads);
    }
}
