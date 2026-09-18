import type {SqlResultSnapshot} from '../results/sqlResultSnapshot';
import {
  isDateLikeName,
  isDateLikeValues,
  isYearLikeName,
  isYearLikeValues,
  snapshotColumnValues
} from '../analysis/resultColumnProfiles';
import {chartColorHex, DEFAULT_CHART_COLOR, MULTI_CHART_COLOR_PALETTE} from '../charts/chartColors';

export interface RRecipe {
  id: string;
  title: string;
  code: string;
}

type SnapshotColumn = SqlResultSnapshot['columns'][number];

const PLOT_ROW_LIMIT = 10_000;
const CATEGORY_PLOT_LIMIT = 15;
// R plots use the same chart colors as the SQL laboratory: single series in Dunkelblau,
// multiple series from the "Mehrfarbig" palette.
const SINGLE_PLOT_COLOR = chartColorHex(DEFAULT_CHART_COLOR);
const TIME_CANDIDATE_LIMIT = 2;
const CATEGORY_CANDIDATE_LIMIT = 2;
const MEASURE_CANDIDATE_LIMIT = 3;
const MULTI_TREND_MEASURE_LIMIT = 3;

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

  const columnValues = snapshotColumnValues(snapshot);
  const timeColumns = pickTimeColumns(snapshot.columns, columnValues);
  const measureColumns = pickMeasureColumns(snapshot.columns, columnValues, timeColumns);
  const categoryColumns = pickCategoryColumns(snapshot.columns, columnValues, timeColumns);
  const measure = measureColumns[0];
  const time = timeColumns[0];
  const category = categoryColumns[0];

  if (snapshot.columns.some((column) => column.rType === 'numeric' || column.rType === 'integer')) {
    recipes.push({
      id: 'numeric-summary',
      title: 'Numerische Zusammenfassung',
      code: `numeric_cols <- names(${dataFrameName})[sapply(${dataFrameName}, is.numeric)]\nsummary(${dataFrameName}[numeric_cols])`
    });
  }

  if (measure) {
    recipes.push(histogramRecipe(measure, dataFrameName));
  }

  categoryColumns.forEach((column, index) => {
    recipes.push(barCategoryRecipe(column, index, dataFrameName));
  });

  if (measure && category) {
    recipes.push(barMeasureCategoryRecipe(measure, category, dataFrameName));
    recipes.push(boxplotRecipe(measure, category, dataFrameName));
  }

  timeColumns.forEach((column, index) => {
    if (measure) {
      recipes.push(trendRecipe(measure, column, index, dataFrameName));
    }
  });

  if (time && measureColumns.length >= 2) {
    recipes.push(multiTrendRecipe(measureColumns.slice(0, MULTI_TREND_MEASURE_LIMIT), time, dataFrameName));
  }

  if (measureColumns.length >= 2) {
    recipes.push(scatterRecipe(measureColumns[0], measureColumns[1], dataFrameName));
  }

  if (time && !measure) {
    recipes.push(timeCountRecipe(time, dataFrameName));
  }

  return recipes;
}

function histogramRecipe(measure: SnapshotColumn, dataFrameName: string): RRecipe {
  return {
    id: 'histogram',
    title: `Histogramm ${swissQuote(measure.name)}`,
    code: `library(ggplot2)\nplot_limit <- ${PLOT_ROW_LIMIT}\nplot_data <- ${dataFrameName}[!is.na(${dataFrameName}[[${rString(measure.name)}]]), , drop = FALSE]\nplot_data <- head(plot_data, plot_limit)\nbins <- min(30, max(5, floor(sqrt(max(1, nrow(plot_data))))))\nggplot(plot_data, aes(x = .data[[${rString(measure.name)}]])) +\n  geom_histogram(bins = bins, fill = "${SINGLE_PLOT_COLOR}", color = "white") +\n  labs(title = ${rString(`Verteilung ${measure.name}`)}, x = ${rString(measure.name)}, y = "Häufigkeit") +\n  theme_minimal()`
  };
}

