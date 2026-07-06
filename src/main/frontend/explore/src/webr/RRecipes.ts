import type {SqlResultSnapshot} from '../results/sqlResultSnapshot';

export interface RRecipe {
  id: string;
  title: string;
  code: string;
}

export function buildRRecipes(snapshot: SqlResultSnapshot | undefined, dataFrameName = 'daten'): RRecipe[] {
  const recipes: RRecipe[] = [
    {
      id: 'start',
      title: 'Start',
      code: `str(${dataFrameName})\nsummary(${dataFrameName})`
    },
    {
      id: 'structure',
      title: 'Spaltenstruktur',
      code: `daten_schema\nstr(${dataFrameName})`
    },
    {
      id: 'missing',
      title: 'Fehlende Werte',
      code: `data.frame(\n  spalte = names(${dataFrameName}),\n  fehlende = sapply(${dataFrameName}, function(x) sum(is.na(x))),\n  anteil = round(sapply(${dataFrameName}, function(x) mean(is.na(x))) * 100, 1),\n  row.names = NULL\n)`
    }
  ];

  if (!snapshot) {
    return recipes;
  }

  const numeric = snapshot.columns.find((column) => column.rType === 'numeric' || column.rType === 'integer');
  const category = snapshot.columns.find((column) =>
    column.rType === 'character' && (column.roles.includes('category') || column.roles.includes('label') || column.roles.includes('municipality'))
  ) ?? snapshot.columns.find((column) => column.rType === 'character');
  const date = snapshot.columns.find((column) => column.rType === 'Date' || column.rType === 'POSIXct')
    ?? snapshot.columns.find((column) => column.roles.includes('year'));

  if (numeric) {
    recipes.push({
      id: 'numeric-summary',
      title: 'Numerische Zusammenfassung',
      code: `numeric_cols <- names(${dataFrameName})[sapply(${dataFrameName}, is.numeric)]\nsummary(${dataFrameName}[numeric_cols])`
    });
    recipes.push({
      id: 'histogram',
      title: `Histogramm ${numeric.name}`,
      code: `library(ggplot2)\nggplot(${dataFrameName}, aes(x = .data[[${rString(numeric.name)}]])) +\n  geom_histogram(bins = 30, fill = "#d00009", color = "white") +\n  labs(title = ${rString(`Verteilung ${numeric.name}`)}, x = ${rString(numeric.name)}, y = "Häufigkeit") +\n  theme_minimal()`
    });
  }

  if (numeric && category) {
    recipes.push({
      id: 'boxplot-category',
      title: `Boxplot nach ${category.name}`,
      code: `library(ggplot2)\nplot_data <- ${dataFrameName}[!is.na(${dataFrameName}[[${rString(category.name)}]]) & !is.na(${dataFrameName}[[${rString(numeric.name)}]]), , drop = FALSE]\ntop_categories <- names(sort(table(plot_data[[${rString(category.name)}]]), decreasing = TRUE))[1:min(10, length(unique(plot_data[[${rString(category.name)}]])))]\nplot_data <- plot_data[plot_data[[${rString(category.name)}]] %in% top_categories, , drop = FALSE]\nggplot(plot_data, aes(x = .data[[${rString(category.name)}]], y = .data[[${rString(numeric.name)}]])) +\n  geom_boxplot(fill = "#e8eff6", color = "#24364b") +\n  coord_flip() +\n  labs(title = ${rString(`${numeric.name} nach ${category.name}`)}, x = ${rString(category.name)}, y = ${rString(numeric.name)}) +\n  theme_minimal()`
    });
  }

  if (numeric && date) {
    recipes.push({
      id: 'trend',
      title: `Trend nach ${date.name}`,
      code: `library(ggplot2)\ntrend_data <- aggregate(${dataFrameName}[[${rString(numeric.name)}]], by = list(${date.name} = ${dataFrameName}[[${rString(date.name)}]]), FUN = mean, na.rm = TRUE)\nnames(trend_data) <- c(${rString(date.name)}, ${rString(numeric.name)})\nggplot(trend_data, aes(x = .data[[${rString(date.name)}]], y = .data[[${rString(numeric.name)}]])) +\n  geom_line(color = "#d00009") +\n  geom_point(color = "#24364b") +\n  labs(title = ${rString(`Trend ${numeric.name}`)}, x = ${rString(date.name)}, y = ${rString(numeric.name)}) +\n  theme_minimal()`
    });
  }

  return recipes;
}

function rString(value: string): string {
  return JSON.stringify(value);
}
