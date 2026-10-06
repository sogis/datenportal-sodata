package ch.so.agi.datenportal.web.view;

import ch.so.agi.datenportal.support.metadata.MetadataTextRenderer.Html;
import java.util.Optional;

public record MetadataLineVm(String value, Optional<String> href, Optional<Html> formattedValue) {

    public MetadataLineVm(String value, Optional<String> href) {
        this(value, href, Optional.empty());
    }

    public MetadataLineVm {
        value = value == null ? "" : value.trim();
        href = href == null ? Optional.empty() : href;
        formattedValue = formattedValue == null ? Optional.empty() : formattedValue;
    }
}
