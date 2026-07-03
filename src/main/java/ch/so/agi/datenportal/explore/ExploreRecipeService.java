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
                "select *\nfrom " + tableName(table) + ";",
                Optional.empty());
    }

    ExploreRecipeDto countRecipe(ExploreTableDto table) {
        return recipe(
                table,
                "count",
                "Anzahl Datensätze",
                "Zählt alle Zeilen der Tabelle.",
                ExploreRecipeCategory.PROFILE,
                "select count(*) as anzahl\nfrom " + tableName(table) + ";",
                Optional.empty());
    }

    ExploreRecipeDto describeRecipe(ExploreTableDto table) {
        return recipe(
                table,
                "describe",
                "Tabellenstruktur",
                "Beschreibt die Spalten der Tabelle.",
                ExploreRecipeCategory.PROFILE,
                "describe " + tableName(table) + ";",
                Optional.empty());
    }

    Optional<ExploreRecipeDto> nullProfileRecipe(ExploreTableDto table) {
        if (table.columns().isEmpty()) {
            return Optional.empty();
        }

        StringBuilder sql = new StringBuilder("select\n  count(*) as zeilen");
        table.columns().stream()
                .limit(NULL_PROFILE_COLUMN_LIMIT)
                .forEach(column -> sql.append(",\n  count(*) filter (where ")
                        .append(columnName(column))
                        .append(" is null) as ")
                        .append(sqlNameSanitizer.quoteIdentifier(sqlNameSanitizer.toSafeTableName(column.name()) + "_fehlt")));
        sql.append("\nfrom ").append(tableName(table)).append(";");

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
                        "Nach " + column.name() + " gruppieren",
                        "Zählt Datensätze pro Kategorie.",
                        ExploreRecipeCategory.CATEGORY,
                        "select " + columnName(column) + ", count(*) as anzahl\n"
                                + "from " + tableName(table) + "\n"
                                + "where " + columnName(column) + " is not null\n"
                                + "group by " + columnName(column) + "\n"
                                + "order by anzahl desc\n"
                                + "limit 50;",
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
                        column.name() + " zusammenfassen",
                        "Berechnet Minimum, Durchschnitt und Maximum.",
                        ExploreRecipeCategory.NUMERIC,
                        "select\n"
                                + "  min(" + columnName(column) + ") as minimum,\n"
                                + "  avg(" + columnName(column) + ") as durchschnitt,\n"
                                + "  max(" + columnName(column) + ") as maximum\n"
                                + "from " + tableName(table) + "\n"
                                + "where " + columnName(column) + " is not null;",
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
                        "Zeitreihe nach " + column.name(),
                        "Zählt Datensätze pro Zeitwert.",
                        ExploreRecipeCategory.TIME,
                        "select " + columnName(column) + ", count(*) as anzahl\n"
                                + "from " + tableName(table) + "\n"
                                + "where " + columnName(column) + " is not null\n"
                                + "group by " + columnName(column) + "\n"
                                + "order by " + columnName(column) + ";",
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
        return sqlNameSanitizer.quoteIdentifier(table.name());
    }

    private String columnName(ExploreColumnDto column) {
        return sqlNameSanitizer.quoteIdentifier(column.name());
    }
}
