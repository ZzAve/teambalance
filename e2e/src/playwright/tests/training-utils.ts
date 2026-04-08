import { expect, Page } from "@playwright/test";
import { addDays, ensure, NOW } from "./utils";

export const createTrainingEvent = async (
  page: Page,
  comment: string,
  date: Date = addDays(NOW, 1),
) => {
  await page.getByRole("button", { name: "nieuwe training" }).click();

  // Build the date string manually to avoid locale/OS differences across browsers.
  // MUI MobileDateTimePicker input expects "dd-mm-yyyy hh:mm" (Dutch format).
  const pad = (n: number) => String(n).padStart(2, "0");
  const formattedDate =
    [pad(date.getDate()), pad(date.getMonth() + 1), String(date.getFullYear())].join("-") +
    " " +
    [pad(date.getHours()), pad(date.getMinutes())].join(":");

  // Click the input directly to avoid relying on MUI's dialog ARIA labels,
  // which are not reliably exposed in webkit. Then type the value
  // sequentially so the masked input registers each keystroke.
  const dateInput = page.getByPlaceholder("dd-mm-yyyy hh:mm");
  await dateInput.click();
  await dateInput.pressSequentially(formattedDate, { delay: 50 });

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
  await page.getByRole("alert").getByRole("button").click();
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
