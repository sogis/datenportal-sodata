package ch.so.agi.datenportal.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.boot.info.BuildProperties;

class FooterViewModelFactoryTest {

    @Test
    void formatsArtifactVersionAndCommitFromBuildProperties() {
        FooterViewModelFactory factory = new FooterViewModelFactory(providerFor(buildProperties(
                "datenportal-sodata", "0.1.0-SNAPSHOT", "27cd81fb0d0e")));

        assertThat(factory.create().buildLabel())
                .isEqualTo("datenportal-sodata-0.1.0-SNAPSHOT-27cd81fb0d0e");
    }

    @Test
    void fallsBackToUnknownWhenCommitIsMissing() {
        FooterViewModelFactory factory = new FooterViewModelFactory(providerFor(buildProperties(
                "datenportal-sodata", "0.1.0-SNAPSHOT", null)));

        assertThat(factory.create().buildLabel())
                .isEqualTo("datenportal-sodata-0.1.0-SNAPSHOT-unknown");
    }

    private static BuildProperties buildProperties(String artifact, String version, String commit) {
        Properties properties = new Properties();
        properties.setProperty("artifact", artifact);
        properties.setProperty("version", version);
        if (commit != null) {
            properties.setProperty("commit", commit);
        }
        return new BuildProperties(properties);
    }

    private static org.springframework.beans.factory.ObjectProvider<BuildProperties> providerFor(BuildProperties buildProperties) {
        return new StaticListableBeanFactory(Map.of("buildProperties", buildProperties))
                .getBeanProvider(BuildProperties.class);
    }
}
