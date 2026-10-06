package ch.so.agi.datenportal.web.view;

import ch.so.agi.datenportal.support.metadata.MetadataTextRenderer.Html;
import java.util.List;

public record SeriesDetailPageVm(
        PageChromeVm chrome,
        String title,
        Html description,
        List<SeriesIssueVm> issues) {

    public SeriesDetailPageVm {
        issues = List.copyOf(issues);
    }
}
