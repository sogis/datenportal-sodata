# Phase Model

Implementation must proceed in small runnable phases. Each phase must leave the project documented, tested and executable.

Recommended phase order:

1. Project bootstrap and package structure
2. Domain model and PublishedCatalog fixtures
3. XTF/XML import and immutable snapshot
4. Lucene indexing and search service
5. Page chrome with web components
6. Catalog list view with search and multi filters
7. Expandable data series rows
8. Catalog card view
9. Detail pages for datasets, data series and issues
10. Protected reload endpoint
11. Operational documentation and final hardening

Every phase must define acceptance criteria before coding starts.
