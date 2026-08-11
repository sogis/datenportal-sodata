package ch.so.agi.datenportal.search;

import java.util.List;

enum EmptyCatalogSearchIndex implements CatalogSearchIndex {
    INSTANCE;

    @Override
    public List<SearchHit> search(String userQuery) {
        return List.of();
    }

    @Override
    public int documentCount() {
        return 0;
    }

    @Override
    public void close() {
        // Singleton has no resources to release.
    }
}
