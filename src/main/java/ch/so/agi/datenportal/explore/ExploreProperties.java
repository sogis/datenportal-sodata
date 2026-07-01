package ch.so.agi.datenportal.explore;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "datenportal.explore")
public record ExploreProperties(
        boolean enabled,
        int maxPreviewRows,
        int maxResultRows,
        int queryTimeoutMs,
        boolean chartsEnabled,
        boolean localHistoryEnabled,
        boolean aiEnabled,
        boolean webrEnabled,
        boolean vegaEnabled,
        boolean mosaicEnabled,
        boolean geospatialEnabled) {

    private static final int DEFAULT_MAX_PREVIEW_ROWS = 100;
    private static final int DEFAULT_MAX_RESULT_ROWS = 10_000;
    private static final int DEFAULT_QUERY_TIMEOUT_MS = 30_000;

    public ExploreProperties {
        maxPreviewRows = maxPreviewRows > 0 ? maxPreviewRows : DEFAULT_MAX_PREVIEW_ROWS;
        maxResultRows = maxResultRows > 0 ? maxResultRows : DEFAULT_MAX_RESULT_ROWS;
        queryTimeoutMs = queryTimeoutMs > 0 ? queryTimeoutMs : DEFAULT_QUERY_TIMEOUT_MS;
    }

    public ExploreExecutionDto execution() {
        return new ExploreExecutionDto(
                "duckdb-wasm",
                "browser-local",
                maxPreviewRows,
                maxResultRows,
                queryTimeoutMs);
    }

    public ExploreFeatureFlagsDto featureFlags() {
        return new ExploreFeatureFlagsDto(
                chartsEnabled,
                localHistoryEnabled,
                aiEnabled,
                webrEnabled,
                vegaEnabled,
                mosaicEnabled,
                geospatialEnabled);
    }
}
