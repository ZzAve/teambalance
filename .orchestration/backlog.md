# Backlog

## Format Documentation

**Task line format:**
- Parent task: `- [ ] \`[P1]\` Task name` (priority only, no type tag)
- Subtask: `- [ ] \`[P1]\` \`[execute]\` Subtask name` (priority + type tag, 4-space indent)
- Standalone task (no subtasks): `- [ ] \`[P1]\` \`[execute]\` Task name` (flat, unchanged)

**Metadata sub-bullets (4-space indent, no checkbox):**
- `- Depends: <comma-separated task names or "none">`
- `- Task file: .orchestration/tasks/<slug>.md` (all tasks)

**Hierarchical tasks:**
- A parent task OWNS one or more subtasks, nested 4 spaces beneath it
- Subtasks inherit their parent's Intent via the task file
- The orchestrator auto-injects a final `[reflect]` subtask when it first processes a parent
- A parent is Done when ALL its subtasks are Done
- Standalone flat tasks still work — use them when no decomposition is needed

**WIP preference:**
- Prefer ≤2 active parents per round
- Prefer ≤4 subtasks dispatched per round
- Always prefer finishing an in-progress parent over starting a new one

**Task types:** `[research]`, `[plan]`, `[execute]`, `[test]`, `[ci]`, `[reflect]`

**Conventions:**
- `[P1]`/`[P2]`/`[P3]` = priority (P1 highest, must be completed first)
- `[review]` = triggers reviewer subagent after completion
- `Depends:` = task names this blocks on (must be in Done before this can start)
- `Context:` = concise info the worker needs (file paths, key decisions, constraints)

## Active

- [ ] `[P0]` `[execute]` E2e tests depend on startOfSeason — add GET endpoint and make e2e dynamic `[review]`
    - Depends: none
    - Task file: .orchestration/tasks/e2e-dynamic-start-of-season.md

- [ ] `[P1]` make startOfSeason configurable as per gh #334
    - Task file: .orchestration/tasks/make-start-of-season-configurable.md
    - [x] `[P2]` `[execute]` Implement startOfSeason configurable per team (2026-04-11)
        - Context: Plan at `.orchestration/plans/2026-04-11-start-of-season-configurable.md`. Commit: ac1d0b1 on feature/start-of-season. PR #364 created.
    - [x] `[P1]` `[execute]` Fix Liquibase XML in PR #364 (preConditions at invalid position) (2026-04-12)
        - Context: Removed `<preConditions>` block from inside `<changeSet>`. Commit d634f39 pushed to feature/start-of-season.
    - [x] `[P1]` `[execute]` Address CodeRabbit PR comments on PR #364 (2026-04-11)
        - Context: Commit 60d5f6b. Moved controller to api.settings, added @Admin, @JsonFormat, DateTimeParseException handler, Liquibase preConditions, removed MockK deps.
    - [x] `[P1]` `[ci]` Validate CI for PR #364 and merge (2026-04-11)
        - Context: All checks passed. Auto-merge armed.
    - [ ] `[P2]` `[research]` Research admin screen UX for updating season start date
        - Depends: none
        - Context: Find a good way to surface the season start config in the admin screen so admins can update it without SQL access. Research existing admin UI patterns in the frontend and propose integration point.
    - [ ] `[P2]` `[reflect]` Verify result matches intent

- [ ] `[P1]` `[execute]` Implement dedicated task file per backlog entry (2026-04-11)
    - Task file: .orchestration/tasks/implement-dedicated-task-file-per-backlog-entry.md
    - PR: #370 (feature/task-files)
    - [x] `[P1]` `[execute]` Implement (2026-04-11)
    - [x] `[P1]` `[ci]` Validate CI for PR #370 (feature/task-files) and merge (2026-04-11)
        - Context: All checks passed. Auto-merge armed.
    - [ ] `[P1]` `[execute]` Migrate existing backlog Active entries to dedicated task files
        - Depends: Implement dedicated task file per backlog entry
        - Task file: .orchestration/tasks/migrate-backlog-entries-to-task-files.md
        - Context: One-shot migration. For each Active/Parked entry lacking a `Task file:` bullet: compute slug, create `.orchestration/tasks/<slug>.md` (Intent = existing Context text), replace `Context:` with `Task file:` pointer, preserve `Depends:`. Done section: untouched. Commit atomically. Full spec at `.orchestration/plans/2026-04-11-task-files-everywhere.md` §7.
    - [ ] `[P2]` `[reflect]` Verify result matches intent

