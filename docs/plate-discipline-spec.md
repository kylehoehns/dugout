# Plate-Discipline Endpoint — Spec

The input the sub-agent team builds from. Source of truth is GitHub issue #17
("Add a plate-discipline endpoint to /api/stats (walk-to-strikeout eye)"),
grilled into the decisions below.

## Goal

Add **one** endpoint to the existing `/api/stats` API — same JSON style as the
rest of `/api/stats`, no UI. It ranks the team by **plate discipline**: each
qualified kid's **walk-to-strikeout eye**, patient hitters first. Additive only —
it does **not** change the existing `PlayerStats` entity, the JSON/API shape of
`PlayerStatsResponse`, `TeamSummaryResponse`, or any existing `/api/stats`
endpoint. One internal-only refactor is allowed per the Reuse section (extract a
shared rounded-division helper that both batting average and the eye number call
— no serialized shape or endpoint behavior changes).

## Reuse — do NOT re-derive (read first)

These are the constraints reviewers most often catch. Honor them verbatim:

1. **Reuse the existing scale-3 HALF_UP rounding** from `PlayerStatsResponse`
   (constants `BATTING_AVG_SCALE` / `BATTING_AVG_ROUNDING`). Do **not** introduce a
   new scale, rounding mode, or ad-hoc `BigDecimal` division for the eye number.
   Extract a small package-visible rounded-division helper in
   `PlayerStatsResponse` (e.g. `roundedQuotient(numerator, denominator)` →
   round3 HALF_UP, denominator `0 → 0.000`) and have **both** `battingAverage`
   **and** the eye computation call it — one source of truth for rounded rates.
2. **Reuse the qualification rule** from the team summary — `atBats >= 10`,
   the existing `PlayerStatsService.QUALIFYING_AT_BATS` constant. Do **not**
   introduce a second threshold or re-read the meaning of "barely batted".
3. **Reuse `PlayerStatsResponse` to source each entry's fields.** Map
   `PlayerStats → PlayerStatsResponse` (existing `from`) first, then build the
   slim plate-discipline entry from it — this reuses the existing
   `firstName + " " + lastName` name concatenation rather than re-deriving it.
4. **Reuse the existing service's find/map/sort patterns** (`PlayerStatsService`,
   `PlayerStatsRepository.findAll()`), rather than adding a new data path.

## Model

No new entity. No schema change. Ranking happens in the service over
`PlayerStatsRepository.findAll()`. `walks` and `strikeouts` already exist on
`PlayerStats` / `PlayerStatsResponse`.

- **DTO `PlateDisciplineResponse`** (record) in `com.kylehoehns.dugout.stats` —
  a slim, purpose-built entry with exactly the fields the ticket lists:
  - `Integer jerseyNumber`
  - `String name`
  - `Integer walks`
  - `Integer strikeouts`
  - `BigDecimal eye`
- Static factory `PlateDisciplineResponse.from(PlayerStatsResponse)` that copies
  `jerseyNumber`, `name`, `walks`, `strikeouts` and computes `eye` (below).

The endpoint returns a bare `List<PlateDisciplineResponse>` (best eye first) —
the same list-of-players shape as `GET /api/stats`, not a wrapper object.

## Qualification

A player is **qualified** — and therefore appears in the ranking — only if
**`atBats >= 10`** (reused `QUALIFYING_AT_BATS`). Players below the threshold are
excluded, exactly as in the team summary. "Barely batted" = at-bats, not walks or
plate appearances.

## API

Base path `/api/stats` (existing `PlayerStatsController`).

| request | result |
|---------|--------|
| `GET /api/stats/plate-discipline` | JSON array of ranked qualified players (below) |

Response body:

```json
[
  { "jerseyNumber": 23, "name": "Mason Reed", "walks": 12, "strikeouts": 1, "eye": 12.000 },
  { "jerseyNumber": 92, "name": "Cooper Lane", "walks": 18, "strikeouts": 9, "eye": 2.000 }
]
```

