import { expect, test } from "@playwright/test";

test("automated mock knowledge loop runs without manual clicks", async ({ page }) => {
  const requestsAfterUploadTrigger: string[] = [];

  await page.goto("/");
  const prototype = page.frameLocator('iframe[title="Atlas Knowledge Hub Phase 1 Prototype"]');

  await expect(prototype.getByText("Atlas Knowledge Hub")).toBeVisible();
  await expect(prototype.locator(".library-card", { hasText: "IBM i Modernization" })).toBeVisible();

  await prototype.locator('[data-space="IBM i Modernization"]').click();
  await expect(prototype.locator("h1", { hasText: "IBM i Modernization" })).toBeVisible();

  await prototype.locator('[data-tab="docs"]').click();

  page.on("request", (request) => {
    requestsAfterUploadTrigger.push(request.url());
  });

  await prototype.getByTestId("upload-folder").click();
  await expect(prototype.getByTestId("upload-review")).toBeVisible();
  await expect(prototype.getByTestId("inventory-row")).toHaveCount(8);
  await expect(prototype.getByTestId("unsupported-row")).toHaveCount(2);
  expect(requestsAfterUploadTrigger).toEqual([]);

  await prototype.getByTestId("create-batch").click();
  await expect(prototype.getByTestId("batch-metrics")).toContainText(/8|文件总数/);
  await expect(prototype.getByTestId("file-tree")).toContainText("Target_Architecture.pptx");

  await prototype.getByTestId("view-report").click();
  const report = prototype.getByTestId("batch-report");
  await expect(report).toBeVisible();
  await expect(report.getByText(/source_trace:/).first()).toBeVisible();
  await expect(report.getByText("LOW_CONFIDENCE").first()).toBeVisible();
  await expect(report.getByText("REVIEW_REQUIRED").first()).toBeVisible();
  await report.locator("[data-report-close]").first().click();
  await expect(prototype.locator("#uploadView")).not.toHaveClass(/active/);

  await prototype.locator('[data-tab="review"]').click();
  await expect(prototype.getByTestId("review-publish-summary")).toContainText("Ready to publish");
  await expect(prototype.locator('[data-review-queue="missing-source-trace"]')).toContainText(
    /publish blocked|禁止发布/
  );
  await expect(prototype.locator('[data-review-queue-type="MISSING_SOURCE_TRACE"]')).toBeVisible();
  await expect(prototype.getByTestId("ready-publish-row")).toContainText("APPROVED");
  await expect(prototype.getByTestId("review-blocked-row").first()).toContainText(
    /Blocked from Wiki\/Graph\/Ask|不会发布到 Wiki\/Graph\/Ask/
  );

  await prototype.locator('[data-tab="wiki"]').click();
  await expect(prototype.getByTestId("published-wiki-metadata")).toContainText("PUBLISHED");
  await expect(prototype.getByTestId("published-wiki-metadata")).toContainText("source_trace");

  await prototype.locator('[data-tab="graph"]').click();
  await expect(prototype.locator(".graph-svg")).toBeVisible();

  await prototype.locator('[data-tab="ask"]').click();
  await expect(prototype.getByTestId("trusted-ask")).toBeVisible();
  await expect(prototype.getByTestId("ask-answer")).toHaveAttribute("data-status", "SUCCEEDED");
  await expect(prototype.getByTestId("ask-evidence")).toContainText("REVIEW_REQUIRED");
  await expect(prototype.getByTestId("ask-review-warning")).toContainText("REVIEW_REQUIRED");
  await expect(prototype.getByTestId("ask-no-evidence")).toContainText("No eligible approved evidence");
  await expect(prototype.getByTestId("ask-safe-error")).toContainText("failed safely");

  await prototype.locator('[data-nav-key="chat"]').click();
  await expect(prototype.locator('[data-chat-kb="IBM i Modernization"]')).toBeVisible();
  await expect(prototype.getByText(/知识库\(2\)|Knowledge\(2\)/)).toBeVisible();
});
