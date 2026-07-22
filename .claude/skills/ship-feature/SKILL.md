---
name: ship-feature
description: >
  Use when building a feature end-to-end from a GitHub issue with this project's
  sub-agent team — asks like "ship the feature in issue #8", "build the ticket
  #8", or "ship GitHub issue #8". Reads the issue as the source of truth, grills
  it into a spec with grill-with-docs, then orchestrates the developer, tester,
  refactorer, reviewers, and doc-writer through build → test → refactor → review →
  PR → CI → address-review → verify live → notify.
---

# Ship a Feature (team orchestration)

The source of truth is a **GitHub issue**, not a spec file in the repo. Real
tickets are fuzzy, over-loaded, sometimes self-contradictory, and often ask for
things the data can't support. So the pipeline has two halves:

- **Understand** — *collaborative.* You and the human grill the ticket into a
  locked, written spec. This part is interactive — you ask, the human decides.
- **Build → Ship → Verify** — *autonomous.* Once the spec is locked, this runs
  unattended (the "kick it off, go to lunch" half). You orchestrate the
  sub-agents in `.claude/agents/`; you do not implement directly.

Run this top to bottom. **Do not start Build until the human confirms the spec.**

## Understand

1. **Read the ticket.** Pull the issue you were given — body *and* comments; it's
   the requirement:

   ```bash
   gh issue view "$ISSUE" --comments        # $ISSUE = the number you were given
   ```

   Do **not** resolve the gaps yourself. Note the fuzzy terms, the sentences that
   contradict each other, the unstated thresholds, and anything the code or CSV
   can't actually provide — then take them into the grilling.

2. **Grill it into a shared understanding.** Invoke the **`grill-with-docs`**
   skill and work the ticket with the human. It interviews one question at a
   time — recommending an answer and waiting for the call — and looks facts up in
   the environment rather than asking. As decisions land it records them per the
   `domain-modeling` formats:
   **`CONTEXT.md`** (the domain glossary) and **`docs/adr/NNNN-*.md`** (a real
   trade-off worth remembering — sparingly). Drive every ambiguity, contradiction,
   and data-gap to a decision. **Do not proceed until the human says it's locked.**

3. **Write the spec.** Turn the locked understanding into a buildable spec at
   `docs/<feature>-spec.md` — the same shape the team builds from: **Goal**,
   **Model**, **API** (paths + results), **formula rules**, and **acceptance
   examples** with concrete numbers. This spec — not the raw ticket — is what the
   Build phase consumes. **Treat the issue as strictly read-only: do not comment on it,
   label it, edit it, or close it** — it is a reusable example ticket meant to be
   run many times from a clean state. Leave the spec, `CONTEXT.md`, and any ADRs on
   disk (uncommitted is fine — the Ship step commits everything). Once the spec is
   written, get the human's go-ahead — the autonomous half runs from it.

## Build

> The spec on disk is the entire handoff — the Build phase needs nothing from the
> grilling conversation. Start this half in a **fresh context** (`/clear` or a new
> session in the same working tree) pointed at the spec: it keeps the unattended
> run clean, and it proves the spec stands on its own — if the build reaches for
> something only the chat knew, the spec was incomplete.

4. **Read `docs/<feature>-spec.md`** and plan from it. As you read, pull out every
   explicit **"reuse X / don't re-derive Y"** constraint the spec states — these
   are what reviewers most often catch as violations, so they must reach the
   developer verbatim (see step 5), not stay in your head.
5. **developer** — implement the production code. Get two things right in the
   hand-off prompt:
   - **Promote the spec's "reuse / don't re-derive" constraints to first-class,
     up-front instructions** — e.g. "reuse the existing `battingAverage` rounding
     rather than re-deriving scale/rounding." Buried in prose they get missed and
     cost a fix pass; stated plainly they get honored.
   - Tell the developer to finish by running **`./gradlew spotlessApply`** and then
     `./gradlew compileJava` before handing back — Spotless (googleJavaFormat, AOSP)
     violations otherwise surface later at the tester's full build and waste a
     round-trip. Wait until it compiles.
