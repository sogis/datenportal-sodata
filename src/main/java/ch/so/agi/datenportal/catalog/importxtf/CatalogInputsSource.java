package ch.so.agi.datenportal.catalog.importxtf;

/** Loads a coherent publication, or an explicitly configured pair of independent sources. */
@FunctionalInterface
public interface CatalogInputsSource {
    CatalogInputs load();

    static CatalogInputsSource independent(CatalogSource published, CatalogSource duckDb) {
        return () -> new CatalogInputs(published.load(), duckDb.load());
    }
}