The literal path `plate-discipline` is distinct from `GET /api/stats/{number}`
(which binds an `Integer`); Spring matches the literal mapping first, so there is
no route collision — same as the existing `team-summary` path.

## Formula rules

**`eye` — walk-to-strikeout ratio (see `docs/adr/0003-eye-so-zero-guard.md`):**

```
effectiveStrikeouts = (strikeouts == 0) ? 1 : strikeouts
eye = round3( walks / effectiveStrikeouts )
```

- Treat null `walks`/`strikeouts` as `0`.
- **SO=0 guard:** divide by `1`, so `eye = walks` (finite, never `NaN`/`Infinity`
  /exception). `walks = 0, strikeouts = 0 → 0.000`.
- Round with the **reused** scale-3 HALF_UP helper (Reuse #1). `eye` is
  unbounded above (a 12-walk / 1-strikeout line is `12.000`) — that is correct.

**Sort order (best eye first):**

1. `eye` **descending** (best eye first — the coach's primary ask).
2. tie → `walks` **descending** (the more patient hitter first).
3. tie → `jerseyNumber` **ascending** (stable, deterministic).

## Acceptance examples

Against the seeded CSV roster (all 12 players have `atBats >= 10`, so all
qualify; every seeded player has `strikeouts >= 1`; columns are
`… ,BB,SO,SB` — `BB` = walks, `SO` = strikeouts, `SB` = stolen bases):

| request | expectation |
|---------|-------------|
| `GET /api/stats/plate-discipline` | HTTP 200, array length = **12** |
| same | `[0]` = **#23 Mason Reed** — `walks` 12, `strikeouts` 1, `eye` = **12.000** (unique max) |
| same | `[1]` = **#92 Cooper Lane** — `eye` = **2.000** (18/9) |
| same | `[2]` = **#86 Easton Gray** — `eye` = **1.636** (18/11) |
| same | last = **#16 Brody Vance** — `eye` = **0.500** (9/18, the worst eye) |

Full seeded ranking (for reference, `BB/SO`): #23 (12.000), #92 (2.000),
#86 (1.636), #36 (1.400), #64 (1.300), #45 (1.158), #4 (1.125), #11 (1.091),
#99 (1.000), #1 (0.667), #12 (0.524), #16 (0.500). The seeded roster has **no
natural eye tie**, so the walks-tiebreak is covered by a self-seeded test case
(below), not the CSV data.

Additional cases the tests must cover (using their own seeded data — the loader
only seeds when the table is empty, so extra players are saved in-test as
`TeamSummaryApiIT` does):

- **Walks tiebreak:** two seeded qualified players with the **same eye** but
  different walk counts (e.g. 10 BB / 5 SO = 2.000 and 4 BB / 2 SO = 2.000) —
  the one with **more walks** sorts first; jersey number breaks a further tie.
- **SO=0 guard:** a seeded qualified player with `walks > 0, strikeouts = 0`
  (e.g. 7 BB, 0 SO, `atBats >= 10`) gets `eye` = **7.000** (never blows up) and
  sorts to the top.
- **No walks, no strikeouts:** a seeded qualified player with `walks = 0,
  strikeouts = 0` gets `eye` = **0.000** (no divide-by-zero).
- **Below-threshold exclusion:** a seeded player with `atBats < 10` (e.g. a
  1-for-1 line) is **absent** from the ranking.

## Conventions & constraints

- Follow `AGENTS.md` — **Gradle** (`./gradlew`, never Maven), no Lombok, JPA
  entities are plain classes / DTOs are records.
- Tests follow the house integration-testing conventions (the `writing-tests`
  skill) and must include the SO=0 guard, the 0/0 → `0.000` case, and the
  below-threshold exclusion.
- Finish with `./gradlew spotlessApply` then `./gradlew build`; the full build
  (JaCoCo 80% coverage gate + Spotless) must be green.

## Delivery

Built by the sub-agent team via the `ship-feature` skill (tdd-developer →
parallel review + docs → one fix pass → open PR → CI → address Copilot review →
verify live → notify). Targets base branch `stage-9-start`.
