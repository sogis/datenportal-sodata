package ch.so.agi.datenportal.web;

import java.util.regex.Pattern;

record CardDescriptionPreview(String text, boolean truncated) {

    private static final int WORD_LIMIT = 50;
    private static final Pattern WORD_PATTERN = Pattern.compile(
            "[\\p{L}\\p{M}\\p{N}]+(?:[-'’][\\p{L}\\p{M}\\p{N}]+)*");

    static CardDescriptionPreview from(String description) {
        var matcher = WORD_PATTERN.matcher(description);
        var lastIncludedWordEnd = 0;
        var wordCount = 0;

        while (matcher.find()) {
            wordCount++;
            if (wordCount > WORD_LIMIT) {
                return new CardDescriptionPreview(
                        description.substring(0, lastIncludedWordEnd).stripTrailing(),
                        true);
            }
            lastIncludedWordEnd = matcher.end();
        }

        return new CardDescriptionPreview(description, false);
    }
}
