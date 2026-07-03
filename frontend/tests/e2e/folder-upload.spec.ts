import { expect, test } from "@playwright/test";

test("folder upload mock flow creates a batch and opens the report", async ({ page }) => {
  const requestsAfterTrigger: string[] = [];

  await page.goto("/");
  const prototype = page.frameLocator('iframe[title="Atlas Knowledge Hub Phase 1 Prototype"]');

  await prototype.locator('[data-space="IBM i Modernization"]').click();
  await prototype.locator('[data-tab="docs"]').click();

  page.on("request", (request) => {
    requestsAfterTrigger.push(request.url());
  });

  await prototype.getByTestId("upload-folder").click();
  await expect(prototype.getByTestId("upload-review")).toBeVisible();
  await expect(prototype.getByTestId("inventory-row")).toHaveCount(8);
  await expect(prototype.getByTestId("unsupported-row")).toHaveCount(2);
  await expect(prototype.getByText(/source package|来源包/i)).toBeVisible();
  expect(requestsAfterTrigger).toEqual([]);

  await prototype.getByTestId("create-batch").click();
  await expect(prototype.getByTestId("upload-review")).toHaveCount(0);
  await expect(prototype.getByTestId("batch-metrics")).toContainText(/8|文件总数/);
  await expect(prototype.getByTestId("batch-progress").locator(".progress-head")).toHaveCount(4);
  await expect(prototype.getByTestId("file-tree")).toContainText("Target_Architecture.pptx");

  await prototype.getByTestId("view-report").click();
  const report = prototype.getByTestId("batch-report");
  await expect(report).toBeVisible();
  await expect(report.getByText(/source_trace:/).first()).toBeVisible();
  await expect(report.getByText("UNSUPPORTED").first()).toBeVisible();
  await expect(report.getByText("PDF_CONVERT_FAILED").first()).toBeVisible();
  await expect(report.getByText("LOW_CONFIDENCE").first()).toBeVisible();
  await expect(report.getByText("REVIEW_REQUIRED").first()).toBeVisible();
});
