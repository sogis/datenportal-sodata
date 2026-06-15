package ch.so.agi.datenportal;

import static org.assertj.core.api.Assertions.assertThat;

import ch.so.agi.datenportal.catalog.service.CatalogService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DatenportalApplicationTests {

    @Autowired
    private CatalogService catalogService;

    @Test
    void contextLoadsAndStartupCatalogIsAvailable() {
        assertThat(catalogService.currentSnapshot().sourceDescription())
                .isEqualTo("classpath:published_catalog_full_54_entries.xtf");
        assertThat(catalogService.visibleEntries()).hasSize(54);
        assertThat(catalogService.findVisibleEntry("ch.so.bauinventar")).isPresent();
        assertThat(catalogService.findAnyEntry("ch.so.abstimmungsresultate_2026")).isPresent();
    }
}
