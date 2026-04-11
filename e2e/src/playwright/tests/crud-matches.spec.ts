import { expect, test } from "@playwright/test";
import { HOST } from "./utils";
import { v4 as uuid } from "uuid";
import { createMatchEvent, deleteMatch, updateMatch } from "./match-utils";

test.describe("Matches", () => {
  test.beforeEach(async ({ page }) => {
    await page.goto(HOST);
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

  test.skip("View match details and set attendance", async ({ page }) => {
    // TODO: Requires event-card-{id} testid and Aanwezig/Misschien/Afwezig
    // attendance buttons with data-selected attribute — not yet in the app.
  });

  test.skip("Change attendance state transitions", async ({ page }) => {
    // TODO: Same as above — attendance UI not yet implemented.
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
