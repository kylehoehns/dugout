# Eye number is BB/SO — guard SO=0 by dividing by 1

The plate-discipline ticket (#17) asks to rank kids by their "walk-to-strikeout
eye" and explicitly flags the case it can't resolve: "we've got a kid or two who
**almost never strike out** — no idea how the math should treat that. Just make
it come out sensible and please don't let it blow up."

**Decision.** The eye number is the literal walk-to-strikeout ratio,
`eye = walks / strikeouts` (the standard baseball BB/K ratio) — higher is a
better eye. When `strikeouts = 0`, divide by `1` instead, so the value is finite
and equals the walk count (`eye = walks`). A kid with no walks and no strikeouts
comes out `0.000`. Rounded scale-3, HALF_UP — the same rounding as batting
average, one shared source of truth.

**Why divide-by-1 (not the alternatives).**
- *Bounded `BB/(BB+SO)`* (0–1, `SO=0 → 1.0`) reads clean but isn't a
  "walk-to-strikeout" ratio, compresses the whole team into a narrow band, and
  **still** divides by zero when `BB=SO=0`. It solves less and matches the ask
  worse.
- *Difference `BB − SO`* never divides but is a volume count, not an eye: it
  rewards a high-walk / high-strikeout kid over a patient low-strikeout one,
  which is the opposite of "good eye".
- *Divide-by-1 for `SO=0`* keeps the plain, literal `BB/SO` everyone else gets,
  gives the almost-never-strikes-out kid a sensibly high finite score (their
  walk total), and cannot blow up. It answers "make it sensible and don't let it
  explode" directly.

**Consequence.** The eye number is unbounded above (e.g. a 12-walk, 1-strikeout
line is `12.000`), which is correct — that kid genuinely has an elite eye. The
seeded roster has no `SO=0` line, so the guard is exercised only by tests that
seed their own zero-strikeout player.
