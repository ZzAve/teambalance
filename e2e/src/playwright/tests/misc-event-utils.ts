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
 * Create a miscellaneous event via the backend API (requires admin credentials).
 * Returns the event ID (teamBalanceId) of the first created event.
 */
export async function createMiscEventViaApi(
  request: APIRequestContext,
  title: string,
  date: Date,
): Promise<string> {
  // Backend expects ISO-8601 LocalDateTime without timezone, e.g. "2026-04-20T14:00:00"
  const pad2 = (n: number) => String(n).padStart(2, "0");
  const startTime = `${date.getFullYear()}-${pad2(date.getMonth() + 1)}-${pad2(date.getDate())}T${pad2(date.getHours())}:${pad2(date.getMinutes())}:${pad2(date.getSeconds())}`;

  const response = await request.post(`${HOST}/api/miscellaneous-events`, {
    headers: adminHeaders,
    data: {
      startTime,
      title,
      location: "E2E Locatie",
    },
  });
  if (!response.ok()) {
    throw new Error(
      `Failed to create misc event: ${response.status()} ${await response.text()}`,
    );
  }
  const body = await response.json();
  const firstEvent = body.events?.[0];
  return ensure(firstEvent?.id as string | undefined, "misc event id");
}

/**
 * Delete a miscellaneous event via the backend API (requires admin credentials).
 * Also deletes all attendees bound to the event.
 */
export async function deleteMiscEventViaApi(
  request: APIRequestContext,
  eventId: string,
): Promise<void> {
  const response = await request.delete(
    `${HOST}/api/miscellaneous-events/${eventId}?delete-attendees=true`,
    { headers: adminHeaders },
  );
  if (!response.ok() && response.status() !== 404) {
    throw new Error(
      `Failed to delete misc event "${eventId}": ${response.status()} ${await response.text()}`,
    );
  }
}

/**
 * Create a miscellaneous event via the admin UI.
 * Navigates to "Overige events", opens the create form, fills it in, and
 * returns the event ID extracted from the success toast.
 */
export async function createMiscEvent(
  page: Page,
  title: string,
  options?: {
    date?: Date;
    location?: string;
    comment?: string;
  },
): Promise<string> {
  const date = options?.date ?? addDays(NOW, 7);
  const location = options?.location ?? "E2E Locatie";
  const comment = options?.comment;

  // Navigate to the Overige events section
  await page.getByRole("button", { name: /overige events/i }).click();

  // Click create button
  await page.getByRole("button", { name: /nieuw evenement/i }).click();

  // Fill title field
  await page.getByLabel("Titel").fill(title);

  // Use shared date picker
  await pickDateTime(page, date);

  // Fill location
  await page.getByLabel("Locatie *").fill(location);

  // Fill comment if provided
  if (comment) {
    await page.getByLabel("Opmerking").fill(comment);
  }

  // Select audience (default: everyone)
  await page.getByLabel("Iedereen").check();

  // Submit
  await page.getByRole("button", { name: "Opslaan" }).click();

  // Wait for success toast and extract event ID.
  // eventType() returns "Misc" for misc events, so the message is "Misc event (id ...)".
  await expect(page.getByRole("alert")).toContainText("Misc event");
  const snackbarText = await page.getByRole("alert").textContent();
  const matches = snackbarText?.match(/id ([a-f0-9-]+)/);
  const eventId = matches && matches[1];

  // Dismiss toast (may auto-hide; ignore errors)
  await page
    .getByRole("alert")
    .getByRole("button")
    .click({ timeout: 3000 })
    .catch(() => {});
  await page
    .getByRole("alert")
    .waitFor({ state: "hidden", timeout: 10000 })
    .catch(() => {});

  return ensure(eventId, "misc event ID");
}

/**
 * Update a miscellaneous event's title via the admin UI.
 */
export async function updateMiscEvent(
  page: Page,
  eventId: string,
  newTitle: string,
): Promise<void> {
  await page
    .getByRole("button", { name: `Update event ${eventId}` })
    .click({ timeout: 5000 });

  await page.getByLabel("Titel").fill(newTitle);

  await page.getByRole("button", { name: "Opslaan" }).click();

  await expect(page.getByRole("alert")).toContainText(
    `Misc event (id ${eventId}) geüpdate`,
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

/**
 * Delete a miscellaneous event via the admin UI with confirmation dialog.
 */
export async function deleteMiscEvent(
  page: Page,
  eventId: string,
): Promise<void> {
  // Navigate to the Overige events section
  await page.getByRole("button", { name: /overige events/i }).click();

  let combobox = page.getByRole("combobox", { name: "Rows per page:" });
  await combobox.isVisible();
  await combobox.click();
  await page.getByRole("option", { name: "50" }).click();

  await page
    .getByRole("button", { name: `Verwijder event ${eventId}` })
    .click();

  await expect(
    page.getByRole("heading", { name: "Weet je zeker" }),
  ).toBeVisible();

  await page.getByRole("button", { name: "OK" }).click();

  // Use filter so multiple concurrent snackbars don't cause a strict-mode violation.
  await expect(
    page
      .getByRole("alert")
      .filter({ hasText: `Event #${eventId} is verwijderd` }),
  ).toBeVisible();
}
