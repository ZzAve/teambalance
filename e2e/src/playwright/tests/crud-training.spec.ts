import { expect, test } from "@playwright/test";
import { HOST, addDays, NOW } from "./utils";
import { v4 as uuid } from "uuid";
import {
  createTrainingEvent,
  createTrainingViaApi,
  deleteTraining,
  deleteTrainingViaApi,
  updateTraining,
} from "./training-utils";
import {
  createUserViaApi,
  deleteUserViaApi,
  setMatchAttendance,
  verifyMatchAttendanceState,
} from "./match-utils";

test.describe("Training ", () => {
  test.beforeEach(async ({ page }) => {
    // Go to the starting url before each test.
    await page.goto(HOST);
  });

  test("CRUD normal training", async ({ page }) => {
    await page.getByRole("button", { name: "Admin dingen" }).click();

    const comment = uuid();
    const eventId = await createTrainingEvent(page, comment);

    await expect(page.locator("tbody")).toContainText(comment);

    await updateTraining(page, eventId);
    await deleteTraining(page, eventId);

    // Ensure page does not contain event anymore (with the used comment)
    let combobox = page.getByRole("combobox", { name: "Rows per page:" });
    await combobox.isVisible();
    await combobox.click();
    await page.getByRole("option", { name: "50" }).click();
    await expect(page.locator("tbody")).not.toContainText(comment);
  });

  test("CRUD recursive training", async ({ page }) => {
    // Take 2
    // Create recursive training
    // Split recursive training
    // Delete both training parts
  });

  test("can set training attendance for a team member", async ({
    request,
    page,
  }) => {
    // WebKit is slower; give ample headroom for 6 state transitions + API calls.
    test.setTimeout(90000);

    // 0. Create a team member via API so the training has attendees.
    const name = `attendee_${Math.round(Math.random() * new Date().getTime())}`;
    const userId = await createUserViaApi(request, name);

    // 1. Create training via API (faster than UI, and avoids admin page nav).
    const trainingDate = addDays(NOW, 3);
    trainingDate.setHours(19, 0, 0, 0);
    const trainingId = await createTrainingViaApi(
      request,
      trainingDate,
      "E2E Sporthal",
      `attendance-test-${uuid().slice(0, 8)}`,
    );

    try {
      // 2. Navigate to trainingen list on the public page.
      await page.goto(HOST);
      await page
        .getByText("Aanstaande trainingen")
        .waitFor({ state: "visible" });

      // The home page "Meer" button in the trainingen section navigates to /trainings.
      await page
        .getByTestId("training-events")
        .getByTestId("more-button")
        .click();
      await page.waitForURL(/trainings/);

      // Ensure list view is active (attendee buttons visible inline).
      const listSwitch = page.locator('input[name="listVsTable"]');
      const isListView = await listSwitch.isChecked().catch(() => false);
      if (!isListView) {
        await listSwitch.click();
      }
      await page.getByTestId("event-list").waitFor({ state: "visible" });

      // Locate the training card by the comment text (unique per run).
      const trainingItem = page
        .getByTestId("event-list-item")
        .filter({ hasText: "E2E Sporthal" });
      await expect(trainingItem).toBeVisible();

      // 3. Set to Attending and verify.
      await setMatchAttendance(trainingItem, "attending", name);
      await verifyMatchAttendanceState(trainingItem, "attending", name);

      // 4. Transition to Maybe and verify.
      await setMatchAttendance(trainingItem, "maybe", name);
      await verifyMatchAttendanceState(trainingItem, "maybe", name);

      // 5. Transition to Absent and verify.
      await setMatchAttendance(trainingItem, "absent", name);
      await verifyMatchAttendanceState(trainingItem, "absent", name);

      // 6. Verify persistence: reload and check state is retained.
      await page.reload();
      await expect(page.getByText("E2E Sporthal")).toBeVisible();
      await verifyMatchAttendanceState(trainingItem, "absent", name);
    } finally {
      // 7. Cleanup: delete training and user via API (best-effort).
      await deleteTrainingViaApi(request, trainingId).catch(() => {});
      await deleteUserViaApi(request, userId).catch(() => {});
    }
  });
});
