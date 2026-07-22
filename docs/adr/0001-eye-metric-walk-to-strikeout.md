# 0001 — The "eye" metric is walks ÷ max(strikeouts, 1)

- Status: Accepted
- Date: 2026-07-21

## Context

The plate-discipline leaderboard ranks kids by their "eye" — the walk-to-strikeout
ratio, `BB / SO`. Higher means more patient. The raw ratio divides by zero for a
kid who has **never struck out**, and the coach explicitly asked that such a kid
"come out sensible and please don't let it blow up." (No kid in the current roster
has 0 strikeouts, so this is a purely defensive case — but it must be defined.)

Options considered:

1. **`BB / max(SO, 1)`** — floor strikeouts at 1. Every kid who has struck out at
   least once (the entire real roster) shows the true, recognizable `BB/SO`. A
   0-strikeout kid is treated as if they had 1 strikeout.
2. **`BB / (SO + 1)`** — add-one smoothing. Always defined, and a 0-K kid still
   ranks above a 1-K kid. But it changes *every* displayed number away from the
   true ratio, so a baseball-savvy parent won't recognize it as walk-to-strikeout.
3. **`BB/SO`, null when `SO = 0`** — report the true ratio and emit `null` for a
   0-K kid. Makes the JSON field nullable, complicating "the app just reads it."

## Decision

Compute the eye as **`walks / max(strikeouts, 1)`**, as a `BigDecimal` rounded to
**scale 3, `HALF_UP`** — reusing the exact rounding convention of
`PlayerStatsResponse.battingAvg` rather than inventing a second one.

## Consequences

- For every kid who has struck out at least once, the number *is* the true,
  recognizable walk-to-strikeout ratio (e.g. Mason Reed #23: 12 BB / 1 SO →
  `12.000`).
- A 0-strikeout kid gets `BB / 1` — a strong but finite number that never blows up.
- **Trade-off:** a 0-strikeout kid and a 1-strikeout kid with equal walks tie on
  eye. We accepted this over add-one smoothing because preserving the intuitive,
  literally-correct ratio for the common case matters more than distinguishing two
  hypothetical edge kids. The tie still breaks deterministically (see the spec's
  sort order).
