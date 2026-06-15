package ch.so.agi.datenportal.search;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Objects;

public final class CatalogSearchFields {

    public static final String ENTRY_ID = "entry_id";
    public static final String ENTRY_TYPE = "entry_type";
    public static final String IDENTIFIER_EXACT = "identifier_exact";
    public static final String IDENTIFIER_TEXT = "identifier_text";
    public static final String TITLE = "title";
    public static final String TITLE_EXACT = "title_exact";
    public static final String DESCRIPTION = "description";
    public static final String KEYWORDS = "keywords";
    public static final String THEME_TEXT = "theme_text";
    public static final String THEME_EXACT = "theme_exact";
    public static final String OFFICE_TEXT = "office_text";
    public static final String OFFICE_EXACT = "office_exact";
    public static final String FORMATS = "formats";
    public static final String PUBLICATION_DATE_EPOCH_DAY = "publication_date_epoch_day";
    public static final String MODIFIED_DATE_EPOCH_DAY = "modified_date_epoch_day";
    public static final String OPEN_DATA = "open_data";
    public static final String STRUCTURE_DESCRIBED = "structure_described";
    public static final String ISSUE_YEARS = "issue_years";
    public static final String ISSUE_TEXT = "issue_text";
    public static final String ALL_TEXT = "all_text";

    private CatalogSearchFields() {}

    public static String normalizeExact(String value) {
        if (value == null) {
            return "";
        }
        var decomposed = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replace("ß", "ss")
                .replace("ẞ", "ss");
        return decomposed.replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^\\p{IsAlphabetic}\\p{IsDigit}]+", " ")
                .trim()
                .replaceAll("\\s+", " ");
    }

    public static String searchableText(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        var normalized = normalizeExact(value);
        if (normalized.isBlank()) {
            return value;
        }
        return value + " " + normalized;
    }

    public static String formatValue(Enum<?> value) {
        return Objects.requireNonNull(value, "value must not be null").name().toLowerCase(Locale.ROOT);
    }
}
