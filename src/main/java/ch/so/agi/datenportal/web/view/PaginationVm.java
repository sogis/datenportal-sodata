package ch.so.agi.datenportal.web.view;

import java.util.List;

public record PaginationVm(
        boolean visible,
        int currentPage,
        int currentSize,
        int totalPages,
        boolean hasPrevious,
        String previousHref,
        boolean hasNext,
        String nextHref,
        List<PaginationItemVm> items,
        List<Integer> sizeOptions) {

    public PaginationVm {
        items = List.copyOf(items);
        sizeOptions = List.copyOf(sizeOptions);
    }
}
