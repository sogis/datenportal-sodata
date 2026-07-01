import type {ExploreContextDto} from '../app/ExploreContext';

export const sampleExploreContext: ExploreContextDto = {
  version: 1,
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
  tables: [
    {
      id: 'ch_so_bauinventar',
      name: 'ch_so_bauinventar',
      title: 'Bauinventar',
      parquetUrl: 'https://data.so.ch/download/ch.so.bauinventar.parquet',
      primary: true,
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
      sql: 'select * from ch_so_bauinventar limit 100;'
    }
  ],
  codeSnippets: [
    {
      id: 'duckdb-cli',
      title: 'DuckDB CLI',
      language: 'sql',
      code: "select * from read_parquet('https://data.so.ch/download/ch.so.bauinventar.parquet') limit 100;"
    }
  ],
  featureFlags: {
    charts: true,
    localHistory: true,
    aiAssistant: false,
    webR: false,
    vega: false,
    mosaic: false
  }
};
