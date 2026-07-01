package ch.so.agi.datenportal.explore;

import java.util.List;
import java.util.Optional;

public record ExploreTableDto(
        String id,
        String name,
        String title,
        Optional<String> description,
        String parquetUrl,
        Optional<Long> sizeBytes,
        Optional<Long> rowCountEstimate,
        boolean primary,
        List<ExploreColumnDto> columns) {

    public ExploreTableDto {
        description = description == null ? Optional.empty() : description.filter(value -> !value.isBlank());
        sizeBytes = sizeBytes == null ? Optional.empty() : sizeBytes;
        rowCountEstimate = rowCountEstimate == null ? Optional.empty() : rowCountEstimate;
        columns = columns == null ? List.of() : List.copyOf(columns);
    }
}
