#!/usr/bin/env node
import { execFileSync } from "node:child_process";
import fs from "node:fs";
import path from "node:path";

const USAGE = `
Usage:
  npm run agent:check-workflow -- [options]

Options:
  --slice <slug>             Also run the SDD gate checker for this slice
  --changed-slices           Detect changed SDD slice docs and check them
  --base-ref <ref>           Base ref/sha for changed-slice and diff checks
  --require-api-guide        Require API guide files when --slice is set
  --report <path>            Completion report used by the SDD gate checker
  --skip-diff-check          Skip git diff --check
  --json                     Output machine-readable JSON
`;

const STATIC_FILES = [
  "AGENTS.md",
  "PROJECT_RULES.md",
  "DEVELOPMENT_STANDARDS.md",
  ".github/copilot-instructions.md",
  "docs/SDD-BOOTSTRAP.md",
  "docs/SDD-BOOTSTRAP.zh-CN.md",
  "package.json",
];

const STATIC_DIRS = [
  ".github/workflows",
  "scripts/agent-workflow",
  "docs/00-context/checklists",
  "docs/00-context/examples",
  "docs/00-context/execution-manifests",
  "docs/00-context/goal-prompts",
];

const STATIC_GLOBS = [
  /^docs\/00-context\/agent-goal-loop-.*\.md$/,
  /^docs\/00-context\/agent-execution-modes(?:\.zh-CN)?\.md$/,
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
  return String(value || "")
    .trim()
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, "-")
    .replace(/^-+|-+$/g, "");
}

function listFiles(root) {
  if (!fs.existsSync(root)) return [];
  const stat = fs.statSync(root);
  if (stat.isFile()) return [root];
  if (!stat.isDirectory()) return [];

  return fs.readdirSync(root, { withFileTypes: true }).flatMap((entry) => {
    const child = path.join(root, entry.name);
    if (entry.isDirectory()) return listFiles(child);
    if (entry.isFile()) return [child];
    return [];
  });
}

function workflowFiles() {
  const files = new Set();
  for (const file of STATIC_FILES) {
    if (fs.existsSync(file)) files.add(file);
  }
  for (const directory of STATIC_DIRS) {
    for (const file of listFiles(directory)) files.add(file);
  }
  for (const file of listFiles("docs/00-context")) {
    const normalized = file.replaceAll(path.sep, "/");
    if (STATIC_GLOBS.some((pattern) => pattern.test(normalized))) files.add(file);
  }
  return [...files].sort();
}

function runCommand(command, args, options = {}) {
  return execFileSync(command, args, {
    encoding: "utf8",
    stdio: ["ignore", "pipe", "pipe"],
    ...options,
  });
}

function gitLines(args) {
  try {
    return runCommand("git", args)
      .split(/\n/)
      .map((line) => line.trim())
      .filter(Boolean);
  } catch {
    return [];
  }
}

function runStep(results, name, fn) {
  try {
    const detail = fn();
    results.steps.push({ name, status: "PASS", detail: detail || "" });
  } catch (error) {
    const stderr = error.stderr ? String(error.stderr) : "";
    const stdout = error.stdout ? String(error.stdout) : "";
    const detail = [error.message, stdout, stderr].filter(Boolean).join("\n").trim();
    results.steps.push({ name, status: "FAIL", detail });
    results.errors.push(`${name}: ${detail}`);
  }
}

function checkTrailingWhitespace(files) {
  const issues = [];
  for (const file of files) {
    const content = fs.readFileSync(file, "utf8");
    content.split(/\n/).forEach((line, index) => {
      if (/[ \t]$/.test(line)) issues.push(`${file}:${index + 1}: trailing whitespace`);
    });
  }
  if (issues.length > 0) throw new Error(issues.join("\n"));
  return `${files.length} files checked`;
}

function checkPrivatePaths(files) {
  const privateUsersPrefix = "/" + "Users" + "/";
  const issues = [];
  for (const file of files) {
    const content = fs.readFileSync(file, "utf8");
    content.split(/\n/).forEach((line, index) => {
      if (line.includes(privateUsersPrefix)) issues.push(`${file}:${index + 1}: private absolute path`);
    });
  }
  if (issues.length > 0) throw new Error(issues.join("\n"));
  return `${files.length} files checked`;
}

function checkSecrets(files) {
  const privateKeyMarker = "BEGIN" + " PRIVATE KEY";
  const secretPatterns = [
    /(?<![A-Za-z])sk-[A-Za-z0-9]{8,}/,
    /ghp_[A-Za-z0-9]{8,}/,
    /github_pat_[A-Za-z0-9_]{20,}/,
    /AKIA[0-9A-Z]{16}/,
  ];
  const issues = [];
  for (const file of files) {
    const content = fs.readFileSync(file, "utf8");
    content.split(/\n/).forEach((line, index) => {
      const hasPattern = secretPatterns.some((pattern) => pattern.test(line));
      if (hasPattern || line.includes(privateKeyMarker)) {
        issues.push(`${file}:${index + 1}: possible secret`);
      }
    });
  }
  if (issues.length > 0) throw new Error(issues.join("\n"));
  return `${files.length} files checked`;
}

function yamlFiles(files) {
  return files.filter((file) => file.endsWith(".yaml") || file.endsWith(".yml"));
}

