#!/bin/bash
set -e

# Setup git hooks for pre-commit and pre-push validation

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
GITHOOKS_DIR="$REPO_ROOT/.githooks"
GIT_HOOKS_DIR="$REPO_ROOT/.git/hooks"

echo "Setting up git hooks from $GITHOOKS_DIR..."

# Copy hooks from .githooks to .git/hooks
for hook in "$GITHOOKS_DIR"/*; do
  if [[ -f "$hook" ]]; then
    hook_name=$(basename "$hook")
    target="$GIT_HOOKS_DIR/$hook_name"
    cp "$hook" "$target"
    chmod +x "$target"
    echo "✓ Installed $hook_name hook"
  fi
done

# Ensure project-level hooksPath is set
git config --local core.hooksPath .git/hooks
echo "✓ Set core.hooksPath to .git/hooks"

echo ""
echo "Git hooks are now active. The following will run automatically:"
echo "  pre-commit: lint, format, and test"
echo "  pre-push:   typecheck and test"
