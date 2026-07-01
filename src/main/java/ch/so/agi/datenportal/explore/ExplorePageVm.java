package ch.so.agi.datenportal.explore;

import ch.so.agi.datenportal.web.view.PageChromeVm;

public record ExplorePageVm(
        PageChromeVm chrome,
        String datasetId,
        String title,
        String breadcrumbLabel,
        String canonicalDatasetUrl,
        String contextJson,
        boolean available,
        String unavailableReason,
        ExploreAssetLinks assetLinks) {

    public ExplorePageVm {
        unavailableReason = unavailableReason == null ? "" : unavailableReason;
        assetLinks = assetLinks == null ? ExploreAssetLinks.none() : assetLinks;
    }
}
