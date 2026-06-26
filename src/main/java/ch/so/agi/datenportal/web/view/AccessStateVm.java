package ch.so.agi.datenportal.web.view;

import java.util.Objects;

public record AccessStateVm(
        boolean openData,
        String label) {

    public AccessStateVm {
        label = Objects.requireNonNull(label, "label must not be null").trim();
        if (label.isEmpty()) {
            throw new IllegalArgumentException("label must not be blank");
        }
    }
}
