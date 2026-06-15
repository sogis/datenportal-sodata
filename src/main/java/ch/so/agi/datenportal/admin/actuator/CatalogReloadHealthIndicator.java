package ch.so.agi.datenportal.admin.actuator;

import ch.so.agi.datenportal.admin.reload.ReloadResult;
import ch.so.agi.datenportal.catalog.service.CatalogReloadService;
import java.util.Objects;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public final class CatalogReloadHealthIndicator implements HealthIndicator {

    private final CatalogReloadService reloadService;

    public CatalogReloadHealthIndicator(CatalogReloadService reloadService) {
        this.reloadService = Objects.requireNonNull(reloadService, "reloadService must not be null");
    }

    @Override
    public Health health() {
        try {
            var status = reloadService.status();
            ReloadResult lastSuccessful = status.lastSuccessfulReload();
            ReloadResult lastFailed = status.lastFailedReload();
            var builder = Health.up()
                    .withDetail("running", status.running())
                    .withDetail("loadedAt", status.loadedAt())
                    .withDetail("lastSuccessfulReloadAt", lastSuccessful == null ? status.loadedAt() : lastSuccessful.finishedAt());
            if (lastFailed != null) {
                builder
                        .withDetail("lastFailedReloadAt", lastFailed.finishedAt())
                        .withDetail("lastFailureType", lastFailed.failureType().name());
            }
            return builder.build();
        } catch (RuntimeException ex) {
            return Health.down(ex).build();
        }
    }
}