6. **tester** — write tests; `./gradlew build` must be green (the coverage gate).
   If the build fails on **Spotless** formatting, run `./gradlew spotlessApply`
   and re-verify — Spotless prints that exact command in its failure output.

   > **Refactor on green (once, before the review).** Now that the build is green,
   > hand the just-built code to the **`refactorer`** for a one-time,
   > behavior-preserving cleanup pass over the **production code** — the green test
   > suite is its safety net. It must re-run `./gradlew build` and confirm it's
   > still green before handing back. Run it **here**, not inside the fix loop
   > (step 8), and keep its instruction generic ("improve internal structure without
   > changing behavior") — don't name duplication or you're just prompting the
   > outcome. Reviewers then see already-clean code and file fewer findings.

7. **Review + docs — launch in parallel.** Capture the change first with
   `git diff HEAD`. Then in ONE message, make four `Task` calls together, **passing
   that diff to each reviewer** (they're read-only and can't fetch it themselves):
   `reuse-reviewer`, `quality-reviewer`, `efficiency-reviewer` (they return
   findings) and `doc-writer` (writes `docs/api.md`). Do **not** run them one at a
   time.
8. **Fix (one pass)** — if the reviewers raised actionable findings, hand them to
   `developer` to fix, then `tester` to re-verify `./gradlew build` is green. Do
   this at most once; do not loop.
9. Confirm `./gradlew build` is green **locally** before you push.

## Ship

10. **Open the PR.** Commit the work, push the branch, and open a PR **targeting
    this stage's scaffold branch** (for this talk that is `stage-8-start`, so the
    PR diff shows only what the team built on top of the scaffold — never `main`):

    ```bash
    OWNER_REPO=$(gh repo view --json nameWithOwner -q .nameWithOwner)   # e.g. kylehoehns/dugout
    BASE=stage-8-start
    HEAD=$(git branch --show-current)                                    # stage-8
    git add -A && git commit -m "<concise feature summary>"
    git push -u origin "$HEAD"
    # gh pr create does NOT support --json/-q; it prints the PR URL. Create it,
    # then read the number back with gh pr view.
    # Reference the issue by number for context, but DO NOT use closing keywords
    # (no "Closes/Fixes #N") — the ticket is a reusable example and must stay open.
    gh pr create --base "$BASE" --head "$HEAD" \
         --title "<feature title>" --body "<what/why, mentions issue #N for context, links the spec>"
    PR=$(gh pr view "$HEAD" --json number -q .number)
    echo "opened PR #$PR"
    ```

## Make CI green

The `CI` workflow runs `./gradlew build` on every push and PR. **Green CI is a
hard gate — do not touch the review until it passes.** Local green does not
guarantee CI green, so trust the pipeline, not your laptop.

11. **Watch the checks and fix red builds.** Block on the run, and if it fails,
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

12. **Wait for Copilot.** Poll every ~30s, up to ~10 minutes, for Copilot (bot
    login contains `copilot`) to **submit its review**. Query BOTH the reviews and
    the threads in one call — a Copilot review often lands with *zero* inline
    threads (it posts a summary review and nothing to act on), so polling only for
    an unresolved thread would spin the full 10 minutes and then falsely report
    "Copilot never reviewed." You need the thread node IDs to reply/resolve anyway:

    ```bash
    gh api graphql -f query='
      query($owner:String!,$repo:String!,$pr:Int!){
        repository(owner:$owner,name:$repo){ pullRequest(number:$pr){
          reviews(first:50){ nodes{ author{login} state } }
          reviewThreads(first:100){ nodes{
            id isResolved isOutdated
            comments(first:20){ nodes{ databaseId author{login} body path line } } } } } } }' \
      -f owner="${OWNER_REPO%/*}" -f repo="${OWNER_REPO#*/}" -F pr="$PR"
    ```

    Stop polling at the **first** of these three terminal states — never hang past
    the window:
    - **Copilot review present *and* it has unresolved Copilot threads** → go to
      step 13 and triage them.
    - **Copilot review present but no unresolved Copilot threads** (e.g. "reviewed
      N/N files and generated no comments") → this is a **clean pass**, not a
      timeout. Note it and skip to the wrap-up.
    - **No Copilot review at all after ~10 min** → say so and skip to the wrap-up.

13. **Triage each unresolved Copilot thread — one at a time.** Read the comment
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

14. **Punch out the fixes.** If step 13 produced any changes: `tester` re-verifies
    `./gradlew build` is green, then commit and push. **The push re-triggers CI —
    go back to step 11 and get it green again** before continuing. Pushing also
    triggers a fresh Copilot pass; loop back to step 12 to handle new threads, but
    **cap the review at 2 rounds total.** After the second round, reply to any
    remaining threads with your decision, resolve them, and move on. Never loop
    unbounded.

## Verify it actually runs

15. **Prove the feature works end-to-end against a running app** — green tests
    are necessary but not sufficient. Boot the app on **port 8081** (never 8080),
    wait until it's ready, then exercise the feature's endpoints from the spec's
    acceptance examples and capture the REAL responses. Best-effort: if it can't
    boot, say so and continue — do **not** fail the run over this.

    ```bash
    SERVER_PORT=8081 ./gradlew bootRun > /tmp/dugout-verify.log 2>&1 &
    APP=$!
    # poll readiness against one of the feature's endpoints (up to ~60s)
    for i in $(seq 1 30); do curl -sf localhost:8081/<an-endpoint> >/dev/null && break; sleep 2; done
    curl -s localhost:8081/<endpoint-from-acceptance-example>
    curl -s localhost:8081/<list-endpoint>
    kill "$APP" 2>/dev/null
    ```

    Post the captured JSON as a PR comment so a reviewer sees reality, not just a
    passing test — and call out any drift you notice between the live response and
    the spec/`docs/api.md`:
    `gh pr comment "$PR" --body "✅ Verified live on a running instance: …"`

## Wrap up

16. **Notify — ping the human.** Post the come-back-from-lunch summary as a PR
    comment that **@mentions the repo owner** (`@kylehoehns`) so it fires a mobile
    notification — the "review it from my phone" handoff. Include: the PR link,
    which issue it addresses, final **CI status**, Copilot comments addressed vs.
    declined (with reasons), the **live-verified snippet** from step 15, and the
    final commit sha.
    `gh pr comment "$PR" --body "@kylehoehns 🍱 Come-back-from-lunch report — …"`
