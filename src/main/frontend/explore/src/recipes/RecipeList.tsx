import type {ExploreRecipeDto, ExploreTableDto} from '../app/ExploreContext';

const categoryLabels: Record<ExploreRecipeDto['category'], string> = {
  preview: 'Vorschau',
  profile: 'Profil',
  quality: 'Qualität',
  category: 'Kategorie',
  numeric: 'Numerik',
  time: 'Zeit',
  custom: 'Weitere'
};

export function RecipeList({
  recipes,
  tables,
  selectedRecipeId,
  onSelect,
  onRun
}: {
  recipes: ExploreRecipeDto[];
  tables: ExploreTableDto[];
  selectedRecipeId?: string;
  onSelect: (recipe: ExploreRecipeDto) => void;
  onRun: (recipe: ExploreRecipeDto) => void;
}) {
  if (recipes.length === 0) {
    return <p className="dp-explore-muted">Keine Beispielabfragen verfügbar.</p>;
  }

  return (
    <div className="dp-explore-recipes" aria-label="Beispielabfragen">
      {tables.map((table) => {
        const tableRecipes = recipes.filter((recipe) => recipe.tableId === table.id);
        if (tableRecipes.length === 0) {
          return null;
        }
        return (
          <section key={table.id} className="dp-explore-recipes__table" aria-labelledby={`recipes-${table.id}`}>
            <h4 id={`recipes-${table.id}`}>{table.title}</h4>
            <div className="dp-explore-recipes__list">
              {tableRecipes.map((recipe) => (
                <button
                  key={recipe.id}
                  type="button"
                  className={selectedRecipeId === recipe.id ? 'dp-explore-recipe is-active' : 'dp-explore-recipe'}
                  aria-pressed={selectedRecipeId === recipe.id}
                  onClick={() => onSelect(recipe)}
                  onDoubleClick={() => onRun(recipe)}
                >
                  <span className="dp-explore-recipe__meta">{categoryLabels[recipe.category]}</span>
                  <span className="dp-explore-recipe__title">{recipe.title}</span>
                  <span className="dp-explore-recipe__description">{recipe.description}</span>
                </button>
              ))}
            </div>
          </section>
        );
      })}
    </div>
  );
}

