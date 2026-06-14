# Quality Gates

Minimum quality gates for every completed phase:

- implementation matches `AGENTS.md`
- relevant skills were applied
- tests added or updated
- documentation updated
- `./gradlew clean check` run when available
- no secrets or local paths committed
- no unrelated changes included
- UI changes checked against `docs/ui-implementation-contract.md`
- parser/search/reload changes checked against their skills

A phase is not done until failed checks are fixed or explicitly documented as not runnable in the current project state.
