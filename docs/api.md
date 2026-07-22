# Dugout HTTP API

Base URL: `http://localhost:8080`

All request and response bodies use `application/json`.

---

## Players

### List players

Returns all players, or filters by position when the optional query parameter is supplied.

```
GET /api/players
```

**Query parameters**

| Name       | Type   | Required | Description                                                  |
|------------|--------|----------|--------------------------------------------------------------|
| `position` | string | No       | Filter by position (case-insensitive). Returns `[]` when no players match. |

**Response — 200 OK**

An array of player objects. Empty array when no results match.

```json
[
  { "id": 1, "name": "Hank Aaron",  "position": "RF" },
  { "id": 2, "name": "Willie Mays", "position": "CF" },
  { "id": 3, "name": "Ozzie Smith", "position": "SS" }
]
```

**Example — all players**

```
GET /api/players
```

```json
[
  { "id": 1, "name": "Hank Aaron",  "position": "RF" },
  { "id": 2, "name": "Willie Mays", "position": "CF" },
  { "id": 3, "name": "Ozzie Smith", "position": "SS" }
]
```

**Example — filter by position**

```
GET /api/players?position=rf
```

```json
[
  { "id": 1, "name": "Hank Aaron", "position": "RF" }
]
```

**Example — no matches**

```
GET /api/players?position=DH
```

```json
[]
```

---

### Get player by ID

```
GET /api/players/{id}
```

**Path parameters**

| Name | Type   | Required | Description     |
|------|--------|----------|-----------------|
| `id` | number | Yes      | Player's numeric ID. |

**Response — 200 OK**

```json
{ "id": 2, "name": "Willie Mays", "position": "CF" }
```

**Error cases**

| Status | Condition                     |
|--------|-------------------------------|
| 404    | No player exists with that ID |

---

### Create a player

```
POST /api/players
```

**Request body**

| Field      | Type   | Required | Description                       |
|------------|--------|----------|-----------------------------------|
| `name`     | string | Yes      | Full name of the player.          |
| `position` | string | Yes      | Fielding position abbreviation (e.g. `"SS"`, `"CF"`). |

```json
{ "name": "Roberto Clemente", "position": "RF" }
```

**Response — 201 Created**

The newly created player, including the server-assigned `id`.

```json
{ "id": 4, "name": "Roberto Clemente", "position": "RF" }
```

**Example**

```
POST /api/players
Content-Type: application/json

{ "name": "Roberto Clemente", "position": "RF" }
```

```
HTTP/1.1 201 Created
Content-Type: application/json

{ "id": 4, "name": "Roberto Clemente", "position": "RF" }
```

---

## Roster Stats

Batting stats for the roster, seeded from `src/main/resources/roster-stats.csv`
on application startup — **only when the `PlayerStats` table is empty** (so
tests can seed their own data). This is a separate table/entity from `Player`
and does not affect the `/api/players` endpoints above.

### List all player stats

Returns every player's batting stats, sorted by `battingAvg` descending
(highest average first).

```
GET /api/stats
```

**Response — 200 OK**

An array of player stats objects.

