# dugout

Companion repo for the talk **"From Sidebar to Sub-Agents: A Live, Hands-On Intro to Building with AI Agents."**

It starts as a blank Spring Boot project with **zero** AI involvement and climbs
the AI-assisted-development ladder one stage at a time — from asking questions in
a sidebar, to letting an agent edit and test code, to a custom skill, to quality
gates, to a team of sub-agents building a feature. **Every stage is a real,
runnable branch**, so you can fork this and walk the whole climb yourself.

The domain is baseball — `Player`s, and eventually a lineup — because it's easy
to read at a glance.

## Run it

```bash
./gradlew bootRun
# then, in another terminal:
curl http://localhost:8080/api/players
```

Requires **Java 25**. Build and test with Gradle (`./gradlew build`) — never Maven.

## The climb (one branch per stage)

`main` is the blank baseline. Each `stage-N` branch is a clean superset of the
one before, so `git log` itself tells the story.

| Branch | What it demonstrates |
|--------|----------------------|
| `main` / `stage-0` | Blank starter + one trivial `GET /api/players` |
| `stage-1` | AI as a smarter search box — ask in the sidebar, paste a snippet |
| `stage-2` | Assisted editing — describe intent, the AI edits your files |
| `stage-3` | Agent mode — it adds persistence, runs the build, fixes itself; plus `AGENTS.md` project context |
| `stage-4` | A custom skill that auto-triggers (the house testing conventions) |
| `stage-5` | Quality gates — coverage, formatting, CI |
| `stage-6` | A skill orchestrating a team of sub-agents to build a feature |

```bash
git checkout stage-3   # jump to any rung
```

## Tech

Java 25 · Spring Boot 4.1 · Gradle · H2 (added live at stage 3) · no Lombok.

---

Built live with [Claude Code](https://claude.com/claude-code).
