package ch.so.agi.datenportal.catalog.importxtf;

import java.util.ArrayList;
import java.util.List;

public record XtfElementPath(List<String> elements) {

    public XtfElementPath {
        elements = List.copyOf(elements);
    }

    public static XtfElementPath root() {
        return new XtfElementPath(List.of());
    }

    public XtfElementPath push(String localName) {
        var next = new ArrayList<String>(elements);
        next.add(localName);
        return new XtfElementPath(next);
    }

    public String display() {
        if (elements.isEmpty()) {
            return "/";
        }
        return "/" + String.join("/", elements);
    }
}
