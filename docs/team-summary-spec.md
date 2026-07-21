# Team Season Summary Endpoint — Spec

The input the sub-agent team builds from. Locked from GitHub issue #14 via the
`ship-feature` grilling. **Additive** — it does not change the existing
`PlayerStats` entity, `PlayerStatsResponse` DTO, or the existing `/api/stats`
endpoints.

## Goal

Add **one** endpoint that summarizes how the whole team is hitting this season:
a single top-line team batting average plus the roster's real contributors,
ordered so the hits leader is first. JSON only, no UI, same house shape so the
parents' app can read it directly.

## Model

Reuse everything that exists — **no new entity, no schema change**:

- **Entity `PlayerStats`** — unchanged (season totals: `atBats`, `hits`, …).
- **DTO `PlayerStatsResponse`** — unchanged; each player in the summary is one of
  these existing objects (`jerseyNumber`, `name`, counting stats, `battingAvg`).
- **New DTO `TeamStatsResponse`** (record): `teamBattingAvg` (BigDecimal) and
  `players` (`List<PlayerStatsResponse>`).

## API

Base path `/api/stats`.

| request | result |
|---------|--------|
| `GET /api/stats/team` | the team summary: `{ teamBattingAvg, players[] }`, HTTP 200 |

`/api/stats/team` is a literal sibling of `GET /api/stats/{number}` (which binds
an `Integer`); the literal path wins over the pattern, so there is no collision.

### Response shape

```json
{
  "teamBattingAvg": 0.438,
  "players": [ /* PlayerStatsResponse objects, in the order defined below */ ]
}
```

## Formula & rules

### Eligibility ("real contributors")

- A player is **eligible** iff **`atBats >= 10`**.
- Ineligible players are excluded from **both** the `players` list **and** the
  `teamBattingAvg` calculation. (Guards against a fluke 1-for-1 → `1.000`.)

### `teamBattingAvg`

- **True team average = Σhits ÷ Σat-bats** over the eligible players (NOT the
  mean of individual averages — see ADR 0001).
- `BigDecimal`, scale **3**, `RoundingMode.HALF_UP` — same convention as the
  existing per-player `battingAvg`.
- **No eligible players** (empty roster, or everyone under 10 AB): `teamBattingAvg`
  is `0.000` (never a divide-by-zero) and `players` is `[]`, still HTTP 200.

### Ordering of `players`

1. **Pinned first** = the **hits leader** (eligible player with the most hits).
   Tie-break: higher `battingAvg`, then lower `jerseyNumber`.
2. **All others** follow by `battingAvg` **descending**, then `jerseyNumber`
   **ascending**. The pinned player appears exactly once (removed from the tail).

This intentionally is **not** a pure average sort — the hits leader can sit above a
higher-average teammate.

### Not included

- **No `hot`/`cold` tag.** Deferred: our data is season totals only, with nothing
  to support "lately" (see ADR 0002). No such field appears in the response.

## Acceptance examples

Against the seeded `roster-stats.csv` (all 12 players have AB ≥ 10, so all are
eligible):

| assertion | expectation |
|-----------|-------------|
| `GET /api/stats/team` status | 200 |
| `teamBattingAvg` | `0.438` (191 hits ÷ 436 at-bats) |
| `players` size | 12 |
| `players[0]` (pinned hits leader) | jersey `92`, "Cooper Lane", `hits` = 26, `battingAvg` = 0.578 |
| `players[1]` | jersey `23`, "Mason Reed", `hits` = 25, `battingAvg` = 0.610 |
| ordering proof | `players[1].battingAvg` (0.610) > `players[0].battingAvg` (0.578) — the hits leader is pinned above a higher-average teammate |
| tail order | after the pin, `battingAvg` is descending (Mason 0.610, Owen 0.519, Landon 0.488, …, Silas Fox 0.257 last) |

Additional cases to cover in tests:

- **Eligibility filter**: seed a fluke player (e.g. jersey 500, 1 AB / 1 H,
  avg 1.000). Assert they are absent from `players` **and** do not change
  `teamBattingAvg`.
- **Hits tie-break**: two eligible players tied on hits → the one with the higher
  `battingAvg` is `players[0]`.
- **Empty/none-eligible**: a repository with no eligible players →
  `teamBattingAvg` = `0.000`, `players` = `[]`, status 200.

## Conventions & constraints

- Follow `AGENTS.md` — **Gradle** (`./gradlew`), no Lombok, entities are plain
  classes / DTOs are records.
- Reuse the existing `battingAverage` rounding logic in `PlayerStatsResponse`
  rather than re-deriving scale/rounding.
- Tests follow the house integration-testing conventions (the `writing-tests`
  skill) and must include the eligibility-filter and empty cases above.
- **`./gradlew build` must be green** — the JaCoCo coverage gate and Spotless.

## Delivery

Built by the sub-agent team via the `ship-feature` skill (developer → tester →
parallel review + docs → one fix pass → open PR → make CI green → address Copilot
review → verify live → notify).
