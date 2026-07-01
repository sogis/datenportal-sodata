package ch.so.agi.datenportal.explore;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public final class ExploreSqlNameSanitizer {

    private static final Pattern SAFE_TABLE_NAME = Pattern.compile("[a-z_][a-z0-9_]*");
    private static final Set<String> RESERVED_KEYWORDS = Set.of(
            "all",
            "alter",
            "and",
            "as",
            "by",
            "create",
            "delete",
            "describe",
            "drop",
            "from",
            "group",
            "insert",
            "into",
            "limit",
            "order",
            "select",
            "table",
            "update",
            "where",
            "with");

    public String toSafeTableName(String rawName) {
        String value = rawName == null || rawName.isBlank() ? "table" : rawName;
        value = value.toLowerCase(Locale.ROOT)
                .replace("ä", "ae")
                .replace("ö", "oe")
                .replace("ü", "ue")
                .replace("ß", "ss");
        value = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^a-z0-9_]", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_+|_+$", "");
        if (value.isBlank()) {
            value = "table";
        }
        if (Character.isDigit(value.charAt(0))) {
            value = "t_" + value;
        }
        if (RESERVED_KEYWORDS.contains(value)) {
            value = value + "_table";
        }
        assertSafeTableName(value);
        return value;
    }

    public String quoteIdentifier(String identifier) {
        return "\"" + identifier.replace("\"", "\"\"") + "\"";
    }

    public void assertSafeTableName(String tableName) {
        if (tableName == null || !SAFE_TABLE_NAME.matcher(tableName).matches()) {
            throw new IllegalArgumentException("Unsafe SQL table name: " + tableName);
        }
    }
}
