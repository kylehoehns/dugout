# dugout

Companion repo for the talk **"From Sidebar to Sub-Agents: A Live, Hands-On Intro to Building with AI Agents."**

**Slides:** [www.kylehoehns.com/talks/sidebar-to-subagents](https://www.kylehoehns.com/talks/sidebar-to-subagents)

This repo is a **game with levels**. It starts as a plain Spring Boot project with
**zero** AI involvement, and each level adds one new way of working with an AI
coding agent — from asking questions in a sidebar, to letting the agent edit and
test your code, to a whole team of AI sub-agents shipping a feature through a real
pull request.

**Every level is its own branch you can open and run.** So you don't just read
about each step — you can check it out, run it, and poke at it yourself.

The domain is baseball — `Player`s and their stats — because it's easy to read at
a glance. You don't need to know baseball (or Java) to follow along.

## New here? Start here

1. **You're on `main` right now** — that's Level 0: the plain starting point, no AI yet.
2. **The levels below are links.** Click one to browse that branch on GitHub and see
   exactly what got added.
3. Want to run a level on your own machine? Clone the repo and
   `git checkout stage-3` (or any level), then [run it](#run-it).

> On GitHub you can also use the **branch dropdown** near the top-left of the file
> list to jump between levels — every `stage-N` branch is one of these levels.

## The levels

`main` is the blank starting point. Each `stage-N` branch builds on the one before
it, so you can walk them in order and watch the project level up one step at a time.

| Level | Branch | What you'll find there |
|-------|--------|------------------------|
| **0** | [`main`](https://github.com/kylehoehns/dugout/tree/main) | The plain starting line — a bare Spring Boot app with one tiny `GET /api/players`. **No AI involved.** |
| **1** | [`stage-1`](https://github.com/kylehoehns/dugout/tree/stage-1) | **AI as a smarter search box** — you ask questions in the sidebar and paste snippets in yourself. The AI doesn't touch your files. |
| **2** | [`stage-2`](https://github.com/kylehoehns/dugout/tree/stage-2) | **Assisted editing** — you describe what you want in plain English and the AI edits your files for you. |
| **3** | [`stage-3`](https://github.com/kylehoehns/dugout/tree/stage-3) | **Agent mode** — the AI adds a database, runs the build, and fixes its own mistakes. Adds `AGENTS.md` so the AI knows the project's house rules. |
| **4** | [`stage-4`](https://github.com/kylehoehns/dugout/tree/stage-4) | **Your first custom skill** — a reusable instruction (`writing-tests`) that kicks in automatically whenever tests get written. |
| **5** | [`stage-5`](https://github.com/kylehoehns/dugout/tree/stage-5) | **Quality gates** — test coverage, auto-formatting, and CI, so the AI's work is held to a real standard. |
| **6** | [`stage-6`](https://github.com/kylehoehns/dugout/tree/stage-6) | **A team of sub-agents** — one skill directs a developer, a tester, reviewers, and a doc-writer to build a whole feature together. |
| **7** | [`stage-7-start`](https://github.com/kylehoehns/dugout/tree/stage-7-start) | **The team ships like a real dev** — from a written spec, the agents open a pull request, wait for CI, read the code-review feedback, verify the running app, and ping your phone when it's done. *Built live from this branch during the talk.* |
| **8** | [`stage-8-start`](https://github.com/kylehoehns/dugout/tree/stage-8-start) | **Start from a GitHub issue** — the AI grills the issue into a sharp spec (writing decision records and a glossary as it goes), then the team ships it. *Built live from this branch during the talk.* |

Levels 0–6 are finished branches you can browse and run. Levels 7 and 8 are the
starting points I build from live on stage — check them out to see the setup, then
watch the AI take it the rest of the way.

```bash
git checkout stage-3   # jump to any level on your own machine
```

## Run it

```bash
./gradlew bootRun
# then, in another terminal:
curl http://localhost:8080/api/players
```

Requires **Java 25**. Build and test with Gradle (`./gradlew build`) — never Maven.

## Tech

Java 25 · Spring Boot 4.1 · Gradle · H2 (added live at Level 3) · no Lombok.

---

Built live with [Claude Code](https://claude.com/claude-code).
