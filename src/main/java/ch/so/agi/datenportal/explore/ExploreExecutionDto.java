package ch.so.agi.datenportal.explore;

public record ExploreExecutionDto(
        String engine,
        String mode,
        int maxPreviewRows,
        int maxResultRows,
        int queryTimeoutMs) {
}
