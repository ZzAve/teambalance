import { expect, test } from "@playwright/test";
import { HOST } from "./utils";
import { v4 as uuid } from "uuid";
import {
  createMiscEvent,
  deleteMiscEvent,
  updateMiscEvent,
} from "./misc-event-utils";

test.describe("Misc events", () => {
  test.beforeEach(async ({ page }) => {
    // Go to the starting url before each test.
    await page.goto(HOST);
    await page.getByText("Aanstaande trainingen").waitFor({ state: "visible" });
  });

  test("can create a miscellaneous event", async ({ page }) => {
    await page.getByRole("button", { name: "Admin dingen" }).click();

    const title = `E2E Overig ${uuid().slice(0, 8)}`;
    const eventId = await createMiscEvent(page, title);

    // Verify the event appears in the list
    let combobox = page.getByRole("combobox", { name: "Rows per page:" });
    await combobox.isVisible();
    await combobox.click();
    await page.getByRole("option", { name: "50" }).click();
    await expect(page.locator("tbody")).toContainText(title);

    // Cleanup
    await deleteMiscEvent(page, eventId);
  });

  test("can update a miscellaneous event", async ({ page }) => {
    await page.getByRole("button", { name: "Admin dingen" }).click();

    const title = `E2E Overig ${uuid().slice(0, 8)}`;
    const eventId = await createMiscEvent(page, title);

    const updatedTitle = `${title} Updated`;
    await updateMiscEvent(page, eventId, updatedTitle);

    // Verify the updated title appears in the list
    let combobox = page.getByRole("combobox", { name: "Rows per page:" });
    await combobox.isVisible();
    await combobox.click();
    await page.getByRole("option", { name: "50" }).click();
    await expect(page.locator("tbody")).toContainText(updatedTitle);

    // Cleanup
    await deleteMiscEvent(page, eventId);
  });

  test("can delete a miscellaneous event", async ({ page }) => {
    await page.getByRole("button", { name: "Admin dingen" }).click();

    const title = `E2E Overig ${uuid().slice(0, 8)}`;
    const eventId = await createMiscEvent(page, title);

    await deleteMiscEvent(page, eventId);

    // Verify the event no longer appears in the list
    let combobox = page.getByRole("combobox", { name: "Rows per page:" });
    await combobox.isVisible();
    await combobox.click();
    await page.getByRole("option", { name: "50" }).click();
    await expect(page.locator("tbody")).not.toContainText(title);
  });
});
