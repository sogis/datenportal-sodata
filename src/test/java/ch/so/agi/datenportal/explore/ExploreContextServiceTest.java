package ch.so.agi.datenportal.explore;

import static org.assertj.core.api.Assertions.assertThat;

import ch.so.agi.datenportal.catalog.domain.AccessLevel;
import ch.so.agi.datenportal.catalog.domain.Catalog;
import ch.so.agi.datenportal.catalog.domain.CatalogEntryMetadata;
import ch.so.agi.datenportal.catalog.domain.CatalogSnapshot;
import ch.so.agi.datenportal.catalog.domain.DatasetAttribute;
import ch.so.agi.datenportal.catalog.domain.DatasetEntry;
import ch.so.agi.datenportal.catalog.domain.DistributionFormat;
import ch.so.agi.datenportal.catalog.domain.DistributionLink;
import ch.so.agi.datenportal.catalog.domain.Office;
import ch.so.agi.datenportal.catalog.domain.Theme;
import ch.so.agi.datenportal.catalog.service.CatalogService;
import ch.so.agi.datenportal.web.CatalogUrlFactory;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ExploreContextServiceTest {

    @Test
    void contextContainsTablesRecipesSnippetsAndFlags() {
        var service = service(dataset(
                "ch.so.gemeinden",
                "Gemeinden",
                List.of(distribution(DistributionFormat.PARQUET)),
                metadataWithAttributes()));

        ExploreContextDto context = service.buildContext("ch.so.gemeinden");

        assertThat(context.version()).isEqualTo(1);
        assertThat(context.datasetId()).isEqualTo("ch.so.gemeinden");
        assertThat(context.canonicalUrl()).isEqualTo("/datasets/ch.so.gemeinden");
        assertThat(context.tables()).hasSize(1);
        assertThat(context.tables().getFirst().name()).isEqualTo("ch_so_gemeinden");
        assertThat(context.tables().getFirst().columns()).extracting(ExploreColumnDto::name)
                .contains("bfs_nr", "gemeindename", "flaeche_ha");
        assertThat(context.recipes()).isNotEmpty();
        assertThat(context.codeSnippets()).extracting(ExploreCodeSnippetDto::language)
                .contains(ExploreSnippetLanguage.SQL, ExploreSnippetLanguage.PYTHON, ExploreSnippetLanguage.R);
        assertThat(context.featureFlags().charts()).isTrue();
        assertThat(context.featureFlags().aiAssistant()).isFalse();
    }

    @Test
    void datasetWithoutParquetReturnsEmptyTablesRecipesAndSnippets() {
        var service = service(dataset(
                "csv-only",
                "CSV only",
                List.of(distribution(DistributionFormat.CSV)),
                CatalogEntryMetadata.empty()));

        ExploreContextDto context = service.buildContext("csv-only");

        assertThat(context.tables()).isEmpty();
        assertThat(context.recipes()).isEmpty();
        assertThat(context.codeSnippets()).isEmpty();
    }

    @Test
    void embeddedJsonEscapesScriptBreakingSequences() {
        var service = service(dataset(
                "script",
                "</script><!--",
                List.of(distribution(DistributionFormat.PARQUET)),
                CatalogEntryMetadata.empty()));

        String json = service.buildContextJson("script");

        assertThat(json)
                .contains("<\\/script>")
                .contains("\\u003C!--")
                .doesNotContain("</script>");
    }

    private static ExploreContextService service(DatasetEntry dataset) {
        var catalogService = new CatalogService(CatalogSnapshot.of(
                new Catalog(List.of(dataset), List.of()),
                Instant.parse("2026-07-01T08:00:00Z"),
                "test"));
        var sanitizer = new ExploreSqlNameSanitizer();
        var roleDetector = new ExploreColumnRoleDetector();
        var properties = new ExploreProperties(true, 100, 10_000, 30_000, true, true, false, false, false, false);
        return new ExploreContextService(
                catalogService,
                new ExploreTableService(sanitizer, roleDetector),
                new ExploreRecipeService(sanitizer, properties),
                new ExploreCodeSnippetService(),
                properties,
                new CatalogUrlFactory(),
                new ExploreContextJsonWriter());
    }

    private static DatasetEntry dataset(
            String identifier,
            String title,
            List<DistributionLink> distributions,
            CatalogEntryMetadata metadata) {
        return new DatasetEntry(
                identifier,
                title,
                "Beschreibung",
                office(),
                office(),
                List.of(theme()),
                List.of("Keyword"),
                LocalDate.parse("2026-06-30"),
                AccessLevel.OPEN,
                metadata,
                distributions);
    }

    private static CatalogEntryMetadata metadataWithAttributes() {
        return new CatalogEntryMetadata(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                List.of(
                        new DatasetAttribute("bfs_nr", "INTEGER", Optional.of("BFS-Nummer."), Optional.empty(), true),
                        new DatasetAttribute("gemeindename", "VARCHAR", Optional.of("Gemeindename."), Optional.empty(), false),
                        new DatasetAttribute("flaeche_ha", "DOUBLE", Optional.of("Fläche."), Optional.of("ha"), false)),
                Optional.empty());
    }

    private static DistributionLink distribution(DistributionFormat format) {
        return new DistributionLink(
                URI.create("https://data.so.ch/dataset/ch.so.gemeinden"),
                URI.create("https://data.so.ch/download/ch.so.gemeinden." + format.label().toLowerCase()),
                format);
    }

    private static Office office() {
        return new Office("agi", "Amt für Geoinformation", Optional.of("AGI"));
    }

    private static Theme theme() {
        return new Theme("raum", "Raum und Umwelt");
    }
}
