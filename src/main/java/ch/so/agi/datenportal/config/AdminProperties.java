package ch.so.agi.datenportal.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "datenportal.admin")
public record AdminProperties(String reloadToken) {

    public boolean hasReloadToken() {
        return reloadToken != null && !reloadToken.isBlank();
    }
}
