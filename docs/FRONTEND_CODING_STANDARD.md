# Frontend Coding Standard

Atlas Knowledge Hub Phase 1+ frontend development standards for Vue 3 + Vite + TypeScript.

This document extends [DEVELOPMENT_STANDARDS.md](../DEVELOPMENT_STANDARDS.md) § Frontend Standards with concrete patterns and examples specific to Vue. 中文版：[FRONTEND_CODING_STANDARD.zh-CN.md](FRONTEND_CODING_STANDARD.zh-CN.md).

Rules are cross-checked against the [Vue.js Official Style Guide](https://vuejs.org/style-guide/), the [Google TypeScript Style Guide](https://google.github.io/styleguide/tsguide.html), and the [Airbnb JavaScript Style Guide](https://github.com/airbnb/javascript).

## File Organization

```
frontend/src/
├── main.ts              # App entry
├── App.vue              # Root component
├── styles.css           # Global styles (minimal)
├── types.ts             # Domain models (all interfaces/types)
├── composables/         # Reusable logic (use* functions)
│   └── useDialogInput.ts
├── components/          # Vue components, organized by feature/domain
│   ├── Home/
│   │   ├── KnowledgeSpaceCard.vue
│   │   ├── DialogueMode.vue
│   │   └── HomePage.vue
│   ├── Space/
│   │   ├── SpaceShell.vue
│   │   ├── SpaceNav.vue
│   │   └── tabs/
│   │       ├── DocumentsTab.vue
│   │       ├── WikiTab.vue
│   │       └── ...
│   └── Common/
│       ├── Header.vue
│       └── Sidebar.vue
├── data/                # Mock/static data
│   └── atlasMock.ts
└── __tests__/ or .test.ts  # Tests co-located with source
```

- **Feature-first organization**: Group related components by feature (Home, Space, Review, Ask), not by file type.
- **Co-located tests**: Place `.test.ts` / `.spec.ts` next to the component or utility it tests.
- **Keep files focused**: Aim for 200–400 lines; max 800 lines. Extract utilities/components when a file grows beyond scope.

## Component Naming and Structure

### Vue Components

- **PascalCase** for component names and file names: `KnowledgeSpaceCard.vue`, `SpaceShell.vue`.
- **Self-documenting names**: Prefer `KnowledgeSpaceCard` over `Card`, `WikiTab` over `Tab`.

### Component Template

Use `<script setup lang="ts">` pattern (composition API, single-file components):

```vue
<script setup lang="ts">
import { computed } from 'vue'
import type { KnowledgeSpace } from '@/types'

interface Props {
  space: KnowledgeSpace
  onSelect?: (id: string) => void
}

const props = withDefaults(defineProps<Props>(), {
  onSelect: undefined
})

const emit = defineEmits<{
  select: [id: string]
  detail: [space: KnowledgeSpace]
}>()

const displayStatus = computed(() => {
  const statusMap: Record<string, string> = {
    'Ready': '✓ Ready',
    'Review Required': '⚠ Review',
    'Parsing': '⏳ Parsing'
  }
  return statusMap[props.space.status] || props.space.status
})

const handleClick = () => {
  emit('select', props.space.id)
  emit('detail', props.space)
}
</script>

<template>
  <div class="space-card" @click="handleClick">
    <h3>{{ space.name }}</h3>
    <p class="status">{{ displayStatus }}</p>
    <p class="description">{{ space.description }}</p>
  </div>
</template>

<style scoped>
.space-card {
  padding: 1rem;
  border: 1px solid #ccc;
  border-radius: 4px;
  cursor: pointer;
}
</style>
```

**Key conventions**:
- Use `<script setup>` with explicit `Props` and `Emits` types.
- Define props with `withDefaults<Props>()` for clarity.
- Use `defineEmits<{ eventName: [argType] }>()` for strongly-typed events.
- Keep inline styles in `<style scoped>`; extract to `styles.css` only if reused across many components.

## Vue Style Guide Rules

We follow the official Vue.js Style Guide. Priority A is enforced; Priority B is strongly recommended.

### Priority A — Essential (error prevention)

- **Multi-word component names** (except root `App`): `KnowledgeSpaceCard`, not `Card` — avoids clashing with current/future HTML elements.
- **Detailed, typed prop definitions:** every prop is typed; constrain values with unions and, where useful, a validator.
  ```ts
  const props = defineProps<{ status: 'Ready' | 'Review Required' | 'Parsing' }>()
  ```
- **Keyed `v-for`:** always bind a stable `:key` (`:key="space.id"`); never the array index for dynamic lists.
- **Never `v-if` with `v-for` on the same element:** filter via a `computed`, or move `v-if` to a wrapper — `v-if` evaluates before the loop variable exists.
- **Component-scoped styles:** every component (except `App`/layout) uses `<style scoped>` (or CSS modules); no global leakage.

### Priority B — Strongly Recommended

- **Filename casing:** PascalCase (`KnowledgeSpaceCard.vue`), consistent across the repo.
- **Base components** get a `Base`/`App`/`V` prefix (`BaseButton.vue`).
- **Tightly-coupled children carry the parent prefix:** `SpaceNav`, `SpaceDocumentsTab` — reads as belonging to `Space`.
- **Word order high-level → modifier:** `SearchButtonClear`, not `ClearSearchButton`.
- **Self-closing** components in SFC templates: `<KnowledgeSpaceCard />`.
- **One attribute per line** when an element has multiple attributes.
- **Simple template expressions:** no logic in `{{ }}` — move it to a named `computed`.
- **Simple, composable computed properties:** split a complex computed into several well-named ones.
- **Prop names:** `camelCase` in `defineProps`; `kebab-case` when passed in templates.
- **Directive shorthands consistently:** always `:` / `@` / `#` (our convention), never mixed with the long form.

## Modules & Exports

- **Named exports for utilities, composables, and types** (Google TS) — predictable imports, safe renames. Vue SFCs use the implicit default export (unavoidable); everything else is named.
- Use `const` by default, `let` only when reassigning, **never `var`** (Airbnb).
- Prefer `readonly` / `Readonly<T>` for values never reassigned after construction (Google TS); composables return `computed()` for read-only reactive state.
- Import order: framework (`vue`) → third-party → `@/` internal; no wildcard/`* as` imports.

## Types and Interfaces

**Location**: All domain models in `src/types.ts`, grouped by feature.

**Conventions**:
- Use `interface` for extensible object shapes (component props, API responses, entities).
- Use `type` for unions, intersections, and discriminated unions.
- Use string literal unions instead of `enum` (e.g., `"wiki" | "graph" | "ask"`).
- Avoid `any`; use `unknown` for untrusted input, then narrow safely.

### Example from `src/types.ts`

```typescript
// String literal unions for type-safe enums
export type ViewName = "home" | "space"
export type SpaceTab = "documents" | "wiki" | "graph" | "review" | "ask"
export type Locale = "zh" | "en"
export type ThemeMode = "day" | "night"

// Extensible object shapes
export interface KnowledgeSpace {
  id: string
  name: string
  description: string
  owner: string
  updatedAt: string
  status: "Ready" | "Review Required" | "Parsing"
  documents: number
  wikiPages: number
  reviews: number
}

// Discriminated union for different node types
export type GraphNode = {
  id: string
  label: string
  x: number
  y: number
  detail: string
} & (
  | { type: "Wiki Page" }
  | { type: "Entity" }
  | { type: "Concept" }
  | { type: "Document" }
  | { type: "Review Required" }
)
```

## Props, Emits, and Expose

### Props (Input Contract)

```typescript
interface KnowledgeSpaceCardProps {
  space: KnowledgeSpace
  highlightId?: string
  loading?: boolean
  onSelect?: (id: string) => void
}

const props = withDefaults(defineProps<KnowledgeSpaceCardProps>(), {
  highlightId: undefined,
  loading: false,
  onSelect: undefined
})
```

**Conventions**:
- Define a named `Props` interface, even for simple components.
- Document non-obvious props with JSDoc comments.
- Use optional props (`?`) for UI hints (disabled state, loading) and callbacks.
- Avoid boolean prop chains (prefer `variant` or `mode` unions).

### Emits (Output Contract)

```typescript
const emit = defineEmits<{
  select: [id: string]
  detail: [space: KnowledgeSpace]
  error: [message: string]
}>()
```

**Conventions**:
- Name events as past-tense verbs for actions (`selected`, `clicked`) or nouns for state changes (`details`).
- Type event payloads explicitly.
- Use camelCase event names (Vue normalizes them in templates).

### Expose (Public Methods, Rare)

If a parent must call component methods (e.g., focus a form field, scroll a list), use `defineExpose`:

```typescript
const dialogInput = ref<HTMLInputElement>(null)

defineExpose({
  focus: () => dialogInput.value?.focus()
})
```

**Only expose when necessary for accessibility or integration**; prefer props + emits for normal data flow.

## State Management

### Local Component State

Use `ref`, `computed`, and `watch` for component-scoped state.

```typescript
import { ref, computed, watch } from 'vue'

const dialogInput = ref('')
const selectedContext = ref<KnowledgeSpace | null>(null)

const isInputEmpty = computed(() => dialogInput.value.trim() === '')

watch(dialogInput, (newInput) => {
  // Preserve input state when language/theme changes
  sessionStorage.setItem('dialogInput', newInput)
})
```

**Conventions**:
- Use `ref` for primitive and object state; TypeScript infers types.
- Use `computed` for derived state (filtering, mapping, validation).
- Use `watch` to sync state with storage, parent props, or side effects.
- Avoid deep reactivity; prefer immutable updates (see Immutability).

### Shared State (Composables)

For state used across multiple components, extract into a composable:

```typescript
// src/composables/useAppTheme.ts
import { ref, computed, Readonly } from 'vue'
import type { ThemeMode } from '@/types'

export function useAppTheme() {
  const themeMode = ref<ThemeMode>('day')

  const toggleTheme = () => {
    themeMode.value = themeMode.value === 'day' ? 'night' : 'day'
  }

  return {
    themeMode: computed(() => themeMode.value),
    toggleTheme
  }
}

// Usage
const { themeMode, toggleTheme } = useAppTheme()
```

**Conventions**:
- Name composables with `use*` prefix (`useDialogInput`, `useAppTheme`).
- Return reactive state as `computed()` for immutability.
- Keep composables focused on one concern.

## Immutability

Use spread operators and immutable patterns for state updates:

```typescript
// WRONG: Mutation
const space = props.spaces[0]
space.name = 'Updated Name'

// CORRECT: Immutable update
const updateSpace = (id: string, updates: Partial<KnowledgeSpace>) => {
  return props.spaces.map(s =>
    s.id === id ? { ...s, ...updates } : s
  )
}
```

For complex nested updates, use libraries like Immer or plain object spread:

```typescript
const updatedConfig = {
  ...modelConfig,
  settings: {
    ...modelConfig.settings,
    apiKey: 'new_key'
  }
}
```

## Error Handling

### Try-Catch with Error Narrowing

```typescript
async function loadSpace(spaceId: string): Promise<KnowledgeSpace> {
  try {
    const response = await fetch(`/api/spaces/${spaceId}`)
    if (!response.ok) throw new Error(`HTTP ${response.status}`)
    return await response.json()
  } catch (error: unknown) {
    const message = error instanceof Error ? error.message : 'Unknown error'
    console.error('Failed to load space:', message)
    throw new Error(`Failed to load space: ${message}`)
  }
}
```

### User-Facing Error Messages

Always provide actionable, user-friendly messages in the UI:

```typescript
const errorMessage = ref<string>('')

const handleUpload = async (file: File) => {
  try {
    await uploadFile(file)
  } catch (error: unknown) {
    errorMessage.value = error instanceof Error
      ? `Upload failed: ${error.message}`
      : 'Upload failed. Please try again.'
  }
}
```

## Testing

### Vitest Unit and Component Tests

Test pure logic, state transitions, and component rendering:

```typescript
// src/components/KnowledgeSpaceCard.test.ts
import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import KnowledgeSpaceCard from './KnowledgeSpaceCard.vue'
import type { KnowledgeSpace } from '@/types'

describe('KnowledgeSpaceCard', () => {
  const mockSpace: KnowledgeSpace = {
    id: 'test-1',
    name: 'Test Space',
    description: 'A test space',
    owner: 'Test Owner',
    updatedAt: '2026-07-03',
    status: 'Ready',
    documents: 10,
    wikiPages: 5,
    reviews: 0
  }

  it('renders space name and status', () => {
    const wrapper = mount(KnowledgeSpaceCard, {
      props: { space: mockSpace }
    })
    expect(wrapper.text()).toContain('Test Space')
    expect(wrapper.text()).toContain('✓ Ready')
  })

  it('emits select event on click', async () => {
    const wrapper = mount(KnowledgeSpaceCard, {
      props: { space: mockSpace }
    })
    await wrapper.trigger('click')
    expect(wrapper.emitted('select')).toHaveLength(1)
    expect(wrapper.emitted('select')[0]).toEqual(['test-1'])
  })
})
```

**Conventions**:
- Test non-trivial state mapping, formatting, and interaction logic.
- Use `@vue/test-utils` for component mounting.
- Keep mocks aligned with `src/types.ts` domain models.
- Aim for ≥80% coverage of modified code.

### Playwright E2E Tests

Test critical user flows (home navigation, space detail, review workflow):

```typescript
// frontend/tests/e2e/knowledge-space-detail.spec.ts
import { test, expect } from '@playwright/test'

test('user navigates to IBM i space and views wiki', async ({ page }) => {
  await page.goto('/')
  
  // Click IBM i card
  await page.click('text=IBM i Modernization')
  
  // Verify space detail loads
  await expect(page).toHaveURL(/space\/ibm-i/)
  await expect(page.locator('text=IBM i Modernization')).toBeVisible()
  
  // Wiki tab is default
  await expect(page.locator('[aria-selected="true"]')).toContainText('Wiki')
  
  // Wiki content is visible
  await expect(page.locator('.wiki-section')).toBeVisible()
})
```

## Code Quality Checklist (Before Commit)

Before marking code complete, verify:

- [ ] **Naming**: Component names are PascalCase; props/emits/variables are camelCase; composables start with `use*`.
- [ ] **Types**: All props, emits, and public functions have explicit types; no `any`.
- [ ] **Size**: Files <800 lines; functions <50 lines; keep nesting <4 levels.
- [ ] **Immutability**: No mutation of props or external state; use spread operators.
- [ ] **Tests**: Component tests for non-trivial logic; E2E tests for critical flows.
- [ ] **Error handling**: Try-catch blocks for async; user-friendly error messages.
- [ ] **No console.log**: No debug statements in committed code.
- [ ] **Scoped styles**: Use `<style scoped>` to avoid CSS conflicts.
- [ ] **Documentation**: Public APIs, complex logic, and non-obvious patterns have JSDoc comments.

## Examples

See existing components for patterns:
- `frontend/src/App.vue` — Root component structure.
- `frontend/src/types.ts` — Domain model organization.
- `frontend/src/data/atlasMock.ts` — Mock data structure.
- `frontend/src/App.test.ts` — Vitest setup and component testing.
- `frontend/tests/e2e/phase1-smoke.spec.ts` — Playwright E2E smoke tests.

## Linting and Formatting

All code is validated by ESLint and formatted with Prettier. Run before commit:

```bash
npm run lint     # Check code style and TypeScript errors
npm run format   # Auto-format with Prettier
```

Pre-commit hooks enforce both automatically.
