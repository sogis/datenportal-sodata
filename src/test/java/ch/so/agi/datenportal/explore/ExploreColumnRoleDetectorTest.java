package ch.so.agi.datenportal.explore;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class ExploreColumnRoleDetectorTest {

    private final ExploreColumnRoleDetector detector = new ExploreColumnRoleDetector();

    @Test
    void detectsIdentifierAndMunicipality() {
        assertThat(roles("bfs_nr", "INTEGER"))
                .contains(ExploreColumnRole.IDENTIFIER, ExploreColumnRole.MUNICIPALITY)
                .doesNotContain(ExploreColumnRole.MEASURE);
    }

    @Test
    void detectsLabelAndMunicipality() {
        assertThat(roles("gemeindename", "VARCHAR"))
                .contains(ExploreColumnRole.LABEL, ExploreColumnRole.MUNICIPALITY);
    }

    @Test
    void detectsMeasuresButNotYearsAsMeasures() {
        assertThat(roles("flaeche_ha", "DOUBLE")).contains(ExploreColumnRole.MEASURE);
        assertThat(roles("jahr", "INTEGER"))
                .contains(ExploreColumnRole.YEAR)
                .doesNotContain(ExploreColumnRole.MEASURE);
    }

    @Test
    void detectsGeometry() {
        assertThat(roles("geom", "GEOMETRY")).contains(ExploreColumnRole.GEOMETRY);
    }

    private java.util.Set<ExploreColumnRole> roles(String name, String type) {
        return detector.detectRoles(new ExploreColumnSource(name, type, Optional.empty()));
    }
}
