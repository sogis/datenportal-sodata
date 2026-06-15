package ch.so.agi.datenportal.config;

import ch.so.agi.datenportal.search.PageRequest;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "datenportal.search")
public record SearchProperties(
        int maxResults,
        int defaultPageSize,
        int maxPageSize) {

    private static final int DEFAULT_MAX_RESULTS = 500;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int DEFAULT_MAX_PAGE_SIZE = 100;

    public SearchProperties {
        maxResults = maxResults > 0 ? maxResults : DEFAULT_MAX_RESULTS;
        maxPageSize = maxPageSize > 0 ? maxPageSize : DEFAULT_MAX_PAGE_SIZE;
        defaultPageSize = defaultPageSize > 0 ? defaultPageSize : DEFAULT_PAGE_SIZE;
        defaultPageSize = Math.min(defaultPageSize, maxPageSize);
    }

    public PageRequest normalize(PageRequest pageRequest) {
        return pageRequest.normalized(this);
    }
}
