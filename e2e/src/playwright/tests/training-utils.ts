import { expect, APIRequestContext, Page } from "@playwright/test";
import { addDays, ensure, HOST, NOW, pickDateTime } from "./utils";

/**
 * The X-Secret header value for the local/e2e tenant.
 * Reads from E2E_API_SECRET env var; falls back to base64 of "teambalance".
 */
const API_SECRET =
  process.env.E2E_API_SECRET ?? Buffer.from("teambalance").toString("base64");

const apiHeaders = {
  Host: "frontend:3000",
  "X-Secret": API_SECRET,
  "Content-Type": "application/json",
};

const adminHeaders = {
  ...apiHeaders,
  Authorization: `Basic ${Buffer.from("admin:admin").toString("base64")}`,
};

/**
 * Create a training event via the backend API (requires admin credentials).
 * Returns the training ID (teamBalanceId) of the first created event.
 */
export async function createTrainingViaApi(
  request: APIRequestContext,
  date: Date,
  location: string,
  comment?: string,
): Promise<string> {
  // Backend expects ISO-8601 LocalDateTime without timezone, e.g. "2026-04-20T14:00:00"
  const pad2 = (n: number) => String(n).padStart(2, "0");
  const startTime = `${date.getFullYear()}-${pad2(date.getMonth() + 1)}-${pad2(date.getDate())}T${pad2(date.getHours())}:${pad2(date.getMinutes())}:${pad2(date.getSeconds())}`;

  const response = await request.post(`${HOST}/api/trainings`, {
    headers: adminHeaders,
    data: {
      startTime,
      location,
      comment: comment ?? null,
    },
  });
  if (!response.ok()) {
    throw new Error(
      `Failed to create training: ${response.status()} ${await response.text()}`,
    );
  }
  const body = await response.json();
  const firstEvent = body.events?.[0];
  return ensure(firstEvent?.id as string | undefined, "training id");
}

/**
 * Delete a training event via the backend API (requires admin credentials).
 * Also deletes all attendees bound to the training.
 */
export async function deleteTrainingViaApi(
  request: APIRequestContext,
  trainingId: string,
): Promise<void> {
  const response = await request.delete(
    `${HOST}/api/trainings/${trainingId}?delete-attendees=true`,
    { headers: adminHeaders },
  );
  if (!response.ok() && response.status() !== 404) {
    throw new Error(
      `Failed to delete training "${trainingId}": ${response.status()} ${await response.text()}`,
    );
  }
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

  const successAlert = page
    .getByRole("alert")
    .filter({ hasText: "Training event" });
  await expect(successAlert).toContainText("Training event");
  const snackbarText = await successAlert.textContent();
  const matches = snackbarText?.match(/id ([a-f0-9-]+)/);
  const eventId = matches && matches[1];

  // Dismiss the snackbar. The MUI Snackbar auto-hides and the button can
  // become detached mid-click. Try clicking, but if it fails just wait for
  // the snackbar to disappear on its own.
  await successAlert
    .getByRole("button")
    .click({ timeout: 3000 })
    .catch(() => {});
  await successAlert
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
  const updateAlert = page
    .getByRole("alert")
    .filter({ hasText: `Training event (id ${eventId}) geüpdate` });
  await expect(updateAlert).toContainText(
    `Training event (id ${eventId}) geüpdate`,
  );
  await updateAlert
    .getByRole("button")
    .click({ timeout: 3000 })
    .catch(() => {});
  await updateAlert
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
