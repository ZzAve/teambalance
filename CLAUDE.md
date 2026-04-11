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
| `make test` | Seed test data against running backend (requires `make run-local-backend` first) |
| `make ci` | Full CI build including SonarCloud analysis |
| `make e2e` | Run Playwright e2e tests via Docker Compose |
| `make db` | Start PostgreSQL only |
| `make run-local` | Start backend + frontend via Docker Compose |
| `make run-local-backend` | Start backend only |
| `make run-local-frontend` | Start frontend only |
| `make clean` | Clean build artifacts and stop Docker containers |

## Key Paths

- `backend/src/main/kotlin/nl/jvandis/teambalance/` — backend source root
- `backend/src/main/kotlin/nl/jvandis/teambalance/api/` — REST controllers, services, repositories per domain
- `backend/src/test/kotlin/` — Unit & integration tests
- `frontend/src/main/react/src/` — React SPA source root
- `e2e/src/playwright/` — Playwright e2e tests (UI automation)
- `test-data/` — Spring Boot CLI tool that seeds test data; run with `make test`
- `jooq-support/` — Maven module for JOOQ code generation

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

## Testing

- **Backend**: JUnit 5 + MockK (unit tests in `src/test/kotlin/`)
- **Frontend**: Jest + React Testing Library (tests co-located with components)
- **E2E**: Playwright (orchestrated via Docker Compose in `e2e/`)
- **Test data**: Use `make test` to populate a local backend with sample events, users, and transactions

## Coding Standards

- **Spotless**: code formatting — run `make format` before committing
- **SonarCloud**: static analysis runs on every PR
- **Backend structure**: Layered domain packages under `api/` (event, bank, attendees, users, auth)
- **Frontend structure**: Feature-Sliced Design (FSD) for new code; avoid monolithic files

## Do-Nots

- Do not commit directly to `master`; use PRs
- Do not skip tests in CI builds
- Do not hardcode tenant/team IDs; always resolve from request context (subdomain/header)
- Do not modify SQL schemas directly; use Flyway migrations
- Do not share sensitive config (API keys, Bunq tokens) in code; use environment variables

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

## Quick Start (Local Development)

1. Clone the repo and `cd` to project root
2. `make run-local` — starts PostgreSQL, backend (port 8080), and frontend (port 5173) via Docker
3. Add to `/etc/hosts`: `127.0.0.1 4.teambalance.local app.teambalance.local api.teambalance.local`
4. Open `http://4.teambalance.local:5173` in your browser
5. Default credentials: username=`admin`, password=`admin` (set up via `make test`)
6. Run `make format` before committing code

When creating a worktree, be sure to run a `cd frontend && npm i` to bootsrap pre-commit hooks

## Troubleshooting

- **Port already in use**: Kill Docker containers with `make clean` first
- **Frontend not hot-reloading**: Ensure `make run-local-frontend` is watching for changes
- **Database migration failed**: Check Flyway SQL files in `backend/src/main/resources/db/migration/`
- **Build hangs on JOOQ generation**: This is normal; it can take 1–2 minutes for large schemas
