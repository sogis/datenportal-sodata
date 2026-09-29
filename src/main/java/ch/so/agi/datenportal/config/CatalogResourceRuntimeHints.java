package ch.so.agi.datenportal.config;

import java.io.IOException;

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
        for (String resourceName : CATALOG_RESOURCES) {
            hints.resources().registerPattern(resourceName);
        }
        hints.resources().registerPattern("gg/jte/generated/precompiled/**/*.bin");
        registerPrecompiledJteTemplates(hints, classLoader);
        for (String typeName : LUCENE_TEST_SECRET_TYPES) {
            hints.reflection().registerTypeIfPresent(classLoader, typeName);
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
