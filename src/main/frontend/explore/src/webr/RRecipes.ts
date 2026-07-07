import type {SqlResultSnapshot} from '../results/sqlResultSnapshot';

export interface RRecipe {
  id: string;
  title: string;
  code: string;
}

const PLOT_ROW_LIMIT = 10_000;

export function buildRRecipes(snapshot: SqlResultSnapshot | undefined, dataFrameName = 'daten'): RRecipe[] {
  if (!snapshot) {
    return [
      {
        id: 'r-example',
        title: 'R-Beispiel',
        code: 'werte <- 1:5\ndata.frame(wert = werte, quadrat = werte^2)'
      }
    ];
  }

  const recipes: RRecipe[] = [
    {
      id: 'data-overview',
      title: 'Datenüberblick',
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

  const date = pickDateColumn(snapshot.columns);
  const numeric = pickNumericMeasure(snapshot.columns, date?.name);
  const category = pickCategoryColumn(snapshot.columns);

  if (numeric) {
    recipes.push({
      id: 'numeric-summary',
      title: 'Numerische Zusammenfassung',
      code: `numeric_cols <- names(${dataFrameName})[sapply(${dataFrameName}, is.numeric)]\nsummary(${dataFrameName}[numeric_cols])`
    });
    recipes.push({
      id: 'histogram',
      title: `Histogramm ${swissQuote(numeric.name)}`,
      code: `library(ggplot2)\nplot_limit <- ${PLOT_ROW_LIMIT}\nplot_data <- ${dataFrameName}[!is.na(${dataFrameName}[[${rString(numeric.name)}]]), , drop = FALSE]\nplot_data <- head(plot_data, plot_limit)\nbins <- min(30, max(5, floor(sqrt(max(1, nrow(plot_data))))))\nggplot(plot_data, aes(x = .data[[${rString(numeric.name)}]])) +\n  geom_histogram(bins = bins, fill = "#d00009", color = "white") +\n  labs(title = ${rString(`Verteilung ${numeric.name}`)}, x = ${rString(numeric.name)}, y = "Häufigkeit") +\n  theme_minimal()`
    });
  }

  if (numeric && category) {
    recipes.push({
      id: 'boxplot-category',
      title: `Boxplot ${swissQuote(numeric.name)} nach ${swissQuote(category.name)}`,
      code: `library(ggplot2)\nplot_limit <- ${PLOT_ROW_LIMIT}\nplot_data <- ${dataFrameName}[!is.na(${dataFrameName}[[${rString(category.name)}]]) & !is.na(${dataFrameName}[[${rString(numeric.name)}]]), , drop = FALSE]\ntop_categories <- names(sort(table(plot_data[[${rString(category.name)}]]), decreasing = TRUE))[seq_len(min(8, length(unique(plot_data[[${rString(category.name)}]]))))]\nplot_data <- plot_data[plot_data[[${rString(category.name)}]] %in% top_categories, , drop = FALSE]\nplot_data <- head(plot_data, plot_limit)\nggplot(plot_data, aes(x = .data[[${rString(category.name)}]], y = .data[[${rString(numeric.name)}]])) +\n  geom_boxplot(fill = "#e8eff6", color = "#24364b") +\n  coord_flip() +\n  labs(title = ${rString(`${numeric.name} nach ${category.name}`)}, x = ${rString(category.name)}, y = ${rString(numeric.name)}) +\n  theme_minimal()`
    });
  }

  if (numeric && date) {
    recipes.push({
      id: 'trend',
      title: `Trend ${swissQuote(numeric.name)} nach ${swissQuote(date.name)}`,
      code: `library(ggplot2)\ntrend_data <- aggregate(${dataFrameName}[[${rString(numeric.name)}]], by = list(${date.name} = ${dataFrameName}[[${rString(date.name)}]]), FUN = mean, na.rm = TRUE)\nnames(trend_data) <- c(${rString(date.name)}, ${rString(numeric.name)})\ntrend_data <- trend_data[order(trend_data[[${rString(date.name)}]]), , drop = FALSE]\nggplot(trend_data, aes(x = .data[[${rString(date.name)}]], y = .data[[${rString(numeric.name)}]])) +\n  geom_line(color = "#d00009") +\n  geom_point(color = "#24364b") +\n  labs(title = ${rString(`Trend ${numeric.name}`)}, x = ${rString(date.name)}, y = ${rString(numeric.name)}) +\n  theme_minimal()`
    });
  }

  return recipes;
}

function pickNumericMeasure(columns: SqlResultSnapshot['columns'], dateName?: string) {
  return columns
    .filter((column) => column.name !== dateName && (column.rType === 'numeric' || column.rType === 'integer'))
    .sort((left, right) => numericScore(right) - numericScore(left))[0];
}

function numericScore(column: SqlResultSnapshot['columns'][number]): number {
  const name = column.name.toLowerCase();
  let score = column.rType === 'numeric' ? 10 : 4;
  if (column.roles.includes('measure')) {
    score += 20;
  }
  if (/(messwert|wert|value|betrag|anteil|quote|rate|index|flaeche|fläche|hoehe|höhe|laenge|länge)/.test(name)) {
    score += 10;
  }
  if (/(anzahl|count|summe|total)$/.test(name)) {
    score += 4;
  }
  if (column.roles.includes('year') || /(^|_)(jahr|year|monat|month|tag|day)($|_)/.test(name)) {
    score -= 25;
  }
  if (/(^|_)(id|nr|nummer|number|code|plz|bfsnr)($|_)/.test(name)) {
    score -= 20;
  }
  return score;
}

function pickCategoryColumn(columns: SqlResultSnapshot['columns']) {
  return columns
    .filter((column) => column.rType === 'character')
    .sort((left, right) => categoryScore(right) - categoryScore(left))[0];
}

function categoryScore(column: SqlResultSnapshot['columns'][number]): number {
  const name = column.name.toLowerCase();
  let score = 0;
  if (column.roles.includes('category') || column.roles.includes('municipality') || column.roles.includes('label')) {
    score += 20;
  }
  if (/(gemeinde|name|typ|type|klasse|kategorie|category|parameter|status|thema)/.test(name)) {
    score += 10;
  }
  if (/(^|_)(id|nr|nummer|number|code)($|_)/.test(name)) {
    score -= 20;
  }
  return score;
}

function pickDateColumn(columns: SqlResultSnapshot['columns']) {
  return columns.find((column) => column.rType === 'Date' || column.rType === 'POSIXct')
    ?? columns.find((column) => column.roles.includes('year'))
    ?? columns.find((column) => /(^|_)(jahr|year)($|_)/.test(column.name.toLowerCase()));
}

function swissQuote(value: string): string {
  return `«${value}»`;
}

function rString(value: string): string {
  return JSON.stringify(value);
}
