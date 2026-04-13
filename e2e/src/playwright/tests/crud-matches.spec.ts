import { expect, test } from "@playwright/test";
import { HOST } from "./utils";
import { v4 as uuid } from "uuid";
import {
  createMatchEvent,
  deleteMatch,
  updateMatch,
  setMatchAttendance,
  verifyMatchAttendanceState,
} from "./match-utils";

/**
 * Display name of the user authenticated in auth.setup.ts.
 * Must match the name stored by the backend for the 'admin' account.
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

    await expect(page.locator("tbody")).toContainText(opponent);

    await updateMatch(page, eventId, `${opponent} Updated`);
    await deleteMatch(page, eventId);

    await expect(page.locator("tbody")).not.toContainText(opponent);
  });

  test("Set match attendance: attending → maybe → absent", async ({ page }) => {
    // 1. Create match via admin UI.
    await page.getByRole("button", { name: "Admin dingen" }).click();
    const opponent = `Team ${uuid().slice(0, 8)}`;
    const eventId = await createMatchEvent(page, opponent);

    try {
      // 2. Navigate to the events page (list view) where attendee buttons are
      //    always visible inline — no expand toggle needed.
      //    The home page shows "Aanstaande wedstrijden"; navigate there directly.
      await page.goto(HOST);
      await page.getByText("Aanstaande trainingen").waitFor({ state: "visible" });
      // Home page uses MUI Tabs (role="tab") to switch between sections.
      await page.getByRole("tab", { name: /wedstrijden/i }).first().click();

      // Ensure we are in list view — list view renders attendee buttons inline
      // without requiring an expand click.
      // The MUI Switch has name="listVsTable"; checked = list view.
      const listSwitch = page.locator('input[name="listVsTable"]');
      const isListView = await listSwitch.isChecked().catch(() => false);
      if (!isListView) {
        await listSwitch.click();
      }

      // Wait for the newly created match to appear in the list.
      await expect(page.getByText(opponent)).toBeVisible();

      // 3. Set to Attending and verify.
      await setMatchAttendance(page, "attending", TEST_USER_NAME);
      await verifyMatchAttendanceState(page, "attending", TEST_USER_NAME);

      // 4. Transition to Maybe and verify.
      await setMatchAttendance(page, "maybe", TEST_USER_NAME);
      await verifyMatchAttendanceState(page, "maybe", TEST_USER_NAME);

      // 5. Transition to Absent and verify.
      await setMatchAttendance(page, "absent", TEST_USER_NAME);
      await verifyMatchAttendanceState(page, "absent", TEST_USER_NAME);

      // 6. Verify persistence: reload the page and check state is retained.
      await page.reload();
      await expect(page.getByText(opponent)).toBeVisible();
      await verifyMatchAttendanceState(page, "absent", TEST_USER_NAME);
    } finally {
      // 7. Cleanup: always delete the match, even if assertions above failed.
      // Wrap navigation in try/catch: Playwright may have already closed the
      // browser context by the time finally runs, which would cause a 90s timeout.
      try {
        await page.goto(HOST);
        await page.getByRole("button", { name: "Admin dingen" }).click();
        await deleteMatch(page, eventId);
      } catch {
        // Best-effort cleanup — ignore navigation errors after context teardown.
      }
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

    // Attempt delete but cancel
    await page
      .getByRole("button", { name: `Verwijder event ${eventId}` })
      .click();
    await expect(
      page.getByRole("heading", { name: "Weet je zeker" }),
    ).toBeVisible();
    await page.getByRole("button", { name: "Cancel" }).click();

    // Verify match still exists
    await expect(page.locator("tbody")).toContainText(opponent);

    // Cleanup
    await deleteMatch(page, eventId);
    await expect(page.locator("tbody")).not.toContainText(opponent);
  });

  test.skip("CRUD recurring match", async ({ page }) => {
    // TODO: Implement once recurring match UI is finalized
  });
});
