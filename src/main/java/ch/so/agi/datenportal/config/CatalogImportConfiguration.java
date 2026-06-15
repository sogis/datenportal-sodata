package ch.so.agi.datenportal.config;

import ch.so.agi.datenportal.catalog.domain.CatalogSnapshot;
import ch.so.agi.datenportal.catalog.importxtf.CatalogSource;
import ch.so.agi.datenportal.catalog.importxtf.ClasspathCatalogSource;
import ch.so.agi.datenportal.catalog.importxtf.FileCatalogSource;
import ch.so.agi.datenportal.catalog.importxtf.PublishedCatalogParser;
import ch.so.agi.datenportal.catalog.importxtf.XtfPublishedCatalogParser;
import ch.so.agi.datenportal.catalog.service.CatalogSnapshotLoader;
import java.time.Clock;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;

@Configuration
@EnableConfigurationProperties({CatalogProperties.class, SearchProperties.class})
public class CatalogImportConfiguration {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    CatalogSource catalogSource(CatalogProperties properties, ResourceLoader resourceLoader) {
        if (properties.isClasspathSource()) {
            return new ClasspathCatalogSource(resourceLoader, properties.classpathLocation());
        }
        if (properties.isFileSource()) {
            return new FileCatalogSource(properties.fileLocation());
        }

        throw new IllegalStateException("Unsupported catalog source: " + properties.source());
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
