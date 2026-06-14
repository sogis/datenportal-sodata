# Skills Overview

This package contains all repo-local skills currently recommended for the Datenportal web application.

| Skill | Main purpose |
|---|---|
| `datenportal-webapp` | General architecture and implementation rules for the Spring Boot/JTE/HTMX app. |
| `phase-delivery` | Planning, implementing, verifying and handing over one bounded phase. |
| `xtf-publishedcatalog` | PublishedCatalog XTF/XML parser, fixtures, snapshot and series handling. |
| `jte-htmx-ui` | Server-rendered JTE/HTMX UI, fragments, accessibility and minimal JavaScript. |
| `datenportal-ui-contract` | Authoritative UI rules based on the current mockups and UI contract. |
| `lucene-search` | Lucene fields, ranking, filters, indexing and reindexing. |
| `spring-boot-reload-security` | Protected runtime reload endpoint and atomic reload behavior. |
| `commit-after-dod` | Commit workflow after Definition of Done. |

## How agents should use the skills

The skills are intentionally small and task-specific. `AGENTS.md` tells the agent when to use which skill. A typical UI task should read:

1. `AGENTS.md`
2. `datenportal_webapp_agent_spec_detailed_v5.md`
3. `docs/ui-implementation-contract.md`
4. `.agents/skills/datenportal-ui-contract/SKILL.md`
5. `.agents/skills/jte-htmx-ui/SKILL.md`
6. `.agents/skills/phase-delivery/SKILL.md` if the task is a phase

A parser/search/reload phase should read the matching domain skill instead.
