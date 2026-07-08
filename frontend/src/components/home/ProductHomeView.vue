<script setup lang="ts">
import type { CreateSpaceDraft, ProductSpaceCard } from '@/domain/viewModels'

const props = defineProps<{
  productSpaceCards: ProductSpaceCard[]
  apiSpaceCount: number
  canManageSpaces: boolean
  isCreateSpaceOpen: boolean
  isCreatingSpace: boolean
  createSpaceDraft: CreateSpaceDraft
  createSpaceStatus: string
  createSpaceError: string
}>()

const emit = defineEmits<{
  openCreateSpace: []
  closeCreateSpace: []
  createSpace: []
  openSpace: [spaceId: string]
  updateCreateSpaceDraft: [draft: CreateSpaceDraft]
}>()

function updateDraft<K extends keyof CreateSpaceDraft>(key: K, value: CreateSpaceDraft[K]) {
  emit('updateCreateSpaceDraft', {
    ...props.createSpaceDraft,
    [key]: value
  })
}

function readInputValue(event: unknown) {
  const target =
    event && typeof event === 'object' && 'target' in event
      ? (event as { target?: { value?: unknown } }).target
      : null
  return typeof target?.value === 'string' ? target.value : ''
}

function readSelectValue<T extends string>(event: unknown) {
  const target =
    event && typeof event === 'object' && 'target' in event
      ? (event as { target?: { value?: unknown } }).target
      : null
  return typeof target?.value === 'string' ? (target.value as T) : ('' as T)
}
</script>

<template>
  <header class="atlas-page-head">
    <div>
      <h1>知识库</h1>
      <p>管理企业知识空间、文档包、Wiki、图谱和可信问答上下文。</p>
    </div>
    <button
      class="atlas-icon-action"
      data-testid="vue-create-space-open"
      type="button"
      aria-label="新建知识库"
      :disabled="!canManageSpaces"
      @click="emit('openCreateSpace')"
    >
      □＋
    </button>
  </header>
  <p
    v-if="createSpaceStatus"
    class="atlas-inline-success"
    data-testid="vue-space-create-status"
    role="status"
  >
    {{ createSpaceStatus }}
  </p>
  <div class="atlas-library-toolbar">
    <button type="button">♙ 我创建的</button>
    <span>{{ productSpaceCards.length }}</span>
    <span data-testid="vue-api-space-status">
      {{ apiSpaceCount > 0 ? 'API-backed metadata' : 'Sample fallback' }}
    </span>
    <span>⌄</span>
  </div>
  <section class="atlas-library-grid">
    <button
      v-for="space in productSpaceCards"
      :key="space.id"
      class="atlas-library-card"
      :data-testid="`vue-space-card-${space.id}`"
      type="button"
      @click="emit('openSpace', space.id)"
    >
      <h2>{{ space.name }}</h2>
      <p>{{ space.description }}</p>
      <div>
        <span>□ {{ space.documents }}</span>
        <span>Wiki {{ space.wikiPages }}</span>
        <span>{{ space.status }}</span>
        <span>⚗</span>
        <span>{{ space.source === 'api' ? 'API' : 'Mock' }}</span>
        <strong>♙ {{ space.owner }}</strong>
      </div>
    </button>
  </section>
  <form
    v-if="isCreateSpaceOpen"
    class="atlas-create-space-panel"
    data-testid="vue-create-space-panel"
    aria-label="新建知识库"
    @submit.prevent="emit('createSpace')"
  >
    <header>
      <div>
        <h2>新建知识库</h2>
        <p>创建一个用于上传文档、生成 Wiki、图谱和可信问答的知识空间。</p>
      </div>
      <button type="button" aria-label="关闭新建知识库" @click="emit('closeCreateSpace')">×</button>
    </header>
    <label>
      名称
      <input
        :value="createSpaceDraft.name"
        data-testid="vue-create-space-name"
        autocomplete="off"
        placeholder="例如 Claims Ops Hub"
        @input="updateDraft('name', readInputValue($event))"
      />
    </label>
    <label>
      描述
      <textarea
        :value="createSpaceDraft.description"
        data-testid="vue-create-space-description"
        rows="3"
        placeholder="说明这个知识库覆盖的项目、团队或文档范围"
        @input="updateDraft('description', readInputValue($event))"
      ></textarea>
    </label>
    <div class="atlas-create-space-options">
      <label>
        类型
        <select
          :value="createSpaceDraft.type"
          data-testid="vue-create-space-type"
          @change="updateDraft('type', readSelectValue<CreateSpaceDraft['type']>($event))"
        >
          <option value="document">Document</option>
          <option value="faq">FAQ</option>
        </select>
      </label>
      <label>
        索引策略
        <select
          :value="createSpaceDraft.indexStrategy"
          data-testid="vue-create-space-index"
          @change="
            updateDraft('indexStrategy', readSelectValue<CreateSpaceDraft['indexStrategy']>($event))
          "
        >
          <option value="rag">RAG</option>
          <option value="wiki">Wiki</option>
        </select>
      </label>
    </div>
    <p v-if="createSpaceError" class="atlas-inline-warning">{{ createSpaceError }}</p>
    <footer>
      <button class="secondary" type="button" @click="emit('closeCreateSpace')">取消</button>
      <button
        data-testid="vue-create-space-submit"
        type="button"
        :disabled="isCreatingSpace || !canManageSpaces"
        @click="emit('createSpace')"
      >
        {{ isCreatingSpace ? '创建中...' : '创建' }}
      </button>
    </footer>
  </form>
</template>
