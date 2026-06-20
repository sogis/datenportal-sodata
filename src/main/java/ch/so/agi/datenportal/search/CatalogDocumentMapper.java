package ch.so.agi.datenportal.search;

import ch.so.agi.datenportal.catalog.domain.CatalogEntry;
import ch.so.agi.datenportal.catalog.domain.CatalogEntryType;
import ch.so.agi.datenportal.catalog.domain.DatasetIssueEntry;
import ch.so.agi.datenportal.catalog.domain.DatasetSeriesEntry;
import ch.so.agi.datenportal.catalog.domain.DistributionLink;
import ch.so.agi.datenportal.catalog.domain.Office;
import ch.so.agi.datenportal.catalog.domain.Theme;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Objects;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.springframework.stereotype.Component;

@Component
public final class CatalogDocumentMapper {

    public Document toDocument(CatalogEntry entry) {
        Objects.requireNonNull(entry, "entry must not be null");
        if (entry.type() == CatalogEntryType.DATASET_ISSUE) {
            throw new IllegalArgumentException("Dataset issues are not indexed as top-level search documents.");
        }

        var document = new Document();
        var allText = new StringBuilder();

        addStoredIdentifier(document, CatalogSearchFields.ENTRY_ID, entry.identifier());
        addExact(document, CatalogSearchFields.ENTRY_TYPE, typeValue(entry), true);
        addExact(document, CatalogSearchFields.IDENTIFIER_EXACT, entry.identifier(), true);
        addText(document, CatalogSearchFields.IDENTIFIER_TEXT, entry.identifier(), false);
        addNormalizedWholeValue(document, CatalogSearchFields.IDENTIFIER_SUBSTRING, entry.identifier());
        append(allText, entry.identifier());

        addText(document, CatalogSearchFields.TITLE, entry.title(), true);
        addExact(document, CatalogSearchFields.TITLE_EXACT, entry.title(), true);
        addNormalizedWholeValue(document, CatalogSearchFields.TITLE_SUBSTRING, entry.title());
        append(allText, entry.title());

        addText(document, CatalogSearchFields.DESCRIPTION, entry.description(), true);
        addTokenTerms(document, CatalogSearchFields.DESCRIPTION_TERMS, entry.description());
        append(allText, entry.description());

        entry.keywords().forEach(keyword -> {
            addText(document, CatalogSearchFields.KEYWORDS, keyword, false);
            addNormalizedWholeValue(document, CatalogSearchFields.KEYWORD_SUBSTRING, keyword);
            append(allText, keyword);
        });

        entry.themes().forEach(theme -> addTheme(document, allText, theme));
        addOffice(document, allText, entry.creator());

        entry.distributionsForListing().stream()
                .map(DistributionLink::format)
                .distinct()
                .forEach(format -> {
                    var formatValue = CatalogSearchFields.formatValue(format);
                    addRawExact(document, CatalogSearchFields.FORMATS, formatValue, false);
                    addRawExact(document, CatalogSearchFields.FORMAT_TERMS, formatValue, false);
                });

        addDate(document, CatalogSearchFields.PUBLICATION_DATE_EPOCH_DAY, entry.modified());
        addDate(document, CatalogSearchFields.MODIFIED_DATE_EPOCH_DAY, entry.modified());
        addRawExact(document, CatalogSearchFields.OPEN_DATA, Boolean.toString(entry.isOpenData()), true);
        addRawExact(document, CatalogSearchFields.STRUCTURE_DESCRIBED, "false", true);

        if (entry instanceof DatasetSeriesEntry series) {
            addSeriesFields(document, allText, series);
        }

        addText(document, CatalogSearchFields.ALL_TEXT, allText.toString(), false);
        return document;
    }

