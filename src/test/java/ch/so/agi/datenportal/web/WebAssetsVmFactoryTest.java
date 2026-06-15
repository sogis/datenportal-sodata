package ch.so.agi.datenportal.web;

import static org.assertj.core.api.Assertions.assertThat;

import ch.so.agi.datenportal.config.WebComponentsProperties;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;

class WebAssetsVmFactoryTest {

    @Test
    void createsEnabledVendoredAssetsWithFontsWhenPresent() {
        var assets = new WebAssetsVmFactory(
                        new WebComponentsProperties(true, "0.1.9", null, false),
                        new DefaultResourceLoader())
                .create();

        assertThat(assets.webComponentsEnabled()).isTrue();
        assertThat(assets.webComponentsIndexJs()).contains("/vendor/so-web-components/0.1.9/index.js");
        assertThat(assets.webComponentStylesheets())
                .containsExactly(
                        "/vendor/so-web-components/0.1.9/styles/reset.css",
                        "/vendor/so-web-components/0.1.9/styles/fonts.css",
                        "/vendor/so-web-components/0.1.9/styles/tokens.css");
        assertThat(assets.webComponentStylesheets()).doesNotHaveDuplicates();
        assertThat(assets.appCss()).isEqualTo("/css/app.css");
        assertThat(assets.htmxJs()).isEqualTo("/js/htmx.min.js");
    }

    @Test
    void omitsWebComponentAssetsWhenDisabled() {
        var assets = new WebAssetsVmFactory(
                        new WebComponentsProperties(false, "0.1.9", null, false),
                        new DefaultResourceLoader())
                .create();

        assertThat(assets.webComponentsEnabled()).isFalse();
        assertThat(assets.webComponentsIndexJs()).isEmpty();
        assertThat(assets.webComponentStylesheets()).isEmpty();
        assertThat(assets.appCss()).isEqualTo("/css/app.css");
        assertThat(assets.htmxJs()).isEqualTo("/js/htmx.min.js");
    }

    @Test
    void omitsOptionalFontsWhenStaticResourceIsMissing() {
        var assets = new WebAssetsVmFactory(
                        new WebComponentsProperties(true, "9.9.9", "/vendor/so-web-components/9.9.9", false),
                        new DefaultResourceLoader())
                .create();

        assertThat(assets.webComponentStylesheets())
                .containsExactly(
                        "/vendor/so-web-components/9.9.9/styles/reset.css",
                        "/vendor/so-web-components/9.9.9/styles/tokens.css");
    }
}
