package ch.so.agi.datenportal.explore;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ExploreRecipeServiceTest {

    private final ExploreRecipeService service = new ExploreRecipeService(
            new ExploreSqlNameSanitizer(),
            properties());

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
    void generatedSqlUsesQuotedIdentifiers() {
        var categoryRecipe = service.generateRecipes(List.of(table())).stream()
                .filter(recipe -> recipe.id().equals("gemeinden-category-bezirk"))
                .findFirst()
                .orElseThrow();

        assertThat(categoryRecipe.sql())
                .contains("from \"gemeinden\"")
                .contains("select \"bezirk\", count(*) as anzahl")
                .contains("group by \"bezirk\"");
        assertThat(categoryRecipe.preferredChart())
                .get()
                .extracting(ExploreChartConfigDto::type)
                .isEqualTo(ExploreChartType.BAR);
    }

    @Test
    void previewRecipeUsesRegisteredViewWithoutVisibleLimit() {
        var previewRecipe = service.previewRecipe(table());

        assertThat(previewRecipe.sql())
                .isEqualTo("select *\nfrom \"gemeinden\";");
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

    private static ExploreProperties properties() {
        return new ExploreProperties(true, 100, 10_000, 30_000, true, true, false, false, false, false, false);
    }
}
