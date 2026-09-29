package ch.so.agi.datenportal.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.TypeReference;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;

class CatalogResourceRuntimeHintsTest {

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
