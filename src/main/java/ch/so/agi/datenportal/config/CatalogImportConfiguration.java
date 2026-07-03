package ch.so.agi.datenportal.config;

import ch.so.agi.datenportal.catalog.domain.CatalogSnapshot;
import ch.so.agi.datenportal.catalog.importxtf.CatalogSource;
import ch.so.agi.datenportal.catalog.importxtf.CatalogDownloadUrlPlaceholderResolver;
import ch.so.agi.datenportal.catalog.importxtf.ClasspathCatalogSource;
import ch.so.agi.datenportal.catalog.importxtf.DownloadUrlPlaceholderCatalogSource;
import ch.so.agi.datenportal.catalog.importxtf.FileCatalogSource;
import ch.so.agi.datenportal.catalog.importxtf.HttpCatalogSource;
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
    CatalogSource catalogSource(CatalogProperties properties, ResourceLoader resourceLoader, Clock clock) {
        CatalogSource source;
        if (properties.isClasspathSource()) {
            source = new ClasspathCatalogSource(resourceLoader, properties.classpathLocation(), properties.maxSize(), clock);
        } else if (properties.isFileSource()) {
            source = new FileCatalogSource(properties.fileLocation(), properties.maxSize(), clock);
        } else if (properties.isHttpSource()) {
            source = new HttpCatalogSource(
                    properties.httpUrl(),
                    properties.httpConnectTimeout(),
                    properties.httpReadTimeout(),
                    properties.maxSize(),
                    clock);
        } else {
            throw new IllegalStateException("Unsupported catalog source: " + properties.effectiveSourceType());
        }
        return new DownloadUrlPlaceholderCatalogSource(
                source,
                new CatalogDownloadUrlPlaceholderResolver(),
                properties.downloadUrl());
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
