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

## Stats

Roster batting stats, seeded from a CSV file on startup. Jersey number is the
natural key. This is additive and does not affect the `Player` entity or the
`/api/players` endpoints above.

### List all players' stats

Returns every player's batting stats, **sorted by `battingAvg` descending**.

```
GET /api/stats
```

**Response — 200 OK**

An array of stats objects.

```json
[
  {
    "jerseyNumber": 4,
    "name": "Tate Hoehns",
    "gamesPlayed": 20,
    "atBats": 28,
    "hits": 12,
    "doubles": 3,
    "triples": 0,
    "homeRuns": 2,
    "rbi": 9,
    "runs": 11,
    "walks": 5,
    "strikeouts": 6,
    "stolenBases": 4,
    "battingAvg": 0.429
  },
  {
    "jerseyNumber": 92,
    "name": "Casey Hoehns",
    "gamesPlayed": 18,
    "atBats": 30,
    "hits": 0,
    "doubles": 0,
    "triples": 0,
    "homeRuns": 0,
    "rbi": 0,
    "runs": 2,
    "walks": 1,
    "strikeouts": 10,
    "stolenBases": 41,
    "battingAvg": 0.000
  }
]
```

**Example**

```
GET /api/stats
```

```
HTTP/1.1 200 OK
Content-Type: application/json

[
  { "jerseyNumber": 4, "name": "Tate Hoehns", "gamesPlayed": 20, "atBats": 28, "hits": 12,
    "doubles": 3, "triples": 0, "homeRuns": 2, "rbi": 9, "runs": 11, "walks": 5,
    "strikeouts": 6, "stolenBases": 4, "battingAvg": 0.429 }
]
```

---

### Get stats by jersey number

```
GET /api/stats/{number}
```

**Path parameters**

| Name     | Type   | Required | Description                          |
|----------|--------|----------|--------------------------------------|
| `number` | number | Yes      | Player's jersey number (natural key). |

**Response — 200 OK**

```json
{
  "jerseyNumber": 4,
  "name": "Tate Hoehns",
  "gamesPlayed": 20,
  "atBats": 28,
  "hits": 12,
  "doubles": 3,
  "triples": 0,
  "homeRuns": 2,
  "rbi": 9,
  "runs": 11,
  "walks": 5,
  "strikeouts": 6,
  "stolenBases": 4,
  "battingAvg": 0.429
}
```

**`battingAvg`**

Computed as `hits ÷ atBats`, rounded to 3 decimal places (half-up). A player
with 0 at-bats returns `0.000` rather than a divide-by-zero, `NaN`, or
`Infinity`.

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
