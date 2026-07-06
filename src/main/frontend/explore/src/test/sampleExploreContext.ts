import type {ExploreContextDto} from '../app/ExploreContext';

export const sampleExploreContext: ExploreContextDto = {
  version: 3,
  datasetId: 'ch.so.bauinventar',
  title: 'Bauinventar',
  description: 'Schützenswerte und geschützte Gebäude.',
  canonicalUrl: '/datasets/ch.so.bauinventar',
  updatedAt: '2026-06-30',
  license: 'Open Data',
  execution: {
    engine: 'duckdb-wasm',
    mode: 'browser-local',
    maxPreviewRows: 100,
    maxResultRows: 10000,
    queryTimeoutMs: 30000
  },
  catalogDatabase: {
    url: '/catalog/catalog.duckdb',
    database: 'catalog',
    schema: 'opendata'
  },
  tables: [
    {
      id: 'ch_so_bauinventar',
      name: 'ch_so_bauinventar',
      title: 'Bauinventar',
      parquetUrl: 'https://data.so.ch/download/ch.so.bauinventar.parquet',
      primary: true,
      rowCountEstimate: 36176,
      columns: [
        {
          name: 'egid',
          type: 'INTEGER',
          roles: ['identifier']
        },
        {
          name: 'gemeindename',
          type: 'VARCHAR',
          roles: ['label', 'municipality']
        }
      ]
    }
  ],
  recipes: [
    {
      id: 'ch_so_bauinventar-preview',
      title: 'Vorschau',
      description: 'Zeigt die ersten Zeilen.',
      tableId: 'ch_so_bauinventar',
      category: 'preview',
      sql: 'SELECT *\nFROM opendata.ch_so_bauinventar;'
    }
  ],
  codeSnippets: [
    {
      id: 'duckdb-cli',
      title: 'DuckDB CLI',
      language: 'sql',
      code: "select * from read_parquet('https://data.so.ch/download/ch.so.bauinventar.parquet') limit 100;"
    },
    {
      id: 'python-duckdb',
      title: 'Python mit DuckDB',
      language: 'python',
      code: 'import duckdb\n\nurl = "https://data.so.ch/download/ch.so.bauinventar.parquet"'
    },
    {
      id: 'r-duckdb',
      title: 'R mit duckdb',
      language: 'r',
      code: 'library(duckdb)\n\nurl <- "https://data.so.ch/download/ch.so.bauinventar.parquet"'
    }
  ],
  featureFlags: {
    charts: true,
    localHistory: true,
    aiAssistant: false,
    webR: true,
    vega: false,
    mosaic: false,
    geospatial: false
  },
  rLaboratory: {
    dataFrameName: 'daten',
    runtimeBaseUrl: '/webr/0.6.0/',
    packageRepoUrl: '/webr-packages/',
    packages: ['ggplot2', 'dplyr', 'tidyr', 'readr', 'tibble', 'scales', 'RColorBrewer', 'viridisLite', 'jsonlite'],
    recommendedRows: 5000,
    warningRows: 10000,
    hardRows: 50000,
    plotWidth: 700,
    plotHeight: 420
  }
};