| Field         | Type    | Description                                      |
|---------------|---------|---------------------------------------------------|
| `jerseyNumber`| number  | Jersey number (unique, natural key).               |
| `name`        | string  | `firstName + " " + lastName`.                      |
| `gamesPlayed` | number  | Games played.                                      |
| `atBats`      | number  | At-bats.                                           |
| `hits`        | number  | Hits.                                              |
| `doubles`     | number  | Doubles.                                           |
| `triples`     | number  | Triples.                                           |
| `homeRuns`    | number  | Home runs.                                         |
| `rbi`         | number  | Runs batted in.                                    |
| `runs`        | number  | Runs scored.                                       |
| `walks`       | number  | Walks (base on balls).                             |
| `strikeouts`  | number  | Strikeouts.                                        |
| `stolenBases` | number  | Stolen bases.                                      |
| `battingAvg`  | number  | Computed — see [Batting average](#batting-average) below. |

**Example**

```
GET /api/stats
```

```json
[
  { "jerseyNumber": 23, "name": "Mason Reed",   "gamesPlayed": 23, "atBats": 41, "hits": 25, "doubles": 10, "triples": 0, "homeRuns": 0, "rbi": 20, "runs": 25, "walks": 12, "strikeouts": 1,  "stolenBases": 13, "battingAvg": 0.610 },
  { "jerseyNumber": 92, "name": "Cooper Lane",  "gamesPlayed": 23, "atBats": 45, "hits": 26, "doubles": 2,  "triples": 0, "homeRuns": 0, "rbi": 4,  "runs": 37, "walks": 18, "strikeouts": 9,  "stolenBases": 41, "battingAvg": 0.578 },
  { "jerseyNumber": 36, "name": "Owen Bell",    "gamesPlayed": 21, "atBats": 27, "hits": 14, "doubles": 0,  "triples": 0, "homeRuns": 0, "rbi": 8,  "runs": 17, "walks": 7,  "strikeouts": 5,  "stolenBases": 22, "battingAvg": 0.519 },
  { "jerseyNumber": 64, "name": "Landon Cross", "gamesPlayed": 23, "atBats": 43, "hits": 21, "doubles": 3,  "triples": 0, "homeRuns": 0, "rbi": 18, "runs": 27, "walks": 13, "strikeouts": 10, "stolenBases": 26, "battingAvg": 0.488 },
  { "jerseyNumber": 86, "name": "Easton Gray",  "gamesPlayed": 23, "atBats": 35, "hits": 17, "doubles": 0,  "triples": 1, "homeRuns": 0, "rbi": 11, "runs": 27, "walks": 18, "strikeouts": 11, "stolenBases": 29, "battingAvg": 0.486 },
  { "jerseyNumber": 11, "name": "Carter Hale",  "gamesPlayed": 23, "atBats": 51, "hits": 23, "doubles": 6,  "triples": 1, "homeRuns": 0, "rbi": 22, "runs": 27, "walks": 12, "strikeouts": 11, "stolenBases": 20, "battingAvg": 0.451 },
  { "jerseyNumber": 4,  "name": "Tate Hoehns",  "gamesPlayed": 16, "atBats": 28, "hits": 12, "doubles": 1,  "triples": 0, "homeRuns": 0, "rbi": 10, "runs": 17, "walks": 9,  "strikeouts": 8,  "stolenBases": 20, "battingAvg": 0.429 },
  { "jerseyNumber": 16, "name": "Brody Vance",  "gamesPlayed": 20, "atBats": 38, "hits": 14, "doubles": 4,  "triples": 0, "homeRuns": 0, "rbi": 13, "runs": 18, "walks": 9,  "strikeouts": 18, "stolenBases": 16, "battingAvg": 0.368 },
  { "jerseyNumber": 99, "name": "Nolan Pierce", "gamesPlayed": 23, "atBats": 43, "hits": 15, "doubles": 1,  "triples": 0, "homeRuns": 0, "rbi": 22, "runs": 19, "walks": 12, "strikeouts": 12, "stolenBases": 17, "battingAvg": 0.349 },
  { "jerseyNumber": 1,  "name": "Micah Flynn",  "gamesPlayed": 14, "atBats": 21, "hits": 7,  "doubles": 0,  "triples": 0, "homeRuns": 0, "rbi": 4,  "runs": 10, "walks": 6,  "strikeouts": 9,  "stolenBases": 8,  "battingAvg": 0.333 },
  { "jerseyNumber": 45, "name": "Jonah West",   "gamesPlayed": 23, "atBats": 29, "hits": 8,  "doubles": 1,  "triples": 0, "homeRuns": 0, "rbi": 5,  "runs": 20, "walks": 22, "strikeouts": 19, "stolenBases": 17, "battingAvg": 0.276 },
  { "jerseyNumber": 12, "name": "Silas Fox",    "gamesPlayed": 22, "atBats": 35, "hits": 9,  "doubles": 1,  "triples": 0, "homeRuns": 0, "rbi": 3,  "runs": 14, "walks": 11, "strikeouts": 21, "stolenBases": 8,  "battingAvg": 0.257 }
]
```

---

### Get player stats by jersey number

```
GET /api/stats/{number}
```

**Path parameters**

| Name     | Type   | Required | Description               |
|----------|--------|----------|----------------------------|
| `number` | number | Yes      | Player's jersey number.    |

**Response — 200 OK**

```json
{ "jerseyNumber": 4, "name": "Tate Hoehns", "gamesPlayed": 16, "atBats": 28, "hits": 12, "doubles": 1, "triples": 0, "homeRuns": 0, "rbi": 10, "runs": 17, "walks": 9, "strikeouts": 8, "stolenBases": 20, "battingAvg": 0.429 }
```

**Example**

```
GET /api/stats/4
```

```json
{ "jerseyNumber": 4, "name": "Tate Hoehns", "gamesPlayed": 16, "atBats": 28, "hits": 12, "doubles": 1, "triples": 0, "homeRuns": 0, "rbi": 10, "runs": 17, "walks": 9, "strikeouts": 8, "stolenBases": 20, "battingAvg": 0.429 }
```

**Error cases**

| Status | Condition                              |
|--------|-----------------------------------------|
| 404    | No player stats exist for that jersey number |

**Example — unknown jersey number**

```
GET /api/stats/777
```

```
HTTP/1.1 404 Not Found
```

---

### Plate-discipline leaderboard

Returns qualifying players ranked by plate discipline ("eye" — the
walk-to-strikeout ratio), best eye first. This is purely additive: it doesn't
change the `/api/stats` or `/api/stats/{number}` responses above.

```
GET /api/stats/plate-discipline
```

No query parameters.

**Response — 200 OK**

An array of plate-discipline objects, one per **qualifying** player (see
[Qualification and eye](#qualification-and-eye) below). Returns `200 []` if no
player qualifies.

| Field         | Type    | Description                                      |
|---------------|---------|---------------------------------------------------|
| `jerseyNumber`| number  | Jersey number (unique, natural key).               |
| `name`        | string  | `firstName + " " + lastName`.                      |
| `walks`       | number  | Walks (base on balls).                             |
| `strikeouts`  | number  | Strikeouts.                                        |
| `eye`         | number  | Computed — see [Qualification and eye](#qualification-and-eye) below. |

**Sort order:** `eye` descending, then `walks` descending, then
`jerseyNumber` ascending (deterministic tie-break).

**Example**

```
GET /api/stats/plate-discipline
```

```json
[
  { "jerseyNumber": 23, "name": "Mason Reed",   "walks": 12, "strikeouts": 1,  "eye": 12.000 },
  { "jerseyNumber": 92, "name": "Cooper Lane",  "walks": 18, "strikeouts": 9,  "eye": 2.000 },
  { "jerseyNumber": 86, "name": "Easton Gray",  "walks": 18, "strikeouts": 11, "eye": 1.636 },
  { "jerseyNumber": 36, "name": "Owen Bell",    "walks": 7,  "strikeouts": 5,  "eye": 1.400 },
  { "jerseyNumber": 64, "name": "Landon Cross", "walks": 13, "strikeouts": 10, "eye": 1.300 },
  { "jerseyNumber": 45, "name": "Jonah West",   "walks": 22, "strikeouts": 19, "eye": 1.158 },
  { "jerseyNumber": 4,  "name": "Tate Hoehns",  "walks": 9,  "strikeouts": 8,  "eye": 1.125 },
  { "jerseyNumber": 11, "name": "Carter Hale",  "walks": 12, "strikeouts": 11, "eye": 1.091 },
  { "jerseyNumber": 99, "name": "Nolan Pierce", "walks": 12, "strikeouts": 12, "eye": 1.000 },
  { "jerseyNumber": 1,  "name": "Micah Flynn",  "walks": 6,  "strikeouts": 9,  "eye": 0.667 },
  { "jerseyNumber": 12, "name": "Silas Fox",    "walks": 11, "strikeouts": 21, "eye": 0.524 },
  { "jerseyNumber": 16, "name": "Brody Vance",  "walks": 9,  "strikeouts": 18, "eye": 0.500 }
]
```

**Example — no qualifying players**

```
GET /api/stats/plate-discipline
```

```json
[]
```

**Error cases**

None — the endpoint always returns `200`, with an empty array when nobody
qualifies.

---

### Batting average

`battingAvg` is computed on every read (not stored) as **hits ÷ at-bats**,
rounded to 3 decimal places with `HALF_UP` rounding. A player with **0
at-bats** (or a null at-bats value) returns `0.000` rather than dividing by
zero. For example, jersey `4` (Tate Hoehns) has 12 hits in 28 at-bats:
`12 / 28 = 0.4285... → 0.429`.

---

### Qualification and eye

Both computed on every read (not stored), used only by
`GET /api/stats/plate-discipline`:

- **Eye** = `walks ÷ max(strikeouts, 1)`, rounded to 3 decimal places with
  `HALF_UP` rounding (same scale/rounding as `battingAvg` above). Flooring
  strikeouts at `1` avoids a divide-by-zero for a player who has never
  struck out — e.g. `walks = 8`, `strikeouts = 0` → `eye = 8.000`.
- **Plate appearances** = `atBats + walks`. A player must have **plate
  appearances >= 10** to appear in the leaderboard; players below that bar
  are omitted entirely (not returned with a `0` or `null` eye).
- A `null` `walks`, `strikeouts`, or `atBats` is treated as `0`.
