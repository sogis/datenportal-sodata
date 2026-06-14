---
name: phase-delivery
description: Use for planning, implementing, verifying, documenting, and handing over one bounded implementation phase of the Datenportal project.
compatibility: codex, opencode
metadata:
  project: datenportal
---

# Skill: Phase Delivery

Use this skill whenever a task is described as a phase or when the change spans more than one file or concern.

## Phase workflow

1. Read `AGENTS.md`.
2. Read `datenportal_webapp_agent_spec_detailed_v5.md`.
3. Read `docs/phase-model.md` if present; otherwise use the phase model in the main specification.
4. Read `docs/quality-gates.md` if present.
5. Identify the current phase goal.
6. Read all relevant skills.
7. Produce a short plan before editing files.
8. Implement only the agreed phase scope.
9. Add or update tests.
10. Update documentation.
11. Run verification.
12. Prepare a concise handover.
13. If appropriate, invoke `commit-after-dod`.

## Plan format

Before implementation, provide:

- phase name
- goal
- files likely to change
- test strategy
- risks
- acceptance criteria

Do not start editing if the phase is ambiguous and the ambiguity can lead to wrong architecture.

## Scope control

Stay inside the phase. If unrelated problems are discovered:

- document them as follow-up
- do not fix them in the same change unless they block the phase

## Verification

Use the strongest available check:

```bash
./gradlew clean check
```

If Gradle is not available yet, run available alternatives and explain the limitation.

## Handover format

At the end, report:

- what changed
- tests run
- documentation updated
- known limitations
- next recommended phase
- commit hash if committed