function barCategoryRecipe(category: SnapshotColumn, index: number, dataFrameName: string): RRecipe {
  return {
    id: index === 0 ? 'bar-category' : `bar-category-${index + 1}`,
    title: `Balkendiagramm ${swissQuote(category.name)}`,
    code: `library(ggplot2)\nplot_data <- as.data.frame(table(${dataFrameName}[[${rString(category.name)}]]), stringsAsFactors = FALSE)\nnames(plot_data) <- c("kategorie", "anzahl")\nplot_data <- head(plot_data[order(plot_data[["anzahl"]], decreasing = TRUE), , drop = FALSE], ${CATEGORY_PLOT_LIMIT})\nggplot(plot_data, aes(x = reorder(.data[["kategorie"]], -.data[["anzahl"]]), y = .data[["anzahl"]])) +\n  geom_col(fill = "${SINGLE_PLOT_COLOR}") +\n  labs(title = ${rString(`Anzahl nach ${category.name}`)}, x = ${rString(category.name)}, y = "Anzahl") +\n  theme_minimal()`
  };
}

function barMeasureCategoryRecipe(measure: SnapshotColumn, category: SnapshotColumn, dataFrameName: string): RRecipe {
  return {
    id: 'bar-measure-category',
    title: `Mittelwert ${swissQuote(measure.name)} nach ${swissQuote(category.name)}`,
    code: `library(ggplot2)\nplot_data <- aggregate(${dataFrameName}[[${rString(measure.name)}]], by = list(kategorie = ${dataFrameName}[[${rString(category.name)}]]), FUN = mean, na.rm = TRUE)\nnames(plot_data) <- c("kategorie", "mittelwert")\nplot_data <- head(plot_data[order(plot_data[["mittelwert"]], decreasing = TRUE), , drop = FALSE], ${CATEGORY_PLOT_LIMIT})\nggplot(plot_data, aes(x = reorder(.data[["kategorie"]], -.data[["mittelwert"]]), y = .data[["mittelwert"]])) +\n  geom_col(fill = "${SINGLE_PLOT_COLOR}") +\n  labs(title = ${rString(`Mittelwert ${measure.name} nach ${category.name}`)}, x = ${rString(category.name)}, y = "Mittelwert") +\n  theme_minimal()`
  };
}

function boxplotRecipe(measure: SnapshotColumn, category: SnapshotColumn, dataFrameName: string): RRecipe {
  return {
    id: 'boxplot-category',
    title: `Boxplot ${swissQuote(measure.name)} nach ${swissQuote(category.name)}`,
    code: `library(ggplot2)\nplot_limit <- ${PLOT_ROW_LIMIT}\nplot_data <- ${dataFrameName}[!is.na(${dataFrameName}[[${rString(category.name)}]]) & !is.na(${dataFrameName}[[${rString(measure.name)}]]), , drop = FALSE]\ntop_categories <- names(sort(table(plot_data[[${rString(category.name)}]]), decreasing = TRUE))[seq_len(min(8, length(unique(plot_data[[${rString(category.name)}]]))))]\nplot_data <- plot_data[plot_data[[${rString(category.name)}]] %in% top_categories, , drop = FALSE]\nplot_data <- head(plot_data, plot_limit)\nggplot(plot_data, aes(x = .data[[${rString(category.name)}]], y = .data[[${rString(measure.name)}]])) +\n  geom_boxplot(fill = "${SINGLE_PLOT_COLOR}", color = "white") +\n  coord_flip() +\n  labs(title = ${rString(`${measure.name} nach ${category.name}`)}, x = ${rString(category.name)}, y = ${rString(measure.name)}) +\n  theme_minimal()`
  };
}

