package ch.so.agi.datenportal.web;

import ch.so.agi.datenportal.catalog.domain.CatalogEntry;
import ch.so.agi.datenportal.catalog.domain.DatasetSeriesEntry;
import ch.so.agi.datenportal.catalog.domain.DistributionLink;
import ch.so.agi.datenportal.web.view.CatalogEntrySummaryVm;
import ch.so.agi.datenportal.web.view.CatalogPageVm;
import ch.so.agi.datenportal.web.view.DownloadButtonVm;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class CatalogPageVmFactory {

    private static final String PAGE_TITLE = "Daten & Statistiken";
    private static final String LEAD =
            "Finden und nutzen Sie offene Daten, Geodaten und Statistiken des Kantons Solothurn.";
    private static final String SECONDARY_LEAD =
            "Alle Datensätze sind – sofern verfügbar – als Open Data mit freien Lizenzen nutzbar.";

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.uuuu");

    private final PageChromeFactory pageChromeFactory;

    public CatalogPageVmFactory(PageChromeFactory pageChromeFactory) {
        this.pageChromeFactory = pageChromeFactory;
    }

    public CatalogPageVm create(List<CatalogEntry> entries) {
        return new CatalogPageVm(
                pageChromeFactory.catalogPage(PAGE_TITLE + " | Datenportal"),
                PAGE_TITLE,
                LEAD,
                SECONDARY_LEAD,
                entries.stream().map(this::toSummaryVm).toList());
    }

    private CatalogEntrySummaryVm toSummaryVm(CatalogEntry entry) {
        return new CatalogEntrySummaryVm(
                entry.title(),
                entry.description(),
                entry.type().label(),
                DATE_FORMATTER.format(entry.modified()),
                entry.creator().displayName(),
                entry instanceof DatasetSeriesEntry,
                currentIssueLabel(entry),
                entry.distributionsForListing().stream()
                        .map(link -> toDownloadButton(entry, link))
                        .toList());
    }

    private Optional<String> currentIssueLabel(CatalogEntry entry) {
        if (entry instanceof DatasetSeriesEntry series) {
            return Optional.of(series.currentIssueLabelForDisplay());
        }
        return Optional.empty();
    }

    private DownloadButtonVm toDownloadButton(CatalogEntry entry, DistributionLink link) {
        var labelSuffix = entry instanceof DatasetSeriesEntry ? " (aktuelle Ausgabe)" : "";
        var label = link.displayLabel() + labelSuffix;
        var ariaLabel = label + " herunterladen: " + entry.title();

        return new DownloadButtonVm(label, link.preferredHref().toString(), ariaLabel);
    }
}
