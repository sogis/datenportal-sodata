package ch.so.agi.datenportal.catalog.service;

import ch.so.agi.datenportal.catalog.domain.CatalogSnapshot;
import ch.so.agi.datenportal.catalog.importxtf.CatalogBytes;
import ch.so.agi.datenportal.catalog.importxtf.CatalogSource;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public final class CatalogSnapshotLoader {

    private final CatalogSource publishedCatalogSource;
    private final CatalogSource duckDbCatalogSource;
    private final CatalogSnapshotBuilder snapshotBuilder;

    public CatalogSnapshotLoader(
            CatalogSource publishedCatalogSource,
            @Qualifier("catalogDuckDbSource") CatalogSource duckDbCatalogSource,
            CatalogSnapshotBuilder snapshotBuilder) {
        this.publishedCatalogSource = Objects.requireNonNull(publishedCatalogSource, "publishedCatalogSource must not be null");
        this.duckDbCatalogSource = Objects.requireNonNull(duckDbCatalogSource, "duckDbCatalogSource must not be null");
        this.snapshotBuilder = Objects.requireNonNull(snapshotBuilder, "snapshotBuilder must not be null");
    }

    public CatalogSnapshot load() {
        CatalogBytes publishedCatalog = publishedCatalogSource.load();
        CatalogBytes duckDbCatalog = duckDbCatalogSource.load();
        return load(publishedCatalog, duckDbCatalog);
    }

    CatalogSnapshot load(CatalogBytes publishedCatalog, CatalogBytes duckDbCatalog) {
        return snapshotBuilder.build(publishedCatalog, duckDbCatalog).snapshot();
    }
}
