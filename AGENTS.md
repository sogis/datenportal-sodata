# AGENTS.md — Datenportal Webapp

This repository contains the Datenportal web application for the Canton Solothurn.
All coding agents must follow this file before making changes.

The project package base is:

```text
ch.so.agi.datenportal
```

## 1. Source of truth

Read these documents before implementation work:

1. `datenportal_webapp_agent_spec_detailed_v5.md` — full functional and technical specification.
2. `docs/ui-implementation-contract.md` — authoritative UI contract.
3. `docs/ui-primitives.md` — authoritative primitive rules for filter chips, status badges and action pills.
4. `docs/component-map.md` — mapping from UI areas to JTE templates, ViewModels and controllers.
5. `docs/llm-coding-agent-usage.md` — agent workflow and expected usage.
6. `spec/mockups/current/startseite_liste.png` — authoritative list-view start page reference.
7. `spec/mockups/current/cards.png` — authoritative card-view reference.
8. `spec/mockups/current/web-components.png` — authoritative visual reference for header and breadcrumb web components.

Older mockups are secondary inspiration only. They must not override the current UI contract.

## 2. Required skills

Repo-local skills are stored under `.agents/skills/` and are intended for both Codex and OpenCode.
Use them as follows:

| Skill | Use when |
|---|---|
| `datenportal-webapp` | General application architecture and implementation. |
| `phase-delivery` | Any bounded implementation phase or larger multi-file task. |
| `xtf-publishedcatalog` | XTF/XML import, PublishedCatalog model, fixtures, reload parsing, series handling. |
| `jte-htmx-ui` | JTE templates, HTMX fragments, filters, list/card/detail pages. |
| `datenportal-ui-contract` | Any UI work; this is mandatory before touching UI. |
| `lucene-search` | Lucene indexing, querying, ranking, filtering and reindexing. |
| `spring-boot-reload-security` | Protected runtime reload endpoint and operational security. |
| `commit-after-dod` | Before creating a Git commit after a completed phase/task. |

When multiple skills apply, read all relevant skills before editing files.
For UI work, `datenportal-ui-contract` takes precedence over generic UI guidance.

## 3. Project intent

The application is a server-side rendered catalog and download portal for datasets and data series.
It reads a PublishedCatalog XTF/XML file at startup, stores an immutable in-memory snapshot, builds a Lucene search index and renders catalog pages using Spring Boot, JTE and HTMX.

The MVP must provide:

- catalog start page with search, filters, list view and card view
- normal dataset detail pages
- data series and issue detail pages
- downloads for CSV, XLSX and Parquet
- metadata display based on the PublishedCatalog model
- Lucene-backed search and filtering
- protected runtime reload endpoint
- good tests and documentation

## 4. Technology stack

Use:

- Java 25
- Spring Boot 4.1.0, with documented fallback to 4.0.x if a dependency is not ready
- Gradle Groovy DSL
- JTE templates
- HTMX for partial server-rendered updates
- Apache Lucene for search
- JUnit/Jupiter and Spring Boot test support

Avoid:

- client-side SPA architecture
- unnecessary JavaScript
- business logic in JTE templates
- large unreviewable commits
- mutable global state outside the controlled snapshot/index holder

## 5. Architecture boundaries

Use the package base `ch.so.agi.datenportal`.
Prefer a package structure close to:

```text
ch.so.agi.datenportal
├─ catalog
│  ├─ domain
│  ├─ importxtf
│  ├─ store
│  └─ service
├─ search
├─ ui
│  ├─ controller
│  ├─ viewmodel
│  └─ component
├─ admin
│  └─ reload
├─ config
└─ support
```

Rules:

- Domain classes should be immutable where practical.
- Controllers should be thin.
- Templates should receive prepared ViewModels.
- Parsing, search indexing and UI formatting must be separate concerns.
- Reload must be atomic: build and validate candidate snapshot and index before activating them.

## 6. UI contract

The start page is the catalog page and defaults to list view.
`spec/mockups/current/startseite_liste.png` is the authoritative visual reference for the initial start page.

