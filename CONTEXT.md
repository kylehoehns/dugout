# dugout

A small Spring Boot API tracking a youth baseball team's roster and batting
stats. The audience is parents and the coach, not statisticians — terminology
favors what they say on the bleachers.

## Language

**Batting average** (`battingAvg`):
A player's hits ÷ at-bats, rounded to 3 decimals (e.g. 12/28 → `0.429`). A player
with 0 at-bats is `0.000`, never a divide-by-zero.

**Team batting average** (`teamBattingAvg`):
The whole team's hits ÷ the whole team's at-bats (Σhits ÷ Σat-bats) over the
eligible players — a single true, at-bat-weighted average. Deliberately **not**
the mean of each player's individual average. See ADR 0001.
_Avoid_: "average of the averages" (that is a different, rejected calculation).

**Eligible player** (a "real contributor"):
A player with **at-bats ≥ 10**. Only eligible players appear in the team summary
and count toward the team batting average; kids who have barely batted are
excluded so a fluke 1-for-1 (`1.000`) can't distort the picture.

**Team summary**:
The season-to-date view of the whole team at `GET /api/stats/team` — one
top-line team batting average plus the eligible players, ordered most-hits-first.
_Avoid_: "team stats" (ambiguous with the per-player list at `GET /api/stats`).

**Hits leader**:
The eligible player with the most hits. Pinned to the top of the team summary
because "who has the most hits" is the question parents ask most.
