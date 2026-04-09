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

_(Completed tasks with completion date)_
