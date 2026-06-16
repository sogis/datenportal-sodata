package ch.so.agi.datenportal.web.view;

import java.util.List;

public record ResultControlsVm(
        String totalLabel,
        String listHref,
        String cardsHref,
        boolean listActive,
        boolean cardsActive,
        List<SortOptionVm> sortOptions,
        PaginationVm pagination) {

    public ResultControlsVm {
        sortOptions = List.copyOf(sortOptions);
    }
}
