package ch.so.agi.datenportal.web.view;

import java.util.Optional;

public record QualityVm(
        Optional<String> modelName,
        Optional<String> validationReportName,
        Optional<String> validationReportHref,
        Optional<String> missingModelMessage) {

    public QualityVm {
        modelName = modelName == null ? Optional.empty() : modelName;
        validationReportName = validationReportName == null ? Optional.empty() : validationReportName;
        validationReportHref = validationReportHref == null ? Optional.empty() : validationReportHref;
        missingModelMessage = missingModelMessage == null ? Optional.empty() : missingModelMessage;
    }
}
