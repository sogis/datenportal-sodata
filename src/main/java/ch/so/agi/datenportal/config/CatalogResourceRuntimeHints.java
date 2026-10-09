package ch.so.agi.datenportal.config;

import ch.so.agi.datenportal.explore.ExploreChartType;
import ch.so.agi.datenportal.explore.ExploreColumnRole;
import ch.so.agi.datenportal.explore.ExploreContextDto;
import ch.so.agi.datenportal.explore.ExploreMapDto;
import ch.so.agi.datenportal.explore.ExploreRecipeCategory;
import java.io.IOException;
import java.util.List;

import org.springframework.aot.hint.BindingReflectionHintsRegistrar;
import org.springframework.aot.hint.ExecutableMode;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;

final class CatalogResourceRuntimeHints implements RuntimeHintsRegistrar {

    // Lucene's IndexWriter initializes TestSecrets, which checks these types by name.
    private static final String[] LUCENE_TEST_SECRET_TYPES = {
            "org.apache.lucene.index.ConcurrentMergeScheduler",
            "org.apache.lucene.index.SegmentReader",
            "org.apache.lucene.index.IndexWriter",
            "org.apache.lucene.store.FilterIndexInput"
    };

    private static final String[] CATALOG_RESOURCES = {
            "published_catalog_examples_only.xtf",
            "published_catalog_full_54_entries.xtf",
            "published_catalog_full_62_entries.xtf",
            "catalog.duckdb",
            "explore_fixture_catalog.duckdb"
    };

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        // Explore serializes DTOs manually to a String, so MVC AOT cannot infer
        // the binding types. Include nested records and generic Optional/List types.
        // Computed @JsonProperty return types are not record components and need
        // explicit registration so all their record accessors survive native AOT.
        new BindingReflectionHintsRegistrar().registerReflectionHints(
                hints.reflection(), ExploreContextDto.class, ExploreMapDto.class);
        registerExploreEnumValues(hints);
        for (String resourceName : CATALOG_RESOURCES) {
            hints.resources().registerPattern(resourceName);
        }
        hints.resources().registerPattern("gg/jte/generated/precompiled/**/*.bin");
        registerPrecompiledJteTemplates(hints, classLoader);
        for (String typeName : LUCENE_TEST_SECRET_TYPES) {
            hints.reflection().registerTypeIfPresent(classLoader, typeName);
        }
    }

    private static void registerExploreEnumValues(RuntimeHints hints) {
        for (Class<?> type : List.of(ExploreChartType.class, ExploreColumnRole.class, ExploreRecipeCategory.class)) {
            try {
                // Preserve the @JsonValue wire values instead of Enum.name().
                hints.reflection().registerMethod(type.getMethod("value"), ExecutableMode.INVOKE);
            } catch (NoSuchMethodException exception) {
                throw new IllegalStateException("Missing Explore enum JSON value method: " + type.getName(), exception);
            }
        }
    }

    private static void registerPrecompiledJteTemplates(RuntimeHints hints, ClassLoader classLoader) {
        var resources = new PathMatchingResourcePatternResolver(classLoader);
        var metadataReaderFactory = new CachingMetadataReaderFactory(classLoader);
        try {
            for (Resource resource : resources.getResources(
                    "classpath*:gg/jte/generated/precompiled/**/*.class")) {
                String className = metadataReaderFactory.getMetadataReader(resource)
                        .getClassMetadata().getClassName();
                hints.reflection().registerTypeIfPresent(
                        classLoader,
                        className,
                        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                        MemberCategory.INVOKE_PUBLIC_METHODS,
                        MemberCategory.ACCESS_PUBLIC_FIELDS);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to register precompiled JTE templates for native image", exception);
        }
    }
}
