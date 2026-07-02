import { countGraphNodesByType, filterModels, getSpaceById } from "./atlasMock";

describe("atlas mock data", () => {
  it("finds the accepted IBM i Modernization demo space", () => {
    expect(getSpaceById("ibm-i")?.name).toBe("IBM i Modernization");
  });

  it("filters model cards by category without mutating the source list", () => {
    const allModels = filterModels("all");
    const embeddingModels = filterModels("embedding");

    expect(allModels.length).toBeGreaterThan(embeddingModels.length);
    expect(embeddingModels).toEqual(
      expect.arrayContaining([expect.objectContaining({ category: "embedding" })])
    );
  });

  it("counts graph node types for the legend", () => {
    expect(countGraphNodesByType()).toMatchObject({
      Entity: 1,
      Concept: 1,
      Document: 1,
      "Review Required": 1,
      "Wiki Page": 1
    });
  });
});
