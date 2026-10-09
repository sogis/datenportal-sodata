package ch.so.agi.datenportal.explore;

/** Shared browser geometry limits and the canton LV95 background service. */
public record ExploreMapDto(
        String crs, String wmtsUrl, String layer, String attribution,
        int maxFeatures, int maxBytes, int maxCoordinates) {
    public static ExploreMapDto defaults() {
        return new ExploreMapDto("EPSG:2056",
                "https://geo.so.ch/api/wmts/1.0.0/ch.so.agi.hintergrundkarte_sw/default/{TileMatrixSet}/{TileMatrix}/{TileRow}/{TileCol}.png",
                "ch.so.agi.hintergrundkarte_sw", "Hintergrundkarte: Kanton Solothurn",
                10_000, 32 * 1024 * 1024, 1_000_000);
    }
}
