---
name: Fix and validate "Set match attendance" test in PR #350 locally
description: PR #350 (feature/e2e-attendance) — Do NOT merge yet. The "Set match attend...
type: execute
---

# Task: Fix and validate "Set match attendance" test in PR #350 locally

**Status:** Active
**Priority:** P1
**Type:** [execute]
**Created:** 2026-04-12
**Depends:** none

## Intent

PR #350 (feature/e2e-attendance) — Do NOT merge yet. The "Set match attendance" test is enabled but failing in CI. Attendance button uses TEXT labels (confirmed). Task: (1) Check out feature/e2e-attendance branch, (2) Look at actual frontend attendance button implementation to identify exact text labels used, (3) Fix the test selector in crud-matches.spec.ts to use correct text, (4) Run `make e2e` locally to verify the test passes, (5) Commit fix. Also apply the e2e stabilization fixes (auth wait + snackbar) from the plan at `.orchestration/plans/2026-04-11-e2e-stabilization-plan.md` to this branch too, since PR #361 (CI=true) is now merged into master.

## Subtasks

## Decisions Log

## Current State

## Reflection
