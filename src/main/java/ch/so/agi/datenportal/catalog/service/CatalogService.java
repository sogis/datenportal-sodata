package ch.so.agi.datenportal.catalog.service;

import ch.so.agi.datenportal.catalog.domain.CatalogEntry;
import ch.so.agi.datenportal.catalog.domain.CatalogSnapshot;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.stereotype.Service;

@Service
public final class CatalogService {

    private final AtomicReference<CatalogSnapshot> snapshotReference;

    public CatalogService(StaticCatalogFactory staticCatalogFactory) {
        this.snapshotReference = new AtomicReference<>(staticCatalogFactory.createSnapshot());
    }

    public CatalogSnapshot currentSnapshot() {
        return snapshotReference.get();
    }

    public List<CatalogEntry> visibleEntries() {
        return currentSnapshot().visibleEntries();
    }

    public Optional<CatalogEntry> findVisibleEntry(String identifier) {
        return currentSnapshot().findVisibleEntry(identifier);
    }

    public Optional<CatalogEntry> findAnyEntry(String identifier) {
        return currentSnapshot().findAnyEntry(identifier);
    }

    public void replaceSnapshot(CatalogSnapshot snapshot) {
        snapshotReference.set(Objects.requireNonNull(snapshot, "snapshot must not be null"));
    }
}
