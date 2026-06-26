package ch.so.agi.datenportal.catalog.importxtf;

import static org.assertj.core.api.Assertions.assertThat;

import ch.so.agi.datenportal.catalog.domain.AccessLevel;
import ch.so.agi.datenportal.catalog.domain.Catalog;
import ch.so.agi.datenportal.catalog.domain.CatalogEntryMetadata;
import ch.so.agi.datenportal.catalog.domain.DatasetAttribute;
import ch.so.agi.datenportal.catalog.domain.DatasetEntry;
import ch.so.agi.datenportal.catalog.domain.DatasetIssueEntry;
import ch.so.agi.datenportal.catalog.domain.DatasetSeriesEntry;
import ch.so.agi.datenportal.catalog.domain.DistributionFormat;
import ch.so.agi.datenportal.catalog.domain.DistributionLink;
import ch.so.agi.datenportal.catalog.domain.Office;
import ch.so.agi.datenportal.catalog.domain.Theme;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class CatalogValidatorTest {

    private static final CatalogValidator VALIDATOR = new CatalogValidator();
    private static final Office OFFICE = new Office("office", "Amt für Tests", Optional.of("TEST"));
    private static final Theme THEME = new Theme("theme", "Thema");

    @Test
    void rejectsDuplicateIdentifiersAcrossDatasetsSeriesAndIssues() {
        Catalog catalog = new Catalog(
                List.of(dataset("duplicate")),
                List.of(series("series", issue("duplicate", "2026", true, LocalDate.parse("2026-05-01")))));

        CatalogValidationResult result = VALIDATOR.validate(catalog);

        assertThat(result.hasErrors()).isTrue();
        assertThat(result.errors()).anyMatch(message -> message.contains("Duplicate identifier 'duplicate'"));
    }

    @Test
    void warnsWhenMultipleCurrentIssuesExist() {
        Catalog catalog = new Catalog(
                List.of(),
                List.of(series(
                        "series",
                        issue("series-2025", "2025", true, LocalDate.parse("2025-12-31")),
                        issue("series-2026", "2026", true, LocalDate.parse("2026-12-31")))));

        CatalogValidationResult result = VALIDATOR.validate(catalog);

        assertThat(result.errors()).isEmpty();
        assertThat(result.warnings()).anyMatch(message -> message.contains("multiple current issues"));
    }

    @Test
    void warnsWhenNoCurrentIssueExists() {
        Catalog catalog = new Catalog(
                List.of(),
                List.of(series(
                        "series",
                        issue("series-2025", "2025", false, LocalDate.parse("2025-12-31")),
                        issue("series-2026", "2026", false, LocalDate.parse("2026-12-31")))));

        CatalogValidationResult result = VALIDATOR.validate(catalog);

        assertThat(result.errors()).isEmpty();
        assertThat(result.warnings()).anyMatch(message -> message.contains("no explicit current issue"));
    }

    @Test
    void acceptsExtendedAccessLevelsAndStructuredMetadata() {
        Catalog catalog = new Catalog(
                List.of(dataset("public-conditions", AccessLevel.PUBLIC_WITH_CONDITIONS)),
                List.of(series(
                        "series",
                        AccessLevel.INTERNAL,
                        issue("series-2026", "2026", true, LocalDate.parse("2026-12-31"), AccessLevel.CONFIDENTIAL))));

        CatalogValidationResult result = VALIDATOR.validate(catalog);

        assertThat(result.errors()).isEmpty();
    }

    private static DatasetEntry dataset(String identifier) {
        return dataset(identifier, AccessLevel.OPEN);
    }

    private static DatasetEntry dataset(String identifier, AccessLevel accessLevel) {
        return new DatasetEntry(
                identifier,
                "Dataset " + identifier,
                "Beschreibung",
                OFFICE,
                OFFICE,
                List.of(THEME),
                List.of(identifier),
                LocalDate.parse("2026-05-01"),
                accessLevel,
                metadata(),
                List.of(new DistributionLink(URI.create("https://example.com/" + identifier + ".csv"), DistributionFormat.CSV)));
    }

    private static DatasetSeriesEntry series(String identifier, DatasetIssueEntry... issues) {
        return series(identifier, AccessLevel.OPEN, issues);
    }

    private static DatasetSeriesEntry series(String identifier, AccessLevel accessLevel, DatasetIssueEntry... issues) {
        return new DatasetSeriesEntry(
                identifier,
                "Series " + identifier,
                "Beschreibung",
                OFFICE,
                OFFICE,
                List.of(THEME),
                List.of(identifier),
                accessLevel,
                metadata(),
                List.of(issues));
    }

    private static DatasetIssueEntry issue(String identifier, String label, boolean current, LocalDate modified) {
        return issue(identifier, label, current, modified, AccessLevel.OPEN);
    }

    private static DatasetIssueEntry issue(
            String identifier,
            String label,
            boolean current,
            LocalDate modified,
            AccessLevel accessLevel) {
        return new DatasetIssueEntry(
                identifier,
                "Issue " + label,
                "Beschreibung",
                OFFICE,
                OFFICE,
                List.of(THEME),
                List.of(label),
                modified,
                accessLevel,
                metadata(),
                List.of(new DistributionLink(URI.create("https://example.com/" + identifier + ".csv"), DistributionFormat.CSV)),
                label,
                current);
    }

    private static CatalogEntryMetadata metadata() {
        return new CatalogEntryMetadata(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                List.of(new DatasetAttribute("gemeinde", "TEXT", Optional.of("Gemeinde"), Optional.empty(), true)),
                Optional.of("SO_AGI_Testmodell_20260624"));
    }
}
