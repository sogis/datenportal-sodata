package ch.so.agi.datenportal.explore;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public final class ExploreRecipeService {

    private static final int NULL_PROFILE_COLUMN_LIMIT = 8;
    private static final int CATEGORY_RECIPE_LIMIT = 3;
    private static final int NUMERIC_RECIPE_LIMIT = 3;
    private static final int TIME_RECIPE_LIMIT = 2;

    private final ExploreSqlNameSanitizer sqlNameSanitizer;
    private final ExploreProperties properties;

    public ExploreRecipeService(ExploreSqlNameSanitizer sqlNameSanitizer, ExploreProperties properties) {
        this.sqlNameSanitizer = sqlNameSanitizer;
        this.properties = properties;
    }

    public List<ExploreRecipeDto> generateRecipes(List<ExploreTableDto> tables) {
        var recipes = new ArrayList<ExploreRecipeDto>();
        for (ExploreTableDto table : tables) {
            recipes.add(previewRecipe(table));
            recipes.add(countRecipe(table));
            recipes.add(describeRecipe(table));
            nullProfileRecipe(table).ifPresent(recipes::add);
            recipes.addAll(categoryRecipes(table));
            recipes.addAll(numericRecipes(table));
            recipes.addAll(timeRecipes(table));
        }
        return List.copyOf(recipes);
    }

    ExploreRecipeDto previewRecipe(ExploreTableDto table) {
        return recipe(
                table,
                "preview",
                "Vorschau",
                "Zeigt die ersten Zeilen der Tabelle.",
                ExploreRecipeCategory.PREVIEW,
                "SELECT *\nFROM " + tableName(table) + ";",
                Optional.empty());
    }

    ExploreRecipeDto countRecipe(ExploreTableDto table) {
        return recipe(
                table,
                "count",
                "Anzahl Datensätze",
                "Zählt alle Zeilen der Tabelle.",
                ExploreRecipeCategory.PROFILE,
                "SELECT count(*) AS anzahl\nFROM " + tableName(table) + ";",
                Optional.empty());
    }

    ExploreRecipeDto describeRecipe(ExploreTableDto table) {
        return recipe(
                table,
                "describe",
                "Tabellenstruktur",
                "Beschreibt die Spalten der Tabelle.",
                ExploreRecipeCategory.PROFILE,
                "DESCRIBE " + tableName(table) + ";",
                Optional.empty());
    }

    Optional<ExploreRecipeDto> nullProfileRecipe(ExploreTableDto table) {
        if (table.columns().isEmpty()) {
            return Optional.empty();
        }

        StringBuilder sql = new StringBuilder("SELECT\n  count(*) AS zeilen");
        table.columns().stream()
                .limit(NULL_PROFILE_COLUMN_LIMIT)
                .forEach(column -> sql.append(",\n  count(*) FILTER (WHERE ")
                        .append(columnName(column))
                        .append(" IS NULL) AS ")
                        .append(sqlNameSanitizer.quoteIdentifier(sqlNameSanitizer.toSafeTableName(column.name()) + "_fehlt")));
        sql.append("\nFROM ").append(tableName(table)).append(";");

        return Optional.of(recipe(
                table,
                "null-profile",
                "Fehlende Werte",
                "Zählt fehlende Werte für die beschriebenen Spalten.",
                ExploreRecipeCategory.QUALITY,
                sql.toString(),
                Optional.empty()));
    }

    List<ExploreRecipeDto> categoryRecipes(ExploreTableDto table) {
        return table.columns().stream()
                .filter(column -> column.roles().contains(ExploreColumnRole.CATEGORY))
                .limit(CATEGORY_RECIPE_LIMIT)
                .map(column -> recipe(
                        table,
                        "category-" + sqlNameSanitizer.toSafeTableName(column.name()),
                        "Nach " + highlightedColumnName(column) + " gruppieren",
                        "Zählt Datensätze pro Kategorie.",
                        ExploreRecipeCategory.CATEGORY,
                        "SELECT " + columnName(column) + ", count(*) AS anzahl\n"
                                + "FROM " + tableName(table) + "\n"
                                + "WHERE " + columnName(column) + " IS NOT NULL\n"
                                + "GROUP BY " + columnName(column) + "\n"
                                + "ORDER BY anzahl DESC\n"
                                + "LIMIT 50;",
                        Optional.of(new ExploreChartConfigDto(
                                ExploreChartType.BAR,
                                Optional.of(column.name()),
                                Optional.of("anzahl"),
                                Optional.empty(),
                                Optional.of("Anzahl nach " + column.name())))))
                .toList();
    }

    List<ExploreRecipeDto> numericRecipes(ExploreTableDto table) {
        return table.columns().stream()
                .filter(column -> column.roles().contains(ExploreColumnRole.MEASURE))
                .limit(NUMERIC_RECIPE_LIMIT)
                .map(column -> recipe(
                        table,
                        "numeric-" + sqlNameSanitizer.toSafeTableName(column.name()),
                        highlightedColumnName(column) + " zusammenfassen",
                        "Berechnet Minimum, Durchschnitt und Maximum.",
                        ExploreRecipeCategory.NUMERIC,
                        "SELECT\n"
                                + "  min(" + columnName(column) + ") AS minimum,\n"
                                + "  avg(" + columnName(column) + ") AS durchschnitt,\n"
                                + "  max(" + columnName(column) + ") AS maximum\n"
                                + "FROM " + tableName(table) + "\n"
                                + "WHERE " + columnName(column) + " IS NOT NULL;",
                        Optional.empty()))
                .toList();
    }

    List<ExploreRecipeDto> timeRecipes(ExploreTableDto table) {
        return table.columns().stream()
                .filter(column -> column.roles().contains(ExploreColumnRole.DATE)
                        || column.roles().contains(ExploreColumnRole.YEAR))
                .limit(TIME_RECIPE_LIMIT)
                .map(column -> recipe(
                        table,
                        "time-" + sqlNameSanitizer.toSafeTableName(column.name()),
                        "Zeitreihe nach " + highlightedColumnName(column),
                        "Zählt Datensätze pro Zeitwert.",
                        ExploreRecipeCategory.TIME,
                        "SELECT " + columnName(column) + ", count(*) AS anzahl\n"
                                + "FROM " + tableName(table) + "\n"
                                + "WHERE " + columnName(column) + " IS NOT NULL\n"
                                + "GROUP BY " + columnName(column) + "\n"
                                + "ORDER BY " + columnName(column) + ";",
                        Optional.of(new ExploreChartConfigDto(
                                ExploreChartType.LINE,
                                Optional.of(column.name()),
                                Optional.of("anzahl"),
                                Optional.empty(),
                                Optional.of("Anzahl nach " + column.name())))))
                .toList();
    }

    private ExploreRecipeDto recipe(
            ExploreTableDto table,
            String suffix,
            String title,
            String description,
            ExploreRecipeCategory category,
            String sql,
            Optional<ExploreChartConfigDto> preferredChart) {
        return new ExploreRecipeDto(
                table.id() + "-" + suffix,
                title,
                description,
                table.id(),
                category,
                sql,
                preferredChart);
    }

    private String tableName(ExploreTableDto table) {
        sqlNameSanitizer.assertSafeTableName(table.name());
        return table.name();
    }

    private String columnName(ExploreColumnDto column) {
        return sqlNameSanitizer.quoteIdentifier(column.name());
    }

    private static String highlightedColumnName(ExploreColumnDto column) {
        return "«" + column.name() + "»";
    }
}
