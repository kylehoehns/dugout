# Plate-Discipline Leaderboard — Spec

The input the sub-agent team builds from. Source of truth is GitHub issue #17
("Add a plate-discipline endpoint to /api/stats"), grilled into the decisions
below. See `CONTEXT.md` for vocabulary and `docs/adr/0001`, `docs/adr/0002` for
the two design trade-offs.

## Goal

Add **one** endpoint to `/api/stats` that ranks the roster by **plate discipline**
— the walk-to-strikeout "eye." Same JSON style as the existing stats endpoints, no
UI. Purely **additive**: the existing `PlayerStats` entity, `PlayerStatsResponse`,
and the two existing `/api/stats` endpoints are unchanged.

## Model

Reuse the existing `PlayerStats` entity and `PlayerStatsRepository` as-is. Add one
DTO:

- **DTO `PlateDisciplineResponse`** (record): `jerseyNumber` (Integer), `name`
  (String, `firstName + " " + lastName` — same as `PlayerStatsResponse`), `walks`
  (Integer), `strikeouts` (Integer), `eye` (`BigDecimal`).

## API

Base path `/api/stats` (existing `PlayerStatsController`).

| request | result |
|---------|--------|
| `GET /api/stats/plate-discipline` | qualifying kids ranked best-eye first; `200 []` if none qualify |

- The literal segment `plate-discipline` takes routing precedence over the existing
  `GET /api/stats/{number}` mapping, so there is no collision.
- No query parameters.

## Formula rules

Let `BB` = walks, `SO` = strikeouts, `AB` = at-bats. Treat a null `BB`, `SO`, or
`AB` as `0` (defensive; CSV data is always populated), mirroring the null guard in
`PlayerStatsResponse.battingAverage`.

- **Eye** = `BB / max(SO, 1)` as a `BigDecimal`, **scale 3, `HALF_UP`** — reuse the
  exact rounding of `PlayerStatsResponse.battingAvg`; do **not** re-derive a new
  scale/rounding. Flooring `SO` at 1 prevents divide-by-zero for a kid who never
  strikes out. (ADR 0001.)
- **Plate appearances (PA)** = `AB + BB`. (ADR 0002.)
- **Qualification gate:** include a kid only if `PA >= 10`. Below the bar → omitted.
  If nobody qualifies, the endpoint returns `200 []`. (ADR 0002.)
- **Sort order** (all applied to the qualifying kids):
  1. `eye` **descending** (best eye first)
  2. then `walks` **descending** (more walks = the eye shown over a bigger sample)
  3. then `jerseyNumber` **ascending** (final deterministic tie-break)

## Acceptance examples

Against the seeded CSV roster (`src/main/resources/roster-stats.csv`). Every kid
has `PA >= 10`, so **all 12 qualify** and the list is fully ranked by eye:

| rank | jersey | name | walks | strikeouts | eye |
|------|--------|------|-------|------------|-----|
| 1 | 23 | Mason Reed | 12 | 1 | `12.000` |
| 2 | 92 | Cooper Lane | 18 | 9 | `2.000` |
| 3 | 86 | Easton Gray | 18 | 11 | `1.636` |
| 4 | 36 | Owen Bell | 7 | 5 | `1.400` |
| 5 | 64 | Landon Cross | 13 | 10 | `1.300` |
| 6 | 45 | Jonah West | 22 | 19 | `1.158` |
| 7 | 4 | Tate Hoehns | 9 | 8 | `1.125` |
| 8 | 11 | Carter Hale | 12 | 11 | `1.091` |
| 9 | 99 | Nolan Pierce | 12 | 12 | `1.000` |
| 10 | 1 | Micah Flynn | 6 | 9 | `0.667` |
| 11 | 12 | Silas Fox | 11 | 21 | `0.524` |
| 12 | 16 | Brody Vance | 9 | 18 | `0.500` |

Key checks:

- `GET /api/stats/plate-discipline` → 200, a JSON array of 12 entries in exactly the
  order above; first entry is `{jerseyNumber:23, name:"Mason Reed", walks:12,
  strikeouts:1, eye:12.000}`.
- Jonah West #45 (29 AB, 22 BB → 51 PA) **is present** — the patient walker is not
  dropped by the gate.
- **Zero-strikeout is sensible, never blows up:** a synthetic qualifying player with
  `SO = 0`, `BB = 8`, enough PA → `eye = 8.000` (i.e. `8 / max(0,1)`), no
  divide-by-zero / `NaN` / `Infinity`.
- **Gate excludes tiny samples:** a synthetic player with `PA < 10` (e.g. `AB = 3`,
  `BB = 2` → 5 PA) does **not** appear in the list. (Seed the player, assert
  absence, delete it — mirror the existing 0-at-bats bench-player test.)
- The existing `GET /api/stats` and `GET /api/stats/{number}` responses are
  unchanged (no `eye` field leaks into them).

## Conventions & constraints

- Follow `AGENTS.md` — **Gradle** (`./gradlew`, never Maven), no Lombok, entities
  are plain classes / DTOs are records.
- Reuse, don't re-derive: the **scale-3 `HALF_UP` rounding** and the **`name`
  concatenation** already live in `PlayerStatsResponse` — reuse those conventions.
- Tests follow the house integration-testing conventions (the `writing-tests`
  skill), driven through `MockMvc` against the seeded roster, and must cover: the
  full ranked order, the zero-strikeout case, and the sub-threshold exclusion.
- **`./gradlew build` must be green** — the JaCoCo coverage gate and Spotless
  (googleJavaFormat, AOSP). Run `./gradlew spotlessApply` before handing off.

## Delivery

Built by the sub-agent team via the `ship-feature` skill (developer → tester →
refactorer → parallel review + docs → one fix pass → open PR → CI → address Copilot
review → verify live → notify). PR targets the `stage-8-start` scaffold branch.
Issue #17 is a reusable example ticket — **read-only**: do not comment on, label,
edit, or close it, and do not use closing keywords in the PR.
