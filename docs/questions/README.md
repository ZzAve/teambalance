# Open Questions

This index tracks all tasks that are parked waiting for user input.

## How It Works

1. When a worker encounters unclear requirements, the orchestrator:
   - Creates a question file: `docs/questions/<task-slug>.md`
   - Adds an entry to this index
   - Moves the task to **Parked** in `docs/backlog.md`

2. User reviews and answers questions in the individual files

3. Orchestrator checks this index at the start of each round:
   - If a question file has answers → move task back to **Active**
   - Task can now be dispatched to a worker

## Status Legend

- **Unanswered**: Waiting for user input
- **Answered**: Ready to unpark (orchestrator will process on next round)
- **Archived**: Historical questions (moved to `archive/` folder)

## Questions

| Task | Status | File |
|------|--------|------|
| _(empty)_ | | |

## Archive

Answered questions can be moved to `docs/questions/archive/` periodically to keep this index clean.
