package ch.so.agi.datenportal.web.view;

import java.util.Optional;

public record QualityVm(
        Optional<String> modelName,
        String modelHref,
        String validationReportName,
        String validationReportHref,
        Optional<String> missingModelMessage) {

    public QualityVm {
        modelName = modelName == null ? Optional.empty() : modelName;
        missingModelMessage = missingModelMessage == null ? Optional.empty() : missingModelMessage;
    }
}
