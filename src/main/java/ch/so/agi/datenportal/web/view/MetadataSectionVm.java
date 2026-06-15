package ch.so.agi.datenportal.web.view;

import java.util.List;

public record MetadataSectionVm(
        String id,
        String title,
        List<MetadataItemVm> items) {

    public MetadataSectionVm {
        items = List.copyOf(items);
    }
}
