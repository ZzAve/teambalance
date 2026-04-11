# Task: Implement dedicated task file per backlog entry

## Intent

Extend the orchestrate task-file model so that ALL tasks (not just parent tasks) have a dedicated `.orchestration/tasks/<slug>.md` file. Remove inline `Context:` from backlog rows and replace with `Task file:` pointers. This makes task intent durable, traceable, and readable across worker handoffs.

See plan: `.orchestration/plans/2026-04-11-task-files-everywhere.md`

## Subtasks

- [x] Update `orchestrate-worker.md`: replace DEPENDENCIES/CONTEXT/PARENT_TASK_FILE with TASK_FILE; generalize "Working with Parent Context" to "Working with Task File"; [research]/[plan] now create follow-up task files
- [x] Update `orchestrate.md`: worker contract, invocation template, slug rules, tolerance fallback, reviewer template
- [x] Update `.orchestration/backlog.md` format header: remove `Context:` row, update task-file note to "all tasks"
- [x] Create this task file (`.orchestration/tasks/implement-dedicated-task-file-per-backlog-entry.md`)
- [ ] Bootstrap husky + commit + push + PR

## Decisions Log

- 2026-04-11 (execute worker): Replaced DEPENDENCIES/CONTEXT/PARENT_TASK_FILE with single TASK_FILE parameter across both orchestrate command files
- 2026-04-11 (execute worker): Added slug generation rules: strip type/priority tags, lowercase kebab-case, max 60 chars, -2/-3 suffix on collision
- 2026-04-11 (execute worker): Added tolerance fallback for unmigrated rows (no task file → derive intent from TASK_NAME)
- 2026-04-11 (orchestrator): Backlog format header updated; task file created for this task itself

## Current State

Changes to `orchestrate.md` and `orchestrate-worker.md` are committed in `.worktrees/task-files` (feature/task-files branch). Backlog format header updated. Task file created. Remaining: bootstrap npm in worktree, commit, push, create PR.
