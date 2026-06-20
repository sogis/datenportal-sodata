package ch.so.agi.datenportal.web;

import ch.so.agi.datenportal.web.view.FooterVm;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Component;

@Component
public final class FooterViewModelFactory {

    static final String DEFAULT_ARTIFACT = "datenportal-sodata";
    static final String UNKNOWN_VALUE = "unknown";
    private static final String PROVIDER_NOTICE =
            "Die Daten werden von den zuständigen Stellen des Kantons Solothurn bereitgestellt.";

    private final ObjectProvider<BuildProperties> buildProperties;

    public FooterViewModelFactory(ObjectProvider<BuildProperties> buildProperties) {
        this.buildProperties = buildProperties;
    }

    public FooterVm create() {
        return new FooterVm(PROVIDER_NOTICE, formatBuildLabel(buildProperties.getIfAvailable()));
    }

    static String formatBuildLabel(BuildProperties buildProperties) {
        if (buildProperties == null) {
            return DEFAULT_ARTIFACT + "-" + UNKNOWN_VALUE + "-" + UNKNOWN_VALUE;
        }

        String artifact = valueOrDefault(buildProperties.getArtifact(), DEFAULT_ARTIFACT);
        String version = valueOrDefault(buildProperties.getVersion(), UNKNOWN_VALUE);
        String commit = valueOrDefault(buildProperties.get("commit"), UNKNOWN_VALUE);
        return artifact + "-" + version + "-" + commit;
    }

    private static String valueOrDefault(String value, String fallback) {
        if (value == null) {
            return fallback;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? fallback : normalized;
    }
}
