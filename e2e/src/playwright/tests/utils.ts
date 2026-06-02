import { APIRequestContext, expect } from "@playwright/test";

export const ensure = <T>(
  value: T | undefined,
  name: string | undefined = "value",
) => {
  return (
    value ??
    (() => {
      throw new Error(
        `Expected non-null values for '${name}' was unexpectedly null`,
      );
    })()
  );
};

export const HOST: string = ensure(process.env.HOST, "host");
export const PASSWORD = ensure(process.env.PASSWORD, "PASSWORD");

const API_SECRET = Buffer.from("teambalance").toString("base64");
const apiHeaders = {
  Host: "frontend:3000",
  "X-Secret": API_SECRET,
  "Content-Type": "application/json",
};

export const NOW = new Date();

/**
 * Fetch the current start-of-season date from the backend config API.
 */
export async function getStartOfSeason(
  request: APIRequestContext,
): Promise<Date> {
  const response = await request.get(`${HOST}/api/config/season`, {
    headers: apiHeaders,
  });
  if (!response.ok()) {
    throw new Error(
      `Failed to fetch start-of-season: ${response.status()} ${response.statusText()}`,
    );
  }
  const body = (await response.json()) as { startOfSeason: string };
  const result = new Date(body.startOfSeason);
  if (isNaN(result.getTime())) {
    throw new Error(
      `getStartOfSeason: invalid date in response: ${body.startOfSeason}`,
    );
  }
  return result;
}

export const addHours = (date: Date, hours: number): Date => {
  const result = new Date(date);
  result.setHours(result.getHours() + hours);
  return result;
};

export const addDays = (date: Date, days: number): Date => {
  const result = new Date(date);
  result.setHours(result.getHours() + days * 24);
  return result;
};

/**
 * Pick a date+time in the MUI X v7 MobileDateTimePicker dialog.
 *
 * The readonly input's accessible name is an auto-generated aria-label
 * ("Choose date, selected date is …") which overrides the visible <label>,
 * so getByLabel("Datum / tijd") matches nothing. Target the stable placeholder.
 *
 * Flow: click field → dialog opens on the "pick date" tab → navigate month →
 * click the day (auto-advances to the "pick time" tab, hours view) → pick hour
 * (auto-advances to minutes) → pick minute → OK.
 *
 * The time view is an analog clock: a transparent MuiClock-squareMask overlay
 * intercepts pointer events, so clock numbers must be clicked with { force: true }.
 * Clock numbers expose role="option" with aria-labels: hours "0" → "00 hours",
 * otherwise "<h> hours"; minutes are zero-padded, e.g. "00 minutes", "30 minutes".
 * The field uses minutesStep={15}, so the target minute is rounded to the nearest
 * 15-min step (no test asserts the exact time — only the unique comment/title).
 */
export async function pickDateTime(
  page: import("@playwright/test").Page,
  date: Date,
): Promise<void> {
  const field = page.getByPlaceholder("DD-MM-YYYY hh:mm");
  await field.waitFor({ state: "visible" });
  await field.click();

  const dialog = page.getByRole("dialog");
  await dialog.waitFor({ state: "visible" });

  // --- Pick date tab: navigate to the correct month, then click the day ---
  const targetYear = date.getFullYear();
  const targetMonth = date.getMonth(); // 0-based
  const nlMonths = [
    "januari",
    "februari",
    "maart",
    "april",
    "mei",
    "juni",
    "juli",
    "augustus",
    "september",
    "oktober",
    "november",
    "december",
  ];

  for (let attempts = 0; attempts < 24; attempts++) {
    // The month/year label (e.g. "juni 2026") lives in its own element; the only
    // button in the header is the year-view toggle, so read the label text.
    const label = dialog
      .locator('[class*="PickersCalendarHeader-label"]')
      .first();
    const titleText = (await label.textContent())?.toLowerCase() ?? "";
    const displayedMonthIndex = nlMonths.findIndex((m) =>
      titleText.includes(m),
    );
    const displayedYearMatch = titleText.match(/\d{4}/);
    const displayedYear = displayedYearMatch
      ? parseInt(displayedYearMatch[0], 10)
      : targetYear;

    const monthDiff =
      (targetYear - displayedYear) * 12 + (targetMonth - displayedMonthIndex);
    if (monthDiff === 0) break;

    await dialog
      .getByRole("button", {
        name:
          monthDiff > 0
            ? /next month|volgende maand/i
            : /previous month|vorige maand/i,
      })
      .click();
  }

  // Day cells are named by day number only (e.g. "15"). Rapidly clicking the
  // month-nav arrow stacks several month slides in the DOM (MUI's slide
  // transition mounts entering + exiting months), so the same day-number can
  // briefly resolve to multiple cells. Wait for the transitions to settle to a
  // single match before clicking. Selecting a day auto-advances to the time tab.
  const dayCell = dialog.getByRole("gridcell", {
    name: String(date.getDate()),
    exact: true,
  });
  await expect(dayCell).toHaveCount(1);
  await dayCell.click();

  // --- Pick time tab (analog clock) ---
  await dialog.getByRole("tab", { name: "pick time" }).click();

  const hourLabel =
    date.getHours() === 0 ? "00 hours" : `${date.getHours()} hours`;
  await dialog.locator(`[aria-label="${hourLabel}"]`).click({ force: true });

  const steppedMinute = Math.min(45, Math.round(date.getMinutes() / 15) * 15);
  const minuteLabel = `${String(steppedMinute).padStart(2, "0")} minutes`;
  await dialog.locator(`[aria-label="${minuteLabel}"]`).click({ force: true });

  // --- Confirm ---
  await dialog.getByRole("button", { name: "OK", exact: true }).click();
  await dialog.waitFor({ state: "hidden" });
}
