package ch.so.agi.datenportal.web.view;

import ch.so.agi.datenportal.support.metadata.MetadataTextRenderer.Html;

public record AttributeRowVm(
        String name,
        String dataType,
        String mandatoryLabel,
        String unit,
        Html description) {}
