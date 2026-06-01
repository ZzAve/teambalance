import { APIRequestContext } from "@playwright/test";

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
 * Pick a date+time in the MUI MobileDateTimePicker dialog.
 *
 * Strategy (MUI X v7 / @mui/x-date-pickers v7):
 *   The v5 "switch to text input" toggle (pencil icon) was removed in v7.
 *   Instead we open the dialog → select the day on the calendar →
 *   the picker auto-advances to the clock view → select the hour → select
 *   the minute → confirm with OK.
 *
 * DOM evidence (trace from run 26754228619):
 *   MUI X v7 MobileDateTimePicker renders a MuiTextField with a <LABEL for=":rXX:">
 *   "Datum / tijd" and a readonly <INPUT aria-label="Choose date, selected date is …">.
 *   There is NO role="group". page.getByLabel("Datum / tijd") resolves to the input
 *   via the label's `for` attribute; clicking it opens the picker dialog.
 *
 * The calendar renders day cells as buttons with aria-label "DD MMMM YYYY"
 * (nl locale via dayjs). The clock renders hour/minute cells similarly.
 * minutesStep={15} means only 0, 15, 30, 45 are shown.
 */
export async function pickDateTime(
  page: import("@playwright/test").Page,
  date: Date,
): Promise<void> {
  // Open the picker – MUI X v7 renders a labelled text field (no role="group").
  // getByLabel resolves via <label for="…"> to the readonly input, clicking opens the dialog.
  const field = page.getByLabel("Datum / tijd");
  await field.waitFor({ state: "visible" });
  await field.click();

  const dialog = page.getByRole("dialog");
  await dialog.waitFor({ state: "visible" });

  // --- Calendar view: navigate to the correct month if needed, then click day ---
  // The "previous month" / "next month" buttons let us navigate.
  // MUI renders the calendar title as a button like "juni 2025".
  const targetYear = date.getFullYear();
  const targetMonth = date.getMonth(); // 0-based

  for (let attempts = 0; attempts < 24; attempts++) {
    // Read the current month/year label (e.g. "juni 2025")
    const titleButton = dialog.locator(
      '[class*="calendarHeader"] button, [class*="CalendarHeader"] button',
    );
    const titleText = await titleButton.first().textContent();
    if (!titleText) break;

    // Parse month index from Dutch month name
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
    const lowerTitle = titleText.toLowerCase();
    const displayedMonthIndex = nlMonths.findIndex((m) =>
      lowerTitle.includes(m),
    );
    const displayedYearMatch = titleText.match(/\d{4}/);
    const displayedYear = displayedYearMatch
      ? parseInt(displayedYearMatch[0], 10)
      : targetYear;

    const monthDiff =
      (targetYear - displayedYear) * 12 + (targetMonth - displayedMonthIndex);

    if (monthDiff === 0) break;

    // Click previous or next month arrow
    if (monthDiff > 0) {
      await dialog
        .getByRole("button", { name: /next month|volgende maand/i })
        .click();
    } else {
      await dialog
        .getByRole("button", { name: /previous month|vorige maand/i })
        .click();
    }
  }

  // Click the target day cell — aria-label is locale-formatted, e.g. "15 juni 2025"
  const nlMonthNames = [
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
  const dayLabel = `${date.getDate()} ${nlMonthNames[targetMonth]} ${targetYear}`;
  await dialog.getByRole("gridcell", { name: dayLabel, exact: true }).click();

  // --- Clock view: select hour ---
  await dialog
    .getByRole("option", { name: `${date.getHours()} hours` })
    .click();

  // --- Clock view: select minute ---
  await dialog
    .getByRole("option", { name: `${date.getMinutes()} minutes` })
    .click();

  // --- Confirm ---
  await dialog.getByRole("button", { name: "OK", exact: true }).click();
  await dialog.waitFor({ state: "hidden" });
}
