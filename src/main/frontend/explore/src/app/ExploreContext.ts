import {z} from 'zod';

export const exploreColumnRoleSchema = z.enum([
  'identifier',
  'label',
  'category',
  'measure',
  'date',
  'year',
  'geometry',
  'municipality',
  'unknown'
]);

export const exploreChartTypeSchema = z.enum(['bar', 'line', 'scatter', 'histogram', 'pie', 'donut']);

export const exploreChartConfigSchema = z.object({
  type: exploreChartTypeSchema,
  x: z.string().optional(),
  y: z.string().optional(),
  color: z.string().optional(),
  title: z.string().optional()
});

export const exploreColumnSchema = z.object({
  name: z.string(),
  type: z.string(),
  nullable: z.boolean().optional(),
  required: z.boolean().optional(),
  description: z.string().optional(),
  example: z.string().optional(),
  roles: z.array(exploreColumnRoleSchema)
});

export const exploreTableSchema = z.object({
  id: z.string(),
  name: z.string(),
  title: z.string(),
  description: z.string().optional(),
  parquetUrl: z.string(),
  sizeBytes: z.number().optional(),
  rowCountEstimate: z.number().optional(),
  primary: z.boolean(),
  columns: z.array(exploreColumnSchema)
});

export const exploreRecipeSchema = z.object({
  id: z.string(),
  title: z.string(),
  description: z.string(),
  tableId: z.string(),
  category: z.enum(['preview', 'profile', 'quality', 'category', 'numeric', 'time', 'custom']),
  sql: z.string(),
  preferredChart: exploreChartConfigSchema.optional()
});

export const exploreCodeSnippetSchema = z.object({
  id: z.string(),
  title: z.string(),
  language: z.enum(['sql', 'python', 'r', 'bash']),
  code: z.string()
});

export const exploreFeatureFlagsSchema = z.object({
  charts: z.boolean(),
  localHistory: z.boolean(),
  aiAssistant: z.boolean(),
  webR: z.boolean(),
  vega: z.boolean(),
  mosaic: z.boolean(),
  geospatial: z.boolean()
});

export const exploreExecutionSchema = z.object({
  engine: z.literal('duckdb-wasm'),
  mode: z.literal('browser-local'),
  maxPreviewRows: z.number(),
  maxResultRows: z.number(),
  queryTimeoutMs: z.number()
});

export const exploreContextSchema = z.object({
  version: z.literal(1),
  datasetId: z.string(),
  title: z.string(),
  description: z.string().optional(),
  canonicalUrl: z.string(),
  updatedAt: z.string().optional(),
  license: z.string().optional(),
  execution: exploreExecutionSchema,
  tables: z.array(exploreTableSchema),
  recipes: z.array(exploreRecipeSchema),
  codeSnippets: z.array(exploreCodeSnippetSchema),
  featureFlags: exploreFeatureFlagsSchema
});

export type ExploreColumnRole = z.infer<typeof exploreColumnRoleSchema>;
export type ExploreChartConfigDto = z.infer<typeof exploreChartConfigSchema>;
export type ExploreColumnDto = z.infer<typeof exploreColumnSchema>;
export type ExploreTableDto = z.infer<typeof exploreTableSchema>;
export type ExploreRecipeDto = z.infer<typeof exploreRecipeSchema>;
export type ExploreCodeSnippetDto = z.infer<typeof exploreCodeSnippetSchema>;
export type ExploreFeatureFlagsDto = z.infer<typeof exploreFeatureFlagsSchema>;
export type ExploreExecutionDto = z.infer<typeof exploreExecutionSchema>;
export type ExploreContextDto = z.infer<typeof exploreContextSchema>;

export function parseExploreContext(rawJson: string): ExploreContextDto {
  const parsed: unknown = JSON.parse(rawJson);
  return exploreContextSchema.parse(parsed);
}
