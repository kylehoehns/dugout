# dugout

The batting-stats domain for a youth baseball team — players, their season
batting numbers, and the team-level rollups the coach and parents read through
the `/api/stats` API.

## Language

**Batting average**:
A hitter's hits ÷ at-bats, expressed to three decimal places (e.g. `0.429`).
Always rounded scale-3, HALF_UP.
_Avoid_: average, avg (except as the JSON field `battingAvg`)

**Team batting average**:
The whole team's hitting as one number: total hits ÷ total at-bats across the
**qualified** players (a pooled rate), rounded the same way as a player's
batting average. It is **not** the average of each player's individual average.
_Avoid_: average of averages, team avg

**Qualified player**:
A player with enough at-bats to be a real contributor — `atBats ≥ 10`. Only
qualified players appear in the team summary and count toward the team batting
average. A player below the threshold (e.g. a 1-for-1 season) is excluded.
_Avoid_: eligible, active, regular

**Team summary**:
The team-level rollup returned by `GET /api/stats/team-summary`: the team
batting average plus the qualified players ordered most-hits-first.
_Avoid_: team stats, roster summary

**Eye number** (walk-to-strikeout eye):
A hitter's plate-discipline ratio — walks ÷ strikeouts (`BB / SO`), a real
baseball "BB/K ratio". Higher means a better eye (patient, rarely chases). A kid
who **almost never strikes out** (`SO = 0`) would divide by zero, so `SO` is
treated as `1` for the division (eye = `BB`); a kid with no walks and no
strikeouts is `0.000`. Expressed to three decimals, rounded the same way as
batting average (scale-3, HALF_UP). See `docs/adr/0003-eye-so-zero-guard.md`.
_Avoid_: BB/K, discipline score, patience rating

**Plate-discipline ranking**:
The team ranked by **eye number** returned by
`GET /api/stats/plate-discipline`: the **qualified** players (same `atBats ≥ 10`
rule as the team summary) ordered best-eye-first, each shown with just enough to
recognize and read them — jersey number, name, walks, strikeouts, and the eye
number. Best eye first; ties broken by more walks, then jersey number.
_Avoid_: eye ranking, discipline leaderboard
