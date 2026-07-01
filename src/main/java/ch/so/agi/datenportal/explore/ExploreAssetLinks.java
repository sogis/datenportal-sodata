package ch.so.agi.datenportal.explore;

public record ExploreAssetLinks(
        String scriptSrc,
        String styleSrc) {

    public ExploreAssetLinks {
        scriptSrc = clean(scriptSrc);
        styleSrc = clean(styleSrc);
    }

    public static ExploreAssetLinks none() {
        return new ExploreAssetLinks("", "");
    }

    public boolean hasScript() {
        return !scriptSrc.isBlank();
    }

    public boolean hasStyle() {
        return !styleSrc.isBlank();
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
