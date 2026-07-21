# Pooled team batting average, not average-of-averages

The ticket asked for the team's batting average as "everybody's average,
averaged together." We instead compute a **pooled** rate — Σhits ÷ Σat-bats
across qualified players — because average-of-averages gives a light-sample
hitter (say 4-for-40) the same weight as a heavy hitter (40-for-100) and so
misrepresents how the team actually hit. Pooling weights every at-bat equally
and is the true team batting average; on the seed roster the two differ (pooled
`0.438` vs average-of-averages `~0.451`). We reuse the existing scale-3 HALF_UP
rounding so the team number is formatted identically to a player's `battingAvg`.
