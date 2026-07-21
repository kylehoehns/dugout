# Team batting average uses true Σhits ÷ Σat-bats, not the mean of averages

Issue #14 asked for the team's batting average by "taking everybody's average and
averaging those together" (the unweighted mean of each player's average). We
instead compute the **true team average = Σhits ÷ Σat-bats** over eligible
players (at-bats ≥ 10).

**Why:** the mean-of-averages over-weights kids with few at-bats — a 3-for-4
bench kid would swing the team number as hard as a 40-at-bat starter. Σhits ÷
Σat-bats is the standard, at-bat-weighted team average and is what "how is the
whole team hitting" actually means. On the current seeded roster the two happen to
be close (true `0.438` vs mean `0.429`), but they diverge as at-bat counts spread
out, so we lock in the correct one now.

This is a deliberate deviation from the ticket's literal wording — recorded here
so a future reader doesn't "fix" the code back to matching the ticket text.
