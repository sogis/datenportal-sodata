package ch.so.agi.datenportal.catalog.domain;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record DatasetSeriesEntry(
        String identifier,
        String title,
        String description,
        Office publisher,
        Office creator,
        List<Theme> themes,
        List<String> keywords,
        AccessLevel accessLevel,
        CatalogEntryMetadata metadata,
        List<DatasetIssueEntry> issues) implements CatalogEntry {

    private static final Comparator<DatasetIssueEntry> ISSUE_ORDER =
            Comparator.comparing(DatasetIssueEntry::modified)
                    .reversed()
                    .thenComparing(DatasetIssueEntry::identifier);

    public DatasetSeriesEntry(
            String identifier,
            String title,
            String description,
            Office publisher,
            Office creator,
            List<Theme> themes,
            List<String> keywords,
            AccessLevel accessLevel,
            List<DatasetIssueEntry> issues) {
        this(
                identifier,
                title,
                description,
                publisher,
                creator,
                themes,
                keywords,
                accessLevel,
                CatalogEntryMetadata.empty(),
                issues);
    }

    public DatasetSeriesEntry {
        Objects.requireNonNull(identifier, "identifier must not be null");
        Objects.requireNonNull(title, "title must not be null");
        Objects.requireNonNull(description, "description must not be null");
        Objects.requireNonNull(publisher, "publisher must not be null");
        Objects.requireNonNull(creator, "creator must not be null");
        themes = List.copyOf(themes);
        keywords = List.copyOf(keywords);
        Objects.requireNonNull(accessLevel, "accessLevel must not be null");
        Objects.requireNonNull(metadata, "metadata must not be null");
        issues = List.copyOf(issues);

        if (issues.isEmpty()) {
            throw new IllegalArgumentException("Dataset series must contain at least one issue");
        }
    }

    @Override
    public LocalDate modified() {
        return currentIssueOrThrow().modified();
    }

    @Override
    public CatalogEntryType type() {
        return CatalogEntryType.DATASET_SERIES;
    }

    public Optional<DatasetIssueEntry> currentIssue() {
        var explicitlyCurrent = issues.stream()
                .filter(DatasetIssueEntry::currentIssue)
                .sorted(ISSUE_ORDER)
                .toList();

        if (!explicitlyCurrent.isEmpty()) {
            return Optional.of(explicitlyCurrent.getFirst());
        }

        return issues.stream().sorted(ISSUE_ORDER).findFirst();
    }

    public DatasetIssueEntry currentIssueOrThrow() {
        return currentIssue()
                .orElseThrow(() -> new IllegalStateException("Dataset series has no current issue"));
    }

    public List<DatasetIssueEntry> issuesNewestFirst() {
        return issues.stream()
                .sorted(ISSUE_ORDER)
                .toList();
    }

    @Override
    public List<DistributionLink> distributionsForListing() {
        return currentIssueOrThrow().primaryDistributions();
    }

    @Override
    public Optional<DistributionLink> distribution(DistributionFormat format) {
        Objects.requireNonNull(format, "format must not be null");
        return currentIssue().flatMap(issue -> issue.distribution(format));
    }

    public String currentIssueLabelForDisplay() {
        return currentIssueOrThrow().issueLabel();
    }

    public int issueCount() {
        return issues.size();
    }
}
