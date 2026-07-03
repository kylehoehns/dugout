---
name: ship-feature
description: >
  Use when building a feature end-to-end from a written spec with this project's
  sub-agent team — asks like "build the feature in docs/<x>-spec.md" or "ship the
  feature in the spec". Orchestrates the developer, tester, reviewers, and
  doc-writer through build → test → review → PR → CI → address-review → ship.
---

# Ship a Feature (team orchestration)

You orchestrate the sub-agents in `.claude/agents/`; you do not implement
directly. Run this pipeline top to bottom.

## Build

1. **Read the spec** you were pointed at and plan from it.
2. **developer** — implement the production code; wait until it compiles.
3. **tester** — write tests; `./gradlew build` must be green (the coverage gate).
   If the build fails on **Spotless** formatting, run `./gradlew spotlessApply`
   and re-verify — Spotless prints that exact command in its failure output.
4. **Review + docs — launch in parallel.** Capture the change first with
   `git diff HEAD`. Then in ONE message, make four `Task` calls together, **passing
   that diff to each reviewer** (they're read-only and can't fetch it themselves):
   `reuse-reviewer`, `quality-reviewer`, `efficiency-reviewer` (they return
   findings) and `doc-writer` (writes `docs/api.md`). Do **not** run them one at a
   time.
5. **Fix (one pass)** — if the reviewers raised actionable findings, hand them to
   `developer` to fix, then `tester` to re-verify `./gradlew build` is green. Do
   this at most once; do not loop.
6. Confirm `./gradlew build` is green **locally** before you push.

## Ship

7. **Open the PR.** Commit the work, push the branch, and open a PR **targeting
   this stage's scaffold branch** (for this talk that is `stage-7-start`, so the
   PR diff shows only what the team built on top of the scaffold — never `main`):

   ```bash
   OWNER_REPO=$(gh repo view --json nameWithOwner -q .nameWithOwner)   # e.g. kylehoehns/dugout
   BASE=stage-7-start
   HEAD=$(git branch --show-current)                                    # stage-7
   git add -A && git commit -m "<concise feature summary>"
   git push -u origin "$HEAD"
   # gh pr create does NOT support --json/-q; it prints the PR URL. Create it,
   # then read the number back with gh pr view.
   gh pr create --base "$BASE" --head "$HEAD" \
        --title "<feature title>" --body "<what/why, links the spec>"
   PR=$(gh pr view "$HEAD" --json number -q .number)
   echo "opened PR #$PR"
   ```

## Make CI green

The `CI` workflow runs `./gradlew build` (tests + JaCoCo coverage + Spotless) on
every push and PR. **Green CI is a hard gate — do not touch the review until the
build passes.** Local green does not guarantee CI green (Java version, formatting,
coverage differences), so trust the pipeline, not your laptop.

8. **Watch the checks and fix red builds.** Block on the run, and if it fails,
   pull the *actual failing output* and fix from it — never guess:

   ```bash
   gh pr checks "$PR" --watch --fail-fast          # blocks; non-zero if a check fails
   # on failure, read the real logs:
   RUN=$(gh run list --branch "$HEAD" --limit 1 --json databaseId -q '.[0].databaseId')
   gh run view "$RUN" --log-failed
   ```

   Hand the failing log to `developer` to fix (e.g. Spotless formatting, a
   coverage shortfall → `tester` adds cases, a compile/test break). Re-verify
   `./gradlew build` locally, then `git commit && git push` — the push re-triggers
   CI. Re-watch. **Cap at 3 attempts;** if still red, stop and report the failure
   in your wrap-up rather than looping.

## Address the Copilot review (the loop)

GitHub Copilot code review is enabled on this repo, so it reviews the PR
automatically. This wait is **poll-based** — there is no push event to hook; you
poll `gh` on an interval until the review lands.

9. **Wait for Copilot.** Poll until an unresolved review thread authored by
   Copilot exists (bot login contains `copilot`). Check every ~30s, up to ~10
   minutes. If nothing arrives in that window, say so and skip to the wrap-up (do
   not hang forever). Fetch threads with GraphQL — you need thread node IDs to
   reply and resolve:

   ```bash
   gh api graphql -f query='
     query($owner:String!,$repo:String!,$pr:Int!){
       repository(owner:$owner,name:$repo){ pullRequest(number:$pr){
         reviewThreads(first:100){ nodes{
           id isResolved isOutdated
           comments(first:20){ nodes{ databaseId author{login} body path line } } } } } } }' \
     -f owner="${OWNER_REPO%/*}" -f repo="${OWNER_REPO#*/}" -F pr="$PR"
   ```

10. **Triage each unresolved Copilot thread — one at a time.** Read the comment
    and the code it points at, then make your **best judgment**:

    - **Address it** when it is a real correctness, safety, layering, or
      house-convention issue consistent with the spec and `AGENTS.md`.
    - **Decline it** when it is stylistic noise, out of scope for the spec, or
      conflicts with `AGENTS.md` (e.g. suggests Lombok or Maven — never do that).

    For a change, hand the specific edit to `developer` (keep a running list so you
    batch the re-verify). Whether you fix or decline, **always reply on the thread
    with a one-line rationale, then resolve it:**

    ```bash
    # reply on the thread
    gh api graphql -f query='
      mutation($t:ID!,$b:String!){ addPullRequestReviewThreadReply(
        input:{pullRequestReviewThreadId:$t, body:$b}){ comment{ id } } }' \
      -f t="<THREAD_ID>" -f b="<Fixed in <sha>: … | Leaving as-is: …>"
    # resolve the thread
    gh api graphql -f query='
      mutation($t:ID!){ resolveReviewThread(input:{threadId:$t}){ thread{ isResolved } } }' \
      -f t="<THREAD_ID>"
    ```

11. **Punch out the fixes.** If step 10 produced any changes: `tester` re-verifies
    `./gradlew build` is green, then commit and push. **The push re-triggers CI —
    go back to step 8 and get it green again** before continuing. Pushing also
    triggers a fresh Copilot pass; loop back to step 9 to handle new threads, but
    **cap the review at 2 rounds total.** After the second round, reply to any
    remaining threads with your decision, resolve them, and move on. Never loop
    unbounded.

## Wrap up

12. **Notify.** Post a short summary: the PR link, final **CI status**, how many
    Copilot comments you addressed vs. declined (with reasons), and the final
    commit sha. This is the "come back from lunch" report.