- [x] `[P1]` `[research]` Investigate e2e test flakiness — root causes and stabilization options (2026-04-11)
    - Context: Findings at `.orchestration/research/2026-04-11-e2e-flakiness.md`. Two root causes: (1) no auth-ready wait after `page.goto()` — app shows loading screen, tests timeout on "Admin dingen"; (2) `CI=true` not passed to Docker container so firefox+webkit run in CI (3× flake). Fixes: add `waitFor("Aanstaande trainingen")` in beforeEach, cherry-pick CI=true fix or merge ci-perf first, fix snackbar inconsistency in deleteTraining.

- [x] `[P1]` `[plan]` Plan e2e stabilization: auth wait + CI env fix + snackbar cleanup (2026-04-11)
    - Context: Plan at `.orchestration/plans/2026-04-11-e2e-stabilization-plan.md`. Covers: (1) waitFor("Aanstaande trainingen") in beforeEach for crud-matches, crud-training, login, previous-events; (2) CI=true deferred to PR #361; (3) snackbar filter fix in deleteTraining; (4) pickDateTime consolidated into utils.ts.

- [x] `[P1]` `[ci]` Validate CI for PR #369 (feature/e2e-stabilization) and merge (2026-04-11)
    - Merged as 10ec717. Fixed duplicate pickDateTime declaration before merge.

- [x] `[P1]` `[research]` Validate "Set match attendance" test status in PR #350 vs local (2026-04-11)
    - Context: Findings at `.orchestration/research/2026-04-11-attendance-test-status.md`. Test is ENABLED in PR #350 branch but FAILING in CI. Attendance button uses text labels (not icons).

- [x] `[P1]` `[ci]` Monitor and merge PR #362 (feature/kotlinx-datetime) (2026-04-11)
    - Context: Merged to master.

- [ ] `[P1]` `[execute]` Fix and validate "Set match attendance" test in PR #350 locally `[review]`
    - Depends: none
    - Task file: .orchestration/tasks/fix-and-validate-set-match-attendance-test-in-pr-350-locally.md

- [ ] `[P2]` `[execute]` Annotate endpoints/controllers with explicit roles `[review]`
    - Depends: none
    - Task file: .orchestration/tasks/annotate-endpointscontrollers-with-explicit-roles.md

- [ ] `[P2]` `[execute]` Improve BunqRepo class: API key handling, validation, performance `[review]`
    - Depends: none
    - Task file: .orchestration/tasks/improve-bunqrepo-class-api-key-handling-validation-performan.md



- [ ] `[P2]` `[plan]` Plan: one-time additional players for events
    - Depends: none
    - Task file: .orchestration/tasks/plan-one-time-additional-players-for-events.md

- [ ] `[P2]` `[execute]` Clean up stale React 18 worktree and upgrade React to v18 `[review]`
    - Depends: none
    - Task file: .orchestration/tasks/clean-up-stale-react-18-worktree-and-upgrade-react-to-v18.md

- [ ] `[P2]` `[execute]` Upgrade MUI from v5 to v6 `[review]`
    - Depends: none
    - Task file: .orchestration/tasks/upgrade-mui-from-v5-to-v6.md

- [ ] `[P3]` `[research]` Evaluate MUI v7 / Pigment CSS migration
    - Depends: Upgrade MUI from v5 to v6
    - Task file: .orchestration/tasks/evaluate-mui-v7-pigment-css-migration.md

- [ ] `[P3]` `[research]` Research Google Calendar integration options
    - Depends: none
    - Task file: .orchestration/tasks/research-google-calendar-integration-options.md

- [ ] `[P3]` `[research]` Research runtime type validation approach
    - Depends: none
    - Task file: .orchestration/tasks/research-runtime-type-validation-approach.md


- [ ] `[P3]` `[research]` Research native image support with Spring Boot
    - Depends: none
    - Task file: .orchestration/tasks/research-native-image-support-with-spring-boot.md

- [ ] `[P3]` `[execute]` Update README `[review]`
    - Depends: none
    - Task file: .orchestration/tasks/update-readme.md

