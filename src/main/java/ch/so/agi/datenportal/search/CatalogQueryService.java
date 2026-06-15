package ch.so.agi.datenportal.search;

import ch.so.agi.datenportal.catalog.domain.CatalogEntry;
import ch.so.agi.datenportal.catalog.domain.CatalogSnapshot;
import ch.so.agi.datenportal.catalog.domain.DatasetIssueEntry;
import ch.so.agi.datenportal.catalog.domain.DatasetSeriesEntry;
import ch.so.agi.datenportal.catalog.domain.DistributionFormat;
import java.text.Normalizer;
import java.time.Clock;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public final class CatalogQueryService {

    private static final Comparator<CatalogEntry> MODIFIED_DESC =
            Comparator.comparing(CatalogEntry::modified)
                    .reversed()
                    .thenComparing(CatalogEntry::title, String.CASE_INSENSITIVE_ORDER);

    private static final Comparator<CatalogEntry> TITLE_ASC =
            Comparator.comparing(CatalogEntry::title, String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(CatalogEntry::modified, Comparator.reverseOrder());

    private final Clock clock;

    public CatalogQueryService(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    public SearchResult search(CatalogSnapshot snapshot, SearchQuery query) {
        Objects.requireNonNull(snapshot, "snapshot must not be null");
        Objects.requireNonNull(query, "query must not be null");

        var terms = searchTerms(query.q());
        var entries = snapshot.visibleEntries().stream()
                .filter(entry -> matchesText(entry, terms))
                .filter(entry -> matchesFilters(entry, query.filters()))
                .sorted(comparatorFor(query, terms))
                .toList();

        return new SearchResult(entries, entries.size(), query);
    }

    private boolean matchesFilters(CatalogEntry entry, SearchFilters filters) {
        return matchesThemes(entry, filters)
                && matchesOffices(entry, filters)
                && matchesModifiedRanges(entry, filters)
                && matchesResourceTypes(entry, filters);
    }

    private boolean matchesThemes(CatalogEntry entry, SearchFilters filters) {
        return filters.themes().isEmpty()
                || entry.themes().stream().anyMatch(theme -> filters.themes().contains(theme.identifier()));
    }

    private boolean matchesOffices(CatalogEntry entry, SearchFilters filters) {
        return filters.offices().isEmpty() || filters.offices().contains(entry.creator().identifier());
    }

    private boolean matchesModifiedRanges(CatalogEntry entry, SearchFilters filters) {
        return filters.modifiedRanges().isEmpty()
                || filters.modifiedRanges().stream().anyMatch(range -> range.matches(entry.modified(), clock));
    }

    private boolean matchesResourceTypes(CatalogEntry entry, SearchFilters filters) {
        return filters.resourceTypes().isEmpty()
                || filters.resourceTypes().stream().anyMatch(entry::hasDistribution);
    }

    private boolean matchesText(CatalogEntry entry, List<String> terms) {
        if (terms.isEmpty()) {
            return true;
        }
        var searchText = searchText(entry);
        return terms.stream().allMatch(searchText::contains);
    }

    private Comparator<CatalogEntry> comparatorFor(SearchQuery query, List<String> terms) {
        if (query.sortMode() == SortMode.TITLE_ASC) {
            return TITLE_ASC;
        }
        if (query.sortMode() == SortMode.RELEVANCE && !terms.isEmpty()) {
            return Comparator.comparingInt((CatalogEntry entry) -> score(entry, terms))
                    .reversed()
                    .thenComparing(MODIFIED_DESC);
        }
        return MODIFIED_DESC;
    }

    private int score(CatalogEntry entry, List<String> terms) {
        int score = 0;
        String identifier = normalize(entry.identifier());
        String title = normalize(entry.title());
        String description = normalize(entry.description());
        String keywords = normalize(String.join(" ", entry.keywords()));
        String themes = normalize(entry.themes().stream()
                .map(theme -> theme.identifier() + " " + theme.displayName())
                .reduce("", (left, right) -> left + " " + right));
        String office = normalize(entry.creator().identifier() + " " + entry.creator().displayName()
                + " " + entry.creator().abbreviation().orElse(""));

        for (String term : terms) {
            if (identifier.equals(term)) {
                score += 80;
            }
            if (title.equals(term) || title.startsWith(term)) {
                score += 60;
            } else if (title.contains(term)) {
                score += 40;
            }
            if (keywords.contains(term)) {
                score += 30;
            }
            if (themes.contains(term)) {
                score += 25;
            }
            if (office.contains(term)) {
                score += 25;
            }
            if (description.contains(term)) {
                score += 10;
            }
            if (entry instanceof DatasetSeriesEntry series && issueSearchText(series).contains(term)) {
                score += 20;
            }
        }
        return score;
    }

    private static List<String> searchTerms(String value) {
        var normalized = normalize(value);
        if (normalized.isBlank()) {
            return List.of();
        }
        return Arrays.stream(normalized.split("\\s+"))
                .filter(term -> !term.isBlank())
                .toList();
    }

    private static String searchText(CatalogEntry entry) {
        var base = entry.identifier() + " "
                + entry.title() + " "
                + entry.description() + " "
                + entry.creator().identifier() + " "
                + entry.creator().displayName() + " "
                + entry.creator().abbreviation().orElse("") + " "
                + String.join(" ", entry.keywords()) + " "
                + entry.themes().stream()
                        .map(theme -> theme.identifier() + " " + theme.displayName())
                        .reduce("", (left, right) -> left + " " + right);

        if (entry instanceof DatasetSeriesEntry series) {
            return normalize(base + " " + issueSearchText(series));
        }
        return normalize(base);
    }

    private static String issueSearchText(DatasetSeriesEntry series) {
        return series.issuesNewestFirst().stream()
                .map(CatalogQueryService::issueSearchText)
                .reduce("", (left, right) -> left + " " + right);
    }

    private static String issueSearchText(DatasetIssueEntry issue) {
        return issue.identifier() + " "
                + issue.title() + " "
                + issue.description() + " "
                + issue.issueLabel() + " "
                + issue.creator().identifier() + " "
                + issue.creator().displayName() + " "
                + issue.creator().abbreviation().orElse("") + " "
                + String.join(" ", issue.keywords()) + " "
                + issue.themes().stream()
                        .map(theme -> theme.identifier() + " " + theme.displayName())
                        .reduce("", (left, right) -> left + " " + right);
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        var decomposed = Normalizer.normalize(value, Normalizer.Form.NFD);
        return decomposed.replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^\\p{Alnum}]+", " ")
                .trim();
    }
}
