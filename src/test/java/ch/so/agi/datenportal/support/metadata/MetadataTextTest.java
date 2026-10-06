package ch.so.agi.datenportal.support.metadata;

import static org.assertj.core.api.Assertions.assertThat;

import gg.jte.output.StringOutput;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class MetadataTextTest {
    @Test
    void rendersTechnicalTermsAndNestedEmphasisFromAnImmutableDocument() {
        var document = MetadataTextParser.parse("Das Attribut `gemeinde_nr` ist **fett und *kursiv***.");

        assertThat(html(MetadataTextRenderer.full(document)))
                .isEqualTo("<p>Das Attribut <code>gemeinde_nr</code> ist <strong>fett und <em>kursiv</em></strong>.</p>");
        assertThat(MetadataTextRenderer.plainText(document))
                .isEqualTo("Das Attribut gemeinde_nr ist fett und kursiv.");
        assertThat(document.blocks()).hasSize(1);
    }

    @ParameterizedTest
    @ValueSource(strings = {"\n", "\r\n", "\r"})
    void rendersEveryNewlineAndSeparatesParagraphs(String newline) {
        String source = "Erste Zeile" + newline + "Zweite Zeile" + newline + newline + "Neuer Absatz";

        assertThat(html(MetadataTextRenderer.full(source)))
                .isEqualTo("<p>Erste Zeile<br>Zweite Zeile</p><p>Neuer Absatz</p>");
        assertThat(html(MetadataTextRenderer.compact(source)))
                .isEqualTo("Erste Zeile Zweite Zeile Neuer Absatz");
    }

    @Test
    void rendersOrderedUnorderedAndNestedListsAndFlattensThemInPreviews() {
        String source = "**Hinweise:**\n\n- `2401`\n  - *Unterpunkt*\n- Zweiter Eintrag\n\n3. Drei\n4. Vier";
        assertThat(html(MetadataTextRenderer.full(source)))
                .contains("<ul><li><p><code>2401</code></p><ul><li><p><em>Unterpunkt</em></p></li></ul></li>")
                .contains("<ol start=\"3\"><li><p>Drei</p></li><li><p>Vier</p></li></ol>");
        assertThat(html(MetadataTextRenderer.compact(source)))
                .isEqualTo("<strong>Hinweise:</strong> <code>2401</code> <em>Unterpunkt</em> Zweiter Eintrag Drei Vier");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "# Titel", "Titel\n=====", "> Zitat", "---", "    select 1",
            "```sql\nselect 1\n```", "[Text](https://example.org)", "![Bild](https://example.org/image.png)",
            "<https://example.org>", "[label]: https://example.org\n\n[label]",
            "Nur *offen", "Ein `offenes", "**ohne Ende"
    })
    void preservesUnsupportedAndIncompleteSyntaxAsVisibleText(String source) {
        assertThat(MetadataTextRenderer.plainText(source)).isEqualTo(source.replaceAll("\\s+", " ").strip());
        assertThat(html(MetadataTextRenderer.full(source)))
                .doesNotContain("<h1", "<h2", "<blockquote", "<hr", "<pre", "<a ", "<img");
    }

    @Test
    void escapesHtmlBlocksInlineHtmlEntitiesAndTechnicalCodeWithoutExecutingThem() {
        String source = "<script>alert('x')</script>\n\nText <img src=x onerror=alert(1)> & `</code><svg onload=x>`";
        String rendered = html(MetadataTextRenderer.full(source));

        assertThat(rendered)
                .contains("&lt;script&gt;alert(&#39;x&#39;)&lt;/script&gt;")
                .contains("&lt;img src=x onerror=alert(1)&gt; &amp;")
                .contains("<code>&lt;/code&gt;&lt;svg onload=x&gt;</code>")
                .doesNotContain("<script", "<img", "<svg");
        assertThat(html(MetadataTextRenderer.full("&lt;script&gt; &amp;")))
                .isEqualTo("<p>&lt;script&gt; &amp;</p>");
    }

    @Test
    void honorsBackslashEscapesAndLeavesUnderscoresInsideIdentifiersAlone() {
        String source = "\\*wörtlich\\* \\`kein Code\\` gemeinde_nr \\[Text](URL)";
        assertThat(html(MetadataTextRenderer.full(source)))
                .isEqualTo("<p>*wörtlich* `kein Code` gemeinde_nr [Text](URL)</p>");
    }

    @Test
    void truncatesVisibleWordsAcrossNestedStylesAndClosesEveryTag() {
        String source = "**" + words(48) + " *Wort49 `gemeinde_nr` Wort51***";
        var preview = MetadataTextRenderer.preview(source, 50);

        assertThat(preview.truncated()).isTrue();
        assertThat(preview.content().plainText()).isEqualTo(words(48) + " Wort49 gemeinde_nr");
        assertThat(html(preview.content()))
                .endsWith("<em>Wort49 <code>gemeinde_nr</code></em></strong>")
                .doesNotContain("Wort51", "<p>");
    }

    @Test
    void keepsFiftyWordsIncludingFinalPunctuationAndHandlesEmptyText() {
        var preview = MetadataTextRenderer.preview("*" + words(50) + ".*", 50);
        assertThat(preview.truncated()).isFalse();
        assertThat(html(preview.content())).endsWith("Wort50.</em>");
        assertThat(html(MetadataTextRenderer.full(""))).isEmpty();
        assertThat(MetadataTextRenderer.plainText(" \n\n")).isEmpty();
    }

    @Test
    void normalizesWhitespaceBetweenStyledRunsWithoutPuttingSpacesInsideCode() {
        assertThat(html(MetadataTextRenderer.compact("  `nr`  **fett**\n\n*kursiv*  ")))
                .isEqualTo("<code>nr</code> <strong>fett</strong> <em>kursiv</em>");
    }

    private static String html(MetadataTextRenderer.Html content) {
        var output = new StringOutput();
        content.writeTo(output);
        return output.toString();
    }

    private static String words(int count) {
        return IntStream.rangeClosed(1, count).mapToObj(index -> "Wort" + index).collect(Collectors.joining(" "));
    }
}