Mandatory UI decisions:

- Header and breadcrumb must be integrated via the `so-web-components` web components.
- Do not rebuild header and breadcrumb as bespoke JTE markup except as semantic fallback.
- Filters must support multiple selected values.
- Active filters must be visible as removable chips.
- List view must not show icons in the “Thema / Datensatz” column.
- `Datensatz` and `Datenreihe` must use the same neutral grey type-badge style.
- Data series root rows must be expandable using plus/minus and row activation.
- Clicking the info link or a download must not expand the row.
- Data series root downloads refer to the current issue and must be labelled accordingly, for example `CSV (aktuelle Ausgabe)`.
- Card view follows `spec/mockups/current/cards.png`.
- Cards must show `Open Data` in the MVP.
- Cards must show a structural badge named `Struktur beschrieben` when attribute descriptions or a data model are available.
- MVP detail pages must not include data previews.
- Data series and issue detail pages must link to other issues.

## 7. Web components

Header and breadcrumb integration must be implemented as reusable JTE components/wrappers.
The web component assets should be served from static resources or otherwise integrated according to the project documentation.

The application may include licensed canton fonts when the repo maintainers provide them from the licensed source. Do not invent font files, and do not commit placeholder binaries pretending to be licensed fonts.

## 8. XTF / PublishedCatalog contract

Runtime input is `SO_AGI_DataCatalog_PublishedCatalog_20260602`.
Datasheet XTF files are not the runtime contract; they may only inspire test content.

The importer must represent:

- normal datasets
- data series
- issues/editions of data series
- distributions/download resources
- formats CSV, XLSX, Parquet, optional API
- theme/topic
- responsible office / Fachstelle / Amt
- publication/update dates
- license, contact, identifier and metadata fields available from the model

## 9. Search contract

Lucene search must support free-text search and filters. Empty query returns all entries subject to filters.
Filters use AND semantics across filter categories and OR semantics within a category unless the specification states otherwise.

Index and snapshot swaps must be atomic during reload.

## 10. Reload endpoint

The runtime reload endpoint must be protected. The first implementation may use a configured shared-secret header, for example:

```http
POST /admin/catalog/reload
X-Reload-Token: <secret>
```

The token must come from configuration/environment, never from source code.
Never log the token.
A failed reload must keep the previous snapshot and index active.

## 11. Testing expectations

Every phase must be tested. Prefer:

- unit tests for domain logic and current issue selection
- parser tests with small fixtures
- search tests for indexing, ranking and filters
- MVC/template tests for pages and fragments
- reload endpoint tests for security and atomic behavior

Default verification command:

```bash
./gradlew clean check
```

If the project is not yet bootstrapped or a command cannot be run, report that explicitly.

## 12. Documentation expectations

Update documentation when behavior, configuration, architecture, UI contract, templates or operations change.
Documentation must be useful for maintainers and coding agents.

## 13. Phase workflow

For phase work:

1. Read this file.
2. Read the specification and relevant docs.
3. Read relevant skills.
4. State a short implementation plan.
5. Implement only the phase scope.
6. Add/update tests.
7. Add/update documentation.
8. Run verification.
9. Summarize changes and limitations.
10. Use `commit-after-dod` if a commit is requested or expected.

## 14. Git and commits

Do not commit automatically unless requested or unless the current task explicitly says to finish with a commit.
When committing, use `.agents/skills/commit-after-dod/SKILL.md`.

Before staging:

```bash
git status --short
git diff
```

Stage only relevant files explicitly. Avoid `git add .` unless the diff is very small and reviewed.

Use informative conventional-style messages such as:

```text
feat: implement catalog list view with multi filters
```

or:

```text
test: add published catalog series fixtures
```

## 15. Safety and quality rules

Never commit:

- secrets, tokens, credentials or private cookies
- machine-specific paths or local IDE settings
- accidental generated caches
- unrelated changes
- fake licensed assets
- failing tests without explicit user approval

Protect user work. If the working tree contains unrelated modifications, do not overwrite them.
