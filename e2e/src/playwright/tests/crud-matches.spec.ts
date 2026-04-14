import { expect, test } from "@playwright/test";
import { HOST } from "./utils";
import { v4 as uuid } from "uuid";
import {
  createMatchEvent,
  createUserViaApi,
  deleteMatch,
  deleteUserViaApi,
  updateMatch,
  setMatchAttendance,
  verifyMatchAttendanceState,
} from "./match-utils";

/**
 * Display name of the team member used for attendance tests.
 * Created via API before the test and deleted afterwards.
 */
const TEST_USER_NAME = "admin";

test.describe("Matches", () => {
  test.beforeEach(async ({ page }) => {
    await page.goto(HOST);
    await page.getByText("Aanstaande trainingen").waitFor({ state: "visible" });
  });

  test("CRUD normal match", async ({ page }) => {
    await page.getByRole("button", { name: "Admin dingen" }).click();

    const opponent = `Team ${uuid().slice(0, 8)}`;
    const eventId = await createMatchEvent(page, opponent);

    let combobox = page.getByRole("combobox", { name: "Rows per page:" });
    await combobox.isVisible();
    await combobox.click();
    await page.getByRole("option", { name: "50" }).click();
    await expect(page.locator("tbody")).toContainText(opponent);

    await updateMatch(page, eventId, `${opponent} Updated`);
    await deleteMatch(page, eventId);

    combobox = page.getByRole("combobox", { name: "Rows per page:" });
    await combobox.isVisible();
    await combobox.click();
    await page.getByRole("option", { name: "50" }).click();
    await expect(page.locator("tbody")).not.toContainText(opponent);
  });

  test("Set match attendance: attending → maybe → absent", async ({
    page,
    request,
  }) => {
    // WebKit is significantly slower than Chromium/Firefox: date picker, 6
    // attendance state transitions, API calls, and page reloads can exceed
    // the global 60 s limit. 90 s is sufficient headroom.
    test.setTimeout(90000);
    // 0. Create a team member so the event has attendees to interact with.
    //    The e2e database starts empty; without a user, events have no attendees.
    let name =
      TEST_USER_NAME + "_" + Math.round(Math.random() * new Date().getTime());
    const userId = await createUserViaApi(request, name);

    // 1. Create match via admin UI.
    await page.getByRole("button", { name: "Admin dingen" }).click();
    const opponent = `Team ${uuid().slice(0, 8)}`;
    const eventId = await createMatchEvent(page, opponent);

    try {
      // 2. Navigate to the events page (list view) where attendee buttons are
      //    always visible inline — no expand toggle needed.
      //    The home page shows "Aanstaande wedstrijden"; navigate there directly.
      await page.goto(HOST);
      await page
        .getByText("Aanstaande trainingen")
        .waitFor({ state: "visible" });
      // Home page has a "Meer" button in the "Aanstaande wedstrijden" section
      // that navigates to the full matches page (/matches).
      await page.getByTestId("match-events").getByTestId("more-button").click();
      await page.waitForURL(/matches/);

      // Ensure we are in list view — list view renders attendee buttons inline
      // without requiring an expand click.
      // The MUI Switch has name="listVsTable"; checked = list view.
      const listSwitch = page.locator('input[name="listVsTable"]');
      const isListView = await listSwitch.isChecked().catch(() => false);
      if (!isListView) {
        await listSwitch.click();
      }

      // Wait for the event-list testid to confirm list view has rendered.
      // The opponent text is visible in both table and list view, so we
      // must confirm via testid that we are actually in the EventsList.
      await page.getByTestId("event-list").waitFor({ state: "visible" });

      // Wait for the newly created match to appear in the list.
      let matchEvent = page
        .getByTestId("event-list-item")
        .filter({ hasText: opponent });
      await expect(matchEvent).toBeVisible();

      // 3. Set to Attending and verify.
      await setMatchAttendance(matchEvent, "attending", name);
      await verifyMatchAttendanceState(matchEvent, "attending", name);

      // 4. Transition to Maybe and verify.
      await setMatchAttendance(matchEvent, "maybe", name);
      await verifyMatchAttendanceState(matchEvent, "maybe", name);

      // 5. Transition to Absent and verify.
      await setMatchAttendance(matchEvent, "absent", name);
      await verifyMatchAttendanceState(matchEvent, "absent", name);

      // 6. Verify persistence: reload the page and check state is retained.
      await page.reload();
      await expect(page.getByText(opponent)).toBeVisible();
      await verifyMatchAttendanceState(matchEvent, "absent", name);
    } finally {
      // 7. Cleanup: always delete the match and the test user, even if assertions failed.
      // Wrap navigation in try/catch: Playwright may have already closed the
      // browser context by the time finally runs, which would cause a 90s timeout.
      try {
        await page.goto(HOST);
        await page.getByRole("button", { name: "Admin dingen" }).click();

        await deleteMatch(page, eventId);
      } catch {
        // Best-effort cleanup — ignore navigation errors after context teardown.
      }
      // Delete the test user via API (best-effort).
      await deleteUserViaApi(request, userId).catch(() => {});
    }
  });

  test("Opponent field validation", async ({ page }) => {
    await page.getByRole("button", { name: "Admin dingen" }).click();
    await page.getByRole("button", { name: /wedstrijden/i }).click();
    await page.getByRole("button", { name: /nieuwe wedstrijd/i }).click();

    // Verify the required Tegenstander field is present
    await expect(page.getByLabel("Tegenstander *")).toBeVisible();

    // Cancel without saving
    await page.getByRole("button", { name: "Annuleren" }).click();

    // Verify we're back on the Wedstrijden list
    await expect(
      page.getByRole("button", { name: /nieuwe wedstrijd/i }),
    ).toBeVisible();
  });

  test("Match deletion confirmation cancel", async ({ page }) => {
    await page.getByRole("button", { name: "Admin dingen" }).click();

    const opponent = `Team ${uuid().slice(0, 8)}`;
    const eventId = await createMatchEvent(page, opponent);
    let combobox = page.getByRole("combobox", { name: "Rows per page:" });
    await combobox.isVisible();
    await combobox.click();
    await page.getByRole("option", { name: "50" }).click();
    // Attempt delete but cancel
    await page
      .getByRole("button", { name: `Verwijder event ${eventId}` })
      .click();
    await expect(
      page.getByRole("heading", { name: "Weet je zeker" }),
    ).toBeVisible();
    await page.getByRole("button", { name: "Cancel" }).click();

    // Verify match still exists
    combobox = page.getByRole("combobox", { name: "Rows per page:" });
    await combobox.isVisible();
    await combobox.click();
    await page.getByRole("option", { name: "50" }).click();

    await expect(page.locator("tbody")).toContainText(opponent);

    // Cleanup
    await deleteMatch(page, eventId);
    combobox = page.getByRole("combobox", { name: "Rows per page:" });
    await combobox.isVisible();
    await combobox.click();
    await page.getByRole("option", { name: "50" }).click();
    await expect(page.locator("tbody")).not.toContainText(opponent);
  });

  test.skip("CRUD recurring match", async ({ page }) => {
    // TODO: Implement once recurring match UI is finalized
  });
});
