import { expect, Page, test } from "@playwright/test";
import { ensure, HOST } from "./utils";
import { v4 as uuid } from "uuid";

const createTrainingEvent = async (page: Page, comment: string) => {
  await page.getByRole("button", { name: "nieuwe training" }).click();
  await page
    .locator("div")
    .filter({ hasText: /^Datum \/ tijd$/ })
    .first()
    .click();
  await page.getByRole("button", { name: "OK", exact: true }).click();
  await page.getByLabel("Locatie *").fill("Test locatie");
  await page.getByLabel("Opmerking").fill(comment);
  await page.getByLabel("Iedereen").check();
  await page.getByRole("button", { name: "Opslaan" }).click();

  await expect(
    page.locator("span").filter({ hasText: "Trainingen" }),
  ).toBeVisible();
  await expect(page.getByRole("alert")).toContainText("Training event");
  const snackbarText = await page.getByRole("alert").textContent();
  const matches = snackbarText?.match(/id ([a-f0-9-]+)/);
  const eventId = matches && matches[1];

  await page.getByRole("alert").getByRole("button").click();
  await expect(page.locator("tbody")).toContainText(comment);

  return ensure(eventId, "event training Id");
};

async function updateTraining(page: Page, eventId: string) {
  await page.getByRole("button", { name: `Update event ${eventId}` }).click();
  await page
    .getByLabel("Locatie *")
    .fill(`Updated location for event ${eventId}`);

  await page.getByRole("button", { name: "Opslaan" }).click();
  await expect(page.getByRole("alert")).toContainText(
    `Training event (id ${eventId}) geüpdate`,
  );
  await page.getByRole("alert").getByRole("button").click();
}

async function deleteTraining(page: Page, eventId: string | void) {
  await page
    .getByRole("button", { name: `Verwijder event ${eventId}` })
    .click();

  await expect(
    page.getByRole("heading", { name: "Weet je zeker" }),
  ).toContainText(
    `Weet je zeker dat je training met id #${eventId} wil verwijderen (en mogelijk meer)?`,
  );

  await page.getByRole("button", { name: "OK" }).click();

  await expect(page.getByRole("alert")).toContainText(
    `Event #${eventId} is verwijderd`,
  );
}

test.describe("Training ", () => {
  test.beforeEach(async ({ page }) => {
    // Go to the starting url before each test.
    await page.goto(HOST);
  });

  test("CRUD normal training", async ({ page }) => {
    await page.getByRole("button", { name: "Admin dingen" }).click();

    const comment = uuid();
    const eventId = await createTrainingEvent(page, comment);
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
