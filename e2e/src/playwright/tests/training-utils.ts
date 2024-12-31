import { expect, Page } from "@playwright/test";
import { addDays, ensure, NOW } from "./utils";

export const createTrainingEvent = async (
  page: Page,
  comment: string,
  date: Date = addDays(NOW, 1),
) => {
  await page.getByRole("button", { name: "nieuwe training" }).click();
  await page
    .locator("div")
    .filter({ hasText: /^Datum \/ tijd$/ })
    .first()
    .click();
  await page.getByLabel("calendar view is open, go to").click();

  const formattedDate = date
    .toLocaleString("nl-NL", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    })
    .replace(",", "");
  await page.getByPlaceholder("dd-mm-yyyy hh:mm").fill(formattedDate);

  await page.getByRole("button", { name: "OK", exact: true }).click();

  // await page.getByLabel("Choose date, selected date is 28 dec").click();

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
