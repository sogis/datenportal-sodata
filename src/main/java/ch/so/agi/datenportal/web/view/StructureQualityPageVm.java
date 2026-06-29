package ch.so.agi.datenportal.web.view;

import java.util.List;
import java.util.Optional;

public record StructureQualityPageVm(
        PageChromeVm chrome,
        String title,
        List<AttributeRowVm> attributes,
        Optional<DataModelVm> dataModel) {

    public StructureQualityPageVm {
        attributes = List.copyOf(attributes);
        dataModel = dataModel == null ? Optional.empty() : dataModel;
    }
}
