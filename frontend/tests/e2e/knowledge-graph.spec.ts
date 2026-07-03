import { expect, test, type Page } from "@playwright/test";

test("Graph tab searches, filters, selects evidence-backed graph objects", async ({ page }) => {
  await mockGraphApi(page, "ready");

  await page.goto("/");

  const graph = page.locator('[data-tab="graph"]');
  await expect(graph).toHaveAttribute("data-state", "ready");
  await expect(graph.getByTestId("graph-state")).toContainText("Graph API connected");
  await expect(graph.getByLabel("API-backed graph canvas")).toBeVisible();
  await expect(graph.getByLabel("Graph legend")).toContainText("Evidence edge");

  await graph.getByTestId("graph-search").fill("RPGLE");
  await expect(graph.locator(".graph-node-button", { hasText: "RPGLE modernization" })).toBeVisible();

  await graph.getByTestId("graph-node-filter").selectOption("CONCEPT");
  await expect(graph.locator(".graph-node-button", { hasText: "Target Architecture" })).toHaveCount(0);

  await graph.getByTestId("graph-node-filter").selectOption("ALL");
  await graph.locator(".graph-node-button", { hasText: "RPGLE modernization" }).click();
  await expect(graph.getByTestId("graph-evidence-detail")).toContainText("Source Trace");
  await expect(graph.getByTestId("graph-evidence-detail")).toContainText("chunk-file-001-p12-b02");
  await expect(graph.getByTestId("graph-evidence-detail")).toContainText("confidence 0.93");
  await expect(graph.getByTestId("graph-evidence-detail")).toContainText("APPROVED");

  await graph.getByTestId("graph-search").fill("");
  await graph.locator(".graph-edge-button", { hasText: "MENTIONS" }).click();
  await expect(graph.getByTestId("graph-evidence-detail")).toContainText("MENTIONS");
  await expect(graph.getByTestId("graph-evidence-detail")).toContainText("Target Architecture -> RPGLE modernization");
});

test("Graph tab exposes unauthorized and empty states", async ({ page }) => {
  await mockGraphApi(page, "unauthorized");
  await page.goto("/");

  const graph = page.locator('[data-tab="graph"]');
  await expect(graph).toHaveAttribute("data-state", "unauthorized");
  await expect(graph.getByTestId("graph-state")).toContainText("Unauthorized graph access");
  await expect(graph).not.toContainText("Using safe mock graph");

  await mockGraphApi(page, "empty");
  await page.reload();

  await expect(graph).toHaveAttribute("data-state", "empty");
  await expect(graph.getByTestId("graph-state")).toContainText("No approved or published evidence");
  await expect(graph).toContainText("Excluded 3");
});

async function mockGraphApi(page: Page, state: "ready" | "unauthorized" | "empty") {
  await page.unroute("**/api/spaces/ibm-i-modernization/graph**").catch(() => undefined);
  await page.route("**/api/spaces/ibm-i-modernization/graph**", async route => {
    const url = route.request().url();
    if (state === "unauthorized") {
      await route.fulfill({
        status: 403,
        contentType: "application/json",
        body: JSON.stringify({
          success: false,
          data: null,
          error: { code: "FORBIDDEN", message: "Forbidden" },
          meta: null
        })
      });
      return;
    }

    if (state === "empty") {
      await route.fulfill({
        contentType: "application/json",
        body: JSON.stringify({
          success: true,
          data: {
            spaceId: "ibm-i-modernization",
            nodes: [],
            edges: [],
            counts: { nodes: 0, edges: 0, excluded: 3 }
          },
          error: null,
          meta: null
        })
      });
      return;
    }

    if (url.includes("/nodes/node-concept-rpgle")) {
      await route.fulfill({
        contentType: "application/json",
        body: JSON.stringify({
          success: true,
          data: {
            node: {
              id: "node-concept-rpgle",
              label: "RPGLE modernization",
              type: "CONCEPT",
              reviewStatus: "APPROVED",
              confidence: 0.93,
              evidenceCount: 1
            },
            adjacentNodes: [],
            adjacentEdges: [],
            evidenceReferences: [
              {
                sourceChunkId: "chunk-file-001-p12-b02",
                sourceFile: "Graph/Modernization.md",
                page: 1,
                section: "RPGLE modernization",
                confidence: 0.93,
                reviewStatus: "APPROVED"
              }
            ]
          },
          error: null,
          meta: null
        })
      });
      return;
    }

    await route.fulfill({
      contentType: "application/json",
      body: JSON.stringify({
        success: true,
        data: {
          spaceId: "ibm-i-modernization",
          nodes: [
            {
              id: "node-document-target-architecture",
              label: "Target Architecture",
              type: "DOCUMENT",
              reviewStatus: "APPROVED",
              confidence: 0.91,
              evidenceCount: 1
            },
            {
              id: "node-concept-rpgle",
              label: "RPGLE modernization",
              type: "CONCEPT",
              reviewStatus: "APPROVED",
              confidence: 0.93,
              evidenceCount: 1
            }
          ],
          edges: [
            {
              id: "edge-source-mentions-rpgle",
              sourceNodeId: "node-document-target-architecture",
              targetNodeId: "node-concept-rpgle",
              type: "MENTIONS",
              reviewStatus: "APPROVED",
              confidence: 0.93,
              evidenceCount: 1
            }
          ],
          counts: { nodes: 2, edges: 1, excluded: 0 }
        },
        error: null,
        meta: null
      })
    });
  });
}
