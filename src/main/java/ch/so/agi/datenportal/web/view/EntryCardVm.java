package ch.so.agi.datenportal.web.view;

import ch.so.agi.datenportal.support.metadata.MetadataTextRenderer.Html;
import java.util.List;

public record EntryCardVm(
        String id,
        String title,
        Html description,
        boolean descriptionTruncated,
        String typeLabel,
        AccessStateVm accessState,
        List<String> keywords,
        List<DownloadLinkVm> downloads,
        String dateLabel,
        String detailHref) {

    public EntryCardVm {
        keywords = List.copyOf(keywords);
        downloads = List.copyOf(downloads);
    }
}
