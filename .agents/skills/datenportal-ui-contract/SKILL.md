---
name: datenportal-ui-contract
description: Use before changing any UI-related files in the Datenportal web application. Enforces the current UI contract, mockup references, web component integration, filters, list/card views, and detail pages.
compatibility: codex, opencode
metadata:
  project: datenportal
---

# Skill: Datenportal UI Contract

Use this skill before changing any UI-related files in the Datenportal web application.

Applies to:

- JTE templates
- CSS
- HTMX fragments
- header or breadcrumb integration
- catalog start page
- filters
- list view
- card view
- detail pages
- download buttons
- accessibility for UI elements

## Required reading

Before coding, read:

1. `docs/ui-implementation-contract.md`
2. `docs/component-map.md`
3. `spec/mockups/current/startseite_liste.png`
4. `spec/mockups/current/cards.png`
5. `spec/mockups/current/web-components.png`

The UI contract is authoritative for UI details and is integrated into `datenportal_webapp_agent_spec_detailed_v5.md`; it overrides older mockups and older UI specification sections.

## Non-negotiable rules

- The start page is the catalog page and defaults to list view.
- Header and breadcrumb must use the `so-web-components` integration, with a semantic fallback only as backup.
- Filters must support multiple selected values and show active filter chips.
- The list view must not show thematic icons in the dataset/title column.
- `Datensatz` and `Datenreihe` use the same blue info type badge style (`dp-status-badge--info`).
- Data series root rows must be expandable with plus/minus and optional row click.
- Clicking an info link or a download link must not expand the row.
- Downloads on data series root rows represent the current issue and must be labelled accordingly.
- Cards show `Open Data` in the MVP.
- Cards show `Struktur beschrieben` when a data model or attribute description exists.
- Detail pages must not include data preview in the MVP.
- Series issue detail pages must link to other issues.

## Implementation approach

Work in small phases:

1. Page chrome: web components, header, breadcrumb, layout.
2. Catalog list view: search, multi filters, active chips, table.
3. Series expansion: plus/minus, expanded issue rows, HTMX/full-page fallback.
4. Card view: grid and cards.
5. Detail pages: normal dataset, data series and issue details.

## Testing expectations

Add MVC or HTML structure tests for every changed UI area. Test for text, key CSS classes, ARIA attributes and URLs. Do not use pixel-perfect screenshot tests in the MVP.

Minimum checks before finishing:

```bash
./gradlew clean check
```

If the project is not yet bootstrapped, document which tests could not be run and why.

## Completion report

At the end of the task, report:

- which UI phase was implemented
- which mockups were used
- which templates changed
- which ViewModels/factories changed
- which tests were added or updated
- any known deviations from the contract

If committing is requested or expected, use the `commit-after-dod` skill after all checks pass.
