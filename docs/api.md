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

Batting stats for the roster, seeded from `roster-stats.csv` on application
startup. This is additive — it does not change the `Player` entity or the
`/api/players` endpoints above.

### List all stats

Returns every player's batting stats, sorted by `battingAvg` descending
(highest average first).

```
GET /api/stats
```

**Response — 200 OK**

An array of `PlayerStatsResponse` objects.

```json
[
  {
    "jerseyNumber": 4,
    "name": "Tate Hoehns",
    "gamesPlayed": 16,
    "atBats": 28,
    "hits": 12,
    "doubles": 1,
    "triples": 0,
    "homeRuns": 0,
    "rbi": 10,
    "runs": 17,
    "walks": 9,
    "strikeouts": 8,
    "stolenBases": 20,
    "battingAvg": 0.429
  },
  {
    "jerseyNumber": 92,
    "name": "Cooper Lane",
    "gamesPlayed": 15,
    "atBats": 32,
    "hits": 9,
    "doubles": 2,
    "triples": 1,
    "homeRuns": 0,
    "rbi": 6,
    "runs": 14,
    "walks": 4,
    "strikeouts": 11,
    "stolenBases": 41,
    "battingAvg": 0.281
  }
]
```

---

### Get stats by jersey number

```
GET /api/stats/{number}
```

**Path parameters**

| Name     | Type   | Required | Description               |
|----------|--------|----------|----------------------------|
| `number` | number | Yes      | Player's jersey number.    |

**Response — 200 OK**

```json
{
  "jerseyNumber": 4,
  "name": "Tate Hoehns",
  "gamesPlayed": 16,
  "atBats": 28,
  "hits": 12,
  "doubles": 1,
  "triples": 0,
  "homeRuns": 0,
  "rbi": 10,
  "runs": 17,
  "walks": 9,
  "strikeouts": 8,
  "stolenBases": 20,
  "battingAvg": 0.429
}
```

**Error cases**

| Status | Condition                             |
|--------|----------------------------------------|
| 404    | No player exists with that jersey number |

**Example — unknown jersey number**

```
GET /api/stats/777
```

```
HTTP/1.1 404 Not Found
```

---

### `PlayerStatsResponse` fields

| Field          | Type    | Description                                       |
|----------------|---------|----------------------------------------------------|
| `jerseyNumber` | number  | Player's jersey number (unique, natural key).      |
| `name`         | string  | `firstName + " " + lastName`.                      |
| `gamesPlayed`  | number  | Games played.                                      |
| `atBats`       | number  | At-bats.                                           |
| `hits`         | number  | Hits.                                              |
| `doubles`      | number  | Doubles.                                           |
| `triples`      | number  | Triples.                                           |
| `homeRuns`     | number  | Home runs.                                         |
| `rbi`          | number  | Runs batted in.                                    |
| `runs`         | number  | Runs scored.                                       |
| `walks`        | number  | Walks (bases on balls).                            |
| `strikeouts`   | number  | Strikeouts.                                        |
| `stolenBases`  | number  | Stolen bases.                                      |
| `battingAvg`   | number  | Computed batting average (see below).              |

**`battingAvg` computation** — `hits ÷ atBats`, rounded to 3 decimal places
(e.g. jersey #4, Tate Hoehns: 12 H / 28 AB → `0.429`). When `atBats` is `0`,
`battingAvg` is `0.000` — never a divide-by-zero, `NaN`, or `Infinity`.
