package ch.so.agi.datenportal.web.view;

import java.util.Objects;

public record DetailFeatureVm(
        String label,
        boolean available) {

    public DetailFeatureVm {
        label = Objects.requireNonNull(label, "label must not be null").trim();
        if (label.isEmpty()) {
            throw new IllegalArgumentException("label must not be blank");
        }
    }
}
