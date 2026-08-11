package ch.so.agi.datenportal.web;

import ch.so.agi.datenportal.catalog.domain.CatalogEntry;
import ch.so.agi.datenportal.catalog.domain.CatalogEntryMetadata;
import ch.so.agi.datenportal.catalog.domain.ContactPoint;
import ch.so.agi.datenportal.catalog.domain.DatasetAttribute;
import ch.so.agi.datenportal.catalog.domain.DatasetEntry;
import ch.so.agi.datenportal.catalog.domain.DatasetIssueEntry;
import ch.so.agi.datenportal.catalog.domain.DatasetSeriesEntry;
import ch.so.agi.datenportal.catalog.domain.DistributionFormat;
import ch.so.agi.datenportal.catalog.domain.DistributionLink;
import ch.so.agi.datenportal.catalog.domain.Office;
import ch.so.agi.datenportal.catalog.domain.TemporalCoverage;
import ch.so.agi.datenportal.catalog.domain.Theme;
import ch.so.agi.datenportal.web.view.AccessStateVm;
import ch.so.agi.datenportal.web.view.AttributeRowVm;
import ch.so.agi.datenportal.web.view.CodeExampleVm;
import ch.so.agi.datenportal.web.view.DetailFeatureVm;
import ch.so.agi.datenportal.web.view.DirectAccessRowVm;
import ch.so.agi.datenportal.web.view.DownloadLinkVm;
import ch.so.agi.datenportal.web.view.EntryDetailPageVm;
import ch.so.agi.datenportal.web.view.KpiVm;
import ch.so.agi.datenportal.web.view.MetadataLineVm;
import ch.so.agi.datenportal.web.view.MetadataItemVm;
import ch.so.agi.datenportal.web.view.MetadataSectionVm;
import ch.so.agi.datenportal.web.view.QualityVm;
import ch.so.agi.datenportal.web.view.RelatedIssueVm;
import ch.so.agi.datenportal.web.view.SeriesDetailPageVm;
import ch.so.agi.datenportal.web.view.SeriesIssueVm;
import ch.so.agi.datenportal.web.view.StructureQualityOriginPageVm;
import ch.so.agi.datenportal.web.view.UsagePageVm;
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

    public EntryDetailPageVm dataset(DatasetEntry dataset) {
        AccessStateVm datasetAccessState = accessState(dataset);
        return new EntryDetailPageVm(
                pageChromeFactory.datasetDetailPage(dataset),
                Optional.empty(),
                Optional.empty(),
                dataset.title(),
                dataset.description(),
                datasetAccessState,
                urlFactory.datasetStructureQualityOrigin(dataset.identifier()),
                urlFactory.datasetExplore(dataset.identifier()),
                urlFactory.datasetUsage(dataset.identifier()),
                false,
                detailFeatures(dataset),
                downloads(dataset.title(), dataset.distributions()),
                new MetadataSectionVm("overview", "Übersicht", overviewItems(dataset)),
                new MetadataSectionVm("temporal-coverage", "Zeitliche Abdeckung", temporalCoverageItems(dataset)),
                new MetadataSectionVm("topics", "Themen und Schlagworte", datasetTopicItems(dataset)),
                responsibilitiesContactSection(dataset),
                List.of());
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
                series.title(),
                series.description(),
                seriesIssues(series));
    }

    public EntryDetailPageVm issue(DatasetSeriesEntry series, DatasetIssueEntry issue) {
        String currentIdentifier = series.currentIssueOrThrow().identifier();
        boolean currentIssue = issue.identifier().equals(currentIdentifier);
        AccessStateVm issueAccessState = accessState(issue);
        return new EntryDetailPageVm(
                pageChromeFactory.issueDetailPage(series, issue),
                Optional.of(series.title()),
                Optional.of(urlFactory.seriesDetail(series.identifier())),
                issue.title(),
                issue.description(),
                issueAccessState,
                currentIssue
                        ? urlFactory.currentIssueStructureQualityOrigin(series.identifier())
                        : urlFactory.issueStructureQualityOrigin(series.identifier(), issue.identifier()),
                currentIssue
                        ? urlFactory.currentIssueExplore(series.identifier())
                        : urlFactory.issueExplore(series.identifier(), issue.identifier()),
                currentIssue
                        ? urlFactory.currentIssueUsage(series.identifier())
                        : urlFactory.issueUsage(series.identifier(), issue.identifier()),
                currentIssue,
                detailFeatures(issue),
                downloads(issue.title(), issue.distributions()),
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

    public UsagePageVm datasetUsage(DatasetEntry dataset) {
        return usage(pageChromeFactory.datasetUsagePage(dataset), dataset, dataset.distributions());
    }

    public UsagePageVm issueUsage(DatasetSeriesEntry series, DatasetIssueEntry issue) {
        return usage(pageChromeFactory.issueUsagePage(series, issue), issue, issue.distributions());
    }

    private UsagePageVm usage(
            ch.so.agi.datenportal.web.view.PageChromeVm chrome,
            CatalogEntry entry,
            List<DistributionLink> distributions) {
        List<DistributionLink> visibleDistributions = entry.isOpenData()
                ? distributions.stream().sorted(DISTRIBUTION_ORDER).toList()
                : List.of();
        return new UsagePageVm(
                chrome,
                "Daten verwenden",
                directAccessRows(entry.title(), visibleDistributions),
                codeExamples(entry.identifier(), visibleDistributions));
    }

    private List<SeriesIssueVm> seriesIssues(DatasetSeriesEntry series) {
        String currentIdentifier = series.currentIssueOrThrow().identifier();
        return series.issues().stream()
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
                        .toList();
    }

    private List<RelatedIssueVm> relatedIssues(DatasetSeriesEntry series, String displayedIdentifier, String currentIdentifier) {
        return series.issues().stream()
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
                .toList();
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
        Optional<String> reportHref = metadata.qualitySummary().map(summary -> summary.reportUrl().toString());
        Optional<String> reportName = metadata.qualitySummary().map(summary -> reportFileName(summary.reportUrl()));
        Optional<String> missingModelMessage = metadata.model().isPresent()
                ? Optional.empty()
                : Optional.of("Für dieses Datenthema ist kein Datenmodell hinterlegt. Ohne Datenmodell kann die Struktur nicht automatisiert geprüft oder validiert werden.");
        return new QualityVm(metadata.model(), reportName, reportHref, missingModelMessage);
    }

    private static String reportFileName(URI reportUri) {
        String path = reportUri.getPath();
        if (path != null && !path.isBlank()) {
            int lastSlash = path.lastIndexOf('/');
            String fileName = path.substring(lastSlash + 1);
            if (!fileName.isBlank()) {
                return fileName;
            }
        }
        return "Validierungsreport";
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

    private static List<DirectAccessRowVm> directAccessRows(String ownerTitle, List<DistributionLink> distributions) {
        return distributions.stream()
                .map(link -> new DirectAccessRowVm(
                        link.displayLabel(),
                        directAccessDescription(link.format()),
                        link.preferredHref().toString(),
                        link.displayLabel() + " herunterladen: " + ownerTitle))
                .toList();
    }

    private static String directAccessDescription(DistributionFormat format) {
        return switch (format) {
            case CSV -> "Trennzeichen: Semikolon (;) · Texttrenner: Kein Texttrenner · Encoding: UTF-8";
            case XLSX -> "Excel-Arbeitsmappe";
            case PARQUET -> "Analytisches Spaltenformat";
            case OTHER -> "Weitere Ressource";
        };
    }

    private static List<CodeExampleVm> codeExamples(String identifier, List<DistributionLink> distributions) {
        Optional<String> csvUrl = preferredHref(distributions, DistributionFormat.CSV);
        Optional<String> parquetUrl = preferredHref(distributions, DistributionFormat.PARQUET);
        String downloadUrl = csvUrl.or(() -> parquetUrl).orElse("");
        if (downloadUrl.isBlank()) {
            return List.of();
        }
        String duckDbUrl = parquetUrl.or(() -> csvUrl).orElse(downloadUrl);

        return List.of(
                new CodeExampleVm(
                        "curl",
                        "cURL",
                        """
                        curl -L -o "%s.csv" \\
                          "%s"
                        """.formatted(identifier, downloadUrl).stripTrailing(),
                        true),
                new CodeExampleVm(
                        "python",
                        "Python",
                        """
                        import pandas as pd

                        url = "%s"
                        df = pd.read_csv(url)
                        print(df.head())
                        """.formatted(csvUrl.orElse(downloadUrl)).stripTrailing(),
                        false),
                new CodeExampleVm(
                        "duckdb",
                        "DuckDB",
                        duckDbCode(duckDbUrl, parquetUrl.isPresent()),
                        false));
    }

    private static Optional<String> preferredHref(List<DistributionLink> distributions, DistributionFormat format) {
        return distributions.stream()
                .filter(link -> link.format() == format)
                .map(link -> link.preferredHref().toString())
                .findFirst();
    }

    private static String duckDbCode(String url, boolean parquet) {
        if (parquet) {
            return """
                    SELECT *
                    FROM read_parquet('%s')
                    LIMIT 10;
                    """.formatted(url).stripTrailing();
        }
        return """
                SELECT *
                FROM read_csv_auto('%s')
                LIMIT 10;
                """.formatted(url).stripTrailing();
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

    private List<MetadataItemVm> datasetTopicItems(CatalogEntry entry) {
        List<MetadataItemVm> items = new ArrayList<>();
        join(entry.themes().stream().map(Theme::displayName).toList())
                .ifPresent(value -> items.add(item("Thema", value)));
        join(entry.keywords()).ifPresent(value -> items.add(item("Schlagworte", value)));
        return items;
    }

    private List<MetadataItemVm> temporalCoverageItems(CatalogEntry entry) {
        return entry.metadata().temporalCoverage()
                .flatMap(DetailPageVmFactory::temporalCoverageItem)
                .map(List::of)
                .orElseGet(List::of);
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

    private MetadataSectionVm responsibilitiesContactSection(CatalogEntry entry) {
        return new MetadataSectionVm(
                "responsibilities-contact",
                "Zuständigkeiten und Kontakt",
                List.of(
                        new MetadataItemVm("Datenproduzent", producerLines(entry.creator())),
                        new MetadataItemVm("Kontakt", contactLines(entry.metadata().contactPoint())),
                        new MetadataItemVm("Herausgeber", publisherLines(entry.publisher()))));
    }

    private List<MetadataLineVm> producerLines(Office office) {
        List<MetadataLineVm> lines = new ArrayList<>();
        lines.add(contactLine(office.displayName()));
        office.officeAtWeb().ifPresent(uri -> lines.add(contactLine(uri.toString(), uri.toString())));
        return List.copyOf(lines);
    }

    private List<MetadataLineVm> publisherLines(Office office) {
        List<MetadataLineVm> lines = new ArrayList<>();
        lines.add(contactLine(office.displayName()));
        office.officeAtWeb().ifPresent(uri -> lines.add(contactLine(uri.toString(), uri.toString())));
        office.email().ifPresent(uri -> lines.add(contactLine(mailDisplay(uri), uri.toString())));
        return List.copyOf(lines);
    }

    private List<MetadataLineVm> contactLines(Optional<ContactPoint> contactPoint) {
        List<MetadataLineVm> lines = new ArrayList<>();
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
        return new MetadataItemVm(label, List.of(metadataLine(value, href)));
    }

    private static MetadataLineVm metadataLine(String value, String href) {
        return new MetadataLineVm(value, Optional.ofNullable(href).filter(link -> !link.isBlank()));
    }

    private static MetadataLineVm contactLine(String value) {
        return contactLine(value, null);
    }

    private static MetadataLineVm contactLine(String value, String href) {
        return metadataLine(value, href);
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
