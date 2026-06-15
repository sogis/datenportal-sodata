package ch.so.agi.datenportal.web.view;

import java.util.List;
import java.util.Optional;

public record CatalogEntrySummaryVm(
        String title,
        String description,
        String typeLabel,
        String modifiedLabel,
        String officeLabel,
        boolean series,
        Optional<String> currentIssueLabel,
        List<DownloadButtonVm> downloads) {

    public CatalogEntrySummaryVm {
        currentIssueLabel = currentIssueLabel == null ? Optional.empty() : currentIssueLabel;
        downloads = List.copyOf(downloads);
    }
}
