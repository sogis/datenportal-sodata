package ch.so.agi.datenportal.web;

import ch.so.agi.datenportal.catalog.domain.CatalogEntry;
import ch.so.agi.datenportal.catalog.domain.CatalogEntryMetadata;
import ch.so.agi.datenportal.catalog.domain.ContactPoint;
import ch.so.agi.datenportal.catalog.domain.DatasetAttribute;
import ch.so.agi.datenportal.catalog.domain.DatasetEntry;
import ch.so.agi.datenportal.catalog.domain.DatasetIssueEntry;
import ch.so.agi.datenportal.catalog.domain.DatasetSeriesEntry;
import ch.so.agi.datenportal.catalog.domain.DistributionLink;
import ch.so.agi.datenportal.catalog.domain.Office;
import ch.so.agi.datenportal.catalog.domain.TemporalCoverage;
import ch.so.agi.datenportal.catalog.domain.Theme;
import ch.so.agi.datenportal.web.view.AccessStateVm;
import ch.so.agi.datenportal.web.view.AttributeRowVm;
import ch.so.agi.datenportal.web.view.ContactMetadataItemVm;
import ch.so.agi.datenportal.web.view.ContactMetadataLineVm;
import ch.so.agi.datenportal.web.view.ContactMetadataSectionVm;
import ch.so.agi.datenportal.web.view.DatasetDetailPageVm;
import ch.so.agi.datenportal.web.view.DetailFeatureVm;
import ch.so.agi.datenportal.web.view.DownloadLinkVm;
import ch.so.agi.datenportal.web.view.DownloadSectionVm;
import ch.so.agi.datenportal.web.view.IssueDetailPageVm;
import ch.so.agi.datenportal.web.view.KpiVm;
import ch.so.agi.datenportal.web.view.MetadataItemVm;
import ch.so.agi.datenportal.web.view.MetadataSectionVm;
import ch.so.agi.datenportal.web.view.QualityVm;
import ch.so.agi.datenportal.web.view.RelatedIssueVm;
import ch.so.agi.datenportal.web.view.RelatedIssuesVm;
import ch.so.agi.datenportal.web.view.SeriesDetailPageVm;
import ch.so.agi.datenportal.web.view.SeriesIssueVm;
import ch.so.agi.datenportal.web.view.SeriesIssuesVm;
import ch.so.agi.datenportal.web.view.StructureQualityOriginPageVm;
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
                urlFactory.datasetStructureQualityOrigin(dataset.identifier()),
                formatDate(dataset.modified()),
                formatDate(dataset.metadata().issued()).orElse(""),
                detailFeatures(dataset),
                new DownloadSectionVm(
                        "Downloads",
                        downloadLead(accessState(dataset), "diesem Datensatz", "diesen Datensatz"),
                        accessState(dataset),
                        downloads(dataset.title(), dataset.distributions())),
                new MetadataSectionVm("overview", "Übersicht", overviewItems(dataset)),
                new MetadataSectionVm("temporal-coverage", "Zeitliche Abdeckung", temporalCoverageItems(dataset)),
                new MetadataSectionVm("topics", "Themen und Schlagworte", datasetTopicItems(dataset)),
                responsibilitiesContactSection(dataset),
                metadataSections(dataset, dataset.distributions(), accessState(dataset)));
    }

    private List<DetailFeatureVm> detailFeatures(CatalogEntry entry) {
        CatalogEntryMetadata metadata = entry.metadata();
        return List.of(
                new DetailFeatureVm("Open Data", entry.isOpenData()),
                new DetailFeatureVm("Attribute beschrieben", !metadata.attributes().isEmpty()),
                new DetailFeatureVm("Daten validiert", metadata.model().isPresent()));
    }

    public SeriesDetailPageVm series(DatasetSeriesEntry series) {
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
                seriesIssues(series));
    }

    public IssueDetailPageVm issue(DatasetSeriesEntry series, DatasetIssueEntry issue) {
        String currentIdentifier = series.currentIssueOrThrow().identifier();
        boolean currentIssue = issue.identifier().equals(currentIdentifier);
        AccessStateVm issueAccessState = accessState(issue);
        return new IssueDetailPageVm(
                pageChromeFactory.issueDetailPage(series, issue),
                series.title(),
                urlFactory.seriesDetail(series.identifier()),
                issue.identifier(),
                issue.title(),
                issue.description(),
                issueAccessState,
                issue.metadata().hasStructureInformation(),
                currentIssue
                        ? urlFactory.currentIssueStructureQualityOrigin(series.identifier())
                        : urlFactory.issueStructureQualityOrigin(series.identifier(), issue.identifier()),
                currentIssue,
                formatDate(issue.modified()),
                formatDate(issue.metadata().issued()).orElse(""),
                detailFeatures(issue),
                new DownloadSectionVm(
                        "Downloads",
                        downloadLead(issueAccessState, "dieser Ausgabe", "diese Ausgabe"),
                        issueAccessState,
                        downloads(issue.title(), issue.distributions())),
                new MetadataSectionVm("overview", "Übersicht", overviewItems(issue)),
                new MetadataSectionVm("temporal-coverage", "Zeitliche Abdeckung", temporalCoverageItems(issue)),
                new MetadataSectionVm("topics", "Themen und Schlagworte", datasetTopicItems(issue)),
                responsibilitiesContactSection(issue),
                relatedIssues(series, issue.identifier(), currentIdentifier));
    }

    public StructureQualityOriginPageVm datasetStructureQualityOrigin(DatasetEntry dataset) {
        return new StructureQualityOriginPageVm(
                pageChromeFactory.datasetStructureQualityOriginPage(dataset),
                "Struktur, Qualität und Herkunft",
                structureKpis(dataset.metadata()),
                attributeRows(dataset.metadata().attributes()),
                "Für dieses Datenthema sind keine Attribute beschrieben.",
                quality(dataset.metadata()),
                section("origin-usage", "Herkunft & Verwendung", originUsageItems(dataset)));
    }

    public StructureQualityOriginPageVm issueStructureQualityOrigin(DatasetSeriesEntry series, DatasetIssueEntry issue) {
        return new StructureQualityOriginPageVm(
                pageChromeFactory.issueStructureQualityOriginPage(series, issue),
                "Struktur, Qualität und Herkunft",
                structureKpis(issue.metadata()),
                attributeRows(issue.metadata().attributes()),
                "Für dieses Datenthema sind keine Attribute beschrieben.",
                quality(issue.metadata()),
                section("origin-usage", "Herkunft & Verwendung", originUsageItems(issue)));
    }

    private SeriesIssuesVm seriesIssues(DatasetSeriesEntry series) {
        String currentIdentifier = series.currentIssueOrThrow().identifier();
        return new SeriesIssuesVm(
                "Ausgaben",
                series.issues().stream()
                        .sorted(seriesIssueOrder(currentIdentifier))
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

    private RelatedIssuesVm relatedIssues(DatasetSeriesEntry series, String displayedIdentifier, String currentIdentifier) {
        return new RelatedIssuesVm(series.issues().stream()
                .filter(issue -> !issue.identifier().equals(displayedIdentifier))
                .sorted(seriesIssueOrder(currentIdentifier))
                .map(issue -> new RelatedIssueVm(
                        issue.issueLabel(),
                        issue.title(),
                        publicationDateLabel(issue),
                        issue.identifier().equals(currentIdentifier)
                                ? urlFactory.currentIssueDetail(series.identifier())
                                : urlFactory.issueDetail(series.identifier(), issue.identifier()),
                        issue.identifier().equals(currentIdentifier)))
                .toList());
    }

    private static String publicationDateLabel(CatalogEntry entry) {
        return formatDate(entry.metadata().issued()).orElse(formatDate(entry.modified()));
    }

    private static List<AttributeRowVm> attributeRows(List<DatasetAttribute> attributes) {
        return attributes.stream()
                .map(attribute -> new AttributeRowVm(
                        attribute.name(),
                        attribute.dataType(),
                        attribute.mandatory() ? "Ja" : "Nein",
                        attribute.unit().orElse("–"),
                        attribute.description().orElse("–")))
                .toList();
    }

    private static QualityVm quality(CatalogEntryMetadata metadata) {
        return metadata.model()
                .map(model -> new QualityVm(Optional.of(model), "#", "ilivalidator.log", "#", Optional.empty()))
                .orElseGet(() -> new QualityVm(
                        Optional.empty(),
                        "#",
                        "ilivalidator.log",
                        "#",
                        Optional.of("Für dieses Datenthema ist kein Datenmodell hinterlegt. Ohne Datenmodell kann die Struktur nicht automatisiert geprüft oder validiert werden.")));
    }

    private static List<KpiVm> structureKpis(CatalogEntryMetadata metadata) {
        String validationValue = metadata.qualitySummary()
                .map(summary -> "success".equalsIgnoreCase(summary.status()) ? "Erfolgreich" : "Nicht erfolgreich")
                .orElse("Nicht prüfbar");
        String validationDetail = metadata.qualitySummary()
                .map(summary -> summary.errors() + " Fehler")
                .orElse("Kein Datenmodell");
        String objectCount = metadata.structureSummary()
                .map(summary -> Integer.toString(summary.objectCount()))
                .orElse("–");
        String attributeCount = metadata.structureSummary()
                .map(summary -> Integer.toString(summary.attributeCount()))
                .orElse("–");
        return List.of(
                new KpiVm("Validierung", validationValue, Optional.of(validationDetail), "shield-check"),
                new KpiVm("Objekte", objectCount, Optional.empty(), "database"),
                new KpiVm("Attribute", attributeCount, Optional.empty(), "table"));
    }

    private static Comparator<DatasetIssueEntry> seriesIssueOrder(String currentIdentifier) {
        Comparator<DatasetIssueEntry> historicalOrder = Comparator
                .comparing(DatasetIssueEntry::title, String.CASE_INSENSITIVE_ORDER)
                .reversed()
                .thenComparing(DatasetIssueEntry::issueLabel, String.CASE_INSENSITIVE_ORDER.reversed())
                .thenComparing(DatasetIssueEntry::identifier);
        return Comparator
                .comparing((DatasetIssueEntry issue) -> !issue.identifier().equals(currentIdentifier))
                .thenComparing(historicalOrder);
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

    private List<MetadataItemVm> datasetTopicItems(CatalogEntry entry) {
        List<MetadataItemVm> items = new ArrayList<>();
        join(entry.themes().stream().map(Theme::displayName).toList())
                .ifPresent(value -> items.add(item("Thema", value)));
        join(entry.keywords()).ifPresent(value -> items.add(item("Schlagworte", value)));
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

    private List<MetadataItemVm> temporalCoverageItems(CatalogEntry entry) {
        return entry.metadata().temporalCoverage()
                .flatMap(DetailPageVmFactory::temporalCoverageItem)
                .map(List::of)
                .orElseGet(List::of);
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

    private List<MetadataItemVm> originUsageItems(CatalogEntry entry) {
        List<MetadataItemVm> items = new ArrayList<>();
        entry.metadata().surveyMethod()
                .ifPresent(value -> items.add(item("Erhebungs- / Messmethode", value)));
        entry.metadata().auxiliaryData()
                .ifPresent(value -> items.add(item("Hilfsdaten", value)));
        entry.metadata().furtherUses()
                .ifPresent(value -> items.add(item("Weitere Verwendungen", value)));
        entry.metadata().dataAvailableFrom()
                .ifPresent(value -> items.add(item("Verfügbare Daten ab", value)));
        return items;
    }

    private static Optional<MetadataSectionVm> section(String id, String title, List<MetadataItemVm> items) {
        if (items.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new MetadataSectionVm(id, title, items));
    }

    private ContactMetadataSectionVm responsibilitiesContactSection(CatalogEntry entry) {
        return new ContactMetadataSectionVm(
                "responsibilities-contact",
                "Zuständigkeiten und Kontakt",
                List.of(
                        new ContactMetadataItemVm("Datenproduzent", producerLines(entry.creator())),
                        new ContactMetadataItemVm("Kontakt", contactLines(entry.metadata().contactPoint())),
                        new ContactMetadataItemVm("Herausgeber", publisherLines(entry.publisher()))));
    }

    private List<ContactMetadataLineVm> producerLines(Office office) {
        List<ContactMetadataLineVm> lines = new ArrayList<>();
        lines.add(contactLine(office.displayName()));
        office.officeAtWeb().ifPresent(uri -> lines.add(contactLine(uri.toString(), uri.toString())));
        return List.copyOf(lines);
    }

    private List<ContactMetadataLineVm> publisherLines(Office office) {
        List<ContactMetadataLineVm> lines = new ArrayList<>();
        lines.add(contactLine(office.displayName()));
        office.officeAtWeb().ifPresent(uri -> lines.add(contactLine(uri.toString(), uri.toString())));
        office.email().ifPresent(uri -> lines.add(contactLine(mailDisplay(uri), uri.toString())));
        return List.copyOf(lines);
    }

    private List<ContactMetadataLineVm> contactLines(Optional<ContactPoint> contactPoint) {
        List<ContactMetadataLineVm> lines = new ArrayList<>();
        contactPoint.ifPresent(contact -> {
            lines.add(contactLine(contact.name()));
            contact.organizationUnit()
                    .filter(value -> !value.equals(contact.name()))
                    .ifPresent(value -> lines.add(contactLine(value)));
            contact.email().ifPresent(uri -> lines.add(contactLine(mailDisplay(uri), uri.toString())));
        });
        return List.copyOf(lines);
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

    private static ContactMetadataLineVm contactLine(String value) {
        return contactLine(value, null);
    }

    private static ContactMetadataLineVm contactLine(String value, String href) {
        return new ContactMetadataLineVm(value, Optional.ofNullable(href).filter(link -> !link.isBlank()));
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

    private static Optional<MetadataItemVm> temporalCoverageItem(TemporalCoverage coverage) {
        if (coverage.referenceDate().isPresent()) {
            return Optional.of(item("Stichtag", formatDate(coverage.referenceDate().get())));
        }
        if (coverage.startDate().isPresent() && coverage.endDate().isPresent()) {
            return Optional.of(item(
                    "Zeitraum",
                    formatDate(coverage.startDate().get()) + " bis " + formatDate(coverage.endDate().get())));
        }
        if (coverage.startDate().isPresent()) {
            return Optional.of(item("Zeitraum", "ab " + formatDate(coverage.startDate().get())));
        }
        if (coverage.endDate().isPresent()) {
            return Optional.of(item("Zeitraum", "bis " + formatDate(coverage.endDate().get())));
        }
        return Optional.empty();
    }

    private static String mailDisplay(URI uri) {
        String text = uri.toString();
        if (text.startsWith("mailto:")) {
            return text.substring("mailto:".length());
        }
        return text;
    }
}
