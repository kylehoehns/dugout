# Omit the "hot/cold lately" tag — no time-series data

The ticket asked (conditionally — "if you can") to tag each kid hot or cold
based on how they've been hitting **lately**. We omit it. The model and CSV hold
only season aggregate totals (`atBats`, `hits`, `gamesPlayed`, …) — there is no
per-game log, date, or per-at-bat history anywhere, so recent form is not
derivable. Any tag we shipped would be a season-long signal wearing a "lately"
label it can't back up, which would mislead the coach. Adding it later requires a
new time-ordered data source (per-game or dated at-bat records); until that
exists the honest answer is to leave the field out.
