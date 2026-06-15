package ch.so.agi.datenportal.admin.actuator;

import ch.so.agi.datenportal.catalog.service.CatalogService;
import java.util.Objects;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public final class CatalogSearchIndexHealthIndicator implements HealthIndicator {

    private final CatalogService catalogService;

    public CatalogSearchIndexHealthIndicator(CatalogService catalogService) {
        this.catalogService = Objects.requireNonNull(catalogService, "catalogService must not be null");
    }

    @Override
    public Health health() {
        try {
            return catalogService.withSnapshot(snapshot -> {
                boolean emptyIndex = snapshot.searchIndex().isEmpty();
                var builder = emptyIndex ? Health.down() : Health.up();
                return builder
                        .withDetail("available", !emptyIndex)
                        .withDetail("visibleEntries", snapshot.visibleEntries().size())
                        .withDetail("loadedAt", snapshot.loadedAt())
                        .build();
            });
        } catch (RuntimeException ex) {
            return Health.down(ex).build();
        }
    }
}
