package ch.so.agi.datenportal.web;

import ch.so.agi.datenportal.catalog.domain.DatasetEntry;
import ch.so.agi.datenportal.catalog.domain.DatasetIssueEntry;
import ch.so.agi.datenportal.catalog.domain.DatasetSeriesEntry;
import ch.so.agi.datenportal.web.view.BreadcrumbItemVm;
import ch.so.agi.datenportal.web.view.BreadcrumbVm;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class BreadcrumbFactory {

    public BreadcrumbVm catalog() {
        var items = catalogItems();
        return new BreadcrumbVm(
                List.of(
                        items.get(0),
                        items.get(1),
                        new BreadcrumbItemVm("Daten und Statistiken", Optional.empty(), true)));
    }

    public BreadcrumbVm datasetDetail(DatasetEntry dataset) {
        var items = catalogItems();
        items.add(new BreadcrumbItemVm(dataset.title(), Optional.empty(), true));
        return new BreadcrumbVm(items);
    }

    public BreadcrumbVm datasetExplore(DatasetEntry dataset) {
        var items = catalogItems();
        items.add(new BreadcrumbItemVm(dataset.title(), Optional.of("/datasets/" + encode(dataset.identifier())), false));
        items.add(new BreadcrumbItemVm("Erkunden", Optional.empty(), true));
        return new BreadcrumbVm(items);
    }

    public BreadcrumbVm datasetStructureQualityOrigin(DatasetEntry dataset) {
        var items = catalogItems();
        items.add(new BreadcrumbItemVm(dataset.title(), Optional.of("/datasets/" + encode(dataset.identifier())), false));
        items.add(new BreadcrumbItemVm("Struktur, Qualität und Herkunft", Optional.empty(), true));
        return new BreadcrumbVm(items);
    }

    public BreadcrumbVm datasetUsage(DatasetEntry dataset) {
        var items = catalogItems();
        items.add(new BreadcrumbItemVm(dataset.title(), Optional.of("/datasets/" + encode(dataset.identifier())), false));
        items.add(new BreadcrumbItemVm("Daten verwenden", Optional.empty(), true));
        return new BreadcrumbVm(items);
    }

    public BreadcrumbVm seriesDetail(DatasetSeriesEntry series) {
        var items = catalogItems();
        items.add(new BreadcrumbItemVm(series.title(), Optional.empty(), true));
        return new BreadcrumbVm(items);
    }

    public BreadcrumbVm issueDetail(DatasetSeriesEntry series, DatasetIssueEntry issue) {
        var items = catalogItems();
        items.add(new BreadcrumbItemVm(series.title(), Optional.of("/series/" + encode(series.identifier())), false));
        items.add(new BreadcrumbItemVm(issue.issueLabel(), Optional.empty(), true));
        return new BreadcrumbVm(items);
    }

    public BreadcrumbVm issueStructureQualityOrigin(DatasetSeriesEntry series, DatasetIssueEntry issue) {
        var items = catalogItems();
        items.add(new BreadcrumbItemVm(series.title(), Optional.of("/series/" + encode(series.identifier())), false));
        String issueHref = issueHref(series, issue);
        items.add(new BreadcrumbItemVm(issue.issueLabel(), Optional.of(issueHref), false));
        items.add(new BreadcrumbItemVm("Struktur, Qualität und Herkunft", Optional.empty(), true));
        return new BreadcrumbVm(items);
    }

    public BreadcrumbVm issueUsage(DatasetSeriesEntry series, DatasetIssueEntry issue) {
        var items = catalogItems();
        items.add(new BreadcrumbItemVm(series.title(), Optional.of("/series/" + encode(series.identifier())), false));
        items.add(new BreadcrumbItemVm(issue.issueLabel(), Optional.of(issueHref(series, issue)), false));
        items.add(new BreadcrumbItemVm("Daten verwenden", Optional.empty(), true));
        return new BreadcrumbVm(items);
    }

    public BreadcrumbVm issueExplore(DatasetSeriesEntry series, DatasetIssueEntry issue) {
        var items = catalogItems();
        items.add(new BreadcrumbItemVm(series.title(), Optional.of("/series/" + encode(series.identifier())), false));
        items.add(new BreadcrumbItemVm(issue.issueLabel(), Optional.of(issueHref(series, issue)), false));
        items.add(new BreadcrumbItemVm("Erkunden", Optional.empty(), true));
        return new BreadcrumbVm(items);
    }

    public BreadcrumbVm notFound() {
        return error("Seite nicht gefunden");
    }

    public BreadcrumbVm error(String label) {
        var items = catalogItems();
        items.add(new BreadcrumbItemVm(label, Optional.empty(), true));
        return new BreadcrumbVm(items);
    }

    private static ArrayList<BreadcrumbItemVm> catalogItems() {
        return new ArrayList<>(List.of(
                new BreadcrumbItemVm("so.ch", Optional.of("https://so.ch"), false),
                new BreadcrumbItemVm("Datenportal", Optional.of("/datasets"), false),
                new BreadcrumbItemVm("Daten und Statistiken", Optional.of("/datasets"), false)));
    }

    private static String encode(String value) {
        return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8).replace("+", "%20");
    }

    private static String issueHref(DatasetSeriesEntry series, DatasetIssueEntry issue) {
        return issue.identifier().equals(series.currentIssueOrThrow().identifier())
                ? "/series/" + encode(series.identifier()) + "/issues/current"
                : "/series/" + encode(series.identifier()) + "/issues/" + encode(issue.identifier());
    }
}
