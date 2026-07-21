# Hot/cold "hitting lately" tag deferred — no per-game data exists

Issue #14 asked (with an explicit "if you can") to tag each player **hot** or
**cold** based on how they have been hitting **lately**. We are **not** shipping
that tag in this endpoint.

**Why:** "lately" requires recent, time-ordered performance — a game-by-game log
with dates. Our only data source (`roster-stats.csv` → `PlayerStats`) holds
**season totals only** (GP, AB, H, …); nothing records when hits happened. Any
"hot/cold" derived from season totals would be a proxy that mislabels a fast
starter now slumping, which is worse than omitting it.

Revisit if/when a per-game data source is added. Until then the team summary
carries no hot/cold field.
