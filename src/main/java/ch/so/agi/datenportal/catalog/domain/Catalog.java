package ch.so.agi.datenportal.catalog.domain;

import java.util.ArrayList;
import java.util.List;

public record Catalog(
        List<DatasetEntry> datasets,
        List<DatasetSeriesEntry> datasetSeries) {

    public Catalog {
        datasets = List.copyOf(datasets);
        datasetSeries = List.copyOf(datasetSeries);
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
