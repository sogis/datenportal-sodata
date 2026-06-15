package ch.so.agi.datenportal.web.view;

import java.util.Optional;

public record MetadataItemVm(
        String label,
        String value,
        Optional<String> href) {}
