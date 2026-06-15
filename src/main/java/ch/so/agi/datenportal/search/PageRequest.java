package ch.so.agi.datenportal.search;

import ch.so.agi.datenportal.config.SearchProperties;

public record PageRequest(int page, int size, boolean paged) {

    private static final PageRequest UNPAGED = new PageRequest(1, 0, false);

    public PageRequest {
        if (!paged) {
            page = 1;
            size = 0;
        }
    }

    public static PageRequest unpaged() {
        return UNPAGED;
    }

    public static PageRequest of(int page, int size) {
        return new PageRequest(page, size, true);
    }

    public int offset() {
        if (!paged) {
            return 0;
        }
        return Math.max(0, page - 1) * Math.max(0, size);
    }

    public PageRequest normalized(SearchProperties properties) {
        if (!paged) {
            return this;
        }

        int normalizedPage = Math.max(1, page);
        int normalizedSize = size > 0 ? size : properties.defaultPageSize();
        normalizedSize = Math.min(normalizedSize, properties.maxPageSize());
        return PageRequest.of(normalizedPage, normalizedSize);
    }
}
