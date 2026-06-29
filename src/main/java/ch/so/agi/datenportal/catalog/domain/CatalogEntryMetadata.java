package ch.so.agi.datenportal.catalog.domain;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public record CatalogEntryMetadata(
        Optional<URI> landingPage,
        Optional<LocalDate> issued,
        Optional<URI> licenseUri,
        Optional<ContactPoint> contactPoint,
        Optional<String> accrualPeriodicity,
        Optional<String> publicationStatus,
        Optional<String> origin,
        Optional<TemporalCoverage> temporalCoverage,
        List<DatasetAttribute> attributes,
        Optional<String> model,
        Optional<String> surveyMethod,
        Optional<String> dataAvailableFrom,
        Optional<String> furtherUses,
        Optional<String> auxiliaryData) {

    private static final CatalogEntryMetadata EMPTY = new CatalogEntryMetadata(
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            List.of(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty());

    public CatalogEntryMetadata(
            Optional<URI> landingPage,
            Optional<LocalDate> issued,
            Optional<URI> licenseUri,
            Optional<ContactPoint> contactPoint,
            Optional<String> accrualPeriodicity,
            Optional<String> publicationStatus,
            Optional<String> origin,
            Optional<TemporalCoverage> temporalCoverage,
            List<DatasetAttribute> attributes,
            Optional<String> model) {
        this(
                landingPage,
                issued,
                licenseUri,
                contactPoint,
                accrualPeriodicity,
                publicationStatus,
                origin,
                temporalCoverage,
                attributes,
                model,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty());
    }

    public CatalogEntryMetadata {
        landingPage = landingPage == null ? Optional.empty() : landingPage;
        issued = issued == null ? Optional.empty() : issued;
        licenseUri = licenseUri == null ? Optional.empty() : licenseUri;
        contactPoint = contactPoint == null ? Optional.empty() : contactPoint;
        accrualPeriodicity = accrualPeriodicity == null ? Optional.empty() : accrualPeriodicity
                .filter(value -> !value.isBlank());
        publicationStatus = publicationStatus == null ? Optional.empty() : publicationStatus
                .filter(value -> !value.isBlank());
        origin = origin == null ? Optional.empty() : origin
                .filter(value -> !value.isBlank());
        temporalCoverage = temporalCoverage == null ? Optional.empty() : temporalCoverage
                .filter(coverage -> !coverage.isEmpty());
        attributes = attributes == null ? List.of() : List.copyOf(attributes);
        model = model == null ? Optional.empty() : model.filter(value -> !value.isBlank());
        surveyMethod = cleanOptional(surveyMethod);
        dataAvailableFrom = cleanOptional(dataAvailableFrom);
        furtherUses = cleanOptional(furtherUses);
        auxiliaryData = cleanOptional(auxiliaryData);
    }

    public static CatalogEntryMetadata empty() {
        return EMPTY;
    }

    public boolean hasStructureInformation() {
        return !attributes.isEmpty() || model.isPresent();
    }

    private static Optional<String> cleanOptional(Optional<String> value) {
        return value == null ? Optional.empty() : value.filter(text -> !text.isBlank());
    }
}
