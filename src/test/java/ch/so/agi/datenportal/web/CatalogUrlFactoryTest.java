package ch.so.agi.datenportal.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CatalogUrlFactoryTest {

    private final CatalogUrlFactory urlFactory = new CatalogUrlFactory();

    @Test
    void removeFilterPreservesOtherStateAndResetsPage() {
        var params = new CatalogQueryParams();
        params.setQ("grenzen");
        params.setTheme(java.util.List.of("Geografie", "Politik"));
        params.setOffice(java.util.List.of("agi"));
        params.setView("cards");
        params.setSort("title-asc");
        params.setPage(3);
        params.setSize(20);
        params.setExpanded(java.util.List.of("series-1"));

        String url = urlFactory.removeFilter(params, "theme", "Geografie");

        assertThat(url)
                .contains("q=grenzen")
                .contains("theme=Politik")
                .contains("office=agi")
                .contains("view=cards")
                .contains("sort=title-asc")
                .doesNotContain("page=")
                .doesNotContain("size=")
                .doesNotContain("expanded=");
    }

    @Test
    void resetFiltersKeepsSearchAndPresentationState() {
        var params = new CatalogQueryParams();
        params.setQ("bauinventar");
        params.setTheme(java.util.List.of("Geografie"));
        params.setView("cards");
        params.setSort("title-asc");
        params.setSize(20);

        String url = urlFactory.resetFilters(params);

        assertThat(url)
                .contains("q=bauinventar")
                .contains("view=cards")
                .contains("sort=title-asc")
                .doesNotContain("page=")
                .doesNotContain("size=")
                .doesNotContain("theme=")
                .doesNotContain("office=")
                .doesNotContain("modified=")
                .doesNotContain("resourceType=");
    }

    @Test
    void withViewAndSortDoNotPropagatePageOrSize() {
        var params = new CatalogQueryParams();
        params.setPage(4);
        params.setSize(20);
        params.setTheme(java.util.List.of("Geografie"));

        assertThat(urlFactory.withView(params, ViewMode.CARDS))
                .contains("theme=Geografie")
                .contains("view=cards")
                .doesNotContain("page=")
                .doesNotContain("size=");
        assertThat(urlFactory.withSort(params, ch.so.agi.datenportal.search.SortMode.TITLE_ASC))
                .contains("theme=Geografie")
                .contains("sort=title-asc")
                .doesNotContain("page=")
                .doesNotContain("size=");
    }
}
