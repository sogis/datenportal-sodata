package ch.so.agi.datenportal.catalog.service;

import ch.so.agi.datenportal.catalog.domain.CatalogEntry;
import ch.so.agi.datenportal.catalog.domain.CatalogSnapshot;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import org.springframework.stereotype.Service;

@Service
public final class CatalogService {

    private final AtomicReference<CatalogSnapshot> snapshotReference;
    private final ReentrantReadWriteLock snapshotLock = new ReentrantReadWriteLock(true);

    public CatalogService(CatalogSnapshot initialSnapshot) {
        this.snapshotReference = new AtomicReference<>(Objects.requireNonNull(initialSnapshot, "initialSnapshot must not be null"));
    }

    public CatalogSnapshot currentSnapshot() {
        var readLock = snapshotLock.readLock();
        readLock.lock();
        try {
            return snapshotReference.get();
        } finally {
            readLock.unlock();
        }
    }

    public <T> T withSnapshot(Function<CatalogSnapshot, T> reader) {
        Objects.requireNonNull(reader, "reader must not be null");
        var readLock = snapshotLock.readLock();
        readLock.lock();
        try {
            return reader.apply(snapshotReference.get());
        } finally {
            readLock.unlock();
        }
    }

    public List<CatalogEntry> visibleEntries() {
        return withSnapshot(CatalogSnapshot::visibleEntries);
    }

    public Optional<CatalogEntry> findVisibleEntry(String identifier) {
        return withSnapshot(snapshot -> snapshot.findVisibleEntry(identifier));
    }

    public Optional<CatalogEntry> findAnyEntry(String identifier) {
        return withSnapshot(snapshot -> snapshot.findAnyEntry(identifier));
    }

    public void replaceSnapshot(CatalogSnapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot must not be null");

        CatalogSnapshot previous;
        var writeLock = snapshotLock.writeLock();
        writeLock.lock();
        try {
            previous = snapshotReference.getAndSet(snapshot);
        } finally {
            writeLock.unlock();
        }

        if (previous != snapshot) {
            previous.close();
        }
    }
}
