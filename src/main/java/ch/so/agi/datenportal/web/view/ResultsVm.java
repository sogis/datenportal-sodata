package ch.so.agi.datenportal.web.view;

import ch.so.agi.datenportal.search.SortMode;
import ch.so.agi.datenportal.web.ViewMode;
import java.util.List;

public record ResultsVm(
        int totalElements,
        ViewMode viewMode,
        String listHref,
        String cardsHref,
        SortMode sortMode,
        List<EntryRowVm> rows,
        List<EntryCardVm> cards) {

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

    public String totalLabel() {
        return totalElements == 1 ? "1 Eintrag gefunden" : totalElements + " Einträge gefunden";
    }
}
