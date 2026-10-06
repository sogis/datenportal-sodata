package ch.so.agi.datenportal.catalog.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record Catalog(
        List<DatasetEntry> datasets,
        List<DatasetSeriesEntry> datasetSeries,
        Optional<String> description) {

    public Catalog(List<DatasetEntry> datasets, List<DatasetSeriesEntry> datasetSeries) {
        this(datasets, datasetSeries, Optional.empty());
    }

    public Catalog {
        datasets = List.copyOf(datasets);
        datasetSeries = List.copyOf(datasetSeries);
        description = description == null ? Optional.empty() : description.filter(text -> !text.isBlank());
    }

    /** Build the public view only after validating the complete source catalog. */
    public Catalog publishedView() {
        var visibleSeries = new ArrayList<DatasetSeriesEntry>();
        for (var series : datasetSeries) {
            if (!isPublished(series.metadata())) {
                continue;
            }
            var issues = series.issues().stream()
                    .filter(issue -> isPublished(issue.metadata())).toList();
            if (!issues.isEmpty()) {
                visibleSeries.add(new DatasetSeriesEntry(series.identifier(), series.title(),
                        series.description(), series.publisher(), series.creator(), series.themes(),
                        series.keywords(), series.accessLevel(), series.metadata(), issues));
            }
        }
        return new Catalog(datasets.stream().filter(dataset -> isPublished(dataset.metadata())).toList(),
                visibleSeries, description);
    }

    private static boolean isPublished(CatalogEntryMetadata metadata) {
        return metadata.publicationStatus().map("published"::equals).orElse(false);
    }

    public List<CatalogEntry> topLevelEntries() {
        var entries = new ArrayList<CatalogEntry>(datasets.size() + datasetSeries.size());
        entries.addAll(datasets);
        entries.addAll(datasetSeries);
        return List.copyOf(entries);
    }

    public int datasetCount() {
        return datasets.size();
    }

    public int seriesCount() {
        return datasetSeries.size();
    }

    public int issueCount() {
        return datasetSeries.stream()
                .mapToInt(DatasetSeriesEntry::issueCount)
                .sum();
    }
}
