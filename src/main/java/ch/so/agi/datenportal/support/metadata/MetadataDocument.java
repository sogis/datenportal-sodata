package ch.so.agi.datenportal.support.metadata;

import java.util.List;

/** Immutable, output-independent representation of the supported metadata text profile. */
public record MetadataDocument(List<Block> blocks) {
    public MetadataDocument {
        blocks = List.copyOf(blocks);
    }

    public sealed interface Block permits Paragraph, ListBlock {}

    public record Paragraph(List<Inline> content) implements Block {
        public Paragraph {
            content = List.copyOf(content);
        }
    }

    public record ListBlock(boolean ordered, int start, List<List<Block>> items) implements Block {
        public ListBlock {
            items = items.stream().map(List::copyOf).toList();
        }
    }

    public sealed interface Inline permits Text, Code, Styled, LineBreak {}

    public record Text(String value) implements Inline {}

    public record Code(String value) implements Inline {}

    public enum Style { STRONG, EMPHASIS, CODE }

    public record Styled(Style style, List<Inline> content) implements Inline {
        public Styled {
            content = List.copyOf(content);
        }
    }

    public record LineBreak() implements Inline {}
}
