package ch.so.agi.datenportal.config;

import java.util.Objects;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "datenportal.web-components")
public record WebComponentsProperties(
        @DefaultValue("true") boolean enabled,
        @DefaultValue("0.1.10") String version,
        String assetBasePath,
        @DefaultValue("false") boolean useCdn) {

    private static final String CDN_BASE_PATH = "https://cdn.jsdelivr.net/npm/so-web-components@";
    private static final String VENDORED_BASE_PATH = "/vendor/so-web-components/";

    public WebComponentsProperties {
        version = normalizeVersion(version);
        assetBasePath = normalizeBasePath(assetBasePath, version, useCdn);
    }

    public String indexJsPath() {
        return assetBasePath + "/index.js";
    }

    public String resetCssPath() {
        return assetBasePath + "/styles/reset.css";
    }

    public String tokensCssPath() {
        return assetBasePath + "/styles/tokens.css";
    }

    public String fontsCssPath() {
        return assetBasePath + "/styles/fonts.css";
    }

    private static String normalizeVersion(String value) {
        String normalized = Objects.requireNonNull(value, "datenportal.web-components.version must not be null")
                .trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("datenportal.web-components.version must not be blank");
        }
        return normalized;
    }

    private static String normalizeBasePath(String value, String version, boolean useCdn) {
        String normalized = value == null || value.isBlank()
                ? defaultBasePath(version, useCdn)
                : value.trim();
        while (normalized.endsWith("/") && normalized.length() > 1) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("datenportal.web-components.asset-base-path must not be blank");
        }
        if (!useCdn && !normalized.startsWith("/")) {
            throw new IllegalArgumentException(
                    "datenportal.web-components.asset-base-path must start with '/' when use-cdn is false");
        }
        return normalized;
    }

    private static String defaultBasePath(String version, boolean useCdn) {
        if (useCdn) {
            return CDN_BASE_PATH + version + "/dist";
        }
        return VENDORED_BASE_PATH + version;
    }
}
