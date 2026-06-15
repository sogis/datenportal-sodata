package ch.so.agi.datenportal.web.view;

import java.util.List;

public record SeriesIssueVm(
        String issueLabel,
        String title,
        String publicationDateLabel,
        String detailHref,
        boolean current,
        List<DownloadLinkVm> downloads) {

    public SeriesIssueVm {
        downloads = List.copyOf(downloads);
    }
}
