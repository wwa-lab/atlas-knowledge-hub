import { expect, test } from "@playwright/test";

test("review publish states show ready content and blocked trace gates", async ({ page }) => {
  await page.goto("/");
  const prototype = page.frameLocator('iframe[title="Atlas Knowledge Hub Phase 1 Prototype"]');

  await prototype.locator('[data-space="IBM i Modernization"]').click();
  await prototype.locator('[data-tab="review"]').click();

  await expect(prototype.getByTestId("review-publish-summary")).toContainText("Ready to publish");
  await expect(prototype.locator('[data-review-queue="missing-source-trace"]')).toContainText(
    /publish blocked|禁止发布/
  );
  await expect(prototype.locator('[data-review-queue-type="MISSING_SOURCE_TRACE"]')).toBeVisible();
  await expect(prototype.getByTestId("ready-publish-row")).toContainText("APPROVED");
  await expect(prototype.getByTestId("ready-publish-row")).toContainText(/Publish|发布/);
  await expect(prototype.getByTestId("review-blocked-row").first()).toContainText(
    /Blocked from Wiki\/Graph\/Ask|不会发布到 Wiki\/Graph\/Ask/
  );

  await prototype.locator('[data-tab="wiki"]').click();
  await expect(prototype.getByTestId("published-wiki-metadata")).toContainText("PUBLISHED");
  await expect(prototype.getByTestId("published-wiki-metadata")).toContainText("source_trace");
});
