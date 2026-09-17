package ch.so.agi.datenportal.catalog.service;

import ch.so.agi.datenportal.catalog.domain.CatalogSnapshot;
import ch.so.agi.datenportal.catalog.importxtf.CatalogBytes;
import ch.so.agi.datenportal.catalog.importxtf.CatalogInputsSource;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public final class CatalogSnapshotLoader {

    private final CatalogInputsSource inputsSource;
    private final CatalogSnapshotBuilder snapshotBuilder;

    public CatalogSnapshotLoader(
            CatalogInputsSource inputsSource,
            CatalogSnapshotBuilder snapshotBuilder) {
        this.inputsSource = Objects.requireNonNull(inputsSource, "inputsSource must not be null");
        this.snapshotBuilder = Objects.requireNonNull(snapshotBuilder, "snapshotBuilder must not be null");
    }

    public CatalogSnapshot load() {
        var inputs = inputsSource.load();
        return load(inputs.publishedCatalog(), inputs.duckDbCatalog());
    }

    CatalogSnapshot load(CatalogBytes publishedCatalog, CatalogBytes duckDbCatalog) {
        return snapshotBuilder.build(publishedCatalog, duckDbCatalog).snapshot();
    }
}
