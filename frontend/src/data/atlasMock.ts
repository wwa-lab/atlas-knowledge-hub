import type {
  AskSource,
  BatchMetric,
  GraphNode,
  KnowledgeSpace,
  ModelCategory,
  ModelConfig,
  ProgressItem,
  SourceFile,
  WikiSection
} from "@/types";

export const spaces: KnowledgeSpace[] = [
  {
    id: "ibm-i",
    name: "IBM i Modernization",
    description: "Discovery package to reviewed LM Wiki, graph evidence, and trusted Ask.",
    owner: "Atlas Delivery",
    updatedAt: "2026-07-02",
    status: "Review Required",
    documents: 128,
    wikiPages: 42,
    reviews: 9
  },
  {
    id: "ai-engineering",
    name: "AI Engineering",
    description: "Evaluation, RAG, model gateway, monitoring, and security practice notes.",
    owner: "AI Platform",
    updatedAt: "2026-06-28",
    status: "Ready",
    documents: 76,
    wikiPages: 31,
    reviews: 2
  }
];

export const batchMetrics: BatchMetric[] = [
  { label: "Total files", value: 128 },
  { label: "PDF converted", value: 112 },
  { label: "Markdown generated", value: 98 },
  { label: "Review required", value: 9 },
  { label: "Failed", value: 2 }
];

export const progressItems: ProgressItem[] = [
  { label: "Office to PDF conversion", value: 87 },
  { label: "Markdown generation", value: 76 },
  { label: "Source Trace validation", value: 68 },
  { label: "SME Review", value: 31 }
];

export const sourceFiles: SourceFile[] = [
  { path: "discovery/BRD_Methodology.pdf", status: "markdown" },
  { path: "architecture/current_state.pptx", status: "converted" },
  { path: "rpg-scan/customer_batch.xlsx", status: "review" },
  { path: "screenshots/legacy-flow.png", status: "ocr" },
  { path: "archive/old-interface.docx", status: "failed" }
];

export const wikiSections: WikiSection[] = [
  {
    id: "overview",
    title: "Space Overview",
    body: "IBM i Modernization normalizes discovery documents, RPG scan output, and migration notes into LM Wiki pages. Every generated conclusion keeps source_trace, confidence, and review_status visible.",
    confidence: "High",
    reviewStatus: "Approved"
  },
  {
    id: "trace",
    title: "Source Trace",
    body: "Source Trace links each Wiki section, graph edge, and Ask answer back to source files, pages, or chunks so SMEs can verify evidence before publication.",
    confidence: "Medium",
    reviewStatus: "Review Required"
  },
  {
    id: "adapters",
    title: "Adapter Boundary",
    body: "trinity-office and document-normalize appear as internal capabilities behind converter and parser adapters. Product UI never depends on a single engine implementation.",
    confidence: "High",
    reviewStatus: "Approved"
  }
];

export const graphNodes: GraphNode[] = [
  { id: "hub", label: "IBM i Modernization", type: "Entity", x: 360, y: 170, detail: "Knowledge Space core node." },
  { id: "wiki", label: "LM Wiki", type: "Wiki Page", x: 190, y: 90, detail: "Reviewed Markdown pages with trace fields." },
  { id: "trace", label: "Source Trace", type: "Concept", x: 170, y: 245, detail: "Evidence links to source documents and chunks." },
  { id: "bundle", label: "Evidence Bundle", type: "Document", x: 505, y: 92, detail: "Source-grounded snippets for graph and Ask." },
  { id: "review", label: "SME Review", type: "Review Required", x: 545, y: 245, detail: "Human review gate for low-confidence content." }
];

export const askSources: AskSource[] = [
  { title: "BRD_Methodology.pdf", page: "page 12", confidence: "Medium" },
  { title: "source_trace_manifest.json", page: "chunk 8", confidence: "High" }
];

export const modelConfigs: ModelConfig[] = [
  { id: "chat-local", category: "chat", name: "qwen2.5:14b", provider: "Ollama", sourceType: "Ollama", status: "Configured" },
  { id: "embed-api", category: "embedding", name: "text-embedding-3-large", provider: "OpenAI-compatible", sourceType: "API", status: "Mock" },
  { id: "rerank", category: "rerank", name: "bge-reranker", provider: "Custom API", sourceType: "API", status: "Available" },
  { id: "vision", category: "vision", name: "gpt-4o-mini", provider: "GitHub Models", sourceType: "GitHub Models", status: "Mock" }
];

export function getSpaceById(id: string): KnowledgeSpace | undefined {
  return spaces.find((space) => space.id === id);
}

export function filterModels(category: ModelCategory): ModelConfig[] {
  if (category === "all") {
    return [...modelConfigs];
  }

  return modelConfigs.filter((model) => model.category === category);
}

export function countGraphNodesByType(): Record<GraphNode["type"], number> {
  return graphNodes.reduce<Record<GraphNode["type"], number>>(
    (counts, node) => ({
      ...counts,
      [node.type]: counts[node.type] + 1
    }),
    {
      "Wiki Page": 0,
      Entity: 0,
      Concept: 0,
      Document: 0,
      "Review Required": 0
    }
  );
}
