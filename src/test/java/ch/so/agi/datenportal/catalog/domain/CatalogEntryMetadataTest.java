package ch.so.agi.datenportal.catalog.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class CatalogEntryMetadataTest {

    @Test
    void emptyMetadataHasNoStructureInformation() {
        var metadata = CatalogEntryMetadata.empty();

        assertThat(metadata.attributes()).isEmpty();
        assertThat(metadata.model()).isEmpty();
        assertThat(metadata.qualitySummary()).isEmpty();
        assertThat(metadata.structureSummary()).isEmpty();
        assertThat(metadata.hasStructureInformation()).isFalse();
    }

    @Test
    void structureInformationIsDetectedFromAttributesOrModel() {
        var withAttributes = new CatalogEntryMetadata(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                List.of(new DatasetAttribute("gemeinde", "TEXT", Optional.empty(), Optional.empty(), true)),
                Optional.empty());
        var withModel = new CatalogEntryMetadata(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                List.of(),
                Optional.of("SO_AGI_Testmodell_20260624"));

        assertThat(withAttributes.hasStructureInformation()).isTrue();
        assertThat(withModel.hasStructureInformation()).isTrue();
    }
}
