package ch.so.agi.datenportal.web;

import static org.assertj.core.api.Assertions.assertThat;

import ch.so.agi.datenportal.catalog.domain.AccessLevel;
import ch.so.agi.datenportal.catalog.domain.DatasetEntry;
import ch.so.agi.datenportal.catalog.domain.DatasetIssueEntry;
import ch.so.agi.datenportal.catalog.domain.DatasetSeriesEntry;
import ch.so.agi.datenportal.catalog.domain.Office;
import ch.so.agi.datenportal.search.SearchFilters;
import ch.so.agi.datenportal.search.SearchQuery;
import ch.so.agi.datenportal.search.SearchResult;
import ch.so.agi.datenportal.search.SortMode;
import ch.so.agi.datenportal.web.view.EntryCardVm;
import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.output.StringOutput;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class ResultsVmFactoryTest {

    private static final Office OFFICE = new Office("office", "Fachstelle", Optional.empty());
    private static final TemplateEngine TEMPLATE_ENGINE = TemplateEngine.createPrecompiled(ContentType.Html);

    private final ResultsVmFactory factory = new ResultsVmFactory(new CatalogUrlFactory());

    @Test
    void keepsDescriptionsUpToFiftyWordsAndTruncatesLongerDescriptionsAtAWordBoundary() {
        var fiftyWords = words(50);
        var fiftyWordCard = card(dataset("dataset.50", fiftyWords));
        var fiftyOneWordCard = card(dataset("dataset.51", fiftyWords + " Wort51"));

        assertThat(fiftyWordCard.description()).isEqualTo(fiftyWords);
        assertThat(fiftyWordCard.descriptionTruncated()).isFalse();
        assertThat(fiftyOneWordCard.description()).isEqualTo(fiftyWords);
        assertThat(fiftyOneWordCard.descriptionTruncated()).isTrue();

        var html = renderCard(fiftyWordCard);
        assertThat(html).doesNotContain(">[Details anzeigen]</a>");
        assertThat(html).contains("class=\"dp-card-detail-link\" href=\"/datasets/dataset.50\"");
    }

    @Test
    void truncatedDatasetAndSeriesCardsKeepTheirExistingDetailTargets() {
        var description = words(51);

        var datasetCard = card(dataset("dataset.long", description));
        var seriesCard = card(series("series.long", description));

        assertThat(datasetCard.descriptionTruncated()).isTrue();
        assertThat(datasetCard.detailHref()).isEqualTo("/datasets/dataset.long");
        assertThat(renderCard(datasetCard)).contains(
                "... <a class=\"dp-result-card__description-link\" href=\"/datasets/dataset.long\" "
                        + "aria-label=\"Details anzeigen: Testdatensatz\">[Details anzeigen]</a>");
        assertThat(renderCard(datasetCard)).contains("class=\"dp-card-detail-link\" href=\"/datasets/dataset.long\"");
        assertThat(seriesCard.descriptionTruncated()).isTrue();
        assertThat(seriesCard.detailHref()).isEqualTo("/series/series.long");
        assertThat(renderCard(seriesCard)).contains(
                "... <a class=\"dp-result-card__description-link\" href=\"/series/series.long\" "
                        + "aria-label=\"Details anzeigen: Testdatenreihe\">[Details anzeigen]</a>");
        assertThat(renderCard(seriesCard)).contains("class=\"dp-card-detail-link\" href=\"/series/series.long\"");
    }

    private EntryCardVm card(ch.so.agi.datenportal.catalog.domain.CatalogEntry entry) {
        var result = new SearchResult(
                List.of(entry),
                1,
                new SearchQuery("", SearchFilters.empty(), SortMode.defaultMode()));
        return factory.create(result, new CatalogQueryParams()).cards().getFirst();
    }

    private static DatasetEntry dataset(String identifier, String description) {
        return new DatasetEntry(
                identifier,
                "Testdatensatz",
                description,
                OFFICE,
                OFFICE,
                List.of(),
                List.of(),
                LocalDate.of(2025, 1, 1),
                AccessLevel.OPEN,
                List.of());
    }

    private static DatasetSeriesEntry series(String identifier, String description) {
        var issue = new DatasetIssueEntry(
                identifier + ".2025",
                "Testdatenreihe 2025",
                "Ausgabe 2025",
                OFFICE,
                OFFICE,
                List.of(),
                List.of(),
                LocalDate.of(2025, 1, 1),
                AccessLevel.OPEN,
                List.of(),
                "2025",
                true);
        return new DatasetSeriesEntry(
                identifier,
                "Testdatenreihe",
                description,
                OFFICE,
                OFFICE,
                List.of(),
                List.of(),
                AccessLevel.OPEN,
                List.of(issue));
    }

    private static String words(int count) {
        return IntStream.rangeClosed(1, count)
                .mapToObj(index -> "Wort" + index)
                .collect(Collectors.joining(" "));
    }

    private static String renderCard(EntryCardVm card) {
        var output = new StringOutput();
        TEMPLATE_ENGINE.render("components/entryCard.jte", Map.of("card", card), output);
        return output.toString();
    }
}
