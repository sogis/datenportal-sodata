package ch.so.agi.datenportal.admin.reload;

import ch.so.agi.datenportal.config.AdminProperties;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public final class ReloadTokenVerifier {

    public static final String HEADER_NAME = "X-Reload-Token";

    private final AdminProperties properties;

    public ReloadTokenVerifier(AdminProperties properties) {
        this.properties = Objects.requireNonNull(properties, "properties must not be null");
    }

    public boolean isConfigured() {
        return properties.hasReloadToken();
    }

    public boolean isValid(String providedToken) {
        if (!isConfigured() || providedToken == null || providedToken.isBlank()) {
            return false;
        }

        byte[] expected = properties.reloadToken().getBytes(StandardCharsets.UTF_8);
        byte[] provided = providedToken.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(expected, provided);
    }
}
