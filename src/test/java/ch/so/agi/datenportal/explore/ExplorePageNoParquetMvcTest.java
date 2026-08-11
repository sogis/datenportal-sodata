package ch.so.agi.datenportal.explore;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.so.agi.datenportal.catalog.CatalogTestArtifacts;
import ch.so.agi.datenportal.catalog.domain.AccessLevel;
import ch.so.agi.datenportal.catalog.domain.Catalog;
import ch.so.agi.datenportal.catalog.domain.CatalogEntryMetadata;
import ch.so.agi.datenportal.catalog.domain.CatalogSnapshot;
import ch.so.agi.datenportal.catalog.domain.DatasetEntry;
import ch.so.agi.datenportal.catalog.domain.DistributionFormat;
import ch.so.agi.datenportal.catalog.domain.DistributionLink;
import ch.so.agi.datenportal.catalog.domain.Office;
import ch.so.agi.datenportal.catalog.domain.Theme;
import ch.so.agi.datenportal.DatenportalApplication;
import java.net.URI;
import java.time.Instant;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

@SpringBootTest(classes = {DatenportalApplication.class, ExplorePageNoParquetMvcTest.NoParquetCatalogConfiguration.class})
@AutoConfigureMockMvc
class ExplorePageNoParquetMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void datasetWithoutParquetRendersUnavailableState() throws Exception {
        mockMvc.perform(get("/datasets/csv-only/explore"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Erkunden nicht verfügbar")))
                .andExpect(content().string(containsString("keine Parquet-Datei publiziert ist")))
                .andExpect(content().string(containsString("Downloads und Metadaten auf der Datensatzseite anzeigen")));

        mockMvc.perform(get("/datasets/csv-only/explore/context.json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datasetId").value("csv-only"))
                .andExpect(jsonPath("$.tables").isEmpty())
                .andExpect(jsonPath("$.recipes").isEmpty())
                .andExpect(jsonPath("$.chartsEnabled").value(true))
                .andExpect(jsonPath("$.webREnabled").value(true));
    }

    @TestConfiguration
    static class NoParquetCatalogConfiguration {

        @Bean
        @Primary
        CatalogSnapshot noParquetCatalogSnapshot() {
            return CatalogSnapshot.of(
                    new Catalog(List.of(csvOnlyDataset()), List.of()),
                    Instant.parse("2026-07-01T08:00:00Z"),
                    Duration.ZERO,
                    CatalogTestArtifacts.published("no-parquet-test"),
                    CatalogTestArtifacts.duckDb("no-parquet-test-duckdb"),
                    ch.so.agi.datenportal.search.CatalogSearchIndex.empty());
        }

        private static DatasetEntry csvOnlyDataset() {
            var office = new Office("agi", "Amt für Geoinformation", Optional.of("AGI"));
            var theme = new Theme("raum", "Raum und Umwelt");
            return new DatasetEntry(
                    "csv-only",
                    "CSV only",
                    "Datensatz ohne Parquet.",
                    office,
                    office,
                    List.of(theme),
                    List.of("CSV"),
                    LocalDate.parse("2026-06-30"),
                    AccessLevel.OPEN,
                    CatalogEntryMetadata.empty(),
                    List.of(new DistributionLink(
                            URI.create("https://data.so.ch/dataset/csv-only"),
                            URI.create("https://data.so.ch/download/csv-only.csv"),
                            DistributionFormat.CSV)));
        }
    }
}