- [ ] `[P2]` `[research]` Research e2e coverage gaps — identify most critical untested flow
    - Depends: none
    - Task file: .orchestration/tasks/research-e2e-coverage-gaps-identify-most-critical-untested-f.md

- [ ] `[P2]` `[research]` Reflect on attendance match e2e implementation — learnings for next cycle
    - Depends: Validate CI for PR #350 (feature/e2e-attendance)
    - Task file: .orchestration/tasks/reflect-on-attendance-match-e2e-implementation-learnings-for.md

- [ ] `[P2]` `[plan]` Plan next e2e test cycle based on reflection
    - Depends: Reflect on attendance match e2e implementation — learnings for next cycle
    - Task file: .orchestration/tasks/plan-next-e2e-test-cycle-based-on-reflection.md

- [ ] `[P2]` `[execute]` Implement next e2e test cycle `[review]`
    - Depends: user approving plan for next e2e test cycle
    - Task file: .orchestration/tasks/implement-next-e2e-test-cycle.md

## Parked

- [ ] `[P1]` `[ci]` Validate CI for PR #361 (feature/ci-perf) and merge
    - Blocked: DockerHub rate limit (429) on `node:22-alpine` for frontend image — transient, resets after ~6h
    - Task file: .orchestration/tasks/validate-ci-for-pr-361-featureci-perf-and-merge.md

- [ ] `[P2]` `[plan]` CI perf step 4: restructure gcp.yml (build-test-push + manual deploy)
    - Questions: [.orchestration/questions/ci-pipeline-step4.md](.orchestration/questions/ci-pipeline-step4.md)
    - Task file: .orchestration/tasks/ci-perf-step-4-restructure-gcpyml-build-test-push-manual-dep.md


## Done

- [x] `[P1]` `[ci]` Validate CI for PR #363 (feature/orchestrate-improvements) and merge (2026-04-12)
    - Context: All CI checks passed. PR #363 merged via squash.
- [x] `[P1]` `[execute]` Execute e2e stabilization: auth wait + snackbar fix + pickDateTime consolidation (2026-04-12)
    - Context: All 4 changes already applied (commit 7b2c0cc). PR #369 created. Reviewer dispatched.
- [x] `[P2]` `[ci]` Validate context-mode fix: node version pinned in ~/.claude/settings.json (2026-04-11)
    - Context: context-mode tools working (node v20.20.2, no ABI mismatch). Fix confirmed working — PATH may have been applied outside settings.json or system was already consistent.
- [x] `[P2]` `[research]` Research fix for context-mode Node.js version mismatch in subagents (2026-04-11)
    - Context: Root cause: better-sqlite3 ABI mismatch. Fix options documented in `.orchestration/research/2026-04-11-context-mode-node-mismatch.md`. User applied fix manually.
- [x] `[P2]` `[execute]` CI perf step 1: update e2e/Dockerfile to playwright base image (2026-04-11)
    - Context: Commit 5aedb2b on feature/ci-perf. Replaced node:20-bookworm with mcr.microsoft.com/playwright:v1.59.1-noble; removed browser install RUN.
- [x] `[P1]` `[execute]` Fix GHA cache scope conflict in PR #361 (build.yml + gcp.yml) (2026-04-11)
    - Context: Commit 1dbd37d on feature/ci-perf. Added scope=frontend and scope=e2e to all 4 cache entries in both workflows.
- [x] `[P2]` `[execute]` CI perf step 4b: switch Docker cache to GHA (2026-04-11)
    - Context: Commit df31ed0 on feature/ci-perf. Both build.yml and gcp.yml now use type=gha with actions: write permission.
- [x] `[P2]` `[execute]` CI perf step 3: add explicit CI=true to e2e step in build.yml (2026-04-11)
    - Context: Commit 510fdbd on feature/ci-perf. Added env: CI: true to E2E tests step in build.yml.
- [x] `[P2]` `[execute]` CI perf step 2: make --build conditional on CI env in Makefile (2026-04-11)
    - Context: Commit 5aedb2b on feature/ci-perf. Added E2E_BUILD_FLAG; e2e target uses it, run-local-backend unchanged.
- [x] `[P2]` `[ci]` Validate CI for PR #320 (node-22-upgrade) (2026-04-11)
    - Context: Merged via squash (commit 410295c5). E2e timeouts were infrastructure flakiness; passed on re-run.
