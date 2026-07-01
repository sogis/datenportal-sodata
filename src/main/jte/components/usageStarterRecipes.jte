@import java.util.List
@import ch.so.agi.datenportal.web.view.StarterRecipeVm
@param List<StarterRecipeVm> recipes
<section class="dp-usage-section dp-usage-recipes" aria-labelledby="usage-recipes-title">
  <h2 id="usage-recipes-title">Starter-Rezepte</h2>
  <ul class="dp-usage-recipe-list">
    @for(var recipe : recipes)
      <li class="dp-usage-recipe">
        <div class="dp-usage-recipe__title">
          <span class="dp-usage-recipe__icon" aria-hidden="true">
            <img src="${recipe.iconSrc()}" alt="${recipe.iconAlt()}" width="24" height="24" loading="lazy">
          </span>
          <span class="dp-usage-recipe__name">${recipe.title()}</span>
        </div>
        <span>${recipe.description()}</span>
        <a class="dp-usage-recipe__link" href="${recipe.href()}">
          <span>${recipe.linkLabel()}</span>
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-box-arrow-up-right" viewBox="0 0 16 16" aria-hidden="true">
            <path fill-rule="evenodd" d="M8.636 3.5a.5.5 0 0 0-.5-.5H1.5A1.5 1.5 0 0 0 0 4.5v10A1.5 1.5 0 0 0 1.5 16h10a1.5 1.5 0 0 0 1.5-1.5V7.864a.5.5 0 0 0-1 0V14.5a.5.5 0 0 1-.5.5h-10a.5.5 0 0 1-.5-.5v-10a.5.5 0 0 1 .5-.5h6.636a.5.5 0 0 0 .5-.5"/>
            <path fill-rule="evenodd" d="M16 .5a.5.5 0 0 0-.5-.5h-5a.5.5 0 0 0 0 1h3.793L6.146 9.146a.5.5 0 1 0 .708.708L15 1.707V5.5a.5.5 0 0 0 1 0z"/>
          </svg>
        </a>
      </li>
    @endfor
  </ul>
</section>
