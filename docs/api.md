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

## Roster Batting Stats

Batting stats for a roster seeded from `roster-stats.csv` on startup. This is
additive — it does not change the `Player` entity or the `/api/players`
endpoints above.

### List all players' stats

Returns every player's stats, sorted by `battingAvg` descending.

```
GET /api/stats
```

**Response — 200 OK**

An array of player stats objects.

| Field          | Type    | Description                              |
|----------------|---------|-------------------------------------------|
| `jerseyNumber` | number  | Unique jersey number (natural key).        |
| `name`         | string  | `firstName + " " + lastName`.              |
| `gamesPlayed`  | number  | Games played.                              |
| `atBats`       | number  | At-bats.                                   |
| `hits`         | number  | Hits.                                      |
| `doubles`      | number  | Doubles.                                   |
| `triples`      | number  | Triples.                                   |
| `homeRuns`     | number  | Home runs.                                 |
| `rbi`          | number  | Runs batted in.                            |
| `runs`         | number  | Runs scored.                               |
| `walks`        | number  | Walks (bases on balls).                    |
| `strikeouts`   | number  | Strikeouts.                                |
| `stolenBases`  | number  | Stolen bases.                              |
| `battingAvg`   | number  | Computed, see [Batting average](#batting-average) below. |

**Example**

```
GET /api/stats
```

```json
[
  {
    "jerseyNumber": 4,
    "name": "Tate Hoehns",
    "gamesPlayed": 14,
    "atBats": 28,
    "hits": 12,
    "doubles": 3,
    "triples": 0,
    "homeRuns": 1,
    "rbi": 9,
    "runs": 10,
    "walks": 5,
    "strikeouts": 6,
    "stolenBases": 4,
    "battingAvg": 0.429
  },
  {
    "jerseyNumber": 92,
    "name": "Sample Player",
    "gamesPlayed": 14,
    "atBats": 30,
    "hits": 9,
    "doubles": 1,
    "triples": 0,
    "homeRuns": 0,
    "rbi": 5,
    "runs": 7,
    "walks": 3,
    "strikeouts": 8,
    "stolenBases": 41,
    "battingAvg": 0.300
  }
]
```

---

### Get stats by jersey number

```
GET /api/stats/{number}
```

**Path parameters**

| Name     | Type   | Required | Description       |
|----------|--------|----------|--------------------|
| `number` | number | Yes      | Player's jersey number. |

**Response — 200 OK**

A single player stats object, same shape as above.

**Example**

```
GET /api/stats/4
```

```json
{
  "jerseyNumber": 4,
  "name": "Tate Hoehns",
  "gamesPlayed": 14,
  "atBats": 28,
  "hits": 12,
  "doubles": 3,
  "triples": 0,
  "homeRuns": 1,
  "rbi": 9,
  "runs": 10,
  "walks": 5,
  "strikeouts": 6,
  "stolenBases": 4,
  "battingAvg": 0.429
}
```

**Error cases**

| Status | Condition                             |
|--------|----------------------------------------|
| 404    | No player exists with that jersey number |

---

### Batting average

`battingAvg` is computed as **hits ÷ at-bats**, rounded to 3 decimal places
(e.g. #4 Tate Hoehns: 12 hits / 28 at-bats → `0.429`). A player with 0 at-bats
returns `0.000` (never a divide-by-zero, `NaN`, or `Infinity`).
