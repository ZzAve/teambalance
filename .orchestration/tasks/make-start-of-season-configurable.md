# Task: make startOfSeason configurable as per gh #334

## Intent

Make the season start date configurable per team via the database config table, so the backend can resolve `since` parameters against a tenant-specific season start instead of a hardcoded value. Expose a `PUT /api/config/season` admin endpoint so admins can update it without SQL access.

See issue: https://github.com/ZzAve/teambalance/issues/334
See plan: `.orchestration/plans/2026-04-11-start-of-season-configurable.md`
PR: #364 (feature/start-of-season), worktree: `.worktrees/start-of-season`

## Subtasks

- [x] Implement startOfSeason configurable per team — commit ac1d0b1
- [x] Fix Liquibase XML (preConditions at invalid position) — commit d634f39
- [ ] Address CodeRabbit PR comments on PR #364
- [ ] Validate CI for PR #364 and merge
- [x] Research admin screen UX for updating season start date (P2) — see `.orchestration/research/2026-04-12-admin-season-start-ux.md`

## Decisions Log

- 2026-04-11 (execute worker): Added `getStartOfSeason()` / `getStartOfSeasonZoned()` to `ConfigurationService`; refactored `DateTimeFilter` + 4 controllers + `BankService` to resolve season start per-tenant
- 2026-04-11 (execute worker): Added `SeasonConfigController` with `PUT /api/config/season`; Flyway migration seeds both tenants with `2025-08-01T00:00:00`
- 2026-04-12 (execute worker): Removed `<preConditions>` block from inside `<changeSet>` in Flyway XML (was at invalid position)
- 2026-04-12 (research worker): Researched frontend admin patterns; identified 3 UX options (add to Admin menu, standalone settings page, or Users page); documented MUI component patterns (MobileDateTimePicker + dayjs); needs user decision on integration point

## Current State

Backend implementation complete on `feature/start-of-season` (PR #364 open). CodeRabbit has 6 actionable comments to address before merge. Frontend admin UX research complete; awaiting user decision on 4 questions before proceeding to implementation plan:
1. Where to place season start date config (Admin menu vs standalone settings page)
2. Whether to add more team config options simultaneously
3. Dutch label text preference
4. API response format clarification

Next: user answers → plan → implement frontend