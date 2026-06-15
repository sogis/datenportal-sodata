package ch.so.agi.datenportal.catalog.domain;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;

public record DistributionLink(
        URI accessUrl,
        Optional<URI> downloadUrl,
        DistributionFormat format) {

    public DistributionLink {
        Objects.requireNonNull(accessUrl, "accessUrl must not be null");
        downloadUrl = Objects.requireNonNullElse(downloadUrl, Optional.empty());
        Objects.requireNonNull(format, "format must not be null");
    }

    public DistributionLink(URI accessUrl, URI downloadUrl, DistributionFormat format) {
        this(accessUrl, Optional.of(downloadUrl), format);
    }

    public DistributionLink(URI accessUrl, DistributionFormat format) {
        this(accessUrl, Optional.empty(), format);
    }

    public URI preferredHref() {
        return downloadUrl.orElse(accessUrl);
    }

    public String displayLabel() {
        return format.label();
    }

    public boolean isPrimaryFormat() {
        return format.isPrimary();
    }
}
