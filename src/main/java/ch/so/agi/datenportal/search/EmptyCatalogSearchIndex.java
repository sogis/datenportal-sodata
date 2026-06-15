package ch.so.agi.datenportal.search;

import java.util.List;

enum EmptyCatalogSearchIndex implements CatalogSearchIndex {
    INSTANCE;

    @Override
    public List<SearchHit> search(String userQuery, int maxResults) {
        return List.of();
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public void close() {
        // Singleton has no resources to release.
    }
}
