package ch.so.agi.datenportal.web;

import static org.assertj.core.api.Assertions.assertThat;

import ch.so.agi.datenportal.support.JsonAttributeEncoder;
import org.junit.jupiter.api.Test;

class HeaderViewModelFactoryTest {

    private final HeaderViewModelFactory factory = new HeaderViewModelFactory(new JsonAttributeEncoder());

    @Test
    void createsDataNavigationAndJsonAttributes() throws Exception {
        var header = factory.create("Daten");

        assertThat(header.activeSection()).isEqualTo("Daten");
        assertThat(header.logoHref()).isEqualTo("/");
        assertThat(header.siteName()).isEqualTo("Datenportal");
        assertThat(header.siteClaim()).isEqualTo("Kanton Solothurn");
        assertThat(header.primaryNav()).extracting("label")
                .containsExactly("Daten", "Themen", "Statistiken", "Karten", "APIs", "Über uns");
        assertThat(header.utilityNav()).extracting("label")
                .containsExactly("Services", "Verwaltung", "my.so.ch");
        assertThat(header.primaryNav()).filteredOn("active", true).singleElement()
                .satisfies(item -> assertThat(item.label()).isEqualTo("Daten"));

        assertThat(header.primaryNavJson())
                .startsWith("[")
                .contains("\"label\":\"Daten\"")
                .contains("\"href\":\"/datasets\"")
                .contains("\"active\":true");
        assertThat(header.utilityNavJson())
                .contains("\"label\":\"my.so.ch\"")
                .contains("\"href\":\"https://my.so.ch/\"");
        assertThat(header.componentConfigJson()).contains("siteClaim");
    }
}
