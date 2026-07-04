package ch.so.agi.datenportal.explore;

import static org.assertj.core.api.Assertions.assertThat;

import ch.so.agi.datenportal.config.CatalogDuckDbProperties;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ExploreRecipeServiceTest {

    private static final List<String> LOWERCASE_KEYWORD_PATTERNS = List.of(
            "\\bselect\\b",
            "\\bfrom\\b",
            "\\bdescribe\\b",
            "\\bas\\b",
            "\\bfilter\\b",
            "\\bwhere\\b",
            "\\bis\\b",
            "\\bnull\\b",
            "\\bgroup\\s+by\\b",
            "\\border\\s+by\\b",
            "\\bdesc\\b",
            "\\blimit\\b");

    private final ExploreRecipeService service = new ExploreRecipeService(
            new ExploreSqlNameSanitizer(),
            properties(),
            duckDbProperties());

    @Test
    void generatesBaselineAndColumnDrivenRecipes() {
        var recipes = service.generateRecipes(List.of(table()));

        assertThat(recipes)
                .extracting(ExploreRecipeDto::category)
                .contains(
                        ExploreRecipeCategory.PREVIEW,
                        ExploreRecipeCategory.PROFILE,
                        ExploreRecipeCategory.QUALITY,
                        ExploreRecipeCategory.CATEGORY,
                        ExploreRecipeCategory.NUMERIC,
                        ExploreRecipeCategory.TIME);
        assertThat(recipes)
                .extracting(ExploreRecipeDto::id)
                .contains(
                        "gemeinden-preview",
                        "gemeinden-count",
                        "gemeinden-describe",
                        "gemeinden-null-profile",
                        "gemeinden-category-bezirk",
                        "gemeinden-numeric-flaeche_ha",
                        "gemeinden-time-jahr");
    }

    @Test
    void generatedSqlUsesSafeTableNamesAndQuotedColumnIdentifiers() {
        var categoryRecipe = service.generateRecipes(List.of(table())).stream()
                .filter(recipe -> recipe.id().equals("gemeinden-category-bezirk"))
                .findFirst()
                .orElseThrow();

        assertThat(categoryRecipe.sql())
                .contains("FROM opendata.gemeinden")
                .contains("SELECT \"bezirk\", count(*) AS anzahl")
                .contains("GROUP BY \"bezirk\"");
        assertThat(categoryRecipe.preferredChart())
                .get()
                .extracting(ExploreChartConfigDto::type)
                .isEqualTo(ExploreChartType.BAR);
    }

    @Test
    void generatedSqlWritesKeywordsUppercase() {
        var recipes = service.generateRecipes(List.of(table()));

        assertThat(recipes)
                .extracting(ExploreRecipeDto::sql)
                .allSatisfy(sql -> LOWERCASE_KEYWORD_PATTERNS.forEach(pattern ->
                        assertThat(sql).doesNotContainPattern(pattern)));
    }

    @Test
    void generatedSqlUsesUppercaseKeywordsInAllRecipeTypes() {
        var recipes = service.generateRecipes(List.of(table()));

        assertThat(sqlFor(recipes, "gemeinden-count"))
                .isEqualTo("SELECT count(*) AS anzahl\nFROM opendata.gemeinden;");
        assertThat(sqlFor(recipes, "gemeinden-describe"))
                .isEqualTo("DESCRIBE opendata.gemeinden;");
        assertThat(sqlFor(recipes, "gemeinden-null-profile"))
                .isEqualTo("""
                        SELECT
                          count(*) AS zeilen,
                          count(*) FILTER (WHERE "bezirk" IS NULL) AS "bezirk_fehlt",
                          count(*) FILTER (WHERE "flaeche_ha" IS NULL) AS "flaeche_ha_fehlt",
                          count(*) FILTER (WHERE "jahr" IS NULL) AS "jahr_fehlt"
                        FROM opendata.gemeinden;""");
        assertThat(sqlFor(recipes, "gemeinden-category-bezirk"))
                .isEqualTo("""
                        SELECT "bezirk", count(*) AS anzahl
                        FROM opendata.gemeinden
                        WHERE "bezirk" IS NOT NULL
                        GROUP BY "bezirk"
                        ORDER BY anzahl DESC
                        LIMIT 50;""");
        assertThat(sqlFor(recipes, "gemeinden-numeric-flaeche_ha"))
                .isEqualTo("""
                        SELECT
                          min("flaeche_ha") AS minimum,
                          avg("flaeche_ha") AS durchschnitt,
                          max("flaeche_ha") AS maximum
                        FROM opendata.gemeinden
                        WHERE "flaeche_ha" IS NOT NULL;""");
        assertThat(sqlFor(recipes, "gemeinden-time-jahr"))
                .isEqualTo("""
                        SELECT "jahr", count(*) AS anzahl
                        FROM opendata.gemeinden
                        WHERE "jahr" IS NOT NULL
                        GROUP BY "jahr"
                        ORDER BY "jahr";""");
    }

    @Test
    void generatedRecipeTitlesHighlightColumnNames() {
        var recipes = service.generateRecipes(List.of(table()));

        assertThat(titleFor(recipes, "gemeinden-category-bezirk"))
                .isEqualTo("Nach «bezirk» gruppieren");
        assertThat(titleFor(recipes, "gemeinden-numeric-flaeche_ha"))
                .isEqualTo("«flaeche_ha» zusammenfassen");
        assertThat(titleFor(recipes, "gemeinden-time-jahr"))
                .isEqualTo("Zeitreihe nach «jahr»");
    }

    @Test
    void previewRecipeUsesRegisteredViewWithoutVisibleLimit() {
        var previewRecipe = service.previewRecipe(table());

        assertThat(previewRecipe.sql())
                .isEqualTo("SELECT *\nFROM opendata.gemeinden;");
    }

    private static ExploreTableDto table() {
        return new ExploreTableDto(
                "gemeinden",
                "gemeinden",
                "Gemeinden",
                Optional.of("Beschreibung"),
                "https://data.so.ch/download/gemeinden.parquet",
                Optional.empty(),
                Optional.empty(),
                true,
                List.of(
                        column("bezirk", "VARCHAR", ExploreColumnRole.CATEGORY),
                        column("flaeche_ha", "DOUBLE", ExploreColumnRole.MEASURE),
                        column("jahr", "INTEGER", ExploreColumnRole.YEAR)));
    }

    private static ExploreColumnDto column(String name, String type, ExploreColumnRole role) {
        return new ExploreColumnDto(
                name,
                type,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                List.of(role));
    }

    private static String sqlFor(List<ExploreRecipeDto> recipes, String id) {
        return recipes.stream()
                .filter(recipe -> recipe.id().equals(id))
                .findFirst()
                .orElseThrow()
                .sql();
    }

    private static String titleFor(List<ExploreRecipeDto> recipes, String id) {
        return recipes.stream()
                .filter(recipe -> recipe.id().equals(id))
                .findFirst()
                .orElseThrow()
                .title();
    }

    private static ExploreProperties properties() {
        return new ExploreProperties(true, 100, 10_000, 30_000, true, true, false, false, false, false, false);
    }

    private static CatalogDuckDbProperties duckDbProperties() {
        return new CatalogDuckDbProperties(null, null, null, null, null, null, null, null);
    }
}
