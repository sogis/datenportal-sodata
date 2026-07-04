package ch.so.agi.datenportal.explore;

public record ExploreCatalogDatabaseDto(
        String url,
        String database,
        String schema) {

    public ExploreCatalogDatabaseDto {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("url must not be blank");
        }
        database = database == null || database.isBlank() ? "catalog" : database.trim();
        schema = schema == null || schema.isBlank() ? "opendata" : schema.trim();
    }
}
