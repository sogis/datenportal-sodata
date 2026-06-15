package ch.so.agi.datenportal.web.view;

import ch.so.agi.datenportal.web.ViewMode;
import java.util.List;

public record ResultsVm(
        int totalElements,
        ViewMode viewMode,
        ResultControlsVm controls,
        List<ResultItemVm> rows,
        List<CardResultVm> cards) {

    public ResultsVm {
        rows = List.copyOf(rows);
        cards = List.copyOf(cards);
    }

    public boolean isList() {
        return viewMode == ViewMode.LIST;
    }

    public boolean isCards() {
        return viewMode == ViewMode.CARDS;
    }

    public boolean isEmpty() {
        return totalElements == 0;
    }
}
