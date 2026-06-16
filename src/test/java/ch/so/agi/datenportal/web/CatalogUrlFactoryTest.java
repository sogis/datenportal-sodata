package ch.so.agi.datenportal.web;

import static org.assertj.core.api.Assertions.assertThat;

import ch.so.agi.datenportal.config.SearchProperties;
import org.junit.jupiter.api.Test;

class CatalogUrlFactoryTest {

    private final CatalogUrlFactory urlFactory = new CatalogUrlFactory(new SearchProperties(500, 10, 100));

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
                .contains("size=20")
                .doesNotContain("page=3")
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
                .contains("size=20")
                .doesNotContain("theme=")
                .doesNotContain("office=")
                .doesNotContain("modified=")
                .doesNotContain("resourceType=");
    }

    @Test
    void withPageSizeResetsPageAndOmitsDefaultSize() {
        var params = new CatalogQueryParams();
        params.setPage(4);
        params.setSize(20);

        assertThat(urlFactory.withPageSize(params, 10)).doesNotContain("page=").doesNotContain("size=");
        assertThat(urlFactory.withPageSize(params, 50)).contains("size=50").doesNotContain("page=");
    }
}
