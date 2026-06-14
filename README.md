# Datenportal Webapp Specification + Agent Context v6

This package combines the full v5 Datenportal webapp specification with the complete repo-local agent context:

- full technical specification
- current UI implementation contract
- current mockup references
- complete `AGENTS.md`
- all recommended skills for Codex/OpenCode
- OpenCode command files
- validation helper

## Most important files

| File | Purpose |
|---|---|
| `AGENTS.md` | Root instruction file for Codex/OpenCode. Tells agents how to work in this repo. |
| `datenportal_webapp_agent_spec_detailed_v5.md` | Full detailed specification with architecture, classes, methods and phases. |
| `docs/ui-implementation-contract.md` | Authoritative UI contract based on the current screenshots. |
| `docs/component-map.md` | Mapping from UI elements to controllers, ViewModels and JTE templates. |
| `docs/skills-overview.md` | Explains all included skills. |
| `docs/installation-and-usage.md` | Copy/unzip instructions for the target repo. |
| `.agents/skills/*/SKILL.md` | Repo-local skills for Codex/OpenCode. |
| `.opencode/commands/*.md` | Optional OpenCode project commands. |
| `spec/mockups/current/*.png` | Authoritative UI screenshots for the current UI contract. |
| `tools/validate-agent-package.py` | Checks that the package was copied completely. |

## Included skills

- `datenportal-webapp`
- `phase-delivery`
- `xtf-publishedcatalog`
- `jte-htmx-ui`
- `datenportal-ui-contract`
- `lucene-search`
- `spring-boot-reload-security`
- `commit-after-dod`

## Quick validation

After copying into the target repository, run:

```bash
python3 tools/validate-agent-package.py
```

The package intentionally does not include licensed font binaries. The specification allows the project maintainers to add licensed font assets from the official licensed source.
