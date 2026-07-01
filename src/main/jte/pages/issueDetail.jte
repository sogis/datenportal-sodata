@import ch.so.agi.datenportal.web.view.IssueDetailPageVm
@param IssueDetailPageVm page
@template.layouts.main(chrome = page.chrome(), content = @`
  <section class="dp-detail-page dp-issue-detail-page">
    <div class="dp-container">
      <p class="dp-detail-kicker">
        Datenreihe:
        <a href="${page.seriesHref()}">${page.seriesTitle()}</a>
      </p>
      @template.components.detailHero(
        title = page.title(),
        description = page.description(),
        typeLabel = "Ausgabe",
        accessState = page.accessState(),
        structureDescribed = page.structureDescribed(),
        modifiedLabel = page.modifiedLabel(),
        issuedLabel = page.issuedLabel(),
        showFacts = false,
        titleBadgeLabel = page.currentIssue() ? "Aktuelle Ausgabe" : ""
      )
      <div class="dp-detail-summary-layout">
        <div class="dp-detail-summary-main">
          @template.components.detailFeatureDownloadStrip(features = page.features(), downloads = page.downloads())
          @template.components.metadataSection(section = page.overview())
          @if(!page.temporalCoverage().items().isEmpty())
            @template.components.metadataSection(section = page.temporalCoverage())
          @endif
          @if(!page.topics().items().isEmpty())
            @template.components.metadataSection(section = page.topics())
          @endif
          @template.components.contactMetadataSection(section = page.responsibilitiesContact())
          @template.components.relatedIssuesCard(relatedIssues = page.relatedIssues())
        </div>
        <aside class="dp-detail-side" aria-label="Daten nutzen">
          @template.components.detailActionPanel(
            structureQualityOriginHref = page.structureQualityOriginHref(),
            structureQualityOriginLabel = "Struktur, Qualität und Herkunft",
            usageHref = page.usageHref(),
            openData = page.accessState().openData()
          )
        </aside>
      </div>
    </div>
  </section>
`)
