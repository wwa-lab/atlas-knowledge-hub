<script setup lang="ts">
import { modelDetail, modelIcon } from '@/domain/viewModels'
import type {
  AddableModelTypeOption,
  ModelCategory,
  ModelCategoryOption,
  VueModelConfig
} from '@/domain/viewModels'

defineProps<{
  isModelAddMenuOpen: boolean
  addableModelTypes: AddableModelTypeOption[]
  modelApiStatus: string
  modelSaveStatus: string
  modelApiError: string
  modelCategories: ModelCategoryOption[]
  activeModelCategory: ModelCategory
  models: VueModelConfig[]
  visibleModels: VueModelConfig[]
}>()

const emit = defineEmits<{
  close: []
  toggleModelAddMenu: []
  updateActiveModelCategory: [category: ModelCategory]
  openModelEditor: [model: VueModelConfig]
}>()

function modelCount(models: VueModelConfig[], category: ModelCategory) {
  return category === 'all'
    ? models.length
    : models.filter(model => model.category === category).length
}
</script>

<template>
  <section class="vue-model-content" data-testid="vue-model-manager">
    <button class="atlas-settings-close" type="button" @click="emit('close')">×</button>
    <header class="vue-model-head">
      <div>
        <h1>模型配置</h1>
        <p>管理不同类型的 AI 模型，支持 Ollama 本地模型和远程 API。</p>
      </div>
      <div class="vue-model-actions">
        <button
          class="vue-add-model"
          data-testid="vue-add-model"
          type="button"
          @click="emit('toggleModelAddMenu')"
        >
          + 添加模型
        </button>
        <div v-if="isModelAddMenuOpen" class="vue-add-menu" role="menu">
          <button
            v-for="category in addableModelTypes"
            :key="category.key"
            :data-testid="`vue-add-${category.key}`"
            type="button"
            disabled
            data-coming-soon="true"
          >
            {{ category.label }} · coming soon
          </button>
        </div>
      </div>
    </header>

    <section class="vue-model-info">
      <strong>内置模型</strong>
      <span data-testid="vue-api-model-status">{{ modelApiStatus }}</span>
      <p>内置模型对所有租户可见，敏感信息会被隐藏，当前界面不会展示完整密钥。</p>
      <p
        v-if="modelSaveStatus"
        class="atlas-inline-success"
        data-testid="vue-model-save-status"
        role="status"
      >
        {{ modelSaveStatus }}
      </p>
      <p v-if="modelApiError" class="atlas-inline-warning">{{ modelApiError }}</p>
      <a href="#" aria-label="查看内置模型管理指南">查看内置模型管理指南 ↗</a>
    </section>

    <nav class="vue-model-tabs" aria-label="Model categories">
      <button
        v-for="category in modelCategories"
        :key="category.key"
        :class="{ active: activeModelCategory === category.key }"
        :data-testid="`vue-model-tab-${category.key}`"
        type="button"
        @click="emit('updateActiveModelCategory', category.key)"
      >
        {{ category.label }}({{ modelCount(models, category.key) }})
      </button>
    </nav>

    <section class="vue-model-grid" aria-label="Configured models">
      <button
        v-for="model in visibleModels"
        :key="model.id"
        class="vue-model-card"
        :data-testid="`vue-model-card-${model.id}`"
        type="button"
        @click="emit('openModelEditor', model)"
      >
        <span class="vue-model-icon">{{ modelIcon(model.category) }}</span>
        <span>
          <strong>{{ model.displayName }}</strong>
          <small>
            {{ model.name }} · {{ model.provider }}{{ modelDetail(model) }} ·
            {{ model.sourceLabel ?? 'sample' }}
          </small>
        </span>
      </button>
    </section>
  </section>
</template>
