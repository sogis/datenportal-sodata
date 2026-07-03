package ch.so.agi.datenportal.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "datenportal.security.csp")
public record SecurityCspProperties(
        List<String> connectSrc,
        @DefaultValue("true") boolean includeCatalogDownloadOrigin) {

    private static final List<String> DEFAULT_CONNECT_SRC = List.of("'self'", "https://data.so.ch");

    public SecurityCspProperties {
        connectSrc = normalizeConnectSrc(connectSrc);
    }

    private static List<String> normalizeConnectSrc(List<String> values) {
        if (values == null || values.isEmpty()) {
            return DEFAULT_CONNECT_SRC;
        }
        List<String> normalized = values.stream()
                .map(value -> value == null ? "" : value.trim())
                .filter(value -> !value.isEmpty())
                .map(SecurityCspProperties::validateDirectiveValue)
                .distinct()
                .toList();
        return normalized.isEmpty() ? DEFAULT_CONNECT_SRC : normalized;
    }

    private static String validateDirectiveValue(String value) {
        if (value.contains(";")) {
            throw new IllegalArgumentException("datenportal.security.csp.connect-src values must not contain ';'");
        }
        return value;
    }
}