function trendRecipe(measure: SnapshotColumn, time: SnapshotColumn, index: number, dataFrameName: string): RRecipe {
  return {
    id: index === 0 ? 'trend' : `trend-${index + 1}`,
    title: `Trend ${swissQuote(measure.name)} nach ${swissQuote(time.name)}`,
    code: `library(ggplot2)\ntrend_data <- aggregate(${dataFrameName}[[${rString(measure.name)}]], by = list(zeit = ${dataFrameName}[[${rString(time.name)}]]), FUN = mean, na.rm = TRUE)\nnames(trend_data) <- c("zeit", "wert")\ntrend_data <- trend_data[order(trend_data[["zeit"]]), , drop = FALSE]\nggplot(trend_data, aes(x = .data[["zeit"]], y = .data[["wert"]])) +\n  geom_line(color = "${SINGLE_PLOT_COLOR}") +\n  geom_point(color = "${SINGLE_PLOT_COLOR}") +\n  labs(title = ${rString(`Trend ${measure.name}`)}, x = ${rString(time.name)}, y = ${rString(measure.name)}) +\n  theme_minimal()`
  };
}

function multiTrendRecipe(measures: SnapshotColumn[], time: SnapshotColumn, dataFrameName: string): RRecipe {
  const measureNames = measures.map((column) => column.name);
  const list = `c(${measureNames.map(rString).join(', ')})`;
  return {
    id: 'trend-multi',
    title: `Trend ${measureNames.map(swissQuote).join(' + ')} nach ${swissQuote(time.name)}`,
    code: `library(ggplot2)\nlibrary(tidyr)\ntrend_cols <- ${list}\ntrend_data <- ${dataFrameName}[c(${rString(time.name)}, trend_cols)]\nnames(trend_data)[1] <- "zeit"\ntrend_data <- pivot_longer(trend_data, cols = -"zeit", names_to = "reihe", values_to = "wert")\ntrend_data <- aggregate(wert ~ zeit + reihe, data = trend_data, FUN = mean, na.rm = TRUE)\ntrend_data <- trend_data[order(trend_data[["zeit"]]), , drop = FALSE]\nggplot(trend_data, aes(x = .data[["zeit"]], y = .data[["wert"]], color = .data[["reihe"]])) +\n  geom_line() +\n  geom_point(size = 1) +\n  scale_color_manual(values = ${multiPlotColors(measureNames.length)}) +\n  labs(title = "Trend mehrerer Messwerte", x = ${rString(time.name)}, y = "Wert", color = "Reihe") +\n  theme_minimal()`
  };
}

function scatterRecipe(first: SnapshotColumn, second: SnapshotColumn, dataFrameName: string): RRecipe {
  return {
    id: 'scatter',
    title: `Streudiagramm ${swissQuote(first.name)} und ${swissQuote(second.name)}`,
    code: `library(ggplot2)\nplot_limit <- ${PLOT_ROW_LIMIT}\nplot_data <- ${dataFrameName}[!is.na(${dataFrameName}[[${rString(first.name)}]]) & !is.na(${dataFrameName}[[${rString(second.name)}]]), , drop = FALSE]\nplot_data <- head(plot_data, plot_limit)\nkorrelation <- cor(plot_data[[${rString(first.name)}]], plot_data[[${rString(second.name)}]], use = "complete.obs")\nggplot(plot_data, aes(x = .data[[${rString(first.name)}]], y = .data[[${rString(second.name)}]])) +\n  geom_point(color = "${SINGLE_PLOT_COLOR}", alpha = 0.6) +\n  labs(title = ${rString(`Zusammenhang ${first.name} und ${second.name}`)}, subtitle = paste("Korrelation:", round(korrelation, 3)), x = ${rString(first.name)}, y = ${rString(second.name)}) +\n  theme_minimal()`
  };
}

