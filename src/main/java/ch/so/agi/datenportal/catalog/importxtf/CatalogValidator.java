package ch.so.agi.datenportal.catalog.importxtf;

import ch.so.agi.datenportal.catalog.domain.Catalog;
import ch.so.agi.datenportal.catalog.domain.CatalogEntry;
import ch.so.agi.datenportal.catalog.domain.DatasetAttribute;
import ch.so.agi.datenportal.catalog.domain.DatasetEntry;
import ch.so.agi.datenportal.catalog.domain.DatasetIssueEntry;
import ch.so.agi.datenportal.catalog.domain.DatasetSeriesEntry;
import ch.so.agi.datenportal.catalog.domain.DistributionLink;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public final class CatalogValidator {

    public CatalogValidationResult validate(Catalog catalog) {
        List<String> warnings = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        if (catalog.topLevelEntries().isEmpty()) {
            errors.add("Catalog must contain at least one dataset or dataset series.");
        }

        Set<String> identifiers = new HashSet<>();
        for (DatasetEntry dataset : catalog.datasets()) {
            validateIdentifier(dataset, "dataset", identifiers, errors);
            validateDistributions(dataset.identifier(), dataset.distributions(), errors);
            validateAttributes(dataset.identifier(), dataset.metadata().attributes(), errors);
        }

        for (DatasetSeriesEntry series : catalog.datasetSeries()) {
            validateIdentifier(series, "dataset series", identifiers, errors);
            validateAttributes(series.identifier(), series.metadata().attributes(), errors);

            if (series.issues().isEmpty()) {
                errors.add("Dataset series '" + series.identifier() + "' must contain at least one issue.");
            }

            long currentIssueCount = series.issues().stream()
                    .filter(DatasetIssueEntry::currentIssue)
                    .count();
            if (currentIssueCount == 0) {
                warnings.add("Dataset series '" + series.identifier() + "' has no explicit current issue; newest modified issue will be used.");
            } else if (currentIssueCount > 1) {
                warnings.add("Dataset series '" + series.identifier() + "' has multiple current issues; newest modified issue will be used.");
            }

            for (DatasetIssueEntry issue : series.issues()) {
                validateIdentifier(issue, "dataset issue", identifiers, errors);
                validateDistributions(issue.identifier(), issue.distributions(), errors);
                validateAttributes(issue.identifier(), issue.metadata().attributes(), errors);
            }
        }

        return new CatalogValidationResult(warnings, errors);
    }

    public void validateOrThrow(Catalog catalog) {
        validate(catalog).throwIfInvalid();
    }

    private static void validateIdentifier(
            CatalogEntry entry,
            String type,
            Set<String> seenIdentifiers,
            List<String> errors) {
        if (!seenIdentifiers.add(entry.identifier())) {
            errors.add("Duplicate identifier '" + entry.identifier() + "' detected for " + type + ".");
        }
    }

    private static void validateDistributions(String ownerIdentifier, List<DistributionLink> distributions, List<String> errors) {
        if (distributions.isEmpty()) {
            errors.add("Entry '" + ownerIdentifier + "' must contain at least one distribution.");
        }
    }

    private static void validateAttributes(String ownerIdentifier, List<DatasetAttribute> attributes, List<String> errors) {
        for (DatasetAttribute attribute : attributes) {
            if (attribute.name().isBlank()) {
                errors.add("Entry '" + ownerIdentifier + "' contains a dataset attribute with blank name.");
            }
            if (attribute.dataType().isBlank()) {
                errors.add("Entry '" + ownerIdentifier + "' contains dataset attribute '" + attribute.name()
                        + "' with blank dataType.");
            }
        }
    }
}
