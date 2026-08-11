package ch.so.agi.datenportal.config;

import static org.assertj.core.api.Assertions.assertThat;

import ch.so.agi.datenportal.catalog.service.CatalogService;
import ch.so.agi.datenportal.catalog.domain.CatalogSnapshot;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "spring.profiles.active=local")
class LocalProfileStartupTest {

    @Autowired
    private CatalogService catalogService;

    @Value("${gg.jte.development-mode}")
    private boolean jteDevelopmentMode;

    @Value("${datenportal.catalog.duckdb.classpath-location}")
    private String duckDbClasspathLocation;

    @Test
    void localProfileStartsWithExplicitFixturesAndDevelopmentJte() {
        assertThat(catalogService.visibleEntries()).hasSize(62);
        String sourceDescription = catalogService.withSnapshot(CatalogSnapshot::sourceDescription);
        assertThat(sourceDescription).isEqualTo("classpath:published_catalog_full_62_entries.xtf");
        assertThat(duckDbClasspathLocation).isEqualTo("catalog.duckdb");
        assertThat(jteDevelopmentMode).isTrue();
    }
}
