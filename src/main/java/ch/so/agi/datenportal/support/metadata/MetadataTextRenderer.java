package ch.so.agi.datenportal.support.metadata;

import ch.so.agi.datenportal.support.metadata.MetadataDocument.Block;
import ch.so.agi.datenportal.support.metadata.MetadataDocument.Inline;
import ch.so.agi.datenportal.support.metadata.MetadataDocument.Style;
import gg.jte.Content;
import gg.jte.TemplateOutput;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public final class MetadataTextRenderer {
    private static final Pattern WORD = Pattern.compile(
            "[\\p{L}\\p{M}\\p{N}]+(?:[-_'’][\\p{L}\\p{M}\\p{N}]+)*");

    private MetadataTextRenderer() {}

    /** Only this renderer can construct content that bypasses JTE's text escaping. */
    public static final class Html implements Content {
        private final String html;
        private final String plainText;

        private Html(String html, String plainText) {
            this.html = html;
            this.plainText = plainText;
        }

        public String plainText() {
            return plainText;
        }

        @Override
        public void writeTo(TemplateOutput output) {
            output.writeContent(html);
        }
    }

    public record Preview(Html content, boolean truncated) {}

    public static Html full(String source) {
        return full(MetadataTextParser.parse(source));
    }

    public static Html full(MetadataDocument document) {
        var html = new StringBuilder();
        renderBlocks(document.blocks(), html);
        return new Html(html.toString(), plainText(document));
    }

    public static Html compact(String source) {
        return compact(MetadataTextParser.parse(source), Integer.MAX_VALUE).content();
    }

    public static Preview preview(String source, int wordLimit) {
        if (wordLimit < 1) {
            throw new IllegalArgumentException("wordLimit must be positive");
        }
        return compact(MetadataTextParser.parse(source), wordLimit);
    }

    public static String plainText(String source) {
        return plainText(MetadataTextParser.parse(source));
    }

    public static String plainText(MetadataDocument document) {
        return text(runs(document));
    }

    private static void renderBlocks(List<Block> blocks, StringBuilder html) {
        for (Block block : blocks) {
            if (block instanceof MetadataDocument.Paragraph paragraph) {
                html.append("<p>");
                renderInlines(paragraph.content(), html);
                html.append("</p>");
            } else if (block instanceof MetadataDocument.ListBlock list) {
                String tag = list.ordered() ? "ol" : "ul";
                html.append('<').append(tag);
                if (list.ordered() && list.start() != 1) {
                    html.append(" start=\"").append(list.start()).append('"');
                }
                html.append('>');
                for (var item : list.items()) {
                    html.append("<li>");
                    renderBlocks(item, html);
                    html.append("</li>");
                }
                html.append("</").append(tag).append('>');
            }
        }
    }

    private static void renderInlines(List<Inline> content, StringBuilder html) {
        for (Inline inline : content) {
            if (inline instanceof MetadataDocument.Text text) {
                escape(text.value(), html);
            } else if (inline instanceof MetadataDocument.Code code) {
                html.append("<code>");
                escape(code.value(), html);
                html.append("</code>");
            } else if (inline instanceof MetadataDocument.Styled styled) {
                html.append('<').append(tag(styled.style())).append('>');
                renderInlines(styled.content(), html);
                html.append("</").append(tag(styled.style())).append('>');
            } else if (inline instanceof MetadataDocument.LineBreak) {
                html.append("<br>");
            }
        }
    }

    private static Preview compact(MetadataDocument document, int wordLimit) {
        var runs = runs(document);
        String text = text(runs);
        var matcher = WORD.matcher(text);
        int count = 0;
        int end = text.length();
        int lastWordEnd = 0;
        boolean truncated = false;
        while (matcher.find()) {
            if (++count > wordLimit) {
                end = lastWordEnd;
                truncated = true;
                break;
            }
            lastWordEnd = matcher.end();
        }

        var html = new StringBuilder();
        List<Style> active = List.of();
        int position = 0;
        for (Run run : runs) {
            if (position >= end) {
                break;
            }
            changeStyles(active, run.styles(), html);
            active = run.styles();
            int length = Math.min(run.text().length(), end - position);
            escape(run.text().substring(0, length), html);
            position += length;
        }
        changeStyles(active, List.of(), html);
        return new Preview(new Html(html.toString(), text.substring(0, end)), truncated);
    }

    private record Run(String text, List<Style> styles) {}

    private static List<Run> runs(MetadataDocument document) {
        var result = new Runs();
        collectBlocks(document.blocks(), result);
        return result.finish();
    }

    private static String text(List<Run> runs) {
        var text = new StringBuilder();
        runs.forEach(run -> text.append(run.text()));
        return text.toString();
    }

    private static void collectBlocks(List<Block> blocks, Runs runs) {
        for (Block block : blocks) {
            if (block instanceof MetadataDocument.Paragraph paragraph) {
                collectInlines(paragraph.content(), List.of(), runs);
            } else if (block instanceof MetadataDocument.ListBlock list) {
                list.items().forEach(item -> collectBlocks(item, runs));
            }
            runs.append(" ", List.of());
        }
    }

    private static void collectInlines(List<Inline> content, List<Style> styles, Runs runs) {
        for (Inline inline : content) {
            if (inline instanceof MetadataDocument.Text text) {
                runs.append(text.value(), styles);
            } else if (inline instanceof MetadataDocument.LineBreak) {
                runs.append(" ", styles);
            } else {
                var nested = new ArrayList<>(styles);
                if (inline instanceof MetadataDocument.Code code) {
                    nested.add(Style.CODE);
                    runs.append(code.value(), List.copyOf(nested));
                } else if (inline instanceof MetadataDocument.Styled styled) {
                    nested.add(styled.style());
                    collectInlines(styled.content(), List.copyOf(nested), runs);
                }
            }
        }
    }

    /** Normalize whitespace across text/style boundaries without losing formatting offsets. */
    private static final class Runs {
        private final List<Run> runs = new ArrayList<>();
        private final StringBuilder current = new StringBuilder();
        private List<Style> currentStyles = List.of();
        private boolean started;
        private boolean spacePending;
        private List<Style> pendingStyles = List.of();

        void append(String value, List<Style> styles) {
            for (int i = 0; i < value.length(); i++) {
                char character = value.charAt(i);
                if (Character.isWhitespace(character) || Character.isSpaceChar(character)) {
                    if (started && !spacePending) {
                        pendingStyles = styles;
                    }
                    spacePending = started;
                } else {
                    if (spacePending) {
                        write(' ', pendingStyles);
                        spacePending = false;
                    }
                    write(character, styles);
                    started = true;
                }
            }
        }

        private void write(char character, List<Style> styles) {
            if (!styles.equals(currentStyles)) {
                flush();
                currentStyles = styles;
            }
            current.append(character);
        }

        private void flush() {
            if (!current.isEmpty()) {
                runs.add(new Run(current.toString(), currentStyles));
                current.setLength(0);
            }
        }

        List<Run> finish() {
            flush();
            return runs;
        }
    }

    private static void changeStyles(List<Style> from, List<Style> to, StringBuilder html) {
        int common = 0;
        while (common < from.size() && common < to.size() && from.get(common) == to.get(common)) {
            common++;
        }
        for (int i = from.size() - 1; i >= common; i--) {
            html.append("</").append(tag(from.get(i))).append('>');
        }
        for (int i = common; i < to.size(); i++) {
            html.append('<').append(tag(to.get(i))).append('>');
        }
    }

    private static String tag(Style style) {
        return switch (style) {
            case STRONG -> "strong";
            case EMPHASIS -> "em";
            case CODE -> "code";
        };
    }

    private static void escape(String text, StringBuilder html) {
        for (int i = 0; i < text.length(); i++) {
            switch (text.charAt(i)) {
                case '&' -> html.append("&amp;");
                case '<' -> html.append("&lt;");
                case '>' -> html.append("&gt;");
                case '"' -> html.append("&quot;");
                case '\'' -> html.append("&#39;");
                default -> html.append(text.charAt(i));
            }
        }
    }
}
