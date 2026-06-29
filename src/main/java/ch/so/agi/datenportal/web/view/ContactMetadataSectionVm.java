package ch.so.agi.datenportal.web.view;

import java.util.List;

public record ContactMetadataSectionVm(
        String id,
        String title,
        List<ContactMetadataItemVm> items) {

    public ContactMetadataSectionVm {
        items = List.copyOf(items);
    }
}
