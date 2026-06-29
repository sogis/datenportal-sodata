package ch.so.agi.datenportal.catalog.importxtf;

import ch.so.agi.datenportal.catalog.domain.AccessLevel;
import ch.so.agi.datenportal.catalog.domain.Catalog;
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
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public final class XtfPublishedCatalogParser implements PublishedCatalogParser {

    private static final String MODEL_NAME = "SO_AGI_DataCatalog_PublishedCatalog_20260602";

    @Override
    public Catalog parse(InputStream inputStream, String sourceDescription) {
        Objects.requireNonNull(inputStream, "inputStream must not be null");
        Objects.requireNonNull(sourceDescription, "sourceDescription must not be null");

        try {
            XMLStreamReader reader = newReader(inputStream);
            try {
                return parseTransfer(reader);
            } finally {
                reader.close();
            }
        } catch (CatalogValidationException ex) {
            throw ex;
        } catch (XMLStreamException ex) {
            throw new XtfParseException(sourceDescription, XtfElementPath.root(), "XML stream is invalid or incomplete", ex);
        }
    }

    private XMLStreamReader newReader(InputStream inputStream) throws XMLStreamException {
        XMLInputFactory factory = XmlSecurity.secureXmlInputFactory();
        return factory.createXMLStreamReader(inputStream);
    }

    private Catalog parseTransfer(XMLStreamReader reader) throws XMLStreamException {
        List<String> models = new ArrayList<>();
        List<RawCatalog> catalogs = new ArrayList<>();

        while (reader.hasNext()) {
            int event = reader.next();
            if (event != XMLStreamConstants.START_ELEMENT) {
                continue;
            }

            String localName = reader.getLocalName();
            if ("model".equals(localName)) {
                models.add(readRequiredText(reader, XtfElementPath.root().push("headersection").push("models").push("model")));
                continue;
            }

            if ("Catalog".equals(localName)) {
                catalogs.add(parseCatalog(reader, XtfElementPath.root().push("Publication").push("Catalog")));
            }
        }

        if (!models.contains(MODEL_NAME)) {
            throw validationError(XtfElementPath.root().push("headersection").push("models"),
                    "Required INTERLIS model '" + MODEL_NAME + "' is missing.");
        }
        if (catalogs.size() != 1) {
            throw validationError(XtfElementPath.root().push("Publication"),
                    "Expected exactly one Catalog element, but found " + catalogs.size() + ".");
        }

        return catalogs.getFirst().toDomain();
    }

    private RawCatalog parseCatalog(XMLStreamReader reader, XtfElementPath path) throws XMLStreamException {
        RawCatalog catalog = new RawCatalog(path);

        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                String localName = reader.getLocalName();
                XtfElementPath childPath = path.push(localName);
                switch (localName) {
                    case "catalogUri" -> catalog.catalogUri = parseOptionalUri(reader, childPath).orElse(null);
                    case "title" -> catalog.title = readOptionalText(reader, childPath).orElse(null);
                    case "description" -> catalog.description = readOptionalText(reader, childPath).orElse(null);
                    case "publisher" -> catalog.publisher = parseOfficeContainer(reader, childPath);
                    case "homepage" -> catalog.homepage = parseOptionalUri(reader, childPath).orElse(null);
                    case "modified" -> catalog.modified = parseOptionalDate(reader, childPath).orElse(null);
                    case "agents" -> {
                        RawOffice office = parseOfficeContainer(reader, childPath);
                        if (office != null) {
                            catalog.agents.add(office);
                        }
                    }
                    case "datasets" -> catalog.datasets.addAll(parseDatasetsContainer(reader, childPath));
                    case "datasetSeries" -> catalog.datasetSeries.addAll(parseSeriesContainer(reader, childPath));
                    default -> skipElement(reader);
                }
                continue;
            }

            if (event == XMLStreamConstants.END_ELEMENT && "Catalog".equals(reader.getLocalName())) {
                return catalog;
            }
        }

        throw new XMLStreamException("Catalog element is not closed");
    }

    private List<RawDataset> parseDatasetsContainer(XMLStreamReader reader, XtfElementPath path) throws XMLStreamException {
        List<RawDataset> datasets = new ArrayList<>();

        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                if ("Dataset".equals(reader.getLocalName())) {
                    datasets.add(parseDataset(reader, path.push("Dataset")));
                } else {
                    skipElement(reader);
                }
                continue;
            }

            if (event == XMLStreamConstants.END_ELEMENT && "datasets".equals(reader.getLocalName())) {
                return datasets;
            }
        }

        throw new XMLStreamException("datasets element is not closed");
    }

    private RawDataset parseDataset(XMLStreamReader reader, XtfElementPath path) throws XMLStreamException {
        RawDataset dataset = new RawDataset(path);
        parseCommonEntry(reader, dataset, "Dataset");
        return dataset;
    }

    private List<RawSeries> parseSeriesContainer(XMLStreamReader reader, XtfElementPath path) throws XMLStreamException {
        List<RawSeries> series = new ArrayList<>();

        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                if ("DatasetSeries".equals(reader.getLocalName())) {
                    series.add(parseSeries(reader, path.push("DatasetSeries")));
                } else {
                    skipElement(reader);
                }
                continue;
            }

            if (event == XMLStreamConstants.END_ELEMENT && "datasetSeries".equals(reader.getLocalName())) {
                return series;
            }
        }

        throw new XMLStreamException("datasetSeries element is not closed");
    }

    private RawSeries parseSeries(XMLStreamReader reader, XtfElementPath path) throws XMLStreamException {
        RawSeries series = new RawSeries(path);

        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                String localName = reader.getLocalName();
                XtfElementPath childPath = path.push(localName);
                switch (localName) {
                    case "resourceUri" -> series.resourceUri = parseOptionalUri(reader, childPath).orElse(null);
                    case "identifier" -> series.identifier = readRequiredText(reader, childPath);
                    case "title" -> series.title = readRequiredText(reader, childPath);
                    case "description" -> series.description = readRequiredText(reader, childPath);
                    case "publisher" -> series.publisher = parseOfficeContainer(reader, childPath);
                    case "creator" -> series.creator = parseOfficeContainer(reader, childPath);
                    case "contactPoint" -> series.contactPoint = parseContactPointContainer(reader, childPath);
                    case "themes" -> {
                        RawTheme theme = parseThemeContainer(reader, childPath);
                        if (theme != null) {
                            series.themes.add(theme);
                        }
                    }
                    case "keywords" -> readOptionalText(reader, childPath).ifPresent(series.keywords::add);
                    case "landingPage" -> series.landingPage = parseOptionalUri(reader, childPath).orElse(null);
                    case "issued" -> series.issued = parseOptionalDate(reader, childPath).orElse(null);
                    case "modified" -> series.modified = parseRequiredDate(reader, childPath);
                    case "licenseUri" -> series.licenseUri = parseOptionalUri(reader, childPath).orElse(null);
                    case "accessRights" -> series.accessLevel = parseAccessRightsContainer(reader, childPath);
                    case "publicationStatus" -> series.publicationStatus = readRequiredText(reader, childPath);
                    case "origin" -> series.origin = readOptionalText(reader, childPath).orElse(null);
                    case "accrualPeriodicity" -> series.accrualPeriodicity = parseAccrualPeriodicityContainer(reader, childPath).orElse(null);
                    case "temporalCoverage" -> series.temporalCoverage = parseTemporalCoverageContainer(reader, childPath).orElse(null);
                    case "attributes" -> {
                        RawDatasetAttribute attribute = parseDatasetAttributeContainer(reader, childPath);
                        if (attribute != null) {
                            series.attributes.add(attribute);
                        }
                    }
                    case "model" -> series.model = readOptionalText(reader, childPath).orElse(null);
                    case "surveyMethod" -> series.surveyMethod = readOptionalText(reader, childPath).orElse(null);
                    case "dataAvailableFrom" -> series.dataAvailableFrom = readOptionalText(reader, childPath).orElse(null);
                    case "furtherUses" -> series.furtherUses = readOptionalText(reader, childPath).orElse(null);
                    case "auxiliaryData" -> series.auxiliaryData = readOptionalText(reader, childPath).orElse(null);
                    case "issues" -> series.issues.addAll(parseIssuesContainer(reader, childPath));
                    default -> skipElement(reader);
                }
                continue;
            }

            if (event == XMLStreamConstants.END_ELEMENT && "DatasetSeries".equals(reader.getLocalName())) {
                return series;
            }
        }

        throw new XMLStreamException("DatasetSeries element is not closed");
    }

    private void parseCommonEntry(XMLStreamReader reader, RawDataset dataset, String closingElement) throws XMLStreamException {
        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                String localName = reader.getLocalName();
                XtfElementPath childPath = dataset.path.push(localName);
                switch (localName) {
                    case "resourceUri" -> dataset.resourceUri = parseOptionalUri(reader, childPath).orElse(null);
                    case "identifier" -> dataset.identifier = readRequiredText(reader, childPath);
                    case "title" -> dataset.title = readRequiredText(reader, childPath);
                    case "description" -> dataset.description = readRequiredText(reader, childPath);
                    case "publisher" -> dataset.publisher = parseOfficeContainer(reader, childPath);
                    case "creator" -> dataset.creator = parseOfficeContainer(reader, childPath);
                    case "contactPoint" -> dataset.contactPoint = parseContactPointContainer(reader, childPath);
                    case "themes" -> {
                        RawTheme theme = parseThemeContainer(reader, childPath);
                        if (theme != null) {
                            dataset.themes.add(theme);
                        }
                    }
                    case "keywords" -> readOptionalText(reader, childPath).ifPresent(dataset.keywords::add);
                    case "landingPage" -> dataset.landingPage = parseOptionalUri(reader, childPath).orElse(null);
                    case "issued" -> dataset.issued = parseOptionalDate(reader, childPath).orElse(null);
                    case "modified" -> dataset.modified = parseRequiredDate(reader, childPath);
                    case "licenseUri" -> dataset.licenseUri = parseOptionalUri(reader, childPath).orElse(null);
                    case "accessRights" -> dataset.accessLevel = parseAccessRightsContainer(reader, childPath);
                    case "publicationStatus" -> dataset.publicationStatus = readRequiredText(reader, childPath);
                    case "origin" -> dataset.origin = readOptionalText(reader, childPath).orElse(null);
                    case "accrualPeriodicity" -> dataset.accrualPeriodicity = parseAccrualPeriodicityContainer(reader, childPath).orElse(null);
                    case "temporalCoverage" -> dataset.temporalCoverage = parseTemporalCoverageContainer(reader, childPath).orElse(null);
                    case "attributes" -> {
                        RawDatasetAttribute attribute = parseDatasetAttributeContainer(reader, childPath);
                        if (attribute != null) {
                            dataset.attributes.add(attribute);
                        }
                    }
                    case "model" -> dataset.model = readOptionalText(reader, childPath).orElse(null);
                    case "surveyMethod" -> dataset.surveyMethod = readOptionalText(reader, childPath).orElse(null);
                    case "dataAvailableFrom" -> dataset.dataAvailableFrom = readOptionalText(reader, childPath).orElse(null);
                    case "furtherUses" -> dataset.furtherUses = readOptionalText(reader, childPath).orElse(null);
                    case "auxiliaryData" -> dataset.auxiliaryData = readOptionalText(reader, childPath).orElse(null);
                    case "distributions" -> {
                        RawDistribution distribution = parseDistributionContainer(reader, childPath);
                        if (distribution != null) {
                            dataset.distributions.add(distribution);
                        }
                    }
                    default -> skipElement(reader);
                }
                continue;
            }

            if (event == XMLStreamConstants.END_ELEMENT && closingElement.equals(reader.getLocalName())) {
                return;
            }
        }

        throw new XMLStreamException(closingElement + " element is not closed");
    }

    private List<RawIssue> parseIssuesContainer(XMLStreamReader reader, XtfElementPath path) throws XMLStreamException {
        List<RawIssue> issues = new ArrayList<>();

        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                if ("DatasetIssue".equals(reader.getLocalName())) {
                    issues.add(parseIssue(reader, path.push("DatasetIssue")));
                } else {
                    skipElement(reader);
                }
                continue;
            }

            if (event == XMLStreamConstants.END_ELEMENT && "issues".equals(reader.getLocalName())) {
                return issues;
            }
        }

        throw new XMLStreamException("issues element is not closed");
    }

    private RawIssue parseIssue(XMLStreamReader reader, XtfElementPath path) throws XMLStreamException {
        RawIssue issue = new RawIssue(path);

        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                String localName = reader.getLocalName();
                XtfElementPath childPath = path.push(localName);
                switch (localName) {
                    case "resourceUri" -> issue.resourceUri = parseOptionalUri(reader, childPath).orElse(null);
                    case "identifier" -> issue.identifier = readRequiredText(reader, childPath);
                    case "title" -> issue.title = readRequiredText(reader, childPath);
                    case "description" -> issue.description = readRequiredText(reader, childPath);
                    case "publisher" -> issue.publisher = parseOfficeContainer(reader, childPath);
                    case "creator" -> issue.creator = parseOfficeContainer(reader, childPath);
                    case "contactPoint" -> issue.contactPoint = parseContactPointContainer(reader, childPath);
                    case "themes" -> {
                        RawTheme theme = parseThemeContainer(reader, childPath);
                        if (theme != null) {
                            issue.themes.add(theme);
                        }
                    }
                    case "keywords" -> readOptionalText(reader, childPath).ifPresent(issue.keywords::add);
                    case "landingPage" -> issue.landingPage = parseOptionalUri(reader, childPath).orElse(null);
                    case "issued" -> issue.issued = parseOptionalDate(reader, childPath).orElse(null);
                    case "modified" -> issue.modified = parseRequiredDate(reader, childPath);
                    case "licenseUri" -> issue.licenseUri = parseOptionalUri(reader, childPath).orElse(null);
                    case "accessRights" -> issue.accessLevel = parseAccessRightsContainer(reader, childPath);
                    case "publicationStatus" -> issue.publicationStatus = readRequiredText(reader, childPath);
                    case "origin" -> issue.origin = readOptionalText(reader, childPath).orElse(null);
                    case "accrualPeriodicity" -> issue.accrualPeriodicity = parseAccrualPeriodicityContainer(reader, childPath).orElse(null);
                    case "temporalCoverage" -> issue.temporalCoverage = parseTemporalCoverageContainer(reader, childPath).orElse(null);
                    case "attributes" -> {
                        RawDatasetAttribute attribute = parseDatasetAttributeContainer(reader, childPath);
                        if (attribute != null) {
                            issue.attributes.add(attribute);
                        }
                    }
                    case "model" -> issue.model = readOptionalText(reader, childPath).orElse(null);
                    case "surveyMethod" -> issue.surveyMethod = readOptionalText(reader, childPath).orElse(null);
                    case "dataAvailableFrom" -> issue.dataAvailableFrom = readOptionalText(reader, childPath).orElse(null);
                    case "furtherUses" -> issue.furtherUses = readOptionalText(reader, childPath).orElse(null);
                    case "auxiliaryData" -> issue.auxiliaryData = readOptionalText(reader, childPath).orElse(null);
                    case "distributions" -> {
                        RawDistribution distribution = parseDistributionContainer(reader, childPath);
                        if (distribution != null) {
                            issue.distributions.add(distribution);
                        }
                    }
                    case "issueLabel" -> issue.issueLabel = readRequiredText(reader, childPath);
                    case "isCurrentIssue" -> issue.currentIssue = parseRequiredBoolean(reader, childPath);
                    default -> skipElement(reader);
                }
                continue;
            }

            if (event == XMLStreamConstants.END_ELEMENT && "DatasetIssue".equals(reader.getLocalName())) {
                return issue;
            }
        }

        throw new XMLStreamException("DatasetIssue element is not closed");
    }

    private RawOffice parseOfficeContainer(XMLStreamReader reader, XtfElementPath path) throws XMLStreamException {
        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                if ("Office".equals(reader.getLocalName())) {
                    return parseOffice(reader, path.push("Office"));
                }
                skipElement(reader);
                continue;
            }

            if (event == XMLStreamConstants.END_ELEMENT && path.elements().getLast().equals(reader.getLocalName())) {
                return null;
            }
        }

        throw new XMLStreamException(path.display() + " is not closed");
    }

    private RawOffice parseOffice(XMLStreamReader reader, XtfElementPath path) throws XMLStreamException {
        RawOffice office = new RawOffice(path);

        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                String localName = reader.getLocalName();
                XtfElementPath childPath = path.push(localName);
                switch (localName) {
                    case "officeUri" -> office.officeUri = parseRequiredUri(reader, childPath);
                    case "name" -> office.name = readRequiredText(reader, childPath);
                    case "abbreviation" -> office.abbreviation = readOptionalText(reader, childPath).orElse(null);
                    case "email" -> office.email = parseOptionalUri(reader, childPath);
                    case "officeAtWeb" -> office.officeAtWeb = parseOptionalUri(reader, childPath);
                    default -> skipElement(reader);
                }
                continue;
            }

            if (event == XMLStreamConstants.END_ELEMENT && "Office".equals(reader.getLocalName())) {
                return office;
            }
        }

        throw new XMLStreamException("Office element is not closed");
    }

    private RawTheme parseThemeContainer(XMLStreamReader reader, XtfElementPath path) throws XMLStreamException {
        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                if ("ThemeAssignment".equals(reader.getLocalName())) {
                    return parseTheme(reader, path.push("ThemeAssignment"));
                }
                skipElement(reader);
                continue;
            }

            if (event == XMLStreamConstants.END_ELEMENT && "themes".equals(reader.getLocalName())) {
                return null;
            }
        }

        throw new XMLStreamException("themes element is not closed");
    }

    private RawTheme parseTheme(XMLStreamReader reader, XtfElementPath path) throws XMLStreamException {
        RawTheme theme = new RawTheme(path);

        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                String localName = reader.getLocalName();
                XtfElementPath childPath = path.push(localName);
                switch (localName) {
                    case "localTheme" -> theme.localTheme = readRequiredText(reader, childPath);
                    case "themeUri" -> theme.themeUri = parseOptionalUri(reader, childPath);
                    default -> skipElement(reader);
                }
                continue;
            }

            if (event == XMLStreamConstants.END_ELEMENT && "ThemeAssignment".equals(reader.getLocalName())) {
                return theme;
            }
        }

        throw new XMLStreamException("ThemeAssignment element is not closed");
    }

    private AccessLevel parseAccessRightsContainer(XMLStreamReader reader, XtfElementPath path) throws XMLStreamException {
        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                if ("AccessRights".equals(reader.getLocalName())) {
                    return parseAccessRights(reader, path.push("AccessRights"));
                }
                skipElement(reader);
                continue;
            }

            if (event == XMLStreamConstants.END_ELEMENT && "accessRights".equals(reader.getLocalName())) {
                return null;
            }
        }

        throw new XMLStreamException("accessRights element is not closed");
    }

    private AccessLevel parseAccessRights(XMLStreamReader reader, XtfElementPath path) throws XMLStreamException {
        String localAccessLevel = null;

        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                String localName = reader.getLocalName();
                XtfElementPath childPath = path.push(localName);
                switch (localName) {
                    case "localAccessLevel" -> localAccessLevel = readRequiredText(reader, childPath);
                    case "accessRightsUri" -> parseOptionalUri(reader, childPath);
                    default -> skipElement(reader);
                }
                continue;
            }

            if (event == XMLStreamConstants.END_ELEMENT && "AccessRights".equals(reader.getLocalName())) {
                if (localAccessLevel == null) {
                    throw validationError(path.push("localAccessLevel"), "Required field is missing.");
                }
                try {
                    return AccessLevel.fromModelValue(localAccessLevel);
                } catch (IllegalArgumentException ex) {
                    throw validationError(path.push("localAccessLevel"), ex.getMessage());
                }
            }
        }

        throw new XMLStreamException("AccessRights element is not closed");
    }

    private RawContactPoint parseContactPointContainer(XMLStreamReader reader, XtfElementPath path) throws XMLStreamException {
        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                if ("ContactPoint".equals(reader.getLocalName())) {
                    return parseContactPoint(reader, path.push("ContactPoint"));
                }
                skipElement(reader);
                continue;
            }

            if (event == XMLStreamConstants.END_ELEMENT && "contactPoint".equals(reader.getLocalName())) {
                return null;
            }
        }

        throw new XMLStreamException("contactPoint element is not closed");
    }

    private RawContactPoint parseContactPoint(XMLStreamReader reader, XtfElementPath path) throws XMLStreamException {
        RawContactPoint contactPoint = new RawContactPoint(path);

        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                String localName = reader.getLocalName();
                XtfElementPath childPath = path.push(localName);
                switch (localName) {
                    case "name" -> contactPoint.name = readOptionalText(reader, childPath).orElse(null);
                    case "organizationUnit" -> contactPoint.organizationUnit = readOptionalText(reader, childPath).orElse(null);
                    case "phone" -> contactPoint.phone = readOptionalText(reader, childPath).orElse(null);
                    case "email" -> contactPoint.email = parseOptionalUri(reader, childPath);
                    case "url" -> contactPoint.url = parseOptionalUri(reader, childPath);
                    default -> skipElement(reader);
                }
                continue;
            }

            if (event == XMLStreamConstants.END_ELEMENT && "ContactPoint".equals(reader.getLocalName())) {
                return contactPoint;
            }
        }

        throw new XMLStreamException("ContactPoint element is not closed");
    }

    private Optional<String> parseAccrualPeriodicityContainer(XMLStreamReader reader, XtfElementPath path) throws XMLStreamException {
        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                if ("AccrualPeriodicity".equals(reader.getLocalName())) {
                    return parseAccrualPeriodicity(reader, path.push("AccrualPeriodicity"));
                }
                skipElement(reader);
                continue;
            }

            if (event == XMLStreamConstants.END_ELEMENT && "accrualPeriodicity".equals(reader.getLocalName())) {
                return Optional.empty();
            }
        }

        throw new XMLStreamException("accrualPeriodicity element is not closed");
    }

    private Optional<String> parseAccrualPeriodicity(XMLStreamReader reader, XtfElementPath path) throws XMLStreamException {
        String localFrequency = null;

        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                String localName = reader.getLocalName();
                XtfElementPath childPath = path.push(localName);
                switch (localName) {
                    case "localFrequency" -> localFrequency = readRequiredText(reader, childPath);
                    case "frequencyUri" -> parseOptionalUri(reader, childPath);
                    default -> skipElement(reader);
                }
                continue;
            }

            if (event == XMLStreamConstants.END_ELEMENT && "AccrualPeriodicity".equals(reader.getLocalName())) {
                return Optional.ofNullable(localFrequency);
            }
        }

        throw new XMLStreamException("AccrualPeriodicity element is not closed");
    }

    private Optional<RawTemporalCoverage> parseTemporalCoverageContainer(XMLStreamReader reader, XtfElementPath path) throws XMLStreamException {
        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                if ("TemporalCoverage".equals(reader.getLocalName())) {
                    return Optional.of(parseTemporalCoverage(reader, path.push("TemporalCoverage")));
                }
                skipElement(reader);
                continue;
            }

            if (event == XMLStreamConstants.END_ELEMENT && "temporalCoverage".equals(reader.getLocalName())) {
                return Optional.empty();
            }
        }

        throw new XMLStreamException("temporalCoverage element is not closed");
    }

    private RawTemporalCoverage parseTemporalCoverage(XMLStreamReader reader, XtfElementPath path) throws XMLStreamException {
        RawTemporalCoverage temporalCoverage = new RawTemporalCoverage();

        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                String localName = reader.getLocalName();
                XtfElementPath childPath = path.push(localName);
                switch (localName) {
                    case "startDate" -> temporalCoverage.startDate = parseOptionalDate(reader, childPath);
                    case "endDate" -> temporalCoverage.endDate = parseOptionalDate(reader, childPath);
                    case "referenceDate" -> temporalCoverage.referenceDate = parseOptionalDate(reader, childPath);
                    default -> skipElement(reader);
                }
                continue;
            }

            if (event == XMLStreamConstants.END_ELEMENT && "TemporalCoverage".equals(reader.getLocalName())) {
                return temporalCoverage;
            }
        }

        throw new XMLStreamException("TemporalCoverage element is not closed");
    }

    private RawDistribution parseDistributionContainer(XMLStreamReader reader, XtfElementPath path) throws XMLStreamException {
        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                if ("Distribution".equals(reader.getLocalName())) {
                    return parseDistribution(reader, path.push("Distribution"));
                }
                skipElement(reader);
                continue;
            }

            if (event == XMLStreamConstants.END_ELEMENT && "distributions".equals(reader.getLocalName())) {
                return null;
            }
        }

        throw new XMLStreamException("distributions element is not closed");
    }

    private RawDatasetAttribute parseDatasetAttributeContainer(XMLStreamReader reader, XtfElementPath path)
            throws XMLStreamException {
        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                if ("DatasetAttribute".equals(reader.getLocalName())) {
                    return parseDatasetAttribute(reader, path.push("DatasetAttribute"));
                }
                skipElement(reader);
                continue;
            }

            if (event == XMLStreamConstants.END_ELEMENT && "attributes".equals(reader.getLocalName())) {
                return null;
            }
        }

        throw new XMLStreamException("attributes element is not closed");
    }

    private RawDatasetAttribute parseDatasetAttribute(XMLStreamReader reader, XtfElementPath path) throws XMLStreamException {
        RawDatasetAttribute attribute = new RawDatasetAttribute(path);

        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                String localName = reader.getLocalName();
                XtfElementPath childPath = path.push(localName);
                switch (localName) {
                    case "name" -> attribute.name = readRequiredText(reader, childPath);
                    case "dataType" -> attribute.dataType = readRequiredText(reader, childPath);
                    case "description" -> attribute.description = readOptionalText(reader, childPath).orElse(null);
                    case "unit" -> attribute.unit = readOptionalText(reader, childPath).orElse(null);
                    case "mandatory" -> attribute.mandatory = parseRequiredBoolean(reader, childPath);
                    default -> skipElement(reader);
                }
                continue;
            }

            if (event == XMLStreamConstants.END_ELEMENT && "DatasetAttribute".equals(reader.getLocalName())) {
                return attribute;
            }
        }

        throw new XMLStreamException("DatasetAttribute element is not closed");
    }

    private RawDistribution parseDistribution(XMLStreamReader reader, XtfElementPath path) throws XMLStreamException {
        RawDistribution distribution = new RawDistribution(path);

        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                String localName = reader.getLocalName();
                XtfElementPath childPath = path.push(localName);
                switch (localName) {
                    case "distributionUri" -> distribution.distributionUri = parseOptionalUri(reader, childPath).orElse(null);
                    case "accessURL" -> distribution.accessUrl = parseRequiredUri(reader, childPath);
                    case "downloadURL" -> distribution.downloadUrl = parseOptionalUri(reader, childPath);
                    case "format" -> distribution.format = parseDistributionFormat(reader, childPath);
                    default -> skipElement(reader);
                }
                continue;
            }

            if (event == XMLStreamConstants.END_ELEMENT && "Distribution".equals(reader.getLocalName())) {
                return distribution;
            }
        }

        throw new XMLStreamException("Distribution element is not closed");
    }

    private DistributionFormat parseDistributionFormat(XMLStreamReader reader, XtfElementPath path) throws XMLStreamException {
        String value = readRequiredText(reader, path);
        try {
            return DistributionFormat.fromModelValue(value);
        } catch (IllegalArgumentException ex) {
            throw validationError(path, ex.getMessage());
        }
    }

    private String readRequiredText(XMLStreamReader reader, XtfElementPath path) throws XMLStreamException {
        return readOptionalText(reader, path)
                .orElseThrow(() -> validationError(path, "Required field is missing."));
    }

    private Optional<String> readOptionalText(XMLStreamReader reader, XtfElementPath path) throws XMLStreamException {
        String text = reader.getElementText();
        if (text == null) {
            return Optional.empty();
        }

        String trimmed = text.trim();
        if (trimmed.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(trimmed);
    }

    private URI parseRequiredUri(XMLStreamReader reader, XtfElementPath path) throws XMLStreamException {
        return parseOptionalUri(reader, path)
                .orElseThrow(() -> validationError(path, "Required URI is missing."));
    }

    private Optional<URI> parseOptionalUri(XMLStreamReader reader, XtfElementPath path) throws XMLStreamException {
        Optional<String> value = readOptionalText(reader, path);
        if (value.isEmpty()) {
            return Optional.empty();
        }

        try {
            return Optional.of(new URI(value.get()));
        } catch (URISyntaxException ex) {
            throw validationError(path, "Invalid URI value: " + value.get());
        }
    }

    private LocalDate parseRequiredDate(XMLStreamReader reader, XtfElementPath path) throws XMLStreamException {
        return parseOptionalDate(reader, path)
                .orElseThrow(() -> validationError(path, "Required date is missing."));
    }

    private Optional<LocalDate> parseOptionalDate(XMLStreamReader reader, XtfElementPath path) throws XMLStreamException {
        Optional<String> value = readOptionalText(reader, path);
        if (value.isEmpty()) {
            return Optional.empty();
        }

        try {
            return Optional.of(LocalDate.parse(value.get()));
        } catch (DateTimeParseException ex) {
            throw validationError(path, "Invalid date value: " + value.get());
        }
    }

    private boolean parseRequiredBoolean(XMLStreamReader reader, XtfElementPath path) throws XMLStreamException {
        String value = readRequiredText(reader, path);
        return switch (value.toLowerCase()) {
            case "true" -> true;
            case "false" -> false;
            default -> throw validationError(path, "Boolean value must be 'true' or 'false'.");
        };
    }

    private void skipElement(XMLStreamReader reader) throws XMLStreamException {
        int depth = 1;
        while (depth > 0 && reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                depth++;
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                depth--;
            }
        }
    }

    private static CatalogValidationException validationError(XtfElementPath path, String message) {
        return new CatalogValidationException(List.of(path.display() + ": " + message));
    }

    private abstract static class RawEntry {
        final XtfElementPath path;
        URI resourceUri;
        String identifier;
        String title;
        String description;
        RawOffice publisher;
        RawOffice creator;
        final List<RawTheme> themes = new ArrayList<>();
        final List<String> keywords = new ArrayList<>();
        RawContactPoint contactPoint;
        URI landingPage;
        LocalDate issued;
        LocalDate modified;
        URI licenseUri;
        AccessLevel accessLevel;
        String publicationStatus;
        String origin;
        String accrualPeriodicity;
        RawTemporalCoverage temporalCoverage;
        final List<RawDatasetAttribute> attributes = new ArrayList<>();
        String model;
        String surveyMethod;
        String dataAvailableFrom;
        String furtherUses;
        String auxiliaryData;

        RawEntry(XtfElementPath path) {
            this.path = path;
        }

        final void validateCommon() {
            require(identifier, path.push("identifier"));
            require(title, path.push("title"));
            require(description, path.push("description"));
            require(publisher, path.push("publisher"));
            require(creator, path.push("creator"));
            require(modified, path.push("modified"));
            require(accessLevel, path.push("accessRights"));

            if (publicationStatus == null || publicationStatus.isBlank()) {
                throw validationError(path.push("publicationStatus"), "Required field is missing.");
            }
            if (!"published".equalsIgnoreCase(publicationStatus.trim())) {
                throw validationError(path.push("publicationStatus"),
                        "Only publicationStatus 'published' is supported in Phase 2.");
            }
        }

        final Office publisherDomain() {
            return publisher.toDomain();
        }

        final Office creatorDomain() {
            return creator.toDomain();
        }

        final List<Theme> themeDomains() {
            return themes.stream()
                    .map(RawTheme::toDomain)
                    .toList();
        }

        final List<String> keywordValues() {
            return List.copyOf(keywords);
        }

        final List<DistributionLink> distributionDomains(List<RawDistribution> distributions) {
            return distributions.stream()
                    .map(RawDistribution::toDomain)
                    .toList();
        }

        final CatalogEntryMetadata metadataDomain() {
            return new CatalogEntryMetadata(
                    Optional.ofNullable(landingPage),
                    Optional.ofNullable(issued),
                    Optional.ofNullable(licenseUri),
                    Optional.ofNullable(contactPoint).map(RawContactPoint::toDomain),
                    Optional.ofNullable(accrualPeriodicity),
                    Optional.ofNullable(publicationStatus),
                    Optional.ofNullable(origin),
                    Optional.ofNullable(temporalCoverage).map(RawTemporalCoverage::toDomain),
                    attributes.stream().map(RawDatasetAttribute::toDomain).toList(),
                    Optional.ofNullable(model),
                    Optional.ofNullable(surveyMethod),
                    Optional.ofNullable(dataAvailableFrom),
                    Optional.ofNullable(furtherUses),
                    Optional.ofNullable(auxiliaryData));
        }

        private static void require(Object value, XtfElementPath path) {
            if (value == null) {
                throw validationError(path, "Required field is missing.");
            }
        }
    }

    private static final class RawCatalog {
        final XtfElementPath path;
        URI catalogUri;
        String title;
        String description;
        RawOffice publisher;
        URI homepage;
        LocalDate modified;
        final List<RawOffice> agents = new ArrayList<>();
        final List<RawDataset> datasets = new ArrayList<>();
        final List<RawSeries> datasetSeries = new ArrayList<>();

        RawCatalog(XtfElementPath path) {
            this.path = path;
        }

        Catalog toDomain() {
            return new Catalog(
                    datasets.stream().map(RawDataset::toDomain).toList(),
                    datasetSeries.stream().map(RawSeries::toDomain).toList());
        }
    }

    private static final class RawDataset extends RawEntry {
        final List<RawDistribution> distributions = new ArrayList<>();

        RawDataset(XtfElementPath path) {
            super(path);
        }

        DatasetEntry toDomain() {
            validateCommon();
            return new DatasetEntry(
                    identifier,
                    title,
                    description,
                    publisherDomain(),
                    creatorDomain(),
                    themeDomains(),
                    keywordValues(),
                    modified,
                    accessLevel,
                    metadataDomain(),
                    distributionDomains(distributions));
        }
    }

    private static final class RawSeries extends RawEntry {
        final List<RawIssue> issues = new ArrayList<>();

        RawSeries(XtfElementPath path) {
            super(path);
        }

        DatasetSeriesEntry toDomain() {
            validateCommon();
            if (issues.isEmpty()) {
                throw validationError(path.push("issues"), "Dataset series must contain at least one issue.");
            }

            return new DatasetSeriesEntry(
                    identifier,
                    title,
                    description,
                    publisherDomain(),
                    creatorDomain(),
                    themeDomains(),
                    keywordValues(),
                    accessLevel,
                    metadataDomain(),
                    issues.stream().map(RawIssue::toDomain).toList());
        }
    }

    private static final class RawIssue extends RawEntry {
        final List<RawDistribution> distributions = new ArrayList<>();
        String issueLabel;
        boolean currentIssue;

        RawIssue(XtfElementPath path) {
            super(path);
        }

        DatasetIssueEntry toDomain() {
            validateCommon();
            if (issueLabel == null || issueLabel.isBlank()) {
                throw validationError(path.push("issueLabel"), "Required field is missing.");
            }

            return new DatasetIssueEntry(
                    identifier,
                    title,
                    description,
                    publisherDomain(),
                    creatorDomain(),
                    themeDomains(),
                    keywordValues(),
                    modified,
                    accessLevel,
                    metadataDomain(),
                    distributionDomains(distributions),
                    issueLabel,
                    currentIssue);
        }
    }

    private static final class RawContactPoint {
        final XtfElementPath path;
        String name;
        String organizationUnit;
        Optional<URI> email = Optional.empty();
        String phone;
        Optional<URI> url = Optional.empty();

        RawContactPoint(XtfElementPath path) {
            this.path = path;
        }

        ContactPoint toDomain() {
            String displayName = firstPresent(name, organizationUnit, email.map(URI::toString).orElse(null), url.map(URI::toString).orElse(null));
            if (displayName == null) {
                throw validationError(path.push("name"), "Required field is missing.");
            }
            return new ContactPoint(
                    displayName,
                    Optional.ofNullable(organizationUnit),
                    email,
                    Optional.ofNullable(phone),
                    url);
        }

        private static String firstPresent(String... values) {
            for (String value : values) {
                if (value != null && !value.isBlank()) {
                    return value;
                }
            }
            return null;
        }
    }

    private static final class RawTemporalCoverage {
        Optional<LocalDate> startDate = Optional.empty();
        Optional<LocalDate> endDate = Optional.empty();
        Optional<LocalDate> referenceDate = Optional.empty();

        TemporalCoverage toDomain() {
            return new TemporalCoverage(startDate, endDate, referenceDate);
        }
    }

    private static final class RawDatasetAttribute {
        final XtfElementPath path;
        String name;
        String dataType;
        String description;
        String unit;
        Boolean mandatory;

        RawDatasetAttribute(XtfElementPath path) {
            this.path = path;
        }

        DatasetAttribute toDomain() {
            if (name == null || name.isBlank()) {
                throw validationError(path.push("name"), "Required field is missing.");
            }
            if (dataType == null || dataType.isBlank()) {
                throw validationError(path.push("dataType"), "Required field is missing.");
            }
            if (mandatory == null) {
                throw validationError(path.push("mandatory"), "Required field is missing.");
            }
            return new DatasetAttribute(
                    name,
                    dataType,
                    Optional.ofNullable(description),
                    Optional.ofNullable(unit),
                    mandatory);
        }
    }

    private static final class RawOffice {
        final XtfElementPath path;
        URI officeUri;
        String name;
        String abbreviation;
        Optional<URI> email = Optional.empty();
        Optional<URI> officeAtWeb = Optional.empty();

        RawOffice(XtfElementPath path) {
            this.path = path;
        }

        Office toDomain() {
            if (officeUri == null) {
                throw validationError(path.push("officeUri"), "Required field is missing.");
            }
            if (name == null || name.isBlank()) {
                throw validationError(path.push("name"), "Required field is missing.");
            }

            String officeIdentifier = lastPathSegment(officeUri, path.push("officeUri"));
            return new Office(
                    officeIdentifier,
                    name,
                    Optional.ofNullable(abbreviation).filter(value -> !value.isBlank()),
                    email,
                    officeAtWeb);
        }

        private static String lastPathSegment(URI uri, XtfElementPath path) {
            String uriPath = uri.getPath();
            if (uriPath == null || uriPath.isBlank()) {
                throw validationError(path, "Office URI must contain a path segment.");
            }

            String identifier = uriPath.substring(uriPath.lastIndexOf('/') + 1);
            if (identifier.isBlank()) {
                throw validationError(path, "Office URI must end with a non-empty path segment.");
            }
            return identifier;
        }
    }

    private static final class RawTheme {
        private static final Map<String, String> DISPLAY_NAMES = Map.ofEntries(
                Map.entry("Arbeit_Erwerb", "Arbeit und Erwerb"),
                Map.entry("Bau_und_Wohnungswesen", "Bau- und Wohnungswesen"),
                Map.entry("Bevoelkerung", "Bevölkerung"),
                Map.entry("Bildung_Wissenschaft", "Bildung und Wissenschaft"),
                Map.entry("Energie", "Energie"),
                Map.entry("Finanzen", "Finanzen"),
                Map.entry("Geografie", "Geografie"),
                Map.entry("Gesetzgebung", "Gesetzgebung"),
                Map.entry("Gesundheit", "Gesundheit"),
                Map.entry("Handel", "Handel"),
                Map.entry("Industrie_und_Dienstleistungen", "Industrie und Dienstleistungen"),
                Map.entry("Kriminalitaet_Strafrecht", "Kriminalität und Strafrecht"),
                Map.entry("Kultur_Medien_Informationsgesellschaft_Sport", "Kultur, Medien, Informationsgesellschaft und Sport"),
                Map.entry("Landwirtschaft_Forstwirtschaft", "Landwirtschaft und Forstwirtschaft"),
                Map.entry("Mobilitaet_und_Verkehr", "Mobilität und Verkehr"),
                Map.entry("Oeffentliche_Ordnung_und_Sicherheit", "Öffentliche Ordnung und Sicherheit"),
                Map.entry("Politik", "Politik"),
                Map.entry("Preise", "Preise"),
                Map.entry("Raum_und_Umwelt", "Raum und Umwelt"),
                Map.entry("Soziale_Sicherheit", "Soziale Sicherheit"),
                Map.entry("Statistische_Grundlagen", "Statistische Grundlagen"),
                Map.entry("Tourismus", "Tourismus"),
                Map.entry("Verwaltung", "Verwaltung"),
                Map.entry("Volkswirtschaft", "Volkswirtschaft"));

        final XtfElementPath path;
        String localTheme;
        Optional<URI> themeUri = Optional.empty();

        RawTheme(XtfElementPath path) {
            this.path = path;
        }

        Theme toDomain() {
            if (localTheme == null || localTheme.isBlank()) {
                throw validationError(path.push("localTheme"), "Required field is missing.");
            }

            return new Theme(localTheme, DISPLAY_NAMES.getOrDefault(localTheme, localTheme.replace('_', ' ')));
        }
    }

    private static final class RawDistribution {
        final XtfElementPath path;
        URI distributionUri;
        URI accessUrl;
        Optional<URI> downloadUrl = Optional.empty();
        DistributionFormat format;

        RawDistribution(XtfElementPath path) {
            this.path = path;
        }

        DistributionLink toDomain() {
            if (accessUrl == null) {
                throw validationError(path.push("accessURL"), "Required field is missing.");
            }
            if (format == null) {
                throw validationError(path.push("format"), "Required field is missing.");
            }

            return new DistributionLink(accessUrl, downloadUrl, format);
        }
    }
}
