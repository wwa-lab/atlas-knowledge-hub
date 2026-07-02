export type ViewName = "home" | "space";
export type SpaceTab = "documents" | "wiki" | "graph" | "review" | "ask";
export type Locale = "zh" | "en";
export type ThemeMode = "day" | "night";
export type SettingsPanel = "member" | "model" | "api" | "vector" | "parser" | "storage";
export type ModelCategory = "all" | "chat" | "embedding" | "rerank" | "vision" | "speech";

export interface KnowledgeSpace {
  id: string;
  name: string;
  description: string;
  owner: string;
  updatedAt: string;
  status: "Ready" | "Review Required" | "Parsing";
  documents: number;
  wikiPages: number;
  reviews: number;
}

export interface BatchMetric {
  label: string;
  value: number;
}

export interface ProgressItem {
  label: string;
  value: number;
}

export interface SourceFile {
  path: string;
  status: "converted" | "markdown" | "review" | "ocr" | "failed";
}

export interface WikiSection {
  id: string;
  title: string;
  body: string;
  confidence: "High" | "Medium" | "Low";
  reviewStatus: "Approved" | "Review Required";
}

export interface GraphNode {
  id: string;
  label: string;
  type: "Wiki Page" | "Entity" | "Concept" | "Document" | "Review Required";
  x: number;
  y: number;
  detail: string;
}

export interface AskSource {
  title: string;
  page: string;
  confidence: "High" | "Medium" | "Low";
}

export interface ModelConfig {
  id: string;
  category: Exclude<ModelCategory, "all">;
  name: string;
  provider: string;
  sourceType: "Ollama" | "API" | "GitHub Models" | "Copilot";
  status: "Configured" | "Available" | "Mock";
}