function timeCountRecipe(time: SnapshotColumn, dataFrameName: string): RRecipe {
  return {
    id: 'time-count',
    title: `Anzahl pro ${swissQuote(time.name)}`,
    code: `library(ggplot2)\nplot_data <- as.data.frame(table(${dataFrameName}[[${rString(time.name)}]]), stringsAsFactors = FALSE)\nnames(plot_data) <- c("zeit", "anzahl")\nplot_data <- plot_data[order(plot_data[["zeit"]]), , drop = FALSE]\nggplot(plot_data, aes(x = .data[["zeit"]], y = .data[["anzahl"]])) +\n  geom_line(color = "${SINGLE_PLOT_COLOR}") +\n  geom_point(color = "${SINGLE_PLOT_COLOR}") +\n  labs(title = ${rString(`Anzahl pro ${time.name}`)}, x = ${rString(time.name)}, y = "Anzahl") +\n  theme_minimal()`
  };
}

type ColumnValues = ReadonlyMap<string, unknown[]>;

function pickTimeColumns(columns: SnapshotColumn[], columnValues: ColumnValues): SnapshotColumn[] {
  return columns
    .map((column) => ({column, score: timeColumnScore(column, columnValues)}))
    .filter((candidate) => candidate.score > 0)
    .sort((left, right) => right.score - left.score)
    .slice(0, TIME_CANDIDATE_LIMIT)
    .map((candidate) => candidate.column);
}

function timeColumnScore(column: SnapshotColumn, columnValues: ColumnValues): number {
  if (column.rType === 'Date' || column.rType === 'POSIXct') {
    return 3;
  }
  if (column.roles.includes('year') || column.roles.includes('date')
      || isYearLikeName(column.name) || isDateLikeName(column.name)) {
    return 2;
  }
  return isColumnYearLike(column, columnValues) || isColumnDateLike(column, columnValues) ? 1 : 0;
}

function pickMeasureColumns(
  columns: SnapshotColumn[],
  columnValues: ColumnValues,
  timeColumns: SnapshotColumn[]
): SnapshotColumn[] {
  const timeNames = new Set(timeColumns.map((column) => column.name));
  return columns
    .filter((column) => (column.rType === 'numeric' || column.rType === 'integer') && !timeNames.has(column.name))
    .filter((column) => !column.roles.includes('identifier'))
    .filter((column) => !isColumnYearLike(column, columnValues) && !isColumnDateLike(column, columnValues))
    .sort((left, right) => numericScore(right) - numericScore(left))
    .slice(0, MEASURE_CANDIDATE_LIMIT);
}

function pickCategoryColumns(
  columns: SnapshotColumn[],
  columnValues: ColumnValues,
  timeColumns: SnapshotColumn[]
): SnapshotColumn[] {
  const timeNames = new Set(timeColumns.map((column) => column.name));
  return columns
    .filter((column) => column.rType === 'character' && !timeNames.has(column.name))
    .filter((column) => !column.roles.includes('identifier'))
    .filter((column) => !isColumnYearLike(column, columnValues) && !isColumnDateLike(column, columnValues))
    .sort((left, right) => categoryScore(right) - categoryScore(left))
    .slice(0, CATEGORY_CANDIDATE_LIMIT);
}

function isColumnYearLike(column: SnapshotColumn, columnValues: ColumnValues): boolean {
  return column.roles.includes('year')
    || isYearLikeName(column.name)
    || isYearLikeValues(columnValues.get(column.name) ?? []);
}

function isColumnDateLike(column: SnapshotColumn, columnValues: ColumnValues): boolean {
  return column.rType === 'Date'
    || column.rType === 'POSIXct'
    || column.roles.includes('date')
    || isDateLikeName(column.name)
    || isDateLikeValues(columnValues.get(column.name) ?? []);
}

function numericScore(column: SnapshotColumn): number {
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

function categoryScore(column: SnapshotColumn): number {
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

function multiPlotColors(count: number): string {
  const colors = MULTI_CHART_COLOR_PALETTE.slice(0, Math.max(1, count));
  return `c(${colors.map((color) => rString(color)).join(', ')})`;
}

function swissQuote(value: string): string {
  return `«${value}»`;
}

function rString(value: string): string {
  return JSON.stringify(value);
}
