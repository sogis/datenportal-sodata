package ch.so.agi.datenportal.explore;

import java.util.List;

public record ExploreRLaboratoryDto(
        String dataFrameName,
        String runtimeBaseUrl,
        String packageRepoUrl,
        List<String> packages,
        int recommendedRows,
        int warningRows,
        int hardRows,
        int plotWidth,
        int plotHeight) {

    public ExploreRLaboratoryDto {
        packages = packages == null ? List.of() : List.copyOf(packages);
    }
}
