package ch.so.agi.datenportal.catalog.service;

import ch.so.agi.datenportal.catalog.domain.CatalogSnapshot;
import ch.so.agi.datenportal.catalog.importxtf.CatalogBytes;
import ch.so.agi.datenportal.catalog.importxtf.CatalogSource;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public final class CatalogSnapshotLoader {

    private final CatalogSource catalogSource;
    private final CatalogSnapshotBuilder snapshotBuilder;

    public CatalogSnapshotLoader(
            CatalogSource catalogSource,
            CatalogSnapshotBuilder snapshotBuilder) {
        this.catalogSource = Objects.requireNonNull(catalogSource, "catalogSource must not be null");
        this.snapshotBuilder = Objects.requireNonNull(snapshotBuilder, "snapshotBuilder must not be null");
    }

    public CatalogSnapshot load() {
        return load(catalogSource.load());
    }

    CatalogSnapshot load(CatalogBytes bytes) {
        return snapshotBuilder.build(bytes).snapshot();
    }
}
