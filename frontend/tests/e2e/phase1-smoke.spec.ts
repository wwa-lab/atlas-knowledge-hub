import { expect, test } from "@playwright/test";

test("Phase 1 shows the accepted attachment prototype and core flow", async ({ page }) => {
  await page.goto("/");

  const prototype = page.frameLocator('iframe[title="Atlas Knowledge Hub Phase 1 Prototype"]');

  await expect(prototype.getByText("Atlas Knowledge Hub")).toBeVisible();
  await expect(prototype.locator("h1", { hasText: "知识库" })).toBeVisible();
  await expect(prototype.locator(".dialog-hero")).toHaveCount(0);
  await expect(prototype.locator(".library-cards")).toBeVisible();
  await expect(prototype.locator(".library-card", { hasText: "IBM i Modernization" })).toBeVisible();
  await expect(prototype.getByText("内部知识工作台")).toBeVisible();

  await prototype.locator("[data-create-space]").click();
  await expect(prototype.locator("#createSpaceView")).toHaveClass(/active/);
  await expect(prototype.locator("h1", { hasText: "新建知识库" })).toBeVisible();
  await expect(prototype.locator("h2", { hasText: "基本信息" })).toBeVisible();
  await prototype.locator('[data-knowledge-type="faq"]').click();
  await expect(prototype.locator('[data-knowledge-type="faq"]')).toHaveClass(/active/);
  await expect(prototype.locator('[data-knowledge-type="document"]')).not.toHaveClass(/active/);
  await prototype.locator('[data-create-step="model"]').click();
  await expect(prototype.locator('[data-create-step="model"]')).toHaveClass(/active/);
  await expect(prototype.locator('[data-create-step="basic"]')).not.toHaveClass(/active/);
  await expect(prototype.getByText("RAG 检索")).toBeVisible();
  await prototype.locator('[data-index-strategy="wiki"]').click();
  await expect(prototype.locator('[data-index-strategy="wiki"]')).toHaveClass(/active/);
  await expect(prototype.locator('[data-index-strategy="rag"]')).not.toHaveClass(/active/);
  await expect(prototype.getByRole("button", { name: "创建知识库" })).toBeVisible();
  await prototype.locator("#cancelCreateSpace").click();
  await expect(prototype.locator("#createSpaceView")).not.toHaveClass(/active/);

  await expect(prototype.locator(".utility-controls")).toHaveCount(0);
  await prototype.locator("[data-open-settings]").click();
  await expect(prototype.locator("#settingsLanguageSelect")).toHaveValue("zh-CN");
  await prototype.locator("#settingsLanguageSelect").selectOption("en-US");
  await expect(prototype.locator("h1", { hasText: "General Settings" })).toBeVisible();
  await prototype.locator("#settingsThemeSelect").selectOption("night");
  await expect(prototype.locator("html")).toHaveAttribute("data-theme", "night");
  await prototype.locator(".settings-rail").locator('[data-settings-panel="models"]').click();
  await expect(prototype.locator("h1", { hasText: /模型配置|Model Configuration/ })).toBeVisible();
  await expect(prototype.locator(".model-info-panel")).toBeVisible();
  await expect(prototype.locator(".model-list-card")).toHaveCount(2);
  await expect(prototype.locator(".model-list-tab").first()).toContainText("(2)");
  await expect(prototype.getByText(/编辑模型|Edit Model/)).toHaveCount(0);
  await prototype.locator("#closeSettings").click();

  await prototype.locator('[data-space="IBM i Modernization"]').click();
  await expect(prototype.locator("h1", { hasText: "IBM i Modernization" })).toBeVisible();
  await expect(prototype.getByText("35 Review Required")).toBeVisible();

  await prototype.locator('[data-tab="graph"]').click();
  await expect(prototype.locator(".graph-svg")).toBeVisible();

  await prototype.locator('[data-tab="ask"]').click();
  await expect(prototype.getByText("Source Trace 指 Atlas")).toBeVisible();
});
