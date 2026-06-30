@import ch.so.agi.datenportal.web.view.UsagePageVm
@param UsagePageVm page
@template.layouts.main(chrome = page.chrome(), content = @`
  <section class="dp-detail-page dp-usage-page">
    <div class="dp-container">
      <header class="dp-detail-hero dp-usage-hero">
        <h1 class="dp-detail-title">${page.title()}</h1>
      </header>
      <div class="dp-detail-summary-layout dp-usage-layout">
        <div class="dp-detail-summary-main">
          @template.components.usageDirectAccess(rows = page.directAccessRows())
          @if(!page.codeExamples().isEmpty())
            @template.components.usageCodeExamples(examples = page.codeExamples())
          @endif
          @template.components.usageStarterRecipes(recipes = page.starterRecipes())
        </div>
      </div>
    </div>
  </section>
`)
