package ch.so.agi.datenportal.web.view;

import java.util.List;
import java.util.Optional;

public record WebAssetsVm(
        boolean webComponentsEnabled,
        Optional<String> webComponentsIndexJs,
        List<String> webComponentStylesheets,
        String appCss,
        String htmxJs) {

    public WebAssetsVm {
        webComponentsIndexJs = webComponentsIndexJs == null ? Optional.empty() : webComponentsIndexJs;
        webComponentStylesheets = List.copyOf(webComponentStylesheets == null ? List.of() : webComponentStylesheets);
    }
}
