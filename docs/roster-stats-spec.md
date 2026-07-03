# Roster Batting Stats (CSV-seeded) — Spec

The input the sub-agent team builds from.

## Goal

Expose real batting stats for a roster that is loaded from a CSV file on
application startup. This is additive — it does **not** change the existing
`Player` entity or `/api/players` endpoints.

## Data source

`src/main/resources/roster-stats.csv` — one header row, then one row per player.
Columns (all integers except the two name columns):

```
Number,First,Last,GP,AB,H,2B,3B,HR,RBI,R,BB,SO,SB
```

Legend: `GP` games played, `AB` at-bats, `H` hits, `2B/3B/HR` doubles/triples/
home runs, `RBI` runs batted in, `R` runs, `BB` walks, `SO` strikeouts,
`SB` stolen bases. Jersey `Number` is unique and is the natural key.

## Model

- **Entity `PlayerStats`** (plain JPA class — a record cannot be an `@Entity`):
  `jerseyNumber` (Integer, `@Id`), `firstName`, `lastName`, `gamesPlayed`,
  `atBats`, `hits`, `doubles`, `triples`, `homeRuns`, `rbi`, `runs`, `walks`,
  `strikeouts`, `stolenBases`.
- **Repository `PlayerStatsRepository extends JpaRepository<PlayerStats, Integer>`.**
- **DTO `PlayerStatsResponse`** (record): `jerseyNumber`, `name`
  (`firstName + " " + lastName`), `gamesPlayed`, `atBats`, `hits`, `doubles`,
  `triples`, `homeRuns`, `rbi`, `runs`, `walks`, `strikeouts`, `stolenBases`,
  and a computed `battingAvg`.

## Startup loading

- A `@Component` loader (`RosterStatsLoader`, `CommandLineRunner`) reads the CSV
  from the classpath and saves all rows **only if the table is empty** (mirror
  the existing `PlayerService.seedPlayers()` guard so tests can seed their own).
- Parse defensively: skip the header; trim fields; ignore blank lines.

## API

Base path `/api/stats`.

| request | result |
|---------|--------|
| `GET /api/stats` | every player's stats, **sorted by `battingAvg` descending** |
| `GET /api/stats/{number}` | that jersey's stats; **404** if the number is unknown |

## `battingAvg`

Batting average is **hits ÷ at-bats**, rounded to 3 decimal places (e.g. Tate
Hoehns, #4: 12 H / 28 AB → `0.429`). A player with **0 at-bats** must return
`0.000`, never a divide-by-zero, `NaN`, or `Infinity`.

## Acceptance examples

Against the seeded CSV roster:

| request | expectation |
|---------|-------------|
| `GET /api/stats` | 12 players, first entry has the highest `battingAvg` |
| `GET /api/stats/4` | `name` = "Tate Hoehns", `atBats` = 28, `hits` = 12, `battingAvg` = 0.429 |
| `GET /api/stats/92` | `stolenBases` = 41 |
| `GET /api/stats/777` | 404 |

## Conventions & constraints

- Follow `AGENTS.md` — **Gradle** (`./gradlew`), no Lombok, entities are plain
  classes / DTOs are records.
- Tests follow the house integration-testing conventions (the `writing-tests`
  skill) and must include a **0 at-bats → `0.000`** case.
- **`./gradlew build` must be green** — the JaCoCo coverage gate and Spotless.

## Delivery

Built by the sub-agent team via the `ship-feature` skill (developer → tester →
parallel review + docs → one fix pass → open PR → address Copilot review → ship).
