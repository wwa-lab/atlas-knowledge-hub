# 前端开发标准

Atlas Knowledge Hub Phase 1+ 前端开发标准，面向 Vue 3 + Vite + TypeScript。

本文档扩展 [DEVELOPMENT_STANDARDS.md](../DEVELOPMENT_STANDARDS.md) § Frontend Standards，补充 Vue 特有的具体模式与示例。English: [FRONTEND_CODING_STANDARD.md](FRONTEND_CODING_STANDARD.md)。

规则对标 [Vue.js 官方风格指南](https://vuejs.org/style-guide/)、[Google TypeScript 风格指南](https://google.github.io/styleguide/tsguide.html)、[Airbnb JavaScript 风格指南](https://github.com/airbnb/javascript)。

## 文件组织

```
frontend/src/
├── main.ts              # 应用入口
├── App.vue              # 根组件
├── styles.css           # 全局样式（最小化）
├── types.ts             # 领域模型（全部 interface/type）
├── composables/         # 可复用逻辑（use* 函数）
│   └── useDialogInput.ts
├── components/          # Vue 组件，按功能/领域组织
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
├── data/                # mock/静态数据
│   └── atlasMock.ts
└── __tests__/ 或 .test.ts  # 测试与源码共置
```

- **按功能优先组织**：把相关组件按功能分组（Home、Space、Review、Ask），而非按文件类型。
- **测试共置**：`.test.ts` / `.spec.ts` 放在被测组件或工具旁边。
- **文件聚焦**：目标 200–400 行，上限 800 行。文件超出职责时抽出工具/组件。

## 组件命名与结构

### Vue 组件

- 组件名与文件名用 **PascalCase**：`KnowledgeSpaceCard.vue`、`SpaceShell.vue`。
- **自解释命名**：用 `KnowledgeSpaceCard` 而非 `Card`，`WikiTab` 而非 `Tab`。

### 组件模板

使用 `<script setup lang="ts">` 模式（组合式 API、单文件组件）：

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

**关键约定**：
- 用 `<script setup>`，显式声明 `Props` 与 `Emits` 类型。
- 用 `withDefaults<Props>()` 定义 props，清晰表达默认值。
- 用 `defineEmits<{ eventName: [argType] }>()` 声明强类型事件。
- 行内样式放 `<style scoped>`；仅当跨多组件复用时才抽到 `styles.css`。

## Vue 风格指南规则

遵循 Vue.js 官方风格指南。Priority A 强制执行；Priority B 强烈建议。

### Priority A —— 必要（防错）

- **组件名多词**（根 `App` 除外）：`KnowledgeSpaceCard` 而非 `Card`——避免与现有/未来 HTML 元素冲突。
- **详细、带类型的 prop 定义**：每个 prop 有类型；用联合类型约束取值，必要时加 validator。
  ```ts
  const props = defineProps<{ status: 'Ready' | 'Review Required' | 'Parsing' }>()
  ```
- **带 key 的 `v-for`**：始终绑定稳定的 `:key`（`:key="space.id"`）；动态列表绝不用数组下标。
- **同一元素上绝不 `v-if` + `v-for`**：用 `computed` 过滤，或把 `v-if` 移到外层——`v-if` 先于循环变量求值。
- **组件级作用域样式**：每个组件（`App`/布局除外）用 `<style scoped>`（或 CSS modules）；无全局泄漏。

### Priority B —— 强烈建议

- **文件名大小写**：PascalCase（`KnowledgeSpaceCard.vue`），全仓库一致。
- **基础组件**加 `Base`/`App`/`V` 前缀（`BaseButton.vue`）。
- **紧耦合子组件带父级前缀**：`SpaceNav`、`SpaceDocumentsTab`——一眼看出属于 `Space`。
- **词序高层 → 修饰**：`SearchButtonClear`，而非 `ClearSearchButton`。
- SFC 模板中组件**自闭合**：`<KnowledgeSpaceCard />`。
- 元素有多个属性时**每行一个属性**。
- **模板表达式简单**：`{{ }}` 内不写逻辑——移到命名的 `computed`。
- **computed 简单可组合**：把复杂 computed 拆成多个命名良好的小 computed。
- **prop 命名**：`defineProps` 里 `camelCase`；模板传递时 `kebab-case`。
- **指令简写一致**：始终 `:` / `@` / `#`（本项目约定），不与长写法混用。

## 模块与导出

- **工具、composable、类型用具名导出**（Google TS）——导入可预测、改名安全。Vue SFC 使用隐式默认导出（不可避免）；其余一律具名。
- 默认用 `const`，需要重新赋值才用 `let`，**绝不用 `var`**（Airbnb）。
- 构造后不再重新赋值的值优先 `readonly` / `Readonly<T>`（Google TS）；composable 返回 `computed()` 作为只读响应式状态。
- 导入顺序：框架（`vue`）→ 第三方 → `@/` 内部；无通配符 `* as` 导入。

## 类型与接口

**位置**：所有领域模型在 `src/types.ts`，按功能分组。

**约定**：
- 可扩展对象形状用 `interface`（组件 props、API 响应、实体）。
- 联合、交叉、可辨识联合用 `type`。
- 用字符串字面量联合替代 `enum`（如 `"wiki" | "graph" | "ask"`）。
- 避免 `any`；不可信输入用 `unknown`，再安全收窄。

### `src/types.ts` 示例

```typescript
// 类型安全枚举用字符串字面量联合
export type ViewName = "home" | "space"
export type SpaceTab = "documents" | "wiki" | "graph" | "review" | "ask"
export type Locale = "zh" | "en"
export type ThemeMode = "day" | "night"

// 可扩展对象形状
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

// 不同节点类型的可辨识联合
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

## Props、Emits 与 Expose

### Props（输入契约）

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

**约定**：
- 即使简单组件也定义命名的 `Props` 接口。
- 用 JSDoc 记录非显而易见的 prop。
- UI 提示（disabled、loading）与回调用可选 prop（`?`）。
- 避免布尔 prop 堆叠（优先 `variant` 或 `mode` 联合）。

### Emits（输出契约）

```typescript
const emit = defineEmits<{
  select: [id: string]
  detail: [space: KnowledgeSpace]
  error: [message: string]
}>()
```

**约定**：
- 动作事件用过去式动词（`selected`、`clicked`），状态变化用名词（`details`）。
- 事件负载显式类型。
- 事件名用 camelCase（Vue 在模板里会归一化）。

### Expose（公开方法，少用）

若父组件必须调用子组件方法（如聚焦表单、滚动列表），用 `defineExpose`：

```typescript
const dialogInput = ref<HTMLInputElement>(null)

defineExpose({
  focus: () => dialogInput.value?.focus()
})
```

**仅在无障碍或集成必需时暴露**；常规数据流优先 props + emits。

## 状态管理

### 局部组件状态

组件作用域状态用 `ref`、`computed`、`watch`。

```typescript
import { ref, computed, watch } from 'vue'

const dialogInput = ref('')
const selectedContext = ref<KnowledgeSpace | null>(null)

const isInputEmpty = computed(() => dialogInput.value.trim() === '')

watch(dialogInput, (newInput) => {
  // 语言/主题切换时保留输入状态
  sessionStorage.setItem('dialogInput', newInput)
})
```

**约定**：
- 原始值与对象状态用 `ref`；TypeScript 自动推断类型。
- 派生状态（过滤、映射、校验）用 `computed`。
- 用 `watch` 把状态同步到存储、父 props 或副作用。
- 避免深层响应式；优先不可变更新（见"不可变性"）。

### 共享状态（composable）

跨多组件的状态抽成 composable：

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

// 使用
const { themeMode, toggleTheme } = useAppTheme()
```

**约定**：
- composable 用 `use*` 前缀命名（`useDialogInput`、`useAppTheme`）。
- 响应式状态以 `computed()` 返回以保证不可变。
- composable 聚焦单一职责。

## 不可变性

状态更新用展开运算符与不可变模式：

```typescript
// 错误：直接改写
const space = props.spaces[0]
space.name = 'Updated Name'

// 正确：不可变更新
const updateSpace = (id: string, updates: Partial<KnowledgeSpace>) => {
  return props.spaces.map(s =>
    s.id === id ? { ...s, ...updates } : s
  )
}
```

复杂嵌套更新用 Immer 或对象展开：

```typescript
const updatedConfig = {
  ...modelConfig,
  settings: {
    ...modelConfig.settings,
    apiKey: 'new_key'
  }
}
```

## 错误处理

### try-catch 与错误收窄

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

### 面向用户的错误信息

UI 中始终给出可操作、友好的信息：

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

## 测试

### Vitest 单元与组件测试

测试纯逻辑、状态转换与组件渲染：

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

**约定**：
- 测试非平凡的状态映射、格式化与交互逻辑。
- 用 `@vue/test-utils` 挂载组件。
- mock 与 `src/types.ts` 领域模型保持一致。
- 修改代码覆盖率目标 ≥80%。

### Playwright E2E 测试

测试关键用户流程（首页导航、空间详情、Review 工作流）：

```typescript
// frontend/tests/e2e/knowledge-space-detail.spec.ts
import { test, expect } from '@playwright/test'

test('user navigates to IBM i space and views wiki', async ({ page }) => {
  await page.goto('/')

  // 点击 IBM i 卡片
  await page.click('text=IBM i Modernization')

  // 校验空间详情加载
  await expect(page).toHaveURL(/space\/ibm-i/)
  await expect(page.locator('text=IBM i Modernization')).toBeVisible()

  // Wiki 页签默认选中
  await expect(page.locator('[aria-selected="true"]')).toContainText('Wiki')

  // Wiki 内容可见
  await expect(page.locator('.wiki-section')).toBeVisible()
})
```

## 代码质量检查清单（提交前）

标记完成前逐项确认：

- [ ] **命名**：组件名 PascalCase；props/emits/变量 camelCase；composable 以 `use*` 开头。
- [ ] **类型**：所有 props、emits、公开函数有显式类型；无 `any`。
- [ ] **Vue 规则**：带 key 的 `v-for`；不 `v-if`+`v-for` 同元素；组件名多词；`<style scoped>`。
- [ ] **大小**：文件 <800 行；函数 <50 行；嵌套 <4 层。
- [ ] **不可变**：不改写 props 或外部状态；用展开运算符。
- [ ] **测试**：非平凡逻辑有组件测试；关键流程有 E2E 测试。
- [ ] **错误处理**：async 有 try-catch；错误信息友好。
- [ ] **无 console.log**：提交代码不含调试语句。
- [ ] **作用域样式**：用 `<style scoped>` 避免 CSS 冲突。
- [ ] **具名导出**：工具/composable/类型具名导出；无 `var`。
- [ ] **文档**：公开 API、复杂逻辑、非显而易见模式有 JSDoc。

## 示例

参考现有组件的模式：
- `frontend/src/App.vue` —— 根组件结构。
- `frontend/src/types.ts` —— 领域模型组织。
- `frontend/src/data/atlasMock.ts` —— mock 数据结构。
- `frontend/src/App.test.ts` —— Vitest 配置与组件测试。
- `frontend/tests/e2e/phase1-smoke.spec.ts` —— Playwright E2E 冒烟测试。

## Lint 与格式化

所有代码由 ESLint 校验、Prettier 格式化。提交前运行：

```bash
npm run lint     # 检查代码风格与 TypeScript 错误
npm run format   # 用 Prettier 自动格式化
```

pre-commit 钩子自动强制两者。
