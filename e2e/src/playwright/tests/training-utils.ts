import { expect, Page } from "@playwright/test";
import { addDays, ensure, NOW } from "./utils";

/**
 * Pick a date+time in the MUI MobileDateTimePicker dialog.
 *
 * Flow: open dialog → navigate to the right month → tap the day →
 *       set hours → set minutes → confirm with OK.
 *
 * The dialog is opened by clicking the read-only input labelled
 * "Datum / tijd". All subsequent selectors target ARIA roles/labels
 * inside the MUI dialog so they work across chromium, firefox and webkit.
 */
async function pickDateTime(page: Page, date: Date) {
  // Open the MUI MobileDateTimePicker dialog by clicking the date input.
  // The input has a dynamic accessible name like "Choose date, selected date is …".
  const dateInput = page.getByRole("textbox", { name: /Choose date/ });
  await dateInput.waitFor({ state: "visible" });
  await dateInput.click();

  // --- DATE part ---
  // The dialog shows a calendar. We need to navigate to the target month/year.
  // MUI shows "month year" button in the calendar header (e.g. "april 2026").
  // Strategy: click the left arrow button to go backward month-by-month until
  // we reach the target month, or the right arrow to go forward.
  const dialog = page.getByRole("dialog");
  await dialog.waitFor({ state: "visible" });

  // MUI renders navigation arrows as icon buttons. Use aria-label with a
  // fallback to data-testid in case the label is not exposed.
  const prevMonthBtn = dialog
    .getByRole("button", { name: /Previous month/i })
    .or(dialog.getByTestId("ArrowLeftIcon").locator(".."));
  const nextMonthBtn = dialog
    .getByRole("button", { name: /Next month/i })
    .or(dialog.getByTestId("ArrowRightIcon").locator(".."));

  // Dutch month names used by dayjs nl locale (lowercase)
  const dutchMonths = [
    "januari", "februari", "maart", "april", "mei", "juni",
    "juli", "augustus", "september", "oktober", "november", "december",
  ];
  const targetLabel = `${dutchMonths[date.getMonth()]} ${date.getFullYear()}`;

  // Navigate month-by-month (max 24 iterations as safety net)
  for (let i = 0; i < 24; i++) {
    // The current month/year is shown as a button in the calendar header.
    // MUI renders it inside a div with role "presentation", as a fade-transition span.
    const headerText = await dialog
      .locator(".MuiPickersCalendarHeader-label")
      .textContent();
    if (headerText?.trim().toLowerCase() === targetLabel) break;

    // Determine direction: parse current header to compare
    const currentMatch = headerText?.trim().toLowerCase();
    const currentMonthIdx = dutchMonths.findIndex((m) => currentMatch?.startsWith(m));
    const currentYear = parseInt(currentMatch?.split(" ").pop() ?? "0");
    const currentTotal = currentYear * 12 + currentMonthIdx;
    const targetTotal = date.getFullYear() * 12 + date.getMonth();

    if (targetTotal < currentTotal) {
      await prevMonthBtn.click();
    } else {
      await nextMonthBtn.click();
    }
    // Small wait for the calendar transition animation
    await page.waitForTimeout(150);
  }

  // Click the target day button. MUI day buttons have role "gridcell" with
  // the day number as text. Use exact match to avoid e.g. "1" matching "10".
  await dialog
    .getByRole("gridcell", { name: String(date.getDate()), exact: true })
    .click();

  // --- TIME part ---
  // After picking a day, MUI transitions to the clock view (hours first).
  // The clock is rendered as an SVG dial. Clicking hour/minute numbers
  // on the dial is unreliable across browsers. Instead, MUI provides a
  // text-input toggle: the pen/edit icon button switches to keyboard input.
  // Wait a moment for the clock view to render.
  await page.waitForTimeout(300);

  // Click the pen/keyboard-input toggle to switch to text fields.
  // MUI v5 renders this as a button with aria-label "open text input view"
  // and an inner SVG with data-testid="PenIcon".
  const penButton = dialog
    .getByRole("button", { name: /text input/i })
    .or(dialog.getByTestId("PenIcon").locator(".."));
  if (await penButton.isVisible()) {
    await penButton.click();
  }

  // Now the dialog shows two text inputs for hours and minutes.
  const pad = (n: number) => String(n).padStart(2, "0");

  // MUI labels the time inputs "hours" and "minutes"
  const hoursInput = dialog.getByRole("textbox", { name: /hours/i }).or(
    dialog.locator('input[aria-label="hours"]'),
  );
  const minutesInput = dialog.getByRole("textbox", { name: /minutes/i }).or(
    dialog.locator('input[aria-label="minutes"]'),
  );

  if (await hoursInput.isVisible()) {
    await hoursInput.click({ clickCount: 3 });
    await hoursInput.pressSequentially(pad(date.getHours()));
    await minutesInput.click({ clickCount: 3 });
    await minutesInput.pressSequentially(pad(date.getMinutes()));
  }

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
