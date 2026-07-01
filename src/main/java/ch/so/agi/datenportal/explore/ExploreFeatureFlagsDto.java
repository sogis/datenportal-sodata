package ch.so.agi.datenportal.explore;

public record ExploreFeatureFlagsDto(
        boolean charts,
        boolean localHistory,
        boolean aiAssistant,
        boolean webR,
        boolean vega,
        boolean mosaic,
        boolean geospatial) {
}
