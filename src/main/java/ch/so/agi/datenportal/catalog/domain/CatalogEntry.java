package ch.so.agi.datenportal.catalog.domain;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public sealed interface CatalogEntry permits DatasetEntry, DatasetSeriesEntry, DatasetIssueEntry {
    String identifier();

    String title();

    String description();

    Office publisher();

    Office creator();

    List<Theme> themes();

    List<String> keywords();

    LocalDate modified();

    AccessLevel accessLevel();

    CatalogEntryMetadata metadata();

    CatalogEntryType type();

    List<DistributionLink> distributionsForListing();

    Optional<DistributionLink> distribution(DistributionFormat format);

    default boolean hasDistribution(DistributionFormat format) {
        return distribution(format).isPresent();
    }

    default boolean isOpenData() {
        return accessLevel().isOpen();
    }

    default String detailPath() {
        return "/datasets/" + URLEncoder.encode(identifier(), StandardCharsets.UTF_8).replace("+", "%20");
    }
}
