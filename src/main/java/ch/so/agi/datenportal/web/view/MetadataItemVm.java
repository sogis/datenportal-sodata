package ch.so.agi.datenportal.web.view;

import java.util.List;

public record MetadataItemVm(
        String label,
        List<MetadataLineVm> lines) {

    public MetadataItemVm {
        lines = List.copyOf(lines);
    }
}
