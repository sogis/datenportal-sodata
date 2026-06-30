package ch.so.agi.datenportal.web.view;

import java.util.List;
import java.util.Optional;

public record StructureQualityOriginPageVm(
        PageChromeVm chrome,
        String title,
        List<AttributeRowVm> attributes,
        String emptyAttributesText,
        QualityVm quality,
        Optional<MetadataSectionVm> originUsage) {

    public StructureQualityOriginPageVm {
        attributes = List.copyOf(attributes);
        originUsage = originUsage == null ? Optional.empty() : originUsage;
    }
}
