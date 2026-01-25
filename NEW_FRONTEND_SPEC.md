# Frontend Revamp Specification: Teambalance

## 1. Overview & Goals

This document outlines the specification for a complete revamp of the Teambalance frontend. The primary goal is to deliver a brand new, modern, and intuitive user interface built with contemporary web technologies. The application's core focus will remain on **event registration/management** and **money pool tracking**.

### Key Objectives:
- Modernize the user experience and visual design.
- Improve responsiveness and accessibility.
- Establish a scalable and maintainable frontend architecture.
- Consolidate the application to a single domain for simplified deployment and access.

## 2. Technology Stack

The following technologies will be used for the frontend revamp:
-   **Build Tool:** Vite
-   **Framework:** React (latest version)
-   **Styling:** Tailwind CSS
-   **UI Component Library:** Shadcn UI (built on Radix UI and Tailwind CSS)
-   **Language:** TypeScript
-   **State Management:** (To be determined, likely React Context or Zustand for global state, local state with `useState`/`useReducer`)
-   **Routing:** React Router DOM (latest version)
-   **API Client:** `fetch` API or `axios` for HTTP requests

## 3. Design & UI/UX Principles

The new frontend will adhere to the following design and UI/UX principles:
-   **Mobile-First Responsive Design:** The layout will be designed for mobile first, then scaled up for tablet and desktop. Navigation and interactions will be optimized for touch devices.
-   **Modern Aesthetic:** Clean, minimalist, and visually appealing design.
-   **Intuitive Navigation:** Clear and straightforward user flows based on a primary tab bar.
-   **Consistency:** Utilize Shadcn UI components for a consistent look and feel, complemented by Tailwind CSS for custom styling.
-   **Accessibility:** Ensure all interactive elements are accessible as per WCAG guidelines, leveraging Shadcn's foundation in Radix UI.

## 3.5. Layout & Navigation

The application will use a responsive primary navigation system.

-   **On Mobile Screens (e.g., < 768px):** A **bottom tab bar** will be displayed for primary navigation. It will be persistently visible.
-   **On Desktop Screens (e.g., >= 768px):** The navigation will transform into a **persistent sidebar** on the left.

This primary navigation will feature three main destinations:

1.  **Events:** The main hub for all event-related activities.
2.  **Money Pool:** For all financial tracking.
3.  **Team / Profile:** A menu linking to user-specific and team-management pages.

## 4. Core Features & Pages

The application's features are organized around the three primary navigation tabs.

### 4.1. Events Page (Default View)

-   **Description:** The primary landing page of the application. It provides a comprehensive, filterable view of all team events.
-   **Content & Features:**
    -   A **"Filter" button** at the top of the list. Tapping it will open a panel with filtering options:
        -   **Date Range:** "Upcoming" (default) or "Past".
        -   **Event Type:** Checkboxes for `Trainings`, `Matches`, and `Misc Events`.
    -   A scrollable list of **Event Cards**. Each card serves as a summary and shows:
        -   Event Title & Type (e.g., with an icon).
        -   Date, Time, and Location.
        -   **User's Attendance Status:** A clear visual indicator for "Attending", "Maybe", or "Absent".
        -   **Role-Based Attendee Summary:** A dynamic summary of who is attending, based on custom team roles (e.g., "Attending: 12 (1 Trainer, 8 Setters, 3 Mids)").
-   **Event Details Screen (on card tap):**
    -   **Attendance Actions:** Prominent buttons for the user to select **[ Attending ]**, **[ Maybe ]**, or **[ Absent ]**.
    -   **Details Section:** Full event description, location, etc.
    -   **Attendees Section:** Three tabs: "Attending", "Maybe", and "Absent". Each tab lists the relevant users and the "Attending" tab includes a summary by custom role.
    -   **Admin Actions:** "Edit" and "Delete" buttons visible only to ADMINs.

### 4.2. Money Pool Page

-   **Description:** Consolidates all financial information for the team's money pool.
-   **Content:**
    -   A prominent display of the current total balance.
    -   A list of recent transactions (date, description, amount).
    -   A button to "Top Up" the balance.
    -   A filter/search functionality for the transaction list.
-   **Components:** `Card` for balance, `Table` or `List` for transactions, `Button`, `Dialog` for top-up flow.

### 4.3. Team & Profile Page

-   **Description:** This page acts as a menu, providing navigation to secondary settings and management pages.
-   **Content:** A list of navigation links:
    -   **Profile:** Allows users to view/edit their personal information and **select their custom team role** (e.g., 'Setter', 'Mid') from a list defined by the admin.
    -   **Competition:** Navigates to a page displaying linked competition information.
    -   **Users:** (ADMIN only) Navigates to the user management page. Here, an admin can see a list of all members, invite new ones, remove members, and **assign/override the custom team role for any user**.
    -   **Admin:** (ADMIN only) Navigates to the central admin page. This now includes:
        -   **Custom Role Management:** An interface for ADMINs to **Create, Rename, and Delete** the roles available for the team (e.g., 'Trainer', 'Setter', 'Mid').
        -   Team-wide settings and Bunq API connection setup.
    -   **Switch Teams:** Opens an interface for users to switch their active tenant context.
    -   **Logout:** Logs the user out.

