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
    // 1. Create match
    await page.getByRole("button", { name: "Admin dingen" }).click();
    const opponent = `Team ${uuid().slice(0, 8)}`;
    const eventId = await createMatchEvent(page, opponent);

    // 2. Navigate to event details — click opponent text to open detail view
    await page.getByText(opponent).first().click();

    // 3. Set to Attending
    await setMatchAttendance(page, "attending");
    await verifyMatchAttendanceState(page, "attending");

    // 4. Transition to Maybe
    await setMatchAttendance(page, "maybe");
    await verifyMatchAttendanceState(page, "maybe");

    // 5. Transition to Absent
    await setMatchAttendance(page, "absent");
    await verifyMatchAttendanceState(page, "absent");

    // 6. Verify persistence: reload page
    await page.reload();
    await verifyMatchAttendanceState(page, "absent");

    // 7. Cleanup: navigate back to admin section and delete match
    await page.getByRole("button", { name: "Admin dingen" }).click();
    await deleteMatch(page, eventId);
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
