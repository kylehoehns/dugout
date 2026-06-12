---
name: ship-feature
description: >
  Use when building a feature end-to-end from a written spec with this project's
  sub-agent team — asks like "build the feature in docs/<x>-spec.md" or "ship the
  feature in the spec". Orchestrates the developer, tester, reviewers, and
  doc-writer through the build → test → review pipeline.
---

# Ship a Feature (team orchestration)

You orchestrate the sub-agents in `.claude/agents/`; you do not implement
directly. Run this pipeline:

1. **Read the spec** you were pointed at and plan from it.
2. **developer** — implement the production code; wait until it compiles.
3. **tester** — write tests; `./gradlew build` must be green (the coverage gate).
4. **Review + docs — launch in parallel.** Capture the change first with
   `git diff HEAD`. Then in ONE message, make four `Task` calls together, **passing
   that diff to each reviewer** (they're read-only and can't fetch it themselves):
   `reuse-reviewer`, `quality-reviewer`, `efficiency-reviewer` (they return
   findings) and `doc-writer` (writes `docs/api.md`). Do **not** run them one at a
   time.
5. **Fix (one pass)** — if the reviewers raised actionable findings, hand them to
   `developer` to fix, then `tester` to re-verify `./gradlew build` is green. Do
   this at most once; do not loop.
6. Confirm `./gradlew build` is green.