## 5. Multitenancy Implementation

-   **Mechanism:** All API requests from the frontend will include an `X-Tenant-Id` header.
-   **Handling:** The `X-Tenant-Id` will be dynamically retrieved (e.g., from user context or selected team) and automatically added to all outgoing HTTP requests via an API client interceptor (e.g., `axios` interceptor or custom `fetch` wrapper).

## 6. User Roles and Permissions

The application will support two primary roles within a team: `USER` and `ADMIN`. The UI will dynamically adapt based on the logged-in user's role.

-   **USER Role:**
    -   View Overview, Events, Transactions, Competition pages.
    -   Register/Deregister for events.
    -   Top-up money pool.
-   **ADMIN Role:**
    -   All `USER` permissions.
    -   Full CRUD (Create, Read, Update, Delete) for all events.
    -   Manage team members (invite, remove, change roles).
    -   Link competition information.
    -   Set up and configure Bunq API connection.
    -   Manage bank account aliases.

## 7. Component Library (Shadcn UI)

The following Shadcn UI components (or similar from Radix UI base) are anticipated to be extensively used:
-   `Button`
-   `Input`, `Textarea`
-   `Form` (for complex forms)
-   `Dialog` (for modals, confirmations, forms)
-   `Table` (for displaying lists of events, users, transactions)
-   `Card` (for dashboards, summary views)
-   `DatePicker` (for event dates)
-   `Select` (for dropdowns, e.g., roles, filters)
-   `Checkbox`, `Radio Group`
-   `Alert` / `Toast` (for notifications)
-   `Spinner` / `Skeleton` (for loading states)

## 8. Project Structure (Proposed)

A modular and scalable project structure will be adopted:

```
src/
├── api/             // API client setup, interceptors, API service functions
├── assets/          // Images, icons, static files
├── components/      // Reusable UI components (Shadcn wrappers, custom small components)
│   ├── ui/          // Shadcn UI components (generated)
│   └── common/      // Generic custom components
│   └── events/      // Event-specific components
│   └── users/       // User-specific components
│   └── ...
├── hooks/           // Custom React hooks (e.g., useAuth, useEvents)
├── layouts/         // Layout components (e.g., DashboardLayout, AuthLayout)
├── pages/           // Route-level components (correspond to views)
│   ├── auth/
│   │   └── LoginPage.tsx
│   ├── dashboard/
│   │   └── OverviewPage.tsx
│   ├── events/
│   │   └── EventsPage.tsx
│   │   └── EventDetailsPage.tsx
│   ├── users/
│   │   └── UsersPage.tsx
│   ├── transactions/
│   │   └── TransactionsPage.tsx
│   ├── admin/
│   │   └── AdminPage.tsx
│   └── competition/
│       └── CompetitionPage.tsx
├── providers/       // Context providers (e.g., AuthProvider, TenantProvider)
├── router/          // React Router configuration
├── styles/          // Tailwind CSS config, global CSS
├── types/           // TypeScript type definitions
├── utils/           // Utility functions (e.g., date formatting, validation)
└── App.tsx          // Main application component
└── main.tsx         // Entry point
```

## 9. API Integration

-   An API service layer will be implemented to encapsulate all backend communication.
-   This layer will handle:
    -   Attaching the `X-Tenant-Id` header to all requests.
    -   Authentication token management.
    -   Error handling and response parsing.
-   All UI components will interact with the backend exclusively through these service functions.

## 10. High-Level Implementation Steps

1.  **Project Setup:** Scaffold a new Vite React TypeScript project.
2.  **Tooling Integration:** Configure Tailwind CSS and integrate Shadcn UI.
3.  **Core Layout & Routing:** Set up `react-router-dom` and create basic layout components.
4.  **Authentication Flow:** Implement Login page and authentication context/provider.
5.  **API Client Setup:** Configure `fetch` or `axios` with interceptors for `X-Tenant-Id` and auth tokens.
6.  **Admin Page & User Management:** Start with ADMIN functionalities: inviting users, role management.
7.  **Event Management:** Implement CRUD operations for events, including recurring events.
8.  **Money Pool Management:** Develop transaction listing and top-up features.
9.  **Overview & Competition Pages:** Build out the remaining pages.
10. **Refinement & Testing:** Thoroughly test all features, ensure responsiveness, and optimize performance.
