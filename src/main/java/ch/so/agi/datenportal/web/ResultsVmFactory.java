package ch.so.agi.datenportal.web;

import ch.so.agi.datenportal.catalog.domain.CatalogEntry;
import ch.so.agi.datenportal.catalog.domain.DatasetIssueEntry;
import ch.so.agi.datenportal.catalog.domain.DatasetSeriesEntry;
import ch.so.agi.datenportal.catalog.domain.DistributionLink;
import ch.so.agi.datenportal.search.SearchResult;
import ch.so.agi.datenportal.web.view.AccessStateVm;
import ch.so.agi.datenportal.web.view.EntryCardVm;
import ch.so.agi.datenportal.web.view.DownloadLinkVm;
import ch.so.agi.datenportal.web.view.IssueRowVm;
import ch.so.agi.datenportal.web.view.EntryRowVm;
import ch.so.agi.datenportal.web.view.ResultsVm;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public final class ResultsVmFactory {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.uuuu");

    private final CatalogUrlFactory urlFactory;

    public ResultsVmFactory(CatalogUrlFactory urlFactory) {
        this.urlFactory = urlFactory;
    }

    public ResultsVm create(SearchResult searchResult, CatalogQueryParams queryParams) {
        var normalized = queryParams.normalized();
        return new ResultsVm(
                searchResult.totalElements(),
                normalized.viewMode(),
                urlFactory.withView(normalized, ViewMode.LIST),
                urlFactory.withView(normalized, ViewMode.CARDS),
                normalized.sortMode(),
                searchResult.entries().stream()
                        .map(entry -> row(entry, normalized))
                        .toList(),
                searchResult.entries().stream()
                        .map(this::card)
                        .toList());
    }

    private EntryRowVm row(CatalogEntry entry, CatalogQueryParams params) {
        var expanded = entry instanceof DatasetSeriesEntry && params.expanded().contains(entry.identifier());
        return new EntryRowVm(
                entry.identifier(),
                entry.title(),
                entry.description(),
                entry.type().label(),
                themeLabel(entry),
                DATE_FORMATTER.format(entry.modified()),
                detailHref(entry),
                accessState(entry),
                entry.distributionsForListing().stream()
                        .map(link -> download(entry.title(), link))
                        .toList(),
                entry instanceof DatasetSeriesEntry,
                expanded,
                entry instanceof DatasetSeriesEntry ? urlFactory.toggleExpanded(params, entry.identifier()) : "",
                expanded ? "Ausgaben einklappen" : "Ausgaben aufklappen",
                expanded && entry instanceof DatasetSeriesEntry series
                        ? series.issuesNewestFirst().stream().map(issue -> issueRow(series, issue)).toList()
                        : List.of());
    }

    private IssueRowVm issueRow(DatasetSeriesEntry series, DatasetIssueEntry issue) {
        return new IssueRowVm(
                issue.identifier(),
                issue.title(),
                issue.description(),
                issue.type().label(),
                DATE_FORMATTER.format(issue.modified()),
                issueDetailHref(series, issue),
                accessState(issue),
                issue.distributionsForListing().stream()
                        .map(link -> download(issue.title(), link))
                        .toList());
    }

    private EntryCardVm card(CatalogEntry entry) {
        return new EntryCardVm(
                entry.identifier(),
                entry.title(),
                entry.description(),
                entry.type().label(),
                accessState(entry),
                keywords(entry),
                entry.distributionsForListing().stream()
                        .map(link -> download(entry.title(), link))
                        .toList(),
                "Aktualisiert: " + DATE_FORMATTER.format(entry.modified()),
                detailHref(entry));
    }

    private DownloadLinkVm download(String entryTitle, DistributionLink link) {
        var label = link.displayLabel();
        return new DownloadLinkVm(
                label,
                link.preferredHref().toString(),
                label + " herunterladen: " + entryTitle);
    }

    private static AccessStateVm accessState(CatalogEntry entry) {
        return new AccessStateVm(entry.isOpenData(), entry.accessLevel().displayLabel());
    }

    private String detailHref(CatalogEntry entry) {
        if (entry instanceof DatasetSeriesEntry series) {
            return urlFactory.seriesDetail(series.identifier());
        }
        return urlFactory.datasetDetail(entry.identifier());
    }

    private String issueDetailHref(DatasetSeriesEntry series, DatasetIssueEntry issue) {
        return urlFactory.issueDetail(series.identifier(), issue.identifier());
    }

    private static String themeLabel(CatalogEntry entry) {
        return entry.themes().stream()
                .map(theme -> theme.displayName())
                .reduce((left, right) -> left + ", " + right)
                .orElse("");
    }

    private static List<String> keywords(CatalogEntry entry) {
        if (!entry.keywords().isEmpty()) {
            return entry.keywords().stream().limit(5).toList();
        }
        return entry.themes().stream()
                .map(theme -> theme.displayName())
                .limit(5)
                .toList();
    }
}
