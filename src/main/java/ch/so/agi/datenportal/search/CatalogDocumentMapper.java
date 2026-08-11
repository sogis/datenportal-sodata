package ch.so.agi.datenportal.search;

import ch.so.agi.datenportal.catalog.domain.CatalogEntry;
import ch.so.agi.datenportal.catalog.domain.CatalogEntryType;
import ch.so.agi.datenportal.catalog.domain.DatasetIssueEntry;
import ch.so.agi.datenportal.catalog.domain.DatasetSeriesEntry;
import ch.so.agi.datenportal.catalog.domain.DistributionLink;
import ch.so.agi.datenportal.catalog.domain.Office;
import ch.so.agi.datenportal.catalog.domain.Theme;
import java.util.Locale;
import java.util.Objects;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.springframework.stereotype.Component;

@Component
public final class CatalogDocumentMapper {

    public Document toDocument(CatalogEntry entry) {
        Objects.requireNonNull(entry, "entry must not be null");
        if (entry.type() == CatalogEntryType.DATASET_ISSUE) {
            throw new IllegalArgumentException("Dataset issues are not indexed as top-level search documents.");
        }

        var document = new Document();
        addStoredIdentifier(document, CatalogSearchFields.ENTRY_ID, entry.identifier());
        addExact(document, CatalogSearchFields.IDENTIFIER_EXACT, entry.identifier(), true);
        addNormalizedWholeValue(document, CatalogSearchFields.IDENTIFIER_SUBSTRING, entry.identifier());

        addExact(document, CatalogSearchFields.TITLE_EXACT, entry.title(), true);
        addNormalizedWholeValue(document, CatalogSearchFields.TITLE_SUBSTRING, entry.title());

        addTokenTerms(document, CatalogSearchFields.DESCRIPTION_TERMS, entry.description());

        entry.keywords().forEach(keyword -> {
            addNormalizedWholeValue(document, CatalogSearchFields.KEYWORD_SUBSTRING, keyword);
        });

        entry.themes().forEach(theme -> addTheme(document, theme));
        addOffice(document, entry.creator());

        entry.distributionsForListing().stream()
                .map(DistributionLink::format)
                .distinct()
                .forEach(format -> {
                    var formatValue = CatalogSearchFields.formatValue(format);
                    addRawExact(document, CatalogSearchFields.FORMAT_TERMS, formatValue, false);
                });

        if (entry instanceof DatasetSeriesEntry series) {
            addSeriesFields(document, series);
        }

        return document;
    }

    private static void addSeriesFields(Document document, DatasetSeriesEntry series) {
        for (DatasetIssueEntry issue : series.issuesNewestFirst()) {
            var issueYear = Integer.toString(issue.modified().getYear());

            addNormalizedWholeValue(document, CatalogSearchFields.ISSUE_IDENTIFIER_SUBSTRING, issue.identifier());
            addNormalizedWholeValue(document, CatalogSearchFields.ISSUE_TITLE_SUBSTRING, issue.title());
            addNormalizedWholeValue(document, CatalogSearchFields.ISSUE_LABEL_SUBSTRING, issue.issueLabel());
            addNormalizedWholeValue(document, CatalogSearchFields.ISSUE_LABEL_SUBSTRING, issueYear);
            addTokenTerms(document, CatalogSearchFields.ISSUE_DESCRIPTION_TERMS, issue.description());
            issue.keywords().forEach(keyword -> addNormalizedWholeValue(document, CatalogSearchFields.ISSUE_KEYWORD_SUBSTRING, keyword));
            issue.themes().forEach(theme -> addTheme(document, theme));
            addOffice(document, issue.creator());
        }
    }

    private static void addTheme(Document document, Theme theme) {
        addTokenTerms(document, CatalogSearchFields.THEME_TERMS, theme.identifier() + " " + theme.displayName());
    }

    private static void addOffice(Document document, Office office) {
        addTokenTerms(document, CatalogSearchFields.OFFICE_TERMS, officeText(office));
    }

    private static String officeText(Office office) {
        return office.identifier() + " "
                + office.displayName() + " "
                + office.abbreviation().orElse("");
    }

    private static void addNormalizedWholeValue(Document document, String field, String value) {
        var normalized = CatalogSearchFields.normalizeExact(value);
        if (normalized.isBlank()) {
            return;
        }
        addRawExact(document, field, normalized, false);
    }

    private static void addTokenTerms(Document document, String field, String value) {
        CatalogSearchFields.tokenizeNormalized(value).forEach(token -> addRawExact(document, field, token, false));
    }

    private static void addExact(Document document, String field, String value, boolean stored) {
        var normalized = CatalogSearchFields.normalizeExact(value);
        if (normalized.isBlank()) {
            return;
        }
        addRawExact(document, field, normalized, stored);
    }

    private static void addRawExact(Document document, String field, String value, boolean stored) {
        if (value == null || value.isBlank()) {
            return;
        }
        document.add(new StringField(field, value.toLowerCase(Locale.ROOT), stored ? Field.Store.YES : Field.Store.NO));
    }

    private static void addStoredIdentifier(Document document, String field, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        document.add(new StringField(field, value, Field.Store.YES));
    }

}
