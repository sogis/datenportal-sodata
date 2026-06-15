package ch.so.agi.datenportal.support;

import ch.so.agi.datenportal.web.view.NavItemVm;
import java.util.Iterator;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public final class JsonAttributeEncoder {

    public String toJson(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof String string) {
            return quote(string);
        }
        if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        }
        if (value instanceof NavItemVm item) {
            return "{"
                    + quote("label") + ":" + quote(item.label())
                    + "," + quote("href") + ":" + quote(item.href())
                    + "," + quote("active") + ":" + item.active()
                    + "}";
        }
        if (value instanceof Map<?, ?> map) {
            return mapToJson(map);
        }
        if (value instanceof Iterable<?> iterable) {
            return iterableToJson(iterable);
        }
        throw new IllegalArgumentException("Unsupported JSON attribute value type: " + value.getClass().getName());
    }

    private String mapToJson(Map<?, ?> map) {
        StringBuilder builder = new StringBuilder("{");
        Iterator<? extends Map.Entry<?, ?>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<?, ?> entry = iterator.next();
            builder.append(quote(String.valueOf(entry.getKey())))
                    .append(':')
                    .append(toJson(entry.getValue()));
            if (iterator.hasNext()) {
                builder.append(',');
            }
        }
        return builder.append('}').toString();
    }

    private String iterableToJson(Iterable<?> iterable) {
        StringBuilder builder = new StringBuilder("[");
        Iterator<?> iterator = iterable.iterator();
        while (iterator.hasNext()) {
            builder.append(toJson(iterator.next()));
            if (iterator.hasNext()) {
                builder.append(',');
            }
        }
        return builder.append(']').toString();
    }

    private static String quote(String value) {
        StringBuilder builder = new StringBuilder(value.length() + 2).append('"');
        for (int i = 0; i < value.length(); i++) {
            char character = value.charAt(i);
            switch (character) {
                case '"' -> builder.append("\\\"");
                case '\\' -> builder.append("\\\\");
                case '\b' -> builder.append("\\b");
                case '\f' -> builder.append("\\f");
                case '\n' -> builder.append("\\n");
                case '\r' -> builder.append("\\r");
                case '\t' -> builder.append("\\t");
                default -> {
                    if (character < 0x20) {
                        builder.append(String.format("\\u%04x", (int) character));
                    } else {
                        builder.append(character);
                    }
                }
            }
        }
        return builder.append('"').toString();
    }
}
