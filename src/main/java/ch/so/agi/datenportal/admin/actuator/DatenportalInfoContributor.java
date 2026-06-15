package ch.so.agi.datenportal.admin.actuator;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Component;

@Component
public final class DatenportalInfoContributor implements InfoContributor {

    private static final String PACKAGE_BASE = "ch.so.agi.datenportal";

    private final ObjectProvider<BuildProperties> buildProperties;

    public DatenportalInfoContributor(ObjectProvider<BuildProperties> buildProperties) {
        this.buildProperties = buildProperties;
    }

    @Override
    public void contribute(Info.Builder builder) {
        builder.withDetail("app", appDetails());
        BuildProperties build = buildProperties.getIfAvailable();
        if (build != null) {
            builder.withDetail("build", buildDetails(build));
        }
    }

    private static Map<String, Object> appDetails() {
        var details = new LinkedHashMap<String, Object>();
        details.put("name", "datenportal-sodata");
        details.put("packageBase", PACKAGE_BASE);
        details.put("javaVersion", System.getProperty("java.version"));
        return details;
    }

    private static Map<String, Object> buildDetails(BuildProperties build) {
        var details = new LinkedHashMap<String, Object>();
        details.put("name", build.getName());
        details.put("version", build.getVersion());
        details.put("time", build.getTime());
        return details;
    }
}
