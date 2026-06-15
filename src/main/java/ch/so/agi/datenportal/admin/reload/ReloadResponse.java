package ch.so.agi.datenportal.admin.reload;

import java.time.Instant;
import java.util.List;

public record ReloadResponse(
        boolean success,
        ReloadFailureType failureType,
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

    public ReloadResponse {
        failureType = failureType == null ? ReloadFailureType.NONE : failureType;
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
    }

    public static ReloadResponse from(ReloadResult result) {
        return new ReloadResponse(
                result.success(),
                result.failureType(),
                result.message(),
                result.startedAt(),
                result.finishedAt(),
                result.loadedAt(),
                result.sourceDescription(),
                result.contentHash(),
                result.visibleEntries(),
                result.visibleDatasets(),
                result.visibleSeries(),
                result.visibleIssues(),
                result.warnings());
    }

    public static ReloadResponse rejected(String message, ReloadFailureType failureType) {
        return new ReloadResponse(
                false,
                failureType,
                message,
                null,
                null,
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
