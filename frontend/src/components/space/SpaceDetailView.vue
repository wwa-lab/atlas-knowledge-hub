<script setup lang="ts">
import type { MockUploadKind, ProductSpaceCard, SpaceTab } from '@/domain/viewModels'

defineProps<{
  selectedProductSpace: ProductSpaceCard
  activeSpaceTab: SpaceTab
  workflowError: string
}>()

const emit = defineEmits<{
  showHome: []
  openMockUpload: [kind: MockUploadKind]
  updateActiveTab: [tab: SpaceTab]
}>()

const tabs: Array<{ key: SpaceTab; label: string; testId?: string }> = [
  { key: 'docs', label: '文档' },
  { key: 'connectors', label: 'Connectors', testId: 'vue-connector-sync-tab' },
  { key: 'review', label: '处理中心' },
  { key: 'wiki', label: 'Wiki' },
  { key: 'graph', label: '图谱' }
]
</script>

<template>
  <div class="atlas-crumbs">
    知识库 / <strong>{{ selectedProductSpace.name }}</strong>
  </div>
  <header class="atlas-space-head" data-testid="vue-space-head">
    <div>
      <h1>{{ selectedProductSpace.name }}</h1>
      <p>{{ selectedProductSpace.description }}</p>
      <span>{{ selectedProductSpace.source === 'api' ? 'API-backed Space' : 'Mock Space' }}</span>
      <span>{{ selectedProductSpace.reviews }} Review Required</span>
      <span>{{ selectedProductSpace.documents }} Documents</span>
      <span>{{ selectedProductSpace.wikiPages }} Wiki Pages</span>
      <span v-if="workflowError" class="atlas-inline-warning">{{ workflowError }}</span>
    </div>
    <div>
      <button type="button" @click="emit('showHome')">返回知识库</button>
      <button
        data-testid="vue-upload-folder"
        type="button"
        @click="emit('openMockUpload', 'folder')"
      >
        上传文件夹
      </button>
      <button data-testid="vue-upload-zip" type="button" @click="emit('openMockUpload', 'zip')">
        上传 ZIP
      </button>
    </div>
  </header>
  <nav class="atlas-space-tabs" aria-label="Knowledge Space tabs">
    <button
      v-for="tab in tabs"
      :key="tab.key"
      :class="{ active: activeSpaceTab === tab.key }"
      type="button"
      :data-testid="tab.testId"
      @click="emit('updateActiveTab', tab.key)"
    >
      {{ tab.label }}
    </button>
  </nav>
  <section class="atlas-space-panel" data-testid="vue-space-detail">
    <slot />
  </section>
</template>
