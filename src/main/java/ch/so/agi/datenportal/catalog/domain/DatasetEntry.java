package ch.so.agi.datenportal.catalog.domain;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record DatasetEntry(
        String identifier,
        String title,
        String description,
        Office publisher,
        Office creator,
        List<Theme> themes,
        List<String> keywords,
        LocalDate modified,
        AccessLevel accessLevel,
        List<DistributionLink> distributions) implements CatalogEntry {

    private static final Comparator<DistributionLink> DISTRIBUTION_ORDER =
            Comparator.comparingInt(link -> link.format().displayOrder());

    public DatasetEntry {
        Objects.requireNonNull(identifier, "identifier must not be null");
        Objects.requireNonNull(title, "title must not be null");
        Objects.requireNonNull(description, "description must not be null");
        Objects.requireNonNull(publisher, "publisher must not be null");
        Objects.requireNonNull(creator, "creator must not be null");
        themes = List.copyOf(themes);
        keywords = List.copyOf(keywords);
        Objects.requireNonNull(modified, "modified must not be null");
        Objects.requireNonNull(accessLevel, "accessLevel must not be null");
        distributions = List.copyOf(distributions);
    }

    @Override
    public CatalogEntryType type() {
        return CatalogEntryType.DATASET;
    }

    @Override
    public List<DistributionLink> distributionsForListing() {
        return primaryDistributions();
    }

    @Override
    public Optional<DistributionLink> distribution(DistributionFormat format) {
        Objects.requireNonNull(format, "format must not be null");
        return distributions.stream()
                .filter(link -> link.format() == format)
                .findFirst();
    }

    public List<DistributionLink> primaryDistributions() {
        return distributions.stream()
                .filter(DistributionLink::isPrimaryFormat)
                .sorted(DISTRIBUTION_ORDER)
                .toList();
    }
}
