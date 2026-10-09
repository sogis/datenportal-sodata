package ch.so.agi.datenportal.config;

import static org.assertj.core.api.Assertions.assertThat;

import ch.so.agi.datenportal.explore.ExploreCatalogDatabaseDto;
import ch.so.agi.datenportal.explore.ExploreChartConfigDto;
import ch.so.agi.datenportal.explore.ExploreChartType;
import ch.so.agi.datenportal.explore.ExploreColumnDto;
import ch.so.agi.datenportal.explore.ExploreColumnRole;
import ch.so.agi.datenportal.explore.ExploreContextDto;
import ch.so.agi.datenportal.explore.ExploreExecutionDto;
import ch.so.agi.datenportal.explore.ExploreMapDto;
import ch.so.agi.datenportal.explore.ExploreRLaboratoryDto;
import ch.so.agi.datenportal.explore.ExploreRecipeCategory;
import ch.so.agi.datenportal.explore.ExploreRecipeDto;
import ch.so.agi.datenportal.explore.ExploreTableDto;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.TypeReference;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;

class CatalogResourceRuntimeHintsTest {

    @Test
    void registersEveryExploreRecordAccessorIncludingComputedJsonProperties() {
        var hints = new RuntimeHints();
        new CatalogResourceRuntimeHints().registerHints(hints, getClass().getClassLoader());

        for (Class<?> type : List.of(
                ExploreContextDto.class, ExploreExecutionDto.class, ExploreCatalogDatabaseDto.class,
                ExploreTableDto.class, ExploreColumnDto.class, ExploreRecipeDto.class,
                ExploreChartConfigDto.class, ExploreRLaboratoryDto.class, ExploreMapDto.class)) {
            for (var component : type.getRecordComponents()) {
                assertThat(RuntimeHintsPredicates.reflection().onMethodInvocation(component.getAccessor()))
                        .as("%s.%s", type.getSimpleName(), component.getName())
                        .accepts(hints);
            }
        }
    }

    @Test
    void registersExploreEnumJsonValueMethods() throws NoSuchMethodException {
        var hints = new RuntimeHints();
        new CatalogResourceRuntimeHints().registerHints(hints, getClass().getClassLoader());

        for (Class<?> type : List.of(ExploreChartType.class, ExploreColumnRole.class, ExploreRecipeCategory.class)) {
            assertThat(RuntimeHintsPredicates.reflection().onMethodInvocation(type.getMethod("value")))
                    .as("%s.value", type.getSimpleName())
                    .accepts(hints);
        }
    }

    @Test
    void registersRuntimeConfiguredCatalogResources() {
        var hints = new RuntimeHints();
        new CatalogResourceRuntimeHints().registerHints(hints, getClass().getClassLoader());

        assertThat(RuntimeHintsPredicates.resource().forResource(
                "published_catalog_full_62_entries.xtf")).accepts(hints);
        assertThat(RuntimeHintsPredicates.resource().forResource("catalog.duckdb")).accepts(hints);
        assertThat(RuntimeHintsPredicates.resource().forResource("explore_fixture_catalog.duckdb")).accepts(hints);
        assertThat(RuntimeHintsPredicates.resource().forResource(
                "gg/jte/generated/precompiled/pages/JtecatalogGenerated.bin")).accepts(hints);
        assertThat(RuntimeHintsPredicates.reflection().onType(TypeReference.of(
                "org.apache.lucene.index.ConcurrentMergeScheduler"))).accepts(hints);

        var jteTemplateHints = hints.reflection().getTypeHint(TypeReference.of(
                "gg.jte.generated.precompiled.pages.JtecatalogGenerated"));
        assertThat(jteTemplateHints).isNotNull();
        assertThat(jteTemplateHints.getMemberCategories())
                .contains(
                        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                        MemberCategory.INVOKE_PUBLIC_METHODS,
                        MemberCategory.ACCESS_PUBLIC_FIELDS);
    }
}
