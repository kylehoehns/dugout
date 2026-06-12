# Filter Players by Position — Spec

The input the sub-agent team builds from.

## Goal

Let clients list players filtered by fielding position, via an optional query
parameter on the existing players endpoint.

## API

`GET /api/players?position={position}`

- `position` is **optional**:
  - omitted → all players (unchanged behavior).
  - provided → only players whose `position` matches, **case-insensitive**.
- No matches → empty list (`200 []`), not an error.

## Implementation

- `PlayerRepository`: add a derived query `findByPositionIgnoreCase(String position)`.
- `PlayerService`: `getPlayers(String position)` — blank/null → `findAll()`,
  otherwise the derived query.
- `PlayerController`: the existing list method takes
  `@RequestParam(required = false) String position`.

## Acceptance example

Against the seeded roster (Hank Aaron RF, Willie Mays CF, Ozzie Smith SS):

| request | result |
|---------|--------|
| `GET /api/players` | all three players |
| `GET /api/players?position=SS` | just Ozzie Smith |
| `GET /api/players?position=ss` | just Ozzie Smith (case-insensitive) |
| `GET /api/players?position=ZZ` | `[]` |

## Conventions & constraints

- Follow `AGENTS.md` (Gradle — `./gradlew`).
- Tests follow the house testing conventions (the `writing-tests` skill).
- **`./gradlew build` must be green** — the JaCoCo coverage gate and Spotless.

## Delivery

Built by the sub-agent team via the `ship-feature` skill (developer → tester →
parallel review + docs → one fix pass if needed).
