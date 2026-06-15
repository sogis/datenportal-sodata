package ch.so.agi.datenportal.admin.reload;

import ch.so.agi.datenportal.catalog.domain.CatalogSnapshot;
import java.time.Instant;
import java.util.List;

public record ReloadResult(
        boolean success,
        ReloadFailureType failureType,
        int httpStatus,
        String message,
        Instant startedAt,
        Instant finishedAt,
        Instant loadedAt,
        String sourceDescription,
        String contentHash,
        int visibleEntries,
        int visibleDatasets,
        int visibleSeries,
        int visibleIssues,
        List<String> warnings) {

    public ReloadResult {
        failureType = failureType == null ? ReloadFailureType.NONE : failureType;
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }

    public static ReloadResult success(
            CatalogSnapshot snapshot,
            List<String> warnings,
            Instant startedAt,
            Instant finishedAt) {
        return new ReloadResult(
                true,
                ReloadFailureType.NONE,
                200,
                "Catalog reload completed.",
                startedAt,
                finishedAt,
                snapshot.loadedAt(),
                snapshot.sourceDescription(),
                snapshot.contentHash(),
                snapshot.visibleEntries().size(),
                snapshot.catalog().datasetCount(),
                snapshot.catalog().seriesCount(),
                snapshot.catalog().issueCount(),
                warnings);
    }

    public static ReloadResult failure(
            ReloadFailureType failureType,
            int httpStatus,
            String message,
            Instant startedAt,
            Instant finishedAt) {
        return new ReloadResult(
                false,
                failureType,
                httpStatus,
                message,
                startedAt,
                finishedAt,
                null,
                "",
                "",
                0,
                0,
                0,
                0,
                List.of());
    }
}
