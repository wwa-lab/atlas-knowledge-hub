#!/usr/bin/env node
import fs from "node:fs";

const USAGE = `
Usage:
  npm run agent:check-sdd -- --slice <slug> [options]

Options:
  --require-api-guide       Require API implementation guide files
  --report <path>           Completion report to check for skill-chain evidence
  --json                    Output JSON
`;

const REQUIRED_SKILL_FILES = [
  ".agents/skills/atlas-sdd-generate-all/SKILL.md",
  ".agents/skills/req-to-user-story/SKILL.md",
  ".agents/skills/user-story-to-spec/SKILL.md",
  ".agents/skills/spec-to-architecture/SKILL.md",
  ".agents/skills/architecture-to-design/SKILL.md",
  ".agents/skills/design-to-tasks/SKILL.md",
  ".agents/skills/review-doc-quality/SKILL.md",
];

function parseArgs(argv) {
  const args = {};
  for (let index = 0; index < argv.length; index += 1) {
    const token = argv[index];
    if (!token.startsWith("--")) continue;
    const key = token.slice(2);
    const next = argv[index + 1];
    if (!next || next.startsWith("--")) {
      args[key] = true;
      continue;
    }
    args[key] = next;
    index += 1;
  }
  return args;
}

function slugify(value) {
  return String(value)
    .trim()
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, "-")
    .replace(/^-+|-+$/g, "");
}

function exists(file) {
  return fs.existsSync(file) && fs.statSync(file).isFile();
}

function read(file) {
  return fs.readFileSync(file, "utf8");
}

function extractIds(content) {
  const matches = content.match(/\b(?:REQ|US|T|AC)-[A-Z0-9][A-Z0-9-]*-\d{3}\b/g) || [];
  return [...new Set(matches)].sort();
}

function difference(left, right) {
  const rightSet = new Set(right);
  return left.filter((item) => !rightSet.has(item));
}

const args = parseArgs(process.argv.slice(2));
const slice = slugify(args.slice || "");

if (!slice) {
  console.error(USAGE.trim());
  process.exit(1);
}

const requireApiGuide = Boolean(args["require-api-guide"]);
const reportPath = args.report;
const result = {
  slice,
  errors: [],
  warnings: [],
  checkedFiles: [],
};

const pairs = [
  ["requirements", `docs/01-requirements/${slice}-requirements.md`, `docs/01-requirements/${slice}-requirements.zh-CN.md`, true],
  ["stories", `docs/02-user-stories/${slice}-stories.md`, `docs/02-user-stories/${slice}-stories.zh-CN.md`, true],
  ["spec", `docs/03-spec/${slice}-spec.md`, `docs/03-spec/${slice}-spec.zh-CN.md`, true],
  ["architecture", `docs/04-architecture/${slice}-architecture.md`, `docs/04-architecture/${slice}-architecture.zh-CN.md`, true],
  ["data-flow", `docs/04-architecture/${slice}-data-flow.md`, `docs/04-architecture/${slice}-data-flow.zh-CN.md`, true],
  ["data-model", `docs/04-architecture/${slice}-data-model.md`, `docs/04-architecture/${slice}-data-model.zh-CN.md`, true],
  ["design", `docs/05-design/${slice}-design.md`, `docs/05-design/${slice}-design.zh-CN.md`, true],
  ["api-guide", `docs/05-design/contracts/${slice}-API_IMPLEMENTATION_GUIDE.md`, `docs/05-design/contracts/${slice}-API_IMPLEMENTATION_GUIDE.zh-CN.md`, requireApiGuide],
  ["tasks", `docs/06-tasks/${slice}-tasks.md`, `docs/06-tasks/${slice}-tasks.zh-CN.md`, true],
  ["traceability", `docs/00-context/${slice}-traceability.md`, `docs/00-context/${slice}-traceability.zh-CN.md`, true],
];

for (const skillFile of REQUIRED_SKILL_FILES) {
  if (!exists(skillFile)) {
    result.errors.push(`Missing required skill file: ${skillFile}`);
  }
}

for (const [label, englishPath, chinesePath, required] of pairs) {
  const englishExists = exists(englishPath);
  const chineseExists = exists(chinesePath);

  if (required && !englishExists) result.errors.push(`Missing ${label} English artifact: ${englishPath}`);
  if (required && !chineseExists) result.errors.push(`Missing ${label} Chinese artifact: ${chinesePath}`);
  if (!required && (!englishExists || !chineseExists)) {
    result.warnings.push(`Optional ${label} pair incomplete or absent: ${englishPath}, ${chinesePath}`);
    continue;
  }
  if (!englishExists || !chineseExists) continue;

  result.checkedFiles.push(englishPath, chinesePath);
  const englishIds = extractIds(read(englishPath));
  const chineseIds = extractIds(read(chinesePath));
  const missingInChinese = difference(englishIds, chineseIds);
  const missingInEnglish = difference(chineseIds, englishIds);

  if (missingInChinese.length > 0) {
    result.errors.push(`${label}: IDs missing in Chinese file: ${missingInChinese.join(", ")}`);
  }
  if (missingInEnglish.length > 0) {
    result.errors.push(`${label}: IDs missing in English file: ${missingInEnglish.join(", ")}`);
  }
  if (englishIds.length === 0 && chineseIds.length === 0) {
    result.warnings.push(`${label}: no REQ/US/T/AC IDs found in artifact pair.`);
  }
}

if (reportPath) {
  if (!exists(reportPath)) {
    result.errors.push(`Completion report not found: ${reportPath}`);
  } else {
    const report = read(reportPath);
    const requiredEvidence = [
      "SDD skill chain used: yes",
      ".agents/skills/atlas-sdd-generate-all/SKILL.md",
      ".agents/skills/req-to-user-story/SKILL.md",
      ".agents/skills/user-story-to-spec/SKILL.md",
      ".agents/skills/spec-to-architecture/SKILL.md",
      ".agents/skills/architecture-to-design/SKILL.md",
      ".agents/skills/design-to-tasks/SKILL.md",
      ".agents/skills/review-doc-quality/SKILL.md",
    ];
    for (const phrase of requiredEvidence) {
      if (!report.includes(phrase)) {
        result.errors.push(`Completion report missing evidence: ${phrase}`);
      }
    }
  }
} else {
  result.warnings.push("No --report provided; skill-chain evidence in completion report was not checked.");
}

if (args.json) {
  console.log(JSON.stringify(result, null, 2));
} else {
  console.log(`SDD gate check for slice: ${slice}`);
  for (const file of result.checkedFiles) console.log(`checked: ${file}`);
  for (const warning of result.warnings) console.log(`warning: ${warning}`);
  for (const error of result.errors) console.error(`error: ${error}`);
  console.log(result.errors.length === 0 ? "Result: PASS" : "Result: FAIL");
}

process.exit(result.errors.length === 0 ? 0 : 1);

