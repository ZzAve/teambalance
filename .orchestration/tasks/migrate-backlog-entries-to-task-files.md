---
name: Migrate existing backlog Active entries to dedicated task files
description: One-shot migration to add Task file pointers to all Active/Parked backlog entries that lack them
type: execute
---

# Task: Migrate existing backlog Active entries to dedicated task files

**Status:** In Progress
**Priority:** P1
**Created:** 2026-04-12
**Parent backlog entry:** Implement dedicated task file per backlog entry

## Intent

One-shot migration. For each Active/Parked entry lacking a `Task file:` bullet: compute slug (kebab-case of task name), create `.orchestration/tasks/<slug>.md` (Intent = existing Context text), replace `Context:` bullet with `Task file:` pointer, preserve `Depends:`. Done section: untouched. Commit atomically. Full spec at `.orchestration/plans/2026-04-11-task-files-everywhere.md` §7.

## Subtasks

- [x] Migrate existing backlog Active entries to dedicated task files
- [ ] Verify result matches intent

## Decisions Log

- 2026-04-12 (execute worker): Created 17 task files for Active/Parked entries that lacked Task file pointers. Replaced Context bullets with Task file references in backlog.md. Files created: fix-and-validate-set-match-attendance-test-in-pr-350-locally, annotate-endpointscontrollers-with-explicit-roles, improve-bunqrepo-class-api-key-handling-validation-performan, plan-one-time-additional-players-for-events, clean-up-stale-react-18-worktree-and-upgrade-react-to-v18, upgrade-mui-from-v5-to-v6, evaluate-mui-v7-pigment-css-migration, research-google-calendar-integration-options, research-runtime-type-validation-approach, research-native-image-support-with-spring-boot, update-readme, research-e2e-coverage-gaps-identify-most-critical-untested-f, reflect-on-attendance-match-e2e-implementation-learnings-for, plan-next-e2e-test-cycle-based-on-reflection, implement-next-e2e-test-cycle, validate-ci-for-pr-361-featureci-perf-and-merge, ci-perf-step-4-restructure-gcpyml-build-test-push-manual-dep.

## Current State

Migration complete. All Active and Parked entries now have Task file pointers. Backlog inline Context bullets removed. Changes staged and ready for commit.

## Reflection
