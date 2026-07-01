package ch.so.agi.datenportal.explore;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ExploreCodeSnippetServiceTest {

    private final ExploreCodeSnippetService service = new ExploreCodeSnippetService();

    @Test
    void returnsNoSnippetsWithoutTables() {
        assertThat(service.generateSnippets(null, List.of())).isEmpty();
    }

    @Test
    void generatesDuckDbPythonAndRSnippetsForPrimaryTable() {
        var snippets = service.generateSnippets(null, List.of(
                table("secondary", "https://data.so.ch/download/secondary.parquet", false),
                table("primary", "https://data.so.ch/download/primary.parquet", true)));

        assertThat(snippets).extracting(ExploreCodeSnippetDto::language)
                .containsExactly(ExploreSnippetLanguage.SQL, ExploreSnippetLanguage.PYTHON, ExploreSnippetLanguage.R);
        assertThat(snippets).extracting(ExploreCodeSnippetDto::code)
                .allSatisfy(code -> assertThat(code).contains("primary.parquet").doesNotContain("secondary.parquet"));
    }

    @Test
    void fallsBackToFirstTableWhenNoPrimaryTableExists() {
        var snippets = service.generateSnippets(null, List.of(
                table("first", "https://data.so.ch/download/first.parquet", false),
                table("second", "https://data.so.ch/download/second.parquet", false)));

        assertThat(snippets).extracting(ExploreCodeSnippetDto::code)
                .allSatisfy(code -> assertThat(code).contains("first.parquet").doesNotContain("second.parquet"));
    }

    @Test
    void escapesSnippetUrlForEachTargetLanguage() {
        String url = "https://data.so.ch/download/quote'and\"slash\\file.parquet";

        var snippets = service.generateSnippets(null, List.of(table("primary", url, true)));

        assertThat(snippet(snippets, ExploreSnippetLanguage.SQL))
                .contains("quote''and\"slash\\file.parquet");
        assertThat(snippet(snippets, ExploreSnippetLanguage.PYTHON))
                .contains("quote'and\\\"slash\\\\file.parquet");
        assertThat(snippet(snippets, ExploreSnippetLanguage.R))
                .contains("quote'and\\\"slash\\\\file.parquet");
    }

    private static String snippet(List<ExploreCodeSnippetDto> snippets, ExploreSnippetLanguage language) {
        return snippets.stream()
                .filter(snippet -> snippet.language() == language)
                .findFirst()
                .orElseThrow()
                .code();
    }

    private static ExploreTableDto table(String id, String parquetUrl, boolean primary) {
        return new ExploreTableDto(
                id,
                id,
                id,
                Optional.empty(),
                parquetUrl,
                Optional.empty(),
                Optional.empty(),
                primary,
                List.of());
    }
}
