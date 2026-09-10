package ch.so.agi.datenportal.admin.actuator;

import ch.so.agi.datenportal.catalog.service.CatalogService;
import java.util.Objects;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public final class CatalogSnapshotHealthIndicator implements HealthIndicator {

    private final CatalogService catalogService;

    public CatalogSnapshotHealthIndicator(CatalogService catalogService) {
        this.catalogService = Objects.requireNonNull(catalogService, "catalogService must not be null");
    }

    @Override
    public Health health() {
        try {
            return catalogService.withSnapshot(snapshot -> {
                var builder = Health.up();
                return builder
                        .withDetail("catalogState", snapshot.publishedCatalog().absent() ? "awaiting-first-delivery" : "loaded")
                        .withDetail("loadedAt", snapshot.loadedAt())
                        .withDetail("visibleEntries", snapshot.visibleEntries().size())
                        .withDetail("visibleDatasets", snapshot.catalog().datasetCount())
                        .withDetail("visibleSeries", snapshot.catalog().seriesCount())
                        .withDetail("visibleIssues", snapshot.catalog().issueCount())
                        .withDetail("publishedCatalogHash", snapshot.publishedCatalog().contentHash())
                        .withDetail("duckDbCatalogHash", snapshot.duckDbCatalog().contentHash())
                        .withDetail("duckDbCatalogSizeBytes", snapshot.duckDbCatalog().sizeInBytes())
                        .withDetail("loadDurationMs", snapshot.loadDuration().toMillis())
                        .build();
            });
        } catch (RuntimeException ex) {
            return Health.down(ex).build();
        }
    }
}
