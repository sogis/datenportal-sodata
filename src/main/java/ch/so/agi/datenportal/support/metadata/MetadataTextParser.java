package ch.so.agi.datenportal.support.metadata;

import ch.so.agi.datenportal.support.metadata.MetadataDocument.Block;
import ch.so.agi.datenportal.support.metadata.MetadataDocument.Inline;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.commonmark.node.BulletList;
import org.commonmark.node.Code;
import org.commonmark.node.Emphasis;
import org.commonmark.node.HardLineBreak;
import org.commonmark.node.Node;
import org.commonmark.node.OrderedList;
import org.commonmark.node.Paragraph;
import org.commonmark.node.SoftLineBreak;
import org.commonmark.node.StrongEmphasis;
import org.commonmark.node.Text;
import org.commonmark.parser.IncludeSourceSpans;
import org.commonmark.parser.Parser;

public final class MetadataTextParser {
    // Source spans preserve unsupported syntax, including link destinations and code fences.
    private static final Parser PARSER = Parser.builder()
            .includeSourceSpans(IncludeSourceSpans.BLOCKS_AND_INLINES)
            .build();

    private MetadataTextParser() {}

    public static MetadataDocument parse(String text) {
        Objects.requireNonNull(text, "text must not be null");
        String source = text.replace("\r\n", "\n").replace('\r', '\n');
        return new MetadataDocument(blocks(PARSER.parse(source), source));
    }

    private static List<Block> blocks(Node parent, String source) {
        var result = new ArrayList<Block>();
        for (Node node = parent.getFirstChild(); node != null; node = node.getNext()) {
            if (node instanceof Paragraph) {
                result.add(new MetadataDocument.Paragraph(inlines(node, source)));
            } else if (node instanceof BulletList || node instanceof OrderedList) {
                var items = new ArrayList<List<Block>>();
                for (Node item = node.getFirstChild(); item != null; item = item.getNext()) {
                    items.add(blocks(item, source));
                }
                int start = node instanceof OrderedList list ? list.getMarkerStartNumber() : 1;
                result.add(new MetadataDocument.ListBlock(node instanceof OrderedList, start, items));
            } else {
                result.add(new MetadataDocument.Paragraph(literalLines(raw(node, source))));
            }
        }
        return result;
    }

    private static List<Inline> inlines(Node parent, String source) {
        var result = new ArrayList<Inline>();
        for (Node node = parent.getFirstChild(); node != null; node = node.getNext()) {
            if (node instanceof Text text) {
                result.add(new MetadataDocument.Text(text.getLiteral()));
            } else if (node instanceof Code code) {
                result.add(new MetadataDocument.Code(code.getLiteral()));
            } else if (node instanceof Emphasis || node instanceof StrongEmphasis) {
                result.add(new MetadataDocument.Styled(
                        node instanceof Emphasis ? MetadataDocument.Style.EMPHASIS : MetadataDocument.Style.STRONG,
                        inlines(node, source)));
            } else if (node instanceof SoftLineBreak || node instanceof HardLineBreak) {
                result.add(new MetadataDocument.LineBreak());
            } else {
                result.addAll(literalLines(raw(node, source)));
            }
        }
        return result;
    }

    private static String raw(Node node, String source) {
        return node.getSourceSpans().stream()
                .map(span -> source.substring(span.getInputIndex(), span.getInputIndex() + span.getLength()))
                .collect(Collectors.joining("\n"));
    }

    private static List<Inline> literalLines(String text) {
        var result = new ArrayList<Inline>();
        for (String line : text.split("\n", -1)) {
            if (!result.isEmpty()) {
                result.add(new MetadataDocument.LineBreak());
            }
            result.add(new MetadataDocument.Text(line));
        }
        return result;
    }
}
