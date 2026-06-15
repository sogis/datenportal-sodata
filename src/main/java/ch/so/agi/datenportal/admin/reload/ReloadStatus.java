package ch.so.agi.datenportal.admin.reload;

import ch.so.agi.datenportal.catalog.domain.CatalogSnapshot;
import java.time.Instant;

public record ReloadStatus(
        boolean running,
        Instant loadedAt,
        String sourceDescription,
        String contentHash,
        int visibleEntries,
        int visibleDatasets,
        int visibleSeries,
        int visibleIssues,
        ReloadResult lastSuccessfulReload,
        ReloadResult lastFailedReload) {

    public static ReloadStatus from(
            CatalogSnapshot snapshot,
            ReloadResult lastSuccessfulReload,
            ReloadResult lastFailedReload,
            boolean running) {
        return new ReloadStatus(
                running,
                snapshot.loadedAt(),
                snapshot.sourceDescription(),
                snapshot.contentHash(),
                snapshot.visibleEntries().size(),
                snapshot.catalog().datasetCount(),
                snapshot.catalog().seriesCount(),
                snapshot.catalog().issueCount(),
                lastSuccessfulReload,
                lastFailedReload);
    }
}
