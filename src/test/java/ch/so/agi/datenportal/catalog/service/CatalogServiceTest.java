package ch.so.agi.datenportal.catalog.service;

import static org.assertj.core.api.Assertions.assertThat;

import ch.so.agi.datenportal.catalog.domain.AccessLevel;
import ch.so.agi.datenportal.catalog.domain.Catalog;
import ch.so.agi.datenportal.catalog.domain.CatalogSnapshot;
import ch.so.agi.datenportal.catalog.domain.DatasetEntry;
import ch.so.agi.datenportal.catalog.domain.DistributionFormat;
import ch.so.agi.datenportal.catalog.domain.DistributionLink;
import ch.so.agi.datenportal.catalog.domain.Office;
import ch.so.agi.datenportal.catalog.domain.Theme;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class CatalogServiceTest {

    @Test
    void returnsInitialSnapshotAndReadAccessors() {
        CatalogService catalogService = new CatalogService(createInitialSnapshot());

        assertThat(catalogService.currentSnapshot().sourceDescription()).isEqualTo("initial");
        assertThat(catalogService.visibleEntries())
                .extracting(entry -> entry.identifier())
                .containsExactly(
                        "replacement-series",
                        "replacement-dataset");
        assertThat(catalogService.findVisibleEntry("replacement-series")).isPresent();
        assertThat(catalogService.findAnyEntry("replacement-series-2026")).isPresent();
    }

    @Test
    void replaceSnapshotSwapsReadableState() {
        CatalogService catalogService = new CatalogService(createInitialSnapshot());
        CatalogSnapshot replacement = CatalogSnapshot.of(
                new Catalog(
                        List.of(new DatasetEntry(
                                "replacement-dataset",
                                "Replacement",
                                "Beschreibung",
                                new Office("office", "Amt", Optional.empty()),
                                new Office("office", "Amt", Optional.empty()),
                                List.of(new Theme("theme", "Thema")),
                                List.of("Replacement"),
                                LocalDate.parse("2026-06-01"),
                                AccessLevel.OPEN,
                                List.of(new DistributionLink(URI.create("https://example.com/replacement.csv"), DistributionFormat.CSV)))),
                        List.of()),
                Instant.parse("2026-06-14T09:00:00Z"),
                "replacement");

        catalogService.replaceSnapshot(replacement);

        assertThat(catalogService.currentSnapshot().sourceDescription()).isEqualTo("replacement");
        assertThat(catalogService.visibleEntries())
                .extracting(entry -> entry.identifier())
                .containsExactly("replacement-dataset");
    }

    private static CatalogSnapshot createInitialSnapshot() {
        return CatalogSnapshot.of(
                new Catalog(
                        List.of(new DatasetEntry(
                                "replacement-dataset",
                                "Replacement",
                                "Beschreibung",
                                new Office("office", "Amt", Optional.empty()),
                                new Office("office", "Amt", Optional.empty()),
                                List.of(new Theme("theme", "Thema")),
                                List.of("Replacement"),
                                LocalDate.parse("2026-05-01"),
                                AccessLevel.OPEN,
                                List.of(new DistributionLink(URI.create("https://example.com/replacement.csv"), DistributionFormat.CSV)))),
                        List.of(new ch.so.agi.datenportal.catalog.domain.DatasetSeriesEntry(
                                "replacement-series",
                                "Replacement Series",
                                "Beschreibung",
                                new Office("office", "Amt", Optional.empty()),
                                new Office("office", "Amt", Optional.empty()),
                                List.of(new Theme("theme", "Thema")),
                                List.of("Series"),
                                AccessLevel.OPEN,
                                List.of(new ch.so.agi.datenportal.catalog.domain.DatasetIssueEntry(
                                        "replacement-series-2026",
                                        "Replacement Series 2026",
                                        "Beschreibung",
                                        new Office("office", "Amt", Optional.empty()),
                                        new Office("office", "Amt", Optional.empty()),
                                        List.of(new Theme("theme", "Thema")),
                                        List.of("Series"),
                                        LocalDate.parse("2026-06-01"),
                                        AccessLevel.OPEN,
                                        List.of(new DistributionLink(URI.create("https://example.com/replacement-series.csv"), DistributionFormat.CSV)),
                                        "2026",
                                        true))))),
                Instant.parse("2026-06-14T08:00:00Z"),
                "initial");
    }
}
