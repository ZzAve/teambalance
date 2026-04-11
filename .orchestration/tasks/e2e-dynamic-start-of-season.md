# Task: E2e tests depend on startOfSeason — add GET endpoint and make e2e dynamic

## Intent

E2e tests that verify season-based behaviour (e.g. "Aanstaande trainingen", previous events) depend on the `startOfSeason` config value. Currently this is either hardcoded or seeded at a fixed date, making tests brittle when the value changes.

Two-part fix:
1. **Backend**: Add a GET endpoint to expose `startOfSeason` (e.g. `GET /api/config/season`) so the frontend and e2e tests can read the current value at runtime.
2. **E2e**: Update affected Playwright tests to fetch `startOfSeason` dynamically via the API before making date-based assertions, rather than using a hardcoded constant.

Source: https://github.com/ZzAve/teambalance/pull/364#issuecomment-4230135280

Related to  [make-start-of-season-configurable.md](make-start-of-season-configurable.md)

## Decisions Log

- 2026-04-12 (orchestrator): Task created at P0 per user request. Depends on PR #364 being merged (GET endpoint may already be part of that PR — check first before adding).

## Current State

Task created. Not yet started. Check if GET /api/config/season already exists in PR #364 before adding it.