function validateYaml(files) {
  const yamlTargets = yamlFiles(files);
  for (const file of yamlTargets) {
    runCommand("ruby", ["-e", "require 'yaml'; YAML.load_file(ARGV[0])", file]);
  }
  return `${yamlTargets.length} YAML files checked`;
}

function checkNodeSyntax() {
  const scripts = listFiles("scripts/agent-workflow").filter((file) => file.endsWith(".mjs"));
  for (const script of scripts) {
    runCommand(process.execPath, ["--check", script]);
  }
  return `${scripts.length} scripts checked`;
}

function checkPackageJson() {
  JSON.parse(fs.readFileSync("package.json", "utf8"));
  return "package.json parsed";
}

function runSddGateForSlice(args, slice) {
  const sddArgs = ["scripts/agent-workflow/atlas-check-sdd-gate.mjs", "--slice", slice];
  if (args["require-api-guide"]) sddArgs.push("--require-api-guide");
  if (args.report) sddArgs.push("--report", String(args.report));
  runCommand(process.execPath, sddArgs);
  return `SDD gate checked for ${slice}`;
}

function runSddGate(args) {
  const slice = slugify(args.slice);
  if (!slice) return "no slice provided; SDD gate skipped";
  return runSddGateForSlice(args, slice);
}

function changedFiles(args) {
  const baseRef = args["base-ref"];
  if (baseRef) {
    return gitLines(["diff", "--name-only", "--diff-filter=ACMRTUXB", `${baseRef}...HEAD`]);
  }

  const files = new Set([
    ...gitLines(["diff", "--name-only", "--diff-filter=ACMRTUXB"]),
    ...gitLines(["diff", "--cached", "--name-only", "--diff-filter=ACMRTUXB"]),
    ...gitLines(["ls-files", "--others", "--exclude-standard"]),
  ]);
  return [...files].sort();
}

function sliceFromPath(file) {
  const normalized = file.replaceAll(path.sep, "/");
  const patterns = [
    /^docs\/01-requirements\/(.+)-requirements(?:\.zh-CN)?\.md$/,
    /^docs\/02-user-stories\/(.+)-stories(?:\.zh-CN)?\.md$/,
    /^docs\/03-spec\/(.+)-spec(?:\.zh-CN)?\.md$/,
    /^docs\/04-architecture\/(.+)-(?:architecture|data-flow|data-model)(?:\.zh-CN)?\.md$/,
    /^docs\/05-design\/(.+)-design(?:\.zh-CN)?\.md$/,
    /^docs\/05-design\/contracts\/(.+)-API_IMPLEMENTATION_GUIDE(?:\.zh-CN)?\.md$/,
    /^docs\/06-tasks\/(.+)-tasks(?:\.zh-CN)?\.md$/,
    /^docs\/00-context\/(.+)-traceability(?:\.zh-CN)?\.md$/,
  ];

  for (const pattern of patterns) {
    const match = normalized.match(pattern);
    if (match) return slugify(match[1]);
  }
  return "";
}

function changedSlices(args) {
  return [...new Set(changedFiles(args).map(sliceFromPath).filter(Boolean))].sort();
}

function runChangedSliceGates(args) {
  const slices = changedSlices(args);
  if (slices.length === 0) return "no changed SDD slices detected";

  for (const slice of slices) {
    runSddGateForSlice(args, slice);
  }
  return `SDD gates checked for changed slices: ${slices.join(", ")}`;
}

function runDiffCheck(args) {
  const baseRef = args["base-ref"];
  if (baseRef) {
    return runCommand("git", ["diff", "--check", `${baseRef}...HEAD`]).trim() || `clean against ${baseRef}`;
  }
  return runCommand("git", ["diff", "--check"]).trim() || "clean";
}

const args = parseArgs(process.argv.slice(2));
if (args.help) {
  console.log(USAGE.trim());
  process.exit(0);
}

const results = {
  errors: [],
  steps: [],
};
const files = workflowFiles();

runStep(results, "package.json parse", checkPackageJson);
runStep(results, "agent workflow script syntax", checkNodeSyntax);
runStep(results, "workflow YAML parse", () => validateYaml(files));
runStep(results, "workflow trailing whitespace scan", () => checkTrailingWhitespace(files));
runStep(results, "workflow private path scan", () => checkPrivatePaths(files));
runStep(results, "workflow secret pattern scan", () => checkSecrets(files));
if (!args["skip-diff-check"]) {
  runStep(results, "git diff --check", () => runDiffCheck(args));
}
if (args.slice) {
  runStep(results, "slice SDD gate", () => runSddGate(args));
}
if (args["changed-slices"]) {
  runStep(results, "changed slice SDD gates", () => runChangedSliceGates(args));
}

const passed = results.errors.length === 0;

if (args.json) {
  console.log(JSON.stringify({ passed, ...results }, null, 2));
} else {
  console.log("AGENT WORKFLOW GATE");
  console.log("===================");
  for (const step of results.steps) {
    console.log(`${step.status.padEnd(5)} ${step.name}`);
    if (step.detail && step.status === "FAIL") console.log(step.detail);
  }
  console.log("");
  console.log(`Overall: ${passed ? "PASS" : "FAIL"}`);
}

process.exit(passed ? 0 : 1);
