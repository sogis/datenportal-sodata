package ch.so.agi.datenportal.web.view;

import java.util.List;

public record ContactMetadataItemVm(
        String label,
        List<ContactMetadataLineVm> lines) {

    public ContactMetadataItemVm {
        lines = List.copyOf(lines);
    }
}
