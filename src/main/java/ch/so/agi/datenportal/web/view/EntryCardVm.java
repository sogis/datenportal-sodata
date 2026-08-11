package ch.so.agi.datenportal.web.view;

import java.util.List;

public record EntryCardVm(
        String id,
        String title,
        String description,
        String typeLabel,
        AccessStateVm accessState,
        boolean structureDescribed,
        List<String> keywords,
        List<DownloadLinkVm> downloads,
        String dateLabel,
        String detailHref) {

    public EntryCardVm {
        keywords = List.copyOf(keywords);
        downloads = List.copyOf(downloads);
    }
}
