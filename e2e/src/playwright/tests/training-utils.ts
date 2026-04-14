import { expect, Page } from "@playwright/test";
import { addDays, ensure, NOW, pickDateTime } from "./utils";

export const createTrainingEvent = async (
  page: Page,
  comment: string,
  date: Date = addDays(NOW, 1),
) => {
  await page.getByRole("button", { name: "nieuwe training" }).click();

  // Use the MUI MobileDateTimePicker dialog to set the date.
  await pickDateTime(page, date);

  await page.getByLabel("Locatie *").fill("Test locatie");
  await page.getByLabel("Opmerking").fill(comment);
  await page.getByLabel("Iedereen").check();
  await page.getByRole("button", { name: "Opslaan" }).click();

  // Wait for the success alert first — this is the authoritative confirmation
  // that the save completed. The nav span check that was here before was
  // redundant and raced against the form closing in Firefox.
  await expect(page.getByRole("alert")).toContainText("Training event");
  const snackbarText = await page.getByRole("alert").textContent();
  const matches = snackbarText?.match(/id ([a-f0-9-]+)/);
  const eventId = matches && matches[1];

  // Dismiss the snackbar. The MUI Snackbar auto-hides and the button can
  // become detached mid-click. Try clicking, but if it fails just wait for
  // the snackbar to disappear on its own.
  const alertDismiss = page.getByRole("alert").getByRole("button");
  await alertDismiss.click({ timeout: 3000 }).catch(() => {});
  await page
    .getByRole("alert")
    .waitFor({ state: "hidden", timeout: 10000 })
    .catch(() => {});
  return ensure(eventId, "event training Id");
};

export async function updateTraining(page: Page, eventId: string) {
  await page.getByRole("button", { name: `Update event ${eventId}` }).click();
  await page
    .getByLabel("Locatie *")
    .fill(`Updated location for event ${eventId}`);

  await page.getByRole("button", { name: "Opslaan" }).click();
  await expect(page.getByRole("alert")).toContainText(
    `Training event (id ${eventId}) geüpdate`,
  );
  await page
    .getByRole("alert")
    .getByRole("button")
    .click({ timeout: 3000 })
    .catch(() => {});
  await page
    .getByRole("alert")
    .waitFor({ state: "hidden", timeout: 10000 })
    .catch(() => {});
}

export async function deleteTraining(page: Page, eventId: string | void) {
  // Navigate to the Trainingen section (admin page defaults to Trainingen)
  await page.getByRole("button", { name: /trainingen/i }).click();
  let combobox = page.getByRole("combobox", { name: "Rows per page:" });
  await combobox.isVisible();
  await combobox.click();
  await page.getByRole("option", { name: "50" }).click();

  await page
    .getByRole("button", { name: `Verwijder event ${eventId}` })
    .click();

  await expect(
    page.getByRole("heading", { name: "Weet je zeker" }),
  ).toContainText(
    `Weet je zeker dat je training met id #${eventId} wil verwijderen (en mogelijk meer)?`,
  );

  await page.getByRole("button", { name: "OK" }).click();

  // Use filter so multiple concurrent snackbars don't cause a strict-mode violation.
  await expect(
    page
      .getByRole("alert")
      .filter({ hasText: `Event #${eventId} is verwijderd` }),
  ).toBeVisible();
}
