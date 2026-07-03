# Explore DuckDB-Wasm Extensions

This directory mirrors signed official DuckDB-Wasm extensions that the browser
runtime needs at query time.

Phase 3 includes:

- `v1.4.3/wasm_mvp/excel.duckdb_extension.wasm`
- `v1.4.3/wasm_mvp/parquet.duckdb_extension.wasm`
- Source URLs:
  - `https://extensions.duckdb.org/v1.4.3/wasm_mvp/excel.duckdb_extension.wasm`
  - `https://extensions.duckdb.org/v1.4.3/wasm_mvp/parquet.duckdb_extension.wasm`

DuckDB-Wasm is configured with:

```sql
set custom_extension_repository='<origin>/explore-extensions';
```

Refresh this mirror whenever `@duckdb/duckdb-wasm` changes its DuckDB runtime
version or selected Wasm platform.
