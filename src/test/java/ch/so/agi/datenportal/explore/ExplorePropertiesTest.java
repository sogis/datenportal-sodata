package ch.so.agi.datenportal.explore;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ExplorePropertiesTest {

    @Test
    void normalizesLimitsAndBuildsNestedDtos() {
        var properties = new ExploreProperties(true, 0, -1, 0, true, true, false, false, false, false, false);

        assertThat(properties.execution().engine()).isEqualTo("duckdb-wasm");
        assertThat(properties.execution().mode()).isEqualTo("browser-local");
        assertThat(properties.execution().maxPreviewRows()).isEqualTo(100);
        assertThat(properties.execution().maxResultRows()).isEqualTo(10_000);
        assertThat(properties.execution().queryTimeoutMs()).isEqualTo(30_000);
        assertThat(properties.featureFlags().charts()).isTrue();
        assertThat(properties.featureFlags().localHistory()).isTrue();
        assertThat(properties.featureFlags().aiAssistant()).isFalse();
        assertThat(properties.featureFlags().webR()).isFalse();
        assertThat(properties.featureFlags().vega()).isFalse();
        assertThat(properties.featureFlags().mosaic()).isFalse();
        assertThat(properties.featureFlags().geospatial()).isFalse();
    }
}
