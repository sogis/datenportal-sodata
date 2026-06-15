package ch.so.agi.datenportal.admin.reload;

import java.time.Instant;

public record ReloadStatusResponse(
        boolean enabled,
        boolean running,
        Instant loadedAt,
        String sourceDescription,
        String contentHash,
        int visibleEntries,
        int visibleDatasets,
        int visibleSeries,
        int visibleIssues,
        Instant lastSuccessfulReloadAt,
        Instant lastFailedReloadAt,
        String lastFailureMessage) {

    public static ReloadStatusResponse from(ReloadStatus status) {
        ReloadResult success = status.lastSuccessfulReload();
        ReloadResult failure = status.lastFailedReload();
        return new ReloadStatusResponse(
                true,
                status.running(),
                status.loadedAt(),
                status.sourceDescription(),
                status.contentHash(),
                status.visibleEntries(),
                status.visibleDatasets(),
                status.visibleSeries(),
                status.visibleIssues(),
                success == null ? null : success.finishedAt(),
                failure == null ? null : failure.finishedAt(),
                failure == null ? "" : failure.message());
    }

    public static ReloadStatusResponse disabled() {
        return new ReloadStatusResponse(
                false,
                false,
                null,
                "",
                "",
                0,
                0,
                0,
                0,
                null,
                null,
                "Reload endpoint is disabled.");
    }
}
