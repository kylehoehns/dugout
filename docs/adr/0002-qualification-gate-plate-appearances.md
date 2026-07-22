# 0002 — Qualify by plate appearances (AB + BB) ≥ 10, not by at-bats

- Status: Accepted
- Date: 2026-07-21

## Context

The coach asked to "skip the kids who've barely batted — a handful of at-bats all
season shouldn't land you on this list," and said it should work "same as the team
summary." Two facts complicate that:

1. **The team summary has no threshold today** — `GET /api/stats` returns all 12
   kids. So there is nothing to literally copy; the gate is a new decision.
2. **Official at-bats exclude walks.** A very patient kid accrues few at-bats. The
   clearest example on the current roster is **Jonah West #45**: only 29 at-bats,
   but a team-leading **22 walks** → 51 plate appearances. An at-bats-only cutoff
   of 30 would bounce the *most-walked kid on the team* off a plate-discipline
   board — perverse for exactly the metric this endpoint celebrates.

## Decision

Gate qualification on **plate appearances = `atBats + walks`**, with a minimum of
**10 PA**. Kids below the bar are omitted from the list (an empty list is `200 []`,
never an error).

## Consequences

- The gate uses the honest denominator for plate discipline and never punishes a
  patient walker for having few official at-bats.
- **Cutoff of 10:** "a handful" is ~5, so 10 is a clean floor — anything under it
  is unambiguously a tiny sample. On the seeded roster every kid has 27+ PA, so
  **all 12 qualify**; the gate bites only for genuine bench cases. Because it does
  not exclude anyone in the seed data, the gate is proven with a **synthetic
  sub-threshold player** in the tests (mirroring the existing 0-at-bats test).
- **Trade-off:** we chose plate appearances over the coach's literal word
  "at-bats." The word was a proxy for "played a real amount," and PA serves that
  intent without the patient-walker failure mode. Revisiting the *value* (10) later
  is cheap; revisiting the *metric* (AB vs PA) would change who appears on the
  board, which is why it is recorded here.
