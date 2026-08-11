package ch.so.agi.datenportal.explore;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ExplorePropertiesTest {

    @Test
    void normalizesLimitsAndBuildsNestedDtos() {
        var properties = new ExploreProperties(true, 0, -1, 0, true, false);

        assertThat(properties.execution().engine()).isEqualTo("duckdb-wasm");
        assertThat(properties.execution().mode()).isEqualTo("browser-local");
        assertThat(properties.execution().maxPreviewRows()).isEqualTo(100);
        assertThat(properties.execution().maxResultRows()).isEqualTo(10_000);
        assertThat(properties.execution().queryTimeoutMs()).isEqualTo(30_000);
        assertThat(properties.chartsEnabled()).isTrue();
        assertThat(properties.webrEnabled()).isFalse();
        assertThat(properties.rLaboratory().dataFrameName()).isEqualTo("daten");
        assertThat(properties.rLaboratory().runtimeBaseUrl()).isEqualTo("/webr/0.6.0/");
        assertThat(properties.rLaboratory().packageRepoUrl()).isEqualTo("/webr-packages/");
        assertThat(properties.rLaboratory().packages()).containsExactly(
                "ggplot2",
                "dplyr",
                "tidyr",
                "readr",
                "tibble",
                "scales",
                "RColorBrewer",
                "viridisLite",
                "jsonlite");
        assertThat(properties.rLaboratory().recommendedRows()).isEqualTo(5_000);
        assertThat(properties.rLaboratory().warningRows()).isEqualTo(10_000);
        assertThat(properties.rLaboratory().hardRows()).isEqualTo(50_000);
    }
}