- [x] `[P1]` `[execute]` Fix e2e test failures in PR #350 (attendance buttons timeout) (2026-04-11)
    - Context: Rewrote setMatchAttendance/verifyMatchAttendanceState (2-step UI flow, MUI color classes). Commit 48a358b pushed to feature/e2e-attendance. TEST_USER_NAME="admin" — verify against backend.
- [x] `[P2]` `[plan]` Plan CI pipeline performance improvements (2026-04-11)
    - Context: Plan at `.orchestration/plans/2026-04-11-ci-pipeline-plan.md`. 4 changes: e2e Dockerfile base image, Makefile --build conditional, build.yml minor, gcp.yml restructure into 2 jobs with manual deploy step.
- [x] `[P2]` `[research]` Research CI pipeline performance improvements (2026-04-11)
    - Context: Findings at `.orchestration/research/2026-04-11-ci-pipeline-performance.md`. Key issues: e2e Dockerfile installs all 3 browsers on cache miss (~700–900 MB), `node:20-bookworm` base is large, `--build` flag in `make e2e` bypasses pre-built images. 4 questions for user before planning.

- [x] `[P1]` `[ci]` Validate CI for PR #360 (feature/orchestrate-tightenings) (2026-04-11)
    - Context: Already merged to master (commit 44fc9f5). Worktree removed.
- [x] `[P1]` `[ci]` Validate CI for PR #355 (feature/claude-md) (2026-04-11)
    - Context: Already merged to master (commit f8351f3). Worktree removed.
- [x] `[P1]` `[execute]` Write CLAUDE.md for the project (2026-04-11)
    - Context: Created and enhanced CLAUDE.md. Commits: a3c6050 (initial), b82eda7 (4 accuracy fixes). PR pending on feature/claude-md. Reviewer: pass (pending second review).
- [x] `[P1]` `[test]` Rebase PR #350 on master (2026-04-11)
    - Context: Rebase on master done (commit 6fa7e7a). CI pending → new CI check task added.
- [x] `[P2]` `[plan]` Plan orchestrate.md tightenings following delegation rule fix (2026-04-11)
    - Context: Plan at `.orchestration/plans/2026-04-11-orchestrate-tightenings-plan.md`. 6 targeted edits. 3 questions pending user answers — execute task parked.
- [x] `[P1]` `[plan]` Plan orchestrate command improvements (2026-04-11)
    - Context: Plan at `.orchestration/plans/2026-04-11-orchestrate-improvements-plan.md`.
- [x] `[P1]` `[execute]` Implement orchestrate command improvements (2026-04-11)
    - Context: Applied 11 changes to `.claude/commands/orchestrate.md`.
- [x] `[P2]` `[research]` Reflect on orchestrate execution: why orchestrator executed task directly (2026-04-11)
    - Context: Findings at `.orchestration/research/2026-04-11-orchestrate-direct-execution-reflection.md`.
- [x] `[P1]` `[execute]` Fix JOOQ + Kotlin value class issue — Done (2026-04-11)
    - Context: User completed fix on branch `fix/jooq-serialisation-mapping-issues`, PR #353 merged. PR #347 closed as superseded.
- [x] `[P1]` `[execute]` Rebase PR #352 on master and validate CI (2026-04-11)
    - Context: Rebased on master, CI passed, merged via squash.
- [x] `[P1]` `[test]` Validate CI for PR #351 (fix/ci-container-logs-debug) (2026-04-11)
    - Context: PR closed by author (unmerged). CI failed on API tests unrelated to build.yml changes.
- [x] `[P1]` `[test]` Validate CI for PR #338 (fix/e2e-test-timeouts) (2026-04-11)
    - Context: Merged manually by user.
- [x] `[P1]` `[test]` Validate and merge PR #336 (dependabot/lodash bump) (2026-04-11)
    - Context: Rebased on master (post-JOOQ fix), merged. Commit 39ee607.
- [x] `[P1]` `[test]` Validate CI for PR #328 (renovate/postgres-16.x) (2026-04-11)
    - Context: CI auto-merge enabled. Self-merged when build passed.

## Done (Archive)

See [archive](./backlog-archive.md) for a list of completed tasks.
