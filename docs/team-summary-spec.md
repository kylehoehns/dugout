# Team Summary Endpoint — Spec

The input the sub-agent team builds from. Source of truth is GitHub issue #14
("Add a team summary endpoint to /api/stats"), grilled into the decisions below.

## Goal

Add **one** team-summary endpoint to the existing `/api/stats` API — same JSON
style as the rest of `/api/stats`, no UI. It shows how the whole team is hitting
this season: a headline team batting average plus the roster ordered by who has
the most hits. Additive only — it does **not** change the existing `PlayerStats`
entity, `PlayerStatsResponse`, or the existing `/api/stats` endpoints.

## Reuse — do NOT re-derive (read first)

These are the constraints reviewers most often catch. Honor them verbatim:

1. **Reuse the existing batting-average rounding** — scale **3**, `RoundingMode.HALF_UP`
   — from `PlayerStatsResponse` (constants `BATTING_AVG_SCALE` / `BATTING_AVG_ROUNDING`
   and the `battingAverage(hits, atBats)` logic). Do **not** introduce a new scale,
   rounding mode, or ad-hoc `BigDecimal` division for the team average. If the
   existing method/constants are `private`, promote them to package-visible or
   extract a small shared helper in the `stats` package and have **both** the
   per-player average and the team average call it — one source of truth.
2. **Reuse `PlayerStatsResponse` verbatim** for each player entry — do not create a
   second per-player DTO.
3. **Reuse the existing service's find/map patterns** (`PlayerStatsService`,
   `PlayerStatsRepository.findAll()`), rather than adding a new data path.

## Model

No new entity. No schema change. Aggregation happens in the service over
`PlayerStatsRepository.findAll()`.

- **DTO `TeamSummaryResponse`** (record) in `com.kylehoehns.dugout.stats`:
  - `BigDecimal teamBattingAvg`
  - `List<PlayerStatsResponse> players`

## Qualification

A player is **qualified** — and therefore appears in `players` **and** counts
toward `teamBattingAvg` — only if **`atBats >= 10`**. Players below the threshold
are excluded from both. (Guards against silly small-sample lines like a 1-for-1
season reading as a 1.000 hitter.)

## API

Base path `/api/stats` (existing `PlayerStatsController`).

| request | result |
|---------|--------|
| `GET /api/stats/team-summary` | the team summary object (below) |

Response body:

```json
{
  "teamBattingAvg": 0.438,
  "players": [ <PlayerStatsResponse>, <PlayerStatsResponse>, ... ]
}
```

Each `players` element is the existing `PlayerStatsResponse` (jerseyNumber, name,
gamesPlayed, atBats, hits, doubles, triples, homeRuns, rbi, runs, walks,
strikeouts, stolenBases, battingAvg) — byte-identical to `/api/stats` entries.

The literal path `team-summary` is distinct from `GET /api/stats/{number}` (which
binds an `Integer`); Spring matches the literal mapping first, so there is no
route collision.

## Formula rules

**`teamBattingAvg` — pooled rate over qualified players:**

```
teamBattingAvg = round3( Σ hits(qualified) / Σ atBats(qualified) )
```

- Round with the **reused** scale-3 HALF_UP rounding (see Reuse #1). This is a
  **pooled** rate, **not** the average of each player's `battingAvg`.
- **Divide-by-zero guard:** if there are no qualified players (Σ atBats == 0),
  `teamBattingAvg` = `0.000` — never `NaN`, `Infinity`, or an exception.

**`players` — sort order (all descending/ascending as noted):**

1. `hits` **descending** (most hits first — the coach's primary ask).
2. tie → `battingAvg` **descending** (the more efficient hitter first).
3. tie → `jerseyNumber` **ascending** (stable, deterministic).

**Hot/cold tag:** intentionally **omitted** — the data has no time dimension, so
"hitting lately" is not computable. See `docs/adr/0002-omit-hot-cold-tag.md`.

## Acceptance examples

Against the seeded CSV roster (all 12 players have `atBats >= 10`, so all qualify):

| request | expectation |
|---------|-------------|
| `GET /api/stats/team-summary` | `teamBattingAvg` = **0.438** (191 hits / 436 at-bats) |
| same | `players` length = **12** |
| same | `players[0]` = **#92 Cooper Lane**, `hits` = **26** (unique max), `battingAvg` = **0.578** |
| same | tiebreak: **#36 Owen Bell** (14 H, avg 0.519) sorts **before** **#16 Brody Vance** (14 H, avg 0.368) |

Additional cases the tests must cover (using their own seeded data — the loader
only seeds when the table is empty):

- **Below-threshold exclusion:** a seeded player with `atBats < 10` (e.g. a
  1-for-1 line) is **absent** from `players` and does **not** change
  `teamBattingAvg`.
- **Empty / all-excluded:** when no player qualifies, `teamBattingAvg` = `0.000`
  and `players` = `[]` (no divide-by-zero).

## Conventions & constraints

- Follow `AGENTS.md` — **Gradle** (`./gradlew`, never Maven), no Lombok, JPA
  entities are plain classes / DTOs are records.
- Tests follow the house integration-testing conventions (the `writing-tests`
  skill) and must include the below-threshold exclusion and the all-excluded
  `0.000` cases.
- Finish with `./gradlew spotlessApply` then `./gradlew compileJava`; the full
  `./gradlew build` (JaCoCo coverage gate + Spotless) must be green.

## Delivery

Built by the sub-agent team via the `ship-feature` skill (developer → tester →
parallel review + docs → one fix pass → open PR → CI → address Copilot review →
verify live → notify). Targets base branch `stage-8-start`.
