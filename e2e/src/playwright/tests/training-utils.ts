import { expect, Page } from "@playwright/test";
import { addDays, ensure, NOW } from "./utils";

/**
 * Pick a date+time in the MUI MobileDateTimePicker dialog.
 *
 * Strategy: open dialog → switch to text-input view (pen icon) →
 *           type date + time into the text fields → confirm with OK.
 *
 * Previous approaches that used calendar month-navigation + gridcell clicking
 * broke because MUI renders duplicate day gridcells (overflow days from
 * adjacent months or transition animation artifacts). Using the text-input
 * view avoids all of that.
 */
async function pickDateTime(page: Page, date: Date) {
  // Open the MUI MobileDateTimePicker dialog by clicking the date input.
  const dateInput = page.getByRole("textbox", { name: /Choose date/ });
  await dateInput.waitFor({ state: "visible" });
  await dateInput.click();

  const dialog = page.getByRole("dialog");
  await dialog.waitFor({ state: "visible" });

  // Switch to text-input view immediately. The button label is
  // "calendar view is open, go to text input view" (or similar).
  const textInputToggle = dialog.getByRole("button", {
    name: /text input/i,
  });
  await textInputToggle.click();

  // In text-input mode, MUI renders a single date text field (dd/mm/yyyy
  // format for nl locale) and two fields for hours and minutes.
  // The date field is labelled "Datum / tijd" and uses the format dd/mm/yyyy.
  const pad2 = (n: number) => String(n).padStart(2, "0");
  const dateStr = `${pad2(date.getDate())}/${pad2(date.getMonth() + 1)}/${date.getFullYear()}`;

  // The date text input — MUI renders it with the accessible name matching
  // the label "Datum / tijd" (same label as the outer input).
  const dateField = dialog.locator('input[placeholder="dd/mm/yyyy"]').or(
    dialog.getByRole("textbox", { name: /datum/i }),
  );
  await dateField.waitFor({ state: "visible" });
  // Triple-click to select all, then type the new date
  await dateField.click({ clickCount: 3 });
  await dateField.pressSequentially(dateStr);

  // Hours and minutes fields
  const hoursInput = dialog.getByRole("textbox", { name: /hours/i }).or(
    dialog.locator('input[aria-label="hours"]'),
  );
  const minutesInput = dialog.getByRole("textbox", { name: /minutes/i }).or(
    dialog.locator('input[aria-label="minutes"]'),
  );

  await hoursInput.click({ clickCount: 3 });
  await hoursInput.pressSequentially(pad2(date.getHours()));
  await minutesInput.click({ clickCount: 3 });
  await minutesInput.pressSequentially(pad2(date.getMinutes()));

  // Confirm the selection
  await dialog.getByRole("button", { name: "OK", exact: true }).click();

  // Wait for the dialog to close
  await dialog.waitFor({ state: "hidden" });
}

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

  await expect(
    page.locator("span").filter({ hasText: "Trainingen" }),
  ).toBeVisible();
  await expect(page.getByRole("alert")).toContainText("Training event");
  const snackbarText = await page.getByRole("alert").textContent();
  const matches = snackbarText?.match(/id ([a-f0-9-]+)/);
  const eventId = matches && matches[1];

  // Dismiss the snackbar. The MUI Snackbar auto-hides and the button can
  // become detached mid-click. Try clicking, but if it fails just wait for
  // the snackbar to disappear on its own.
  const alertDismiss = page.getByRole("alert").getByRole("button");
  await alertDismiss.click({ timeout: 3000 }).catch(() => {});
  await page.getByRole("alert").waitFor({ state: "hidden", timeout: 10000 }).catch(() => {});
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
  await page.getByRole("alert").getByRole("button").click({ timeout: 3000 }).catch(() => {});
  await page.getByRole("alert").waitFor({ state: "hidden", timeout: 10000 }).catch(() => {});
}

export async function deleteTraining(page: Page, eventId: string | void) {
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
