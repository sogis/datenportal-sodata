package ch.so.agi.datenportal.web;

import ch.so.agi.datenportal.config.WebComponentsProperties;
import ch.so.agi.datenportal.web.view.WebAssetsVm;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
public final class WebAssetsVmFactory {

    private static final String APP_CSS = "/css/app.css";
    private static final String HTMX_JS = "/js/htmx.min.js";

    private final WebComponentsProperties properties;
    private final ResourceLoader resourceLoader;

    public WebAssetsVmFactory(WebComponentsProperties properties, ResourceLoader resourceLoader) {
        this.properties = properties;
        this.resourceLoader = resourceLoader;
    }

    public WebAssetsVm create() {
        if (!properties.enabled()) {
            return new WebAssetsVm(false, Optional.empty(), List.of(), APP_CSS, HTMX_JS);
        }

        List<String> stylesheets = new ArrayList<>();
        stylesheets.add(properties.resetCssPath());
        if (properties.useCdn() || staticResourceExists(properties.fontsCssPath())) {
            stylesheets.add(properties.fontsCssPath());
        }
        stylesheets.add(properties.tokensCssPath());

        return new WebAssetsVm(
                true,
                Optional.of(properties.indexJsPath()),
                List.copyOf(new LinkedHashSet<>(stylesheets)),
                APP_CSS,
                HTMX_JS);
    }

    private boolean staticResourceExists(String publicPath) {
        if (!publicPath.startsWith("/")) {
            return false;
        }
        try {
            return resourceLoader.getResource("classpath:/static" + publicPath).exists();
        } catch (RuntimeException ex) {
            return false;
        }
    }
}
