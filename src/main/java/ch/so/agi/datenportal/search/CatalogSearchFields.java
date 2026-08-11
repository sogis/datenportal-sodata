package ch.so.agi.datenportal.search;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class CatalogSearchFields {

    public static final String ENTRY_ID = "entry_id";
    public static final String IDENTIFIER_EXACT = "identifier_exact";
    public static final String IDENTIFIER_SUBSTRING = "identifier_substring";
    public static final String TITLE_EXACT = "title_exact";
    public static final String TITLE_SUBSTRING = "title_substring";
    public static final String DESCRIPTION_TERMS = "description_terms";
    public static final String KEYWORD_SUBSTRING = "keyword_substring";
    public static final String THEME_TERMS = "theme_terms";
    public static final String OFFICE_TERMS = "office_terms";
    public static final String FORMAT_TERMS = "format_terms";
    public static final String ISSUE_IDENTIFIER_SUBSTRING = "issue_identifier_substring";
    public static final String ISSUE_TITLE_SUBSTRING = "issue_title_substring";
    public static final String ISSUE_LABEL_SUBSTRING = "issue_label_substring";
    public static final String ISSUE_KEYWORD_SUBSTRING = "issue_keyword_substring";
    public static final String ISSUE_DESCRIPTION_TERMS = "issue_description_terms";

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

    public static List<String> tokenizeNormalized(String value) {
        var normalized = normalizeExact(value);
        if (normalized.isBlank()) {
            return List.of();
        }
        return Arrays.stream(normalized.split("\\s+"))
                .filter(token -> !token.isBlank())
                .toList();
    }

    public static String formatValue(Enum<?> value) {
        return Objects.requireNonNull(value, "value must not be null").name().toLowerCase(Locale.ROOT);
    }
}
