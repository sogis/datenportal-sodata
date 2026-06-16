package ch.so.agi.datenportal.web.view;

public record PaginationItemVm(
        String label,
        String href,
        boolean current,
        boolean ellipsis) {}
