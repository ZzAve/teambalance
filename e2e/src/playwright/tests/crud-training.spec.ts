import { expect, test } from "@playwright/test";
import { HOST } from "./utils";
import { v4 as uuid } from "uuid";
import {
  createTrainingEvent,
  deleteTraining,
  updateTraining,
} from "./training-utils";

test.describe("Training ", () => {
  test.beforeEach(async ({ page }) => {
    // Go to the starting url before each test.
    await page.goto(HOST);
  });

  test("CRUD normal training", async ({ page }) => {
    await page.getByRole("button", { name: "Admin dingen" }).click();

    const comment = uuid();
    const eventId = await createTrainingEvent(page, comment);

    await expect(page.locator("tbody")).toContainText(comment);

    await updateTraining(page, eventId);
    await deleteTraining(page, eventId);

    // Ensure page does not contain event anymore (with the used comment)
    await expect(page.locator("tbody")).not.toContainText(comment);
  });

  test("CRUD recursive training", async ({ page }) => {
    // Take 2
    // Create recursive training
    // Split recursive training
    // Delete both training parts
  });
});
