package ch.so.agi.datenportal.web.view;

import java.util.List;

public record CardResultVm(
        String id,
        String title,
        String description,
        String typeLabel,
        boolean openData,
        boolean structureDescribed,
        List<String> keywords,
        List<DownloadLinkVm> downloads,
        String dateLabel,
        String detailHref) {

    public CardResultVm {
        keywords = List.copyOf(keywords);
        downloads = List.copyOf(downloads);
    }
}
