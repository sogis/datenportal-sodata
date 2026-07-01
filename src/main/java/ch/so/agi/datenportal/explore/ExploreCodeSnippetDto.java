package ch.so.agi.datenportal.explore;

public record ExploreCodeSnippetDto(
        String id,
        String title,
        ExploreSnippetLanguage language,
        String code) {
}
