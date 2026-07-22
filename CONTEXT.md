# dugout — Domain Glossary

The ubiquitous language for this project. Definitions only — no implementation
details. See `docs/adr/` for decisions and `docs/*-spec.md` for build specs.

## Plate appearance (PA)

Every trip a kid takes to the plate that ends their turn. For dugout's purposes
it is **at-bats + walks** (`AB + BB`) — the honest measure of "how much a kid
actually batted," because official **at-bats exclude walks**. Used to decide who
has batted enough to qualify for a leaderboard (see [[qualified-batter]]).

## Qualified batter

A kid with enough [[plate-appearance]]s to belong on a leaderboard, rather than
one who "barely batted." The bar is a minimum number of plate appearances; a kid
below it is simply omitted, never an error. See ADR 0002.

## Eye (plate discipline)

How good a kid's judgement is at the plate — patient hitters who **walk a lot and
rarely strike out** have a good eye. Quantified as the **walk-to-strikeout ratio**
(`BB / SO`): higher is better. A kid who never strikes out is handled so the number
stays sensible and never divides by zero (see ADR 0001). "Plate discipline" and
"eye" are the same thing.

## Walk (BB)

A kid reaching first base by not chasing bad pitches — a mark of patience.

## Strikeout (SO / K)

A kid making an out by swinging through or taking three strikes — the opposite of
a [[walk]].
