package ch.so.agi.datenportal.web;

import ch.so.agi.datenportal.catalog.domain.CatalogEntry;
import ch.so.agi.datenportal.catalog.domain.CatalogEntryMetadata;
import ch.so.agi.datenportal.catalog.domain.ContactPoint;
import ch.so.agi.datenportal.catalog.domain.DatasetEntry;
import ch.so.agi.datenportal.catalog.domain.DatasetIssueEntry;
import ch.so.agi.datenportal.catalog.domain.DatasetSeriesEntry;
import ch.so.agi.datenportal.catalog.domain.DistributionLink;
import ch.so.agi.datenportal.catalog.domain.Office;
import ch.so.agi.datenportal.catalog.domain.TemporalCoverage;
import ch.so.agi.datenportal.catalog.domain.Theme;
import ch.so.agi.datenportal.web.view.AccessStateVm;
import ch.so.agi.datenportal.web.view.DatasetDetailPageVm;
import ch.so.agi.datenportal.web.view.DetailFeatureVm;
import ch.so.agi.datenportal.web.view.DownloadLinkVm;
import ch.so.agi.datenportal.web.view.DownloadSectionVm;
import ch.so.agi.datenportal.web.view.IssueDetailPageVm;
import ch.so.agi.datenportal.web.view.MetadataItemVm;
import ch.so.agi.datenportal.web.view.MetadataSectionVm;
import ch.so.agi.datenportal.web.view.SeriesDetailPageVm;
import ch.so.agi.datenportal.web.view.SeriesIssueVm;
import ch.so.agi.datenportal.web.view.SeriesIssuesVm;
import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public final class DetailPageVmFactory {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.uuuu");
    private static final Comparator<DistributionLink> DISTRIBUTION_ORDER =
            Comparator.comparingInt(link -> link.format().displayOrder());

    private final PageChromeFactory pageChromeFactory;
    private final CatalogUrlFactory urlFactory;

    public DetailPageVmFactory(PageChromeFactory pageChromeFactory, CatalogUrlFactory urlFactory) {
        this.pageChromeFactory = pageChromeFactory;
        this.urlFactory = urlFactory;
    }

    public DatasetDetailPageVm dataset(DatasetEntry dataset) {
        return new DatasetDetailPageVm(
                pageChromeFactory.datasetDetailPage(dataset),
                dataset.identifier(),
                dataset.title(),
                dataset.description(),
                dataset.type().label(),
                accessState(dataset),
                dataset.metadata().hasStructureInformation(),
                formatDate(dataset.modified()),
                formatDate(dataset.metadata().issued()).orElse(""),
                datasetFeatures(dataset),
                new DownloadSectionVm(
                        "Downloads",
                        downloadLead(accessState(dataset), "diesem Datensatz", "diesen Datensatz"),
                        accessState(dataset),
                        downloads(dataset.title(), dataset.distributions())),
                new MetadataSectionVm("overview", "Übersicht", overviewItems(dataset)),
                metadataSections(dataset, dataset.distributions(), accessState(dataset)));
    }

    private List<DetailFeatureVm> datasetFeatures(DatasetEntry dataset) {
        CatalogEntryMetadata metadata = dataset.metadata();
        return List.of(
                new DetailFeatureVm("Open Data", dataset.isOpenData()),
                new DetailFeatureVm("Attribute beschrieben", !metadata.attributes().isEmpty()),
                new DetailFeatureVm("Daten validiert", metadata.model().isPresent()));
    }

    public SeriesDetailPageVm series(DatasetSeriesEntry series) {
        DatasetIssueEntry currentIssue = series.currentIssueOrThrow();
        AccessStateVm currentIssueAccessState = accessState(currentIssue);
        return new SeriesDetailPageVm(
                pageChromeFactory.seriesDetailPage(series),
                series.identifier(),
                series.title(),
                series.description(),
                series.type().label(),
                accessState(series),
                false,
                formatDate(series.modified()),
                formatDate(series.metadata().issued()).orElse(""),
                currentIssue.issueLabel(),
                urlFactory.currentIssueDetail(series.identifier()),
                new DownloadSectionVm(
                        "Downloads aktuelle Ausgabe",
                        downloadLead(currentIssueAccessState, "der aktuellen Ausgabe " + currentIssue.issueLabel(), "die aktuelle Ausgabe " + currentIssue.issueLabel()),
                        currentIssueAccessState,
                        downloads(currentIssue.title(), currentIssue.distributions())),
                seriesIssues(series),
                metadataSections(series, currentIssue.distributions(), currentIssueAccessState));
    }

    public IssueDetailPageVm issue(DatasetSeriesEntry series, DatasetIssueEntry issue) {
        AccessStateVm issueAccessState = accessState(issue);
        return new IssueDetailPageVm(
                pageChromeFactory.issueDetailPage(series, issue),
                series.title(),
                urlFactory.seriesDetail(series.identifier()),
                issue.identifier(),
                issue.title(),
                issue.issueLabel(),
                issue.identifier().equals(series.currentIssueOrThrow().identifier()),
                issue.description(),
                issueAccessState,
                false,
                formatDate(issue.modified()),
                formatDate(issue.metadata().issued()).orElse(""),
                new DownloadSectionVm(
                        "Downloads",
                        downloadLead(issueAccessState, "dieser Ausgabe", "diese Ausgabe"),
                        issueAccessState,
                        downloads(issue.title(), issue.distributions())),
                seriesIssues(series),
                metadataSections(issue, issue.distributions(), issueAccessState));
    }

    private SeriesIssuesVm seriesIssues(DatasetSeriesEntry series) {
        String currentIdentifier = series.currentIssueOrThrow().identifier();
        return new SeriesIssuesVm(
                "Ausgaben",
                series.issuesNewestFirst().stream()
                        .map(issue -> new SeriesIssueVm(
                                issue.issueLabel(),
                                issue.title(),
                                formatDate(issue.modified()),
                                issue.identifier().equals(currentIdentifier)
                                        ? urlFactory.currentIssueDetail(series.identifier())
                                        : urlFactory.issueDetail(series.identifier(), issue.identifier()),
                                issue.identifier().equals(currentIdentifier),
                                accessState(issue),
                                downloads(issue.title(), issue.primaryDistributions())))
                        .toList());
    }

    private List<DownloadLinkVm> downloads(String ownerTitle, List<DistributionLink> distributions) {
        return distributions.stream()
                .sorted(DISTRIBUTION_ORDER)
                .map(link -> new DownloadLinkVm(
                        link.displayLabel(),
                        link.preferredHref().toString(),
                        link.displayLabel() + " herunterladen: " + ownerTitle))
                .toList();
    }

    private List<MetadataSectionVm> metadataSections(
            CatalogEntry entry,
            List<DistributionLink> distributions,
            AccessStateVm downloadAccessState) {
        List<MetadataSectionVm> sections = new ArrayList<>();
        addSection(sections, "overview", "Übersicht", overviewItems(entry));
        addSection(sections, "topics", "Themen und Schlagworte", topicItems(entry));
        addSection(sections, "responsibility", "Verantwortlichkeit", responsibilityItems(entry));
        addSection(sections, "usage", "Nutzung und Lizenz", usageItems(entry));
        addSection(sections, "time", "Zeit und Raum", timeItems(entry));
        addSection(sections, "resources", "Ressourcen und Formate", resourceItems(distributions, downloadAccessState));
        return List.copyOf(sections);
    }

    private static void addSection(
            List<MetadataSectionVm> sections,
            String id,
            String title,
            List<MetadataItemVm> items) {
        if (!items.isEmpty()) {
            sections.add(new MetadataSectionVm(id, title, items));
        }
    }

    private List<MetadataItemVm> overviewItems(CatalogEntry entry) {
        List<MetadataItemVm> items = new ArrayList<>();
        items.add(item("Identifier", entry.identifier()));
        items.add(item("Typ", entry.type().label()));
        items.add(item("Zugriff", entry.accessLevel().displayLabel()));
        entry.metadata().origin()
                .map(DetailPageVmFactory::originLabel)
                .ifPresent(value -> items.add(item("Herkunft", value)));
        entry.metadata().publicationStatus()
                .map(DetailPageVmFactory::publicationStatusLabel)
                .ifPresent(value -> items.add(item("Publikationsstatus", value)));
        formatDate(entry.metadata().issued()).ifPresent(value -> items.add(item("Publiziert", value)));
        items.add(item("Aktualisiert", formatDate(entry.modified())));
        entry.metadata().accrualPeriodicity()
                .map(DetailPageVmFactory::frequencyLabel)
                .ifPresent(value -> items.add(item("Aktualisierungsintervall", value)));
        entry.metadata().licenseUri()
                .ifPresent(uri -> items.add(item("Lizenz", uri.toString(), uri.toString())));
        return items;
    }

    private List<MetadataItemVm> topicItems(CatalogEntry entry) {
        List<MetadataItemVm> items = new ArrayList<>();
        join(entry.themes().stream().map(Theme::displayName).toList())
                .ifPresent(value -> items.add(item("Thema", value)));
        join(entry.keywords()).ifPresent(value -> items.add(item("Keywords", value)));
        return items;
    }

    private List<MetadataItemVm> responsibilityItems(CatalogEntry entry) {
        List<MetadataItemVm> items = new ArrayList<>();
        items.add(item("Fachstelle / Amt", officeLabel(entry.creator())));
        items.add(item("Herausgeber", officeLabel(entry.publisher())));
        entry.metadata().contactPoint().ifPresent(contact -> addContactItems(items, contact));
        return items;
    }

    private List<MetadataItemVm> usageItems(CatalogEntry entry) {
        List<MetadataItemVm> items = new ArrayList<>();
        entry.metadata().licenseUri()
                .ifPresent(uri -> items.add(item("Lizenz", uri.toString(), uri.toString())));
        entry.metadata().landingPage()
                .ifPresent(uri -> items.add(item("Landing Page", uri.toString(), uri.toString())));
        return items;
    }

    private List<MetadataItemVm> timeItems(CatalogEntry entry) {
        List<MetadataItemVm> items = new ArrayList<>();
        entry.metadata().accrualPeriodicity()
                .map(DetailPageVmFactory::frequencyLabel)
                .ifPresent(value -> items.add(item("Aktualisierungsintervall", value)));
        entry.metadata().temporalCoverage()
                .map(DetailPageVmFactory::temporalCoverageLabel)
                .filter(value -> !value.isBlank())
                .ifPresent(value -> items.add(item("Zeitlicher Bezug", value)));
        return items;
    }

    private List<MetadataItemVm> resourceItems(List<DistributionLink> distributions, AccessStateVm downloadAccessState) {
        List<MetadataItemVm> items = new ArrayList<>();
        join(distributions.stream()
                        .sorted(DISTRIBUTION_ORDER)
                        .map(link -> link.format().label())
                        .distinct()
                        .toList())
                .ifPresent(value -> items.add(item("Formate", value)));
        if (downloadAccessState.openData()) {
            distributions.stream()
                    .sorted(DISTRIBUTION_ORDER)
                    .forEach(link -> items.add(item(
                            link.displayLabel(),
                            link.preferredHref().toString(),
                            link.preferredHref().toString())));
        }
        return items;
    }

    private static AccessStateVm accessState(CatalogEntry entry) {
        return new AccessStateVm(entry.isOpenData(), entry.accessLevel().displayLabel());
    }

    private static String downloadLead(AccessStateVm accessState, String dativeLabel, String accusativeLabel) {
        if (accessState.openData()) {
            return "Dateien zu " + dativeLabel + " in den verfügbaren Formaten.";
        }
        return "Für " + accusativeLabel + " sind keine Open-Data-Downloads verfügbar.";
    }

    private static void addContactItems(List<MetadataItemVm> items, ContactPoint contact) {
        items.add(item("Kontakt", contact.name()));
        contact.organizationUnit().ifPresent(value -> items.add(item("Organisation", value)));
        contact.email().ifPresent(uri -> items.add(item("E-Mail", mailDisplay(uri), uri.toString())));
        contact.phone().ifPresent(value -> items.add(item("Telefon", value)));
        contact.url().ifPresent(uri -> items.add(item("Kontakt-Webseite", uri.toString(), uri.toString())));
    }

    private static String officeLabel(Office office) {
        return office.abbreviation()
                .map(abbreviation -> office.displayName() + " (" + abbreviation + ")")
                .orElse(office.displayName());
    }

    private static Optional<String> join(List<String> values) {
        var cleaned = values.stream()
                .filter(value -> value != null && !value.isBlank())
                .distinct()
                .toList();
        if (cleaned.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(String.join(", ", cleaned));
    }

    private static MetadataItemVm item(String label, String value) {
        return item(label, value, null);
    }

    private static MetadataItemVm item(String label, String value, String href) {
        return new MetadataItemVm(label, value, Optional.ofNullable(href).filter(link -> !link.isBlank()));
    }

    private static Optional<String> formatDate(Optional<LocalDate> date) {
        return date.map(DetailPageVmFactory::formatDate);
    }

    private static String formatDate(LocalDate date) {
        return DATE_FORMATTER.format(date);
    }

    private static String frequencyLabel(String value) {
        return switch (value.trim().toLowerCase()) {
            case "annually", "annual", "yearly" -> "jährlich";
            case "monthly" -> "monatlich";
            case "quarterly" -> "quartalsweise";
            case "weekly" -> "wöchentlich";
            case "daily" -> "täglich";
            case "continual", "continuous" -> "kontinuierlich";
            case "biweekly" -> "zweiwöchentlich";
            case "biannually" -> "halbjährlich";
            case "asneeded", "as_needed" -> "bei Bedarf";
            case "irregular" -> "unregelmässig";
            case "notplanned", "not_planned" -> "nicht geplant";
            case "unknown" -> "unbekannt";
            default -> value;
        };
    }

    private static String originLabel(String value) {
        return switch (value.trim().toLowerCase()) {
            case "federal" -> "Bund";
            case "cantonal" -> "Kanton";
            case "municipal" -> "Gemeinde";
            case "other" -> "Weitere";
            default -> value;
        };
    }

    private static String publicationStatusLabel(String value) {
        return switch (value.trim().toLowerCase()) {
            case "draft" -> "Entwurf";
            case "in_review", "inreview" -> "in Prüfung";
            case "published" -> "veröffentlicht";
            case "archived" -> "archiviert";
            default -> value;
        };
    }

    private static String temporalCoverageLabel(TemporalCoverage coverage) {
        if (coverage.referenceDate().isPresent()) {
            return "Stichtag " + formatDate(coverage.referenceDate().get());
        }
        if (coverage.startDate().isPresent() && coverage.endDate().isPresent()) {
            return formatDate(coverage.startDate().get()) + " bis " + formatDate(coverage.endDate().get());
        }
        if (coverage.startDate().isPresent()) {
            return "ab " + formatDate(coverage.startDate().get());
        }
        if (coverage.endDate().isPresent()) {
            return "bis " + formatDate(coverage.endDate().get());
        }
        return "";
    }

    private static String mailDisplay(URI uri) {
        String text = uri.toString();
        if (text.startsWith("mailto:")) {
            return text.substring("mailto:".length());
        }
        return text;
    }
}
