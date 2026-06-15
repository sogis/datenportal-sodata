package ch.so.agi.datenportal.catalog.service;

import ch.so.agi.datenportal.catalog.domain.AccessLevel;
import ch.so.agi.datenportal.catalog.domain.Catalog;
import ch.so.agi.datenportal.catalog.domain.CatalogSnapshot;
import ch.so.agi.datenportal.catalog.domain.DatasetEntry;
import ch.so.agi.datenportal.catalog.domain.DatasetIssueEntry;
import ch.so.agi.datenportal.catalog.domain.DatasetSeriesEntry;
import ch.so.agi.datenportal.catalog.domain.DistributionFormat;
import ch.so.agi.datenportal.catalog.domain.DistributionLink;
import ch.so.agi.datenportal.catalog.domain.Office;
import ch.so.agi.datenportal.catalog.domain.Theme;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public final class StaticCatalogFactory {

    private static final Instant SNAPSHOT_LOADED_AT = Instant.parse("2026-06-14T08:00:00Z");

    private static final Office AGI = new Office(
            "agi",
            "Amt für Geoinformation",
            Optional.of("AGI"));
    private static final Office AFIN = new Office(
            "afin",
            "Amt für Finanzen",
            Optional.of("AFIN"));
    private static final Office STAT = new Office(
            "statistik",
            "Fachstelle Statistik",
            Optional.of("STAT"));

    private static final Theme FINANZEN = new Theme("finanzen", "Finanzen");
    private static final Theme VERKEHR = new Theme("verkehr", "Verkehr");
    private static final Theme RAUM = new Theme("raum", "Raum und Umwelt");
    private static final Theme BEVOELKERUNG = new Theme("bevoelkerung", "Bevölkerung");

    public CatalogSnapshot createSnapshot() {
        var catalog = new Catalog(
                List.of(
                        dataset(
                                "steuerfuss-gemeinden",
                                "Steuerfuss Gemeinden",
                                "Steuersätze der Solothurner Gemeinden.",
                                AFIN,
                                FINANZEN,
                                LocalDate.parse("2026-05-20"),
                                "https://data.so.ch/datasets/steuerfuss-gemeinden"),
                        dataset(
                                "verkehrszaehlstellen",
                                "Verkehrszählstellen",
                                "Standorte und Angaben zu Zählstellen.",
                                AGI,
                                VERKEHR,
                                LocalDate.parse("2026-05-14"),
                                "https://data.so.ch/datasets/verkehrszaehlstellen")),
                List.of(
                        new DatasetSeriesEntry(
                                "gemeindegrenzen",
                                "Gemeindegrenzen",
                                "Digitale Abgrenzung der Solothurner Gemeinden.",
                                AGI,
                                AGI,
                                List.of(RAUM),
                                List.of("Gemeinden", "Grenzen"),
                                AccessLevel.OPEN,
                                List.of(
                                        issue(
                                                "gemeindegrenzen-2026-05",
                                                "Gemeindegrenzen Mai 2026",
                                                "Digitale Abgrenzung der Solothurner Gemeinden, Ausgabe Mai 2026.",
                                                AGI,
                                                RAUM,
                                                LocalDate.parse("2026-05-18"),
                                                "Mai 2026",
                                                true,
                                                "https://data.so.ch/series/gemeindegrenzen/2026-05"),
                                        issue(
                                                "gemeindegrenzen-2026-04",
                                                "Gemeindegrenzen April 2026",
                                                "Digitale Abgrenzung der Solothurner Gemeinden, Ausgabe April 2026.",
                                                AGI,
                                                RAUM,
                                                LocalDate.parse("2026-04-18"),
                                                "April 2026",
                                                false,
                                                "https://data.so.ch/series/gemeindegrenzen/2026-04"))),
                        new DatasetSeriesEntry(
                                "bevoelkerungsstatistik",
                                "Bevölkerungsstatistik",
                                "Bevölkerung nach Alter, Geschlecht und Gemeinde.",
                                STAT,
                                STAT,
                                List.of(BEVOELKERUNG),
                                List.of("Bevölkerung", "Gemeinden"),
                                AccessLevel.OPEN,
                                List.of(
                                        issue(
                                                "bevoelkerungsstatistik-2025",
                                                "Bevölkerungsstatistik 2025",
                                                "Bevölkerung nach Alter, Geschlecht und Gemeinde für das Jahr 2025.",
                                                STAT,
                                                BEVOELKERUNG,
                                                LocalDate.parse("2026-05-08"),
                                                "2025",
                                                false,
                                                "https://data.so.ch/series/bevoelkerungsstatistik/2025"),
                                        issue(
                                                "bevoelkerungsstatistik-2024",
                                                "Bevölkerungsstatistik 2024",
                                                "Bevölkerung nach Alter, Geschlecht und Gemeinde für das Jahr 2024.",
                                                STAT,
                                                BEVOELKERUNG,
                                                LocalDate.parse("2025-05-08"),
                                                "2024",
                                                false,
                                                "https://data.so.ch/series/bevoelkerungsstatistik/2024")))));

        return CatalogSnapshot.of(catalog, SNAPSHOT_LOADED_AT, "StaticCatalogFactory");
    }

    private static DatasetEntry dataset(
            String identifier,
            String title,
            String description,
            Office office,
            Theme theme,
            LocalDate modified,
            String baseUrl) {
        return new DatasetEntry(
                identifier,
                title,
                description,
                office,
                office,
                List.of(theme),
                List.of(title, theme.displayName()),
                modified,
                AccessLevel.OPEN,
                List.of(
                        distribution(baseUrl + "/access/csv", baseUrl + "/download.csv", DistributionFormat.CSV),
                        distribution(baseUrl + "/access/xlsx", baseUrl + "/download.xlsx", DistributionFormat.XLSX),
                        distribution(baseUrl + "/access/parquet", baseUrl + "/download.parquet", DistributionFormat.PARQUET)));
    }

    private static DatasetIssueEntry issue(
            String identifier,
            String title,
            String description,
            Office office,
            Theme theme,
            LocalDate modified,
            String issueLabel,
            boolean currentIssue,
            String baseUrl) {
        return new DatasetIssueEntry(
                identifier,
                title,
                description,
                office,
                office,
                List.of(theme),
                List.of(title, issueLabel),
                modified,
                AccessLevel.OPEN,
                List.of(
                        distribution(baseUrl + "/access/csv", baseUrl + "/download.csv", DistributionFormat.CSV),
                        distribution(baseUrl + "/access/xlsx", baseUrl + "/download.xlsx", DistributionFormat.XLSX),
                        distribution(baseUrl + "/access/parquet", baseUrl + "/download.parquet", DistributionFormat.PARQUET)),
                issueLabel,
                currentIssue);
    }

    private static DistributionLink distribution(String accessUrl, String downloadUrl, DistributionFormat format) {
        return new DistributionLink(URI.create(accessUrl), URI.create(downloadUrl), format);
    }
}
