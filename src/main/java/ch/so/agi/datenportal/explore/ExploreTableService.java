package ch.so.agi.datenportal.explore;

import ch.so.agi.datenportal.catalog.domain.DatasetAttribute;
import ch.so.agi.datenportal.catalog.domain.DatasetEntry;
import ch.so.agi.datenportal.catalog.domain.DistributionFormat;
import ch.so.agi.datenportal.catalog.domain.DistributionLink;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public final class ExploreTableService {

    private final ExploreSqlNameSanitizer sqlNameSanitizer;
    private final ExploreColumnRoleDetector roleDetector;

    public ExploreTableService(
            ExploreSqlNameSanitizer sqlNameSanitizer,
            ExploreColumnRoleDetector roleDetector) {
        this.sqlNameSanitizer = sqlNameSanitizer;
        this.roleDetector = roleDetector;
    }

    public List<ExploreTableDto> buildTables(DatasetEntry dataset) {
        List<DistributionLink> parquetLinks = dataset.distributions().stream()
                .filter(link -> link.format() == DistributionFormat.PARQUET)
                .toList();
        if (parquetLinks.isEmpty()) {
            return List.of();
        }

        var usedNames = new LinkedHashSet<String>();
        var tables = new ArrayList<ExploreTableDto>();
        for (int index = 0; index < parquetLinks.size(); index++) {
            tables.add(buildTable(dataset, parquetLinks.get(index), index == 0, usedNames));
        }
        return List.copyOf(tables);
    }

    private ExploreTableDto buildTable(
            DatasetEntry dataset,
            DistributionLink distribution,
            boolean primary,
            LinkedHashSet<String> usedNames) {
        String rawName = fileBaseName(distribution.preferredHref()).orElse(dataset.identifier());
        String safeName = uniqueSafeName(rawName, usedNames);
        return new ExploreTableDto(
                safeName,
                safeName,
                dataset.title(),
                Optional.of(dataset.description()),
                distribution.preferredHref().toString(),
                Optional.empty(),
                dataset.metadata().structureSummary().map(summary -> (long) summary.objectCount()),
                primary,
                buildColumns(dataset.metadata().attributes()));
    }

    private List<ExploreColumnDto> buildColumns(List<DatasetAttribute> attributes) {
        return attributes.stream()
                .map(attribute -> {
                    var source = new ExploreColumnSource(
                            attribute.name(),
                            attribute.dataType(),
                            attribute.description());
                    return new ExploreColumnDto(
                            attribute.name(),
                            attribute.dataType(),
                            Optional.empty(),
                            Optional.of(attribute.mandatory()),
                            attribute.description(),
                            Optional.empty(),
                            roleDetector.detectRoles(source).stream().toList());
                })
                .toList();
    }

    private String uniqueSafeName(String rawName, LinkedHashSet<String> usedNames) {
        String base = sqlNameSanitizer.toSafeTableName(rawName);
        String candidate = base;
        int suffix = 2;
        while (!usedNames.add(candidate)) {
            candidate = base + "_" + suffix;
            suffix++;
        }
        return candidate;
    }

    private static Optional<String> fileBaseName(URI uri) {
        String path = uri.getPath();
        if (path == null || path.isBlank()) {
            return Optional.empty();
        }
        int slash = path.lastIndexOf('/');
        String fileName = slash >= 0 ? path.substring(slash + 1) : path;
        if (fileName.isBlank()) {
            return Optional.empty();
        }
        int dot = fileName.lastIndexOf('.');
        return Optional.of(dot > 0 ? fileName.substring(0, dot) : fileName);
    }
}
