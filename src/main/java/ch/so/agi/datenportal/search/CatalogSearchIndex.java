package ch.so.agi.datenportal.search;

import java.util.List;

public interface CatalogSearchIndex extends AutoCloseable {

    static CatalogSearchIndex empty() {
        return EmptyCatalogSearchIndex.INSTANCE;
    }

    List<SearchHit> search(String userQuery, int maxResults);

    boolean isEmpty();

    @Override
    void close();
}
