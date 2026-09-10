package ch.so.agi.datenportal.catalog.importxtf;

import java.util.Objects;

public final class DownloadUrlPlaceholderCatalogSource implements CatalogSource {

    private final CatalogSource delegate;
    private final CatalogDownloadUrlPlaceholderResolver resolver;
    private final String downloadUrl;

    public DownloadUrlPlaceholderCatalogSource(
            CatalogSource delegate,
            CatalogDownloadUrlPlaceholderResolver resolver,
            String downloadUrl) {
        this.delegate = Objects.requireNonNull(delegate, "delegate must not be null");
        this.resolver = Objects.requireNonNull(resolver, "resolver must not be null");
        this.downloadUrl = downloadUrl;
    }

    @Override
    public CatalogBytes load() throws CatalogSourceException {
        CatalogBytes bytes = delegate.load();
        return bytes.absent() ? bytes : resolver.resolve(bytes, downloadUrl);
    }

    @Override
    public String description() {
        return delegate.description();
    }
}
