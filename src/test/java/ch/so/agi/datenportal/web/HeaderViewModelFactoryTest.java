package ch.so.agi.datenportal.web;

import static org.assertj.core.api.Assertions.assertThat;

import ch.so.agi.datenportal.support.JsonAttributeEncoder;
import org.junit.jupiter.api.Test;

class HeaderViewModelFactoryTest {

    private final HeaderViewModelFactory factory = new HeaderViewModelFactory(new JsonAttributeEncoder());

    @Test
    void createsHeaderNavigationAndJsonAttributes() {
        var header = factory.create("Verwaltung");

        assertThat(header.activeSection()).isEqualTo("Verwaltung");
        assertThat(header.logoHref()).isEqualTo("/");
        assertThat(header.siteName()).isEqualTo("Datenportal");
        assertThat(header.siteClaim()).isEqualTo("Kanton Solothurn");
        assertThat(header.primaryNav()).extracting("label")
                .containsExactly("Services", "Verwaltung");
        assertThat(header.utilityNav()).extracting("label")
                .containsExactly("Regierung", "Gerichte", "Parlament", "Karriere", "my.so.ch");
        assertThat(header.primaryNav()).filteredOn("active", true).singleElement()
                .satisfies(item -> assertThat(item.label()).isEqualTo("Verwaltung"));

        assertThat(header.primaryNavJson())
                .startsWith("[")
                .contains("\"label\":\"Verwaltung\"")
                .contains("\"href\":\"https://my.so.ch\"")
                .contains("\"active\":true");
        assertThat(header.utilityNavJson())
                .contains("\"label\":\"Regierung\"")
                .contains("\"href\":\"https://so.ch/regierung/\"")
                .contains("\"label\":\"my.so.ch\"")
                .contains("\"href\":\"https://my.so.ch/\"");
        assertThat(header.componentConfigJson()).contains("siteClaim");
    }

    @Test
    void catalogAndDetailPagesDefaultToVerwaltungSection() {
        assertThat(factory.forCatalogPage().activeSection()).isEqualTo("Verwaltung");
        assertThat(factory.forDetailPage().activeSection()).isEqualTo("Verwaltung");
    }
}
