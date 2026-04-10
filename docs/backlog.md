# Backlog

## Format Documentation

**Conventions:**
- `[P1]`/`[P2]`/`[P3]` = priority (P1 highest, must be completed first)
- `[research]`/`[plan]`/`[execute]`/`[test]` = task type (determines worker behavior)
- `[review]` = triggers reviewer subagent after completion
- `Depends:` = task names this blocks on (must be in Done before this can start)
- `Context:` = concise info the worker needs (file paths, key decisions, constraints)

**Task Types:**
- `[research]` → explore codebase/docs, write findings to markdown, no code changes
- `[plan]` → design approach, write plan to docs/plans/, no code changes
- `[execute]` → implement code, write tests, commit to feature branch
- `[test]` → write/run tests for existing code, report coverage

**Rules:**
- Subtasks become new top-level items with dependencies, never nested
- One line per task
- Keep Context field concise (≤2 sentences)

## Active

_(Add tasks here in priority order)_

## Parked

_(Tasks with unanswered questions — see docs/questions/README.md)_

## Done

- [x] `[P1]` `[execute]` Get all open Renovate PRs reviewed and merged (2026-04-09)
  - Depends: none
  - Context: Review and merge all pending Renovate dependency update PRs. Use review-renovate skill if available.
- [x] `[P2]` `[execute]` Set up Renovate auto-merge for minor and patch updates (2026-04-09)
  - Depends: none
  - Context: Configure renovate.json to auto-merge minor/patch updates. Requires automergeType and automergeStrategy settings.
- [x] `[P1]` `[plan]` get back to automerge. Those changes are currently not checked in. Using a worktree, commit those changes (2026-04-09)
  - Depends: none
  - Context: See: Set up Renovate auto-merge for minor and patch updates
