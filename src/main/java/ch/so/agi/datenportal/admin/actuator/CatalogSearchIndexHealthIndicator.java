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
                int expectedDocuments = snapshot.visibleEntries().size();
                int indexedDocuments = snapshot.searchIndex().documentCount();
                var builder = expectedDocuments == indexedDocuments ? Health.up() : Health.down();
                return builder
                        .withDetail("expectedDocuments", expectedDocuments)
                        .withDetail("indexedDocuments", indexedDocuments)
                        .withDetail("loadedAt", snapshot.loadedAt())
                        .build();
            });
        } catch (RuntimeException ex) {
            return Health.down(ex).build();
        }
    }
}
