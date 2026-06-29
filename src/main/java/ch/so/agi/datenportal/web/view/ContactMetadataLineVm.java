package ch.so.agi.datenportal.web.view;

import java.util.Optional;

public record ContactMetadataLineVm(
        String value,
        Optional<String> href) {}
