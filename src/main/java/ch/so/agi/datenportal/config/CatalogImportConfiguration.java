package ch.so.agi.datenportal.config;

import ch.so.agi.datenportal.catalog.domain.CatalogSnapshot;
import ch.so.agi.datenportal.catalog.importxtf.CatalogSource;
import ch.so.agi.datenportal.catalog.importxtf.CatalogInputs;
import ch.so.agi.datenportal.catalog.importxtf.CatalogInputsSource;
import ch.so.agi.datenportal.catalog.importxtf.CatalogDownloadUrlPlaceholderResolver;
import ch.so.agi.datenportal.catalog.importxtf.ClasspathCatalogSource;
import ch.so.agi.datenportal.catalog.importxtf.DownloadUrlPlaceholderCatalogSource;
import ch.so.agi.datenportal.catalog.importxtf.FileCatalogSource;
import ch.so.agi.datenportal.catalog.importxtf.HttpCatalogSource;
import ch.so.agi.datenportal.catalog.importxtf.ManifestCatalogSource;
import ch.so.agi.datenportal.catalog.importxtf.PublishedCatalogParser;
import ch.so.agi.datenportal.catalog.importxtf.XtfPublishedCatalogParser;
import ch.so.agi.datenportal.catalog.service.CatalogSnapshotLoader;
import ch.so.agi.datenportal.explore.ExploreProperties;
import java.time.Clock;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;

@Configuration
@EnableConfigurationProperties({
        AdminProperties.class,
        CatalogDuckDbProperties.class,
        CatalogProperties.class,
        ExploreProperties.class,
        SearchProperties.class,
        SecurityCspProperties.class,
        WebComponentsProperties.class})
public class CatalogImportConfiguration {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    CatalogInputsSource catalogInputsSource(CatalogProperties properties, CatalogDuckDbProperties duckDb,
            ResourceLoader resourceLoader, Clock clock) {
        if (duckDb.sourceType() == CatalogProperties.SourceType.MANIFEST) {
            if (properties.sourceType() != CatalogProperties.SourceType.MANIFEST) {
                throw new IllegalArgumentException("DuckDB manifest mode requires datenportal.catalog.source-type=manifest.");
            }
            var source = new ManifestCatalogSource(properties.httpUrl(), properties.httpConnectTimeout(),
                    properties.httpReadTimeout(), properties.maxSize(), clock);
            var resolver = new CatalogDownloadUrlPlaceholderResolver();
            return () -> {
                var inputs = source.loadInputs(duckDb.httpConnectTimeout(), duckDb.httpReadTimeout(), duckDb.maxSize());
                return new CatalogInputs(inputs.publishedCatalog().absent() ? inputs.publishedCatalog()
                        : resolver.resolve(inputs.publishedCatalog(), properties.downloadUrl()), inputs.duckDbCatalog());
            };
        }
        return CatalogInputsSource.independent(catalogSource(properties, resourceLoader, clock),
                catalogDuckDbSource(duckDb, resourceLoader, clock));
    }

    CatalogSource catalogSource(CatalogProperties properties, ResourceLoader resourceLoader, Clock clock) {
        CatalogSource source = switch (properties.sourceType()) {
            case CLASSPATH -> new ClasspathCatalogSource(
                    resourceLoader, properties.classpathLocation(), properties.maxSize(), clock);
            case FILE -> new FileCatalogSource(properties.fileLocation(), properties.maxSize(), clock);
            case MANIFEST -> new ManifestCatalogSource(properties.httpUrl(), properties.httpConnectTimeout(),
                    properties.httpReadTimeout(), properties.maxSize(), clock);
            case HTTP -> new HttpCatalogSource(
                    properties.httpUrl(),
                    properties.httpConnectTimeout(),
                    properties.httpReadTimeout(),
                    properties.maxSize(),
                    clock);
        };
        return new DownloadUrlPlaceholderCatalogSource(
                source,
                new CatalogDownloadUrlPlaceholderResolver(),
                properties.downloadUrl());
    }

    CatalogSource catalogDuckDbSource(CatalogDuckDbProperties properties, ResourceLoader resourceLoader, Clock clock) {
        return switch (properties.sourceType()) {
            case CLASSPATH -> new ClasspathCatalogSource(
                    resourceLoader, properties.classpathLocation(), properties.maxSize(), clock);
            case FILE -> new FileCatalogSource(properties.fileLocation(), properties.maxSize(), clock);
            case MANIFEST -> throw new IllegalArgumentException("Manifest sources do not configure catalog.duckdb.");
            case HTTP -> new HttpCatalogSource(
                    properties.httpUrl(),
                    properties.httpConnectTimeout(),
                    properties.httpReadTimeout(),
                    properties.maxSize(),
                    clock);
        };
    }

    @Bean
    PublishedCatalogParser publishedCatalogParser() {
        return new XtfPublishedCatalogParser();
    }

    @Bean
    CatalogSnapshot initialCatalogSnapshot(CatalogSnapshotLoader loader) {
        return loader.load();
    }
}
