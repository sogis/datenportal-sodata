package ch.so.agi.datenportal.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.so.agi.datenportal.DatenportalApplication;
import ch.so.agi.datenportal.catalog.CatalogTestArtifacts;
import ch.so.agi.datenportal.catalog.domain.AccessLevel;
import ch.so.agi.datenportal.catalog.domain.Catalog;
import ch.so.agi.datenportal.catalog.domain.CatalogEntryMetadata;
import ch.so.agi.datenportal.catalog.domain.CatalogSnapshot;
import ch.so.agi.datenportal.catalog.domain.DatasetAttribute;
import ch.so.agi.datenportal.catalog.domain.DatasetEntry;
import ch.so.agi.datenportal.catalog.domain.DatasetIssueEntry;
import ch.so.agi.datenportal.catalog.domain.DatasetSeriesEntry;
import ch.so.agi.datenportal.catalog.domain.Office;
import ch.so.agi.datenportal.search.CatalogDocumentMapper;
import ch.so.agi.datenportal.search.CatalogSearchIndexBuilder;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = {DatenportalApplication.class, MetadataRenderingMvcTest.FixtureConfiguration.class})
@AutoConfigureMockMvc
class MetadataRenderingMvcTest {
    private static final String DESCRIPTION = "Das Attribut `gemeinde_nr`.\nWert **2401**.\n\n- *Hinweis*\n- Weiterer Punkt\n\n<script>alert('x')</script>";
    private static final Office OFFICE = new Office("test", "**Amt**", Optional.empty());
    private static final LocalDate DATE = LocalDate.of(2026, 10, 6);

    @Autowired
    private MockMvc mockMvc;

    @Test
    void detailPagesRenderBlocksSafelyAndLeaveTitlesLiteral() throws Exception {
        String html = page("/datasets/markdown");
        assertThat(html)
                .contains("<span>**Titel**</span>")
                .contains("<div class=\"dp-detail-description dp-metadata-text\"><p>Das Attribut <code>gemeinde_nr</code>.<br>Wert <strong>2401</strong>.</p>")
                .contains("<ul><li><p><em>Hinweis</em></p></li><li><p>Weiterer Punkt</p></li></ul>")
                .contains("&lt;script&gt;alert(&#39;x&#39;)&lt;/script&gt;")
                .contains("**Amt**")
                .doesNotContain("<script>alert", "<p class=\"dp-detail-description\">");
    }

    @Test
    void structurePagesFormatOnlyAttributeDescriptionsAndTheThreeProseFields() throws Exception {
        String html = page("/datasets/markdown/structure-quality-origin");
        assertThat(html)
                .contains("<th scope=\"row\">**nr**</th>")
                .contains("<td>`TEXT`</td>")
                .contains("<td>*m*</td>")
                .contains("<p>Beschreibung <code>gemeinde_nr</code><br>Zweite Zeile</p>")
                .contains("<p>Methode <strong>jährlich</strong><br>Zweite Zeile</p>")
                .contains("<p>Hilfsdaten <em>extern</em></p>")
                .contains("<ul><li><p>Verwendung</p></li></ul>")
                .contains("**2020**");
    }

    @Test
    void rendersCatalogDescriptionSeriesAndBothIssueRoutes() throws Exception {
        assertThat(page("/"))
                .contains("<div class=\"dp-lead dp-metadata-text\"><p>Katalog <strong>beschrieben</strong><br>Neue Zeile</p>");
        assertThat(page("/series/serie")).contains("<p>Serie <code>nr</code><br>Zweite Zeile</p>");
        for (String route : List.of("/series/serie/issues/current", "/series/serie/issues/issue")) {
            assertThat(page(route)).contains("<p>Ausgabe <em>beschrieben</em><br>Zweite Zeile</p>");
            assertThat(page(route + "/structure-quality-origin")).contains("<code>gemeinde_nr</code><br>Zweite Zeile");
        }
    }

    @Test
    void fullPagesAndHtmxFragmentsUseCompactInlineFormattingIncludingExpandedIssues() throws Exception {
        for (String header : List.of("false", "true")) {
            var response = mockMvc.perform(get("/datasets").param("expanded", "serie").header("HX-Request", header))
                    .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
            assertThat(response)
                    .contains("<p class=\"dp-entry-description\">Das Attribut <code>gemeinde_nr</code>. Wert <strong>2401</strong>. <em>Hinweis</em> Weiterer Punkt")
                    .contains("<p class=\"dp-entry-description\">Ausgabe <em>beschrieben</em> Zweite Zeile</p>")
                    .doesNotContain("<ul>", "<script>alert");
        }
    }

    @Test
    void cardsTruncateVisibleWordsAndCloseFormattingBeforeTheDetailLink() throws Exception {
        String html = mockMvc.perform(get("/datasets").param("view", "cards"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(html)
                .contains("<strong><code>gemeinde_nr</code></strong>... <a class=\"dp-result-card__description-link\" href=\"/datasets/long\"")
                .doesNotContain("Wort51", "<script>alert");
    }

    private String page(String route) throws Exception {
        return mockMvc.perform(get(route)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
    }

    @TestConfiguration
    static class FixtureConfiguration {
        @Bean
        @Primary
        CatalogSnapshot markdownSnapshot() {
            var metadata = metadata();
            var dataset = new DatasetEntry("markdown", "**Titel**", DESCRIPTION, OFFICE, OFFICE,
                    List.of(), List.of(), DATE, AccessLevel.OPEN, metadata, List.of());
            String longDescription = IntStream.rangeClosed(1, 49).mapToObj(i -> "Wort" + i)
                    .collect(Collectors.joining(" ")) + " **`gemeinde_nr` Wort51**";
            var longDataset = new DatasetEntry("long", "Lange Beschreibung", longDescription, OFFICE, OFFICE,
                    List.of(), List.of(), DATE, AccessLevel.OPEN, List.of());
            var issue = new DatasetIssueEntry("issue", "Ausgabe", "Ausgabe *beschrieben*\nZweite Zeile", OFFICE, OFFICE,
                    List.of(), List.of(), DATE, AccessLevel.OPEN, metadata, List.of(), "2026", true);
            var series = new DatasetSeriesEntry("serie", "Datenreihe", "Serie `nr`\nZweite Zeile", OFFICE, OFFICE,
                    List.of(), List.of(), AccessLevel.OPEN, List.of(issue));
            var catalog = new Catalog(List.of(dataset, longDataset), List.of(series),
                    Optional.of("Katalog **beschrieben**\nNeue Zeile"));
            return CatalogSnapshot.of(catalog, Instant.EPOCH, Duration.ZERO,
                    CatalogTestArtifacts.published("markdown-test"), CatalogTestArtifacts.duckDb("markdown-test"),
                    new CatalogSearchIndexBuilder(new CatalogDocumentMapper()).build(catalog.topLevelEntries()));
        }

        private static CatalogEntryMetadata metadata() {
            return new CatalogEntryMetadata(
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                    List.of(new DatasetAttribute("**nr**", "`TEXT`", Optional.of("Beschreibung `gemeinde_nr`\nZweite Zeile"), Optional.of("*m*"), true)),
                    Optional.empty(), Optional.of("Methode **jährlich**\nZweite Zeile"), Optional.of("**2020**"),
                    Optional.of("- Verwendung"), Optional.of("Hilfsdaten *extern*"));
        }
    }
}