    private static void addSeriesFields(Document document, StringBuilder allText, DatasetSeriesEntry series) {
        for (DatasetIssueEntry issue : series.issuesNewestFirst()) {
            var issueYear = Integer.toString(issue.modified().getYear());
            addRawExact(document, CatalogSearchFields.ISSUE_YEARS, issueYear, false);

            var issueText = issue.identifier() + " "
                    + issue.title() + " "
                    + issue.description() + " "
                    + issue.issueLabel() + " "
                    + issueYear + " "
                    + String.join(" ", issue.keywords()) + " "
                    + issue.themes().stream()
                            .map(theme -> theme.identifier() + " " + theme.displayName())
                            .reduce("", (left, right) -> left + " " + right) + " "
                    + officeText(issue.creator());
            addText(document, CatalogSearchFields.ISSUE_TEXT, issueText, false);
            addNormalizedWholeValue(document, CatalogSearchFields.ISSUE_IDENTIFIER_SUBSTRING, issue.identifier());
            addNormalizedWholeValue(document, CatalogSearchFields.ISSUE_TITLE_SUBSTRING, issue.title());
            addNormalizedWholeValue(document, CatalogSearchFields.ISSUE_LABEL_SUBSTRING, issue.issueLabel());
            addNormalizedWholeValue(document, CatalogSearchFields.ISSUE_LABEL_SUBSTRING, issueYear);
            addTokenTerms(document, CatalogSearchFields.ISSUE_DESCRIPTION_TERMS, issue.description());
            issue.keywords().forEach(keyword -> addNormalizedWholeValue(document, CatalogSearchFields.ISSUE_KEYWORD_SUBSTRING, keyword));
            issue.themes().forEach(theme -> addTheme(document, allText, theme));
            addOffice(document, allText, issue.creator());
            append(allText, issueText);
        }
    }

    private static void addTheme(Document document, StringBuilder allText, Theme theme) {
        addText(document, CatalogSearchFields.THEME_TEXT, theme.identifier() + " " + theme.displayName(), false);
        addTokenTerms(document, CatalogSearchFields.THEME_TERMS, theme.identifier() + " " + theme.displayName());
        addExact(document, CatalogSearchFields.THEME_EXACT, theme.identifier(), false);
        addExact(document, CatalogSearchFields.THEME_EXACT, theme.displayName(), false);
        append(allText, theme.identifier());
        append(allText, theme.displayName());
    }

    private static void addOffice(Document document, StringBuilder allText, Office office) {
        addText(document, CatalogSearchFields.OFFICE_TEXT, officeText(office), false);
        addTokenTerms(document, CatalogSearchFields.OFFICE_TERMS, officeText(office));
        addExact(document, CatalogSearchFields.OFFICE_EXACT, office.identifier(), false);
        addExact(document, CatalogSearchFields.OFFICE_EXACT, office.displayName(), false);
        office.abbreviation().ifPresent(abbreviation -> addExact(document, CatalogSearchFields.OFFICE_EXACT, abbreviation, false));
        append(allText, officeText(office));
    }

    private static String officeText(Office office) {
        return office.identifier() + " "
                + office.displayName() + " "
                + office.abbreviation().orElse("");
    }

    private static void addDate(Document document, String field, LocalDate date) {
        long epochDay = date.toEpochDay();
        document.add(new LongPoint(field, epochDay));
        document.add(new StoredField(field, epochDay));
        document.add(new NumericDocValuesField(field, epochDay));
    }

    private static void addText(Document document, String field, String value, boolean stored) {
        if (value == null || value.isBlank()) {
            return;
        }
        document.add(new TextField(field, CatalogSearchFields.searchableText(value), stored ? Field.Store.YES : Field.Store.NO));
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

    private static void append(StringBuilder builder, String value) {
        if (value != null && !value.isBlank()) {
            builder.append(' ').append(value);
        }
    }

    private static String typeValue(CatalogEntry entry) {
        return switch (entry.type()) {
            case DATASET -> "dataset";
            case DATASET_SERIES -> "series";
            case DATASET_ISSUE -> "issue";
        };
    }
}
