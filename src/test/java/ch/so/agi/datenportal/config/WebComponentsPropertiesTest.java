package ch.so.agi.datenportal.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class WebComponentsPropertiesTest {

    @Test
    void usesVendoredDefaults() {
        var properties = new WebComponentsProperties(true, "0.1.10", null, false);

        assertThat(properties.enabled()).isTrue();
        assertThat(properties.version()).isEqualTo("0.1.10");
        assertThat(properties.indexJsPath()).isEqualTo("/vendor/so-web-components/0.1.10/index.js");
        assertThat(properties.resetCssPath()).isEqualTo("/vendor/so-web-components/0.1.10/styles/reset.css");
        assertThat(properties.tokensCssPath()).isEqualTo("/vendor/so-web-components/0.1.10/styles/tokens.css");
        assertThat(properties.fontsCssPath()).isEqualTo("/vendor/so-web-components/0.1.10/styles/fonts.css");
    }

    @Test
    void normalizesTrailingSlash() {
        var properties = new WebComponentsProperties(true, "0.1.10", "/vendor/so-web-components/0.1.10/", false);

        assertThat(properties.indexJsPath()).isEqualTo("/vendor/so-web-components/0.1.10/index.js");
    }

    @Test
    void rejectsBlankVersionAndRelativeVendoredPath() {
        assertThatThrownBy(() -> new WebComponentsProperties(true, " ", null, false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("version");

        assertThatThrownBy(() -> new WebComponentsProperties(true, "0.1.10", "vendor/so-web-components/0.1.10", false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("asset-base-path");
    }
}
