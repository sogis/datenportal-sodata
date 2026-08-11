package ch.so.agi.datenportal.web.view;

import java.util.Optional;

public record MetadataLineVm(String value, Optional<String> href) {

    public MetadataLineVm {
        value = value == null ? "" : value.trim();
        href = href == null ? Optional.empty() : href;
    }
}
