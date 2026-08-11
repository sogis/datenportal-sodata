package ch.so.agi.datenportal.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

class ConfigurationStartupTest {

    @Test
    void missingCatalogSourceTypePreventsStartupInsteadOfUsingFixtureDefaults() {
        catalogRunner()
                .withUserConfiguration(CatalogPropertiesConfiguration.class)
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasRootCauseMessage("datenportal.catalog.source-type must be set");
                });
    }

    @Test
    void missingCatalogLocationPreventsStartup() {
        catalogRunner()
                .withUserConfiguration(CatalogPropertiesConfiguration.class)
                .withPropertyValues("datenportal.catalog.source-type=classpath")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasRootCauseMessage("datenportal.catalog.classpath-location must be set for classpath catalog sources");
                });
    }

    @Test
    void invalidCatalogHttpSchemePreventsStartup() {
        catalogRunner()
                .withUserConfiguration(CatalogPropertiesConfiguration.class)
                .withPropertyValues(
                        "datenportal.catalog.source-type=http",
                        "datenportal.catalog.http-url=ftp://example.com/catalog.xtf")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasRootCauseMessage("datenportal.catalog.http-url must use http or https");
                });
    }

    @Test
    void missingDuckDbSourceTypePreventsStartup() {
        duckDbRunner()
                .withUserConfiguration(CatalogDuckDbPropertiesConfiguration.class)
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasRootCauseMessage("datenportal.catalog.duckdb.source-type must be set");
                });
    }

    @Test
    void missingDuckDbLocationPreventsStartup() {
        duckDbRunner()
                .withUserConfiguration(CatalogDuckDbPropertiesConfiguration.class)
                .withPropertyValues("datenportal.catalog.duckdb.source-type=classpath")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasRootCauseMessage("datenportal.catalog.duckdb.classpath-location must be set for classpath sources");
                });
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(CatalogProperties.class)
    static class CatalogPropertiesConfiguration {}

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(CatalogDuckDbProperties.class)
    static class CatalogDuckDbPropertiesConfiguration {}

    private static ApplicationContextRunner catalogRunner() {
        return new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(ConfigurationPropertiesAutoConfiguration.class))
                .withUserConfiguration(CatalogPropertiesConfiguration.class);
    }

    private static ApplicationContextRunner duckDbRunner() {
        return new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(ConfigurationPropertiesAutoConfiguration.class))
                .withUserConfiguration(CatalogDuckDbPropertiesConfiguration.class);
    }
}
