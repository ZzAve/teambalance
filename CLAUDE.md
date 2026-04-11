# TeamBalance

Sports team management app for Tovo Utrecht volleyball club. Tracks event attendance and manages a shared money pool (Bunq integration). Multitenant: one team per subdomain (e.g. `4.teambalance.local`).

## Tech Stack

- **Backend**: Kotlin 2.x, Spring Boot 3.3.x, Spring Data JPA + JOOQ, Flyway, PostgreSQL, Java 21
- **Frontend**: React + TypeScript, Vite, MUI v5 (Material UI), React Router
- **API contracts**: Wirespec (generates Kotlin + TypeScript types)
- **Build**: Maven (`./mvnw`), multi-module project
- **Infra**: Docker Compose (local), Google Cloud Run (prod), GCP Container Registry

## Key Commands

| Command | What it does |
|---------|-------------|
| `make build` | Full build with format: `./mvnw install -Pformat` |
| `make yolo` | Fast compile, skips tests + lint: `./mvnw install -T0.5C -DskipTests ...` |
| `make format` | Format code: `./mvnw process-sources -Pformat` |
| `make ci` | Full CI build including SonarCloud analysis |
| `make e2e` | Run Playwright e2e tests via Docker Compose |
| `make db` | Start PostgreSQL only |
| `make run-local` | Start backend + frontend via Docker Compose |
| `cleanlogs` | Clean build log files |

## Key Paths

- `backend/src/main/kotlin/nl/jvandis/teambalance/` — backend source root
- `backend/src/main/kotlin/nl/jvandis/teambalance/api/` — REST controllers & services (events, bank, attendees, users, auth)
- `frontend/src/main/react/src/` — React SPA source
- `e2e/src/playwright/` — Playwright e2e tests
- `test-data/` — Spring Boot app that seeds test data against a running backend

## Architecture

**Backend** — layered (not strict hexagonal in current code, but direction is hexagonal DDD):
- `api/` — REST controllers, services, repositories per domain (event, bank, attendees, users)
- Multitenant via schema-per-team; tenant resolved from subdomain

**Frontend** — React SPA served from `app.teambalance.app`:
- MUI v5 components; migrating toward MUI v6
- Feature-Sliced Design (FSD) direction for new code

**Domain routing**:
- `teambalance.app` → landing page
- `app.teambalance.app` → React SPA
- `api.teambalance.app` → REST API

**Local dev**: access via `4.teambalance.local` or `5.teambalance.local` (add to `/etc/hosts`).

## Coding Standards
- **Spotless**: code formatting — run `make format` before committing
- **SonarCloud**: static analysis runs on every PR

## Do-Nots

- Do not commit directly to `master`; use PRs
- Do not skip tests with `-DskipTests` in CI
- Do not hardcode tenant/team IDs; resolve from context

## MCP Tools: code-review-graph

**IMPORTANT: This project has a knowledge graph. ALWAYS use the
code-review-graph MCP tools BEFORE using Grep/Glob/Read to explore
the codebase.** The graph is faster, cheaper (fewer tokens), and gives
you structural context (callers, dependents, test coverage) that file
scanning cannot.

### When to use graph tools FIRST

- **Exploring code**: `semantic_search_nodes` or `query_graph` instead of Grep
- **Understanding impact**: `get_impact_radius` instead of manually tracing imports
- **Code review**: `detect_changes` + `get_review_context` instead of reading entire files
- **Finding relationships**: `query_graph` with callers_of/callees_of/imports_of/tests_for
- **Architecture questions**: `get_architecture_overview` + `list_communities`

Fall back to Grep/Glob/Read **only** when the graph doesn't cover what you need.

### Key Tools

| Tool | Use when |
|------|----------|
| `detect_changes` | Reviewing code changes — gives risk-scored analysis |
| `get_review_context` | Need source snippets for review — token-efficient |
| `get_impact_radius` | Understanding blast radius of a change |
| `get_affected_flows` | Finding which execution paths are impacted |
| `query_graph` | Tracing callers, callees, imports, tests, dependencies |
| `semantic_search_nodes` | Finding functions/classes by name or keyword |
| `get_architecture_overview` | Understanding high-level codebase structure |
| `refactor_tool` | Planning renames, finding dead code |

### Workflow

1. The graph auto-updates on file changes (via hooks).
2. Use `detect_changes` for code review.
3. Use `get_affected_flows` to understand impact.
4. Use `query_graph` pattern="tests_for" to check coverage.
