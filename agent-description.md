# TeamBalance Repository Overview

## What It Does

**TeamBalance** is a full-stack web application designed for sports teams (specifically volleyball clubs) that combines two key features:

1. **Event Management & Attendance Tracking**: Create and manage team events (trainings, matches, social events) with player availability tracking and admin controls

2. **Shared Money Pool**: Integrates with the Bunq banking API to manage a shared team account for group expenses (beer, food, etc.), displaying real-time balances, transaction history, and contributor statistics

The application serves multiple volleyball teams (Tovo Utrecht Heren 4 & 5) and solves the problem of having both event planning and cashless expense sharing in a single platform.

---

## Architectural Setup

### Structure
**Maven multi-module monorepo** with:
- `app`: Combined fullstack application
- `backend`: Spring Boot Kotlin REST API
- `frontend`: React + TypeScript SPA
- `jooq-support`: Custom JOOQ integration library
- `test-data`: Test data seeding utility
- `e2e`: Playwright end-to-end tests

### Backend Stack
- **Spring Boot 3.3.1** with **Kotlin 2.1.20** + coroutines
- **JOOQ 3.20.8** for type-safe SQL (instead of JPA/Hibernate)
- **PostgreSQL** with **Liquibase** migrations
- **Jetty** web server (replaces Tomcat)
- **Caffeine** caching with coroutine support
- **Bunq SDK** for banking integration
- **TestContainers** for integration testing

### Frontend Stack
- **React 17** with **TypeScript 4.9**
- **Vite** build tool
- **Material-UI (MUI) 5** with emotion styling
- **React Router 6** for navigation
- Custom hooks with localStorage persistence

### Infrastructure
- **Docker + Docker Compose** for local development
- **Google Cloud Run** for serverless deployment
- **GitHub Actions** CI/CD pipeline
- **Google Cloud Registry** for container images
- **PostgreSQL** with schema-based multitenancy

---

## Technical Highlights

### 1. **Schema-Based Multitenancy**
Elegant multi-tenant architecture using separate PostgreSQL schemas per team (`tovo_heren_4`, `tovo_heren_5`) with:
- `MultiTenantContext` using ThreadLocal for request-scoped tenant tracking
- `MultiTenantFilter` for domain-based tenant resolution
- MDC logging integration for automatic tenant context

### 2. **JOOQ Over JPA**
Custom `jooq-support` library providing Liquibase integration for schema versioning while leveraging JOOQ's type-safe DSL for explicit SQL control and code generation from database schema.

### 3. **Coroutine-Based Caching**
Modern async caching using `caffeine-coroutines` with `CoroutineLoadingCache` for suspend functions, implementing configurable TTL caching for expensive operations like balance/transaction retrieval.

### 4. **Sophisticated Bunq Integration** (backend/src/main/kotlin/nl/jvandis/teambalance/api/bank/)
`BunqRepo` wrapping the Bunq SDK with:
- Lazy initialization + mutex-based synchronization
- Session refresh mechanisms
- Transaction exclusion filtering (by ID, date, description, counterparty)
- Alias enrichment mapping counterparties to team members

### 5. **Reactive Warmup Pattern**
Application startup pre-loads caches for all tenants to ensure responsive first requests.

### 6. **Clean Domain Model**
Event hierarchy (Trainings, Matches, Miscellaneous) with dedicated controllers, services, and JOOQ repositories. Shared `Attendee` model tracking availability and player roles.

### 7. **Production-Ready DevOps**
- Makefile with helpful targets (`build`, `test`, `e2e`, `deploy`, `yolo`)
- KTLint for Kotlin formatting
- Husky + lint-staged for pre-commit hooks
- SonarCloud integration
- Jib Maven plugin for containerization without Docker daemon

### 8. **Full-Stack Type Safety**
TypeScript frontend with API client abstractions + JOOQ backend providing end-to-end type safety from database to UI.

The project showcases production-grade Kotlin development with Spring Boot, demonstrating advanced patterns like coroutine-based async operations, custom library development, external API integration, and cloud-native deployment—all wrapped in a clean, well-tested architecture.