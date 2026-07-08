<script setup lang="ts">
import type { GeneralOption, MessageIndexStat } from '@/domain/viewModels'

defineProps<{
  messageIndexEnabled: boolean
  messageEmbeddingModel: string
  messageEmbeddingOptions: Array<GeneralOption<string>>
  messageIndexConfigured: boolean
  messageIndexStats: MessageIndexStat[]
}>()

const emit = defineEmits<{
  close: []
  toggleMessageIndexing: []
  updateMessageEmbeddingModel: [model: string]
}>()

function readSelectValue(event: unknown) {
  const target =
    event && typeof event === 'object' && 'target' in event
      ? (event as { target?: { value?: unknown } }).target
      : null
  return typeof target?.value === 'string' ? target.value : ''
}
</script>

<template>
  <section class="atlas-message-settings" data-testid="vue-message-management">
    <button class="atlas-settings-close" type="button" @click="emit('close')">×</button>
    <header class="atlas-general-head" data-testid="vue-admin-panel">
      <h1>消息管理</h1>
      <p>配置聊天历史知识库，将对话消息自动向量化索引，实现语义搜索</p>
    </header>

    <div class="atlas-message-form">
      <section class="atlas-message-row">
        <div>
          <h2>启用消息索引</h2>
          <p>开启后，新的对话消息将自动索引到知识库，支持向量搜索</p>
        </div>
        <button
          class="atlas-switch"
          :class="{ active: messageIndexEnabled }"
          type="button"
          role="switch"
          :aria-checked="messageIndexEnabled"
          data-testid="vue-message-index-toggle"
          @click="emit('toggleMessageIndexing')"
        >
          <span></span>
        </button>
      </section>

      <section v-if="messageIndexEnabled" class="atlas-message-row">
        <div>
          <h2>Embedding 模型</h2>
          <p>选择用于聊天历史语义检索的 Embedding 模型</p>
        </div>
        <select
          :value="messageEmbeddingModel"
          data-testid="vue-message-embedding-model"
          aria-label="消息索引 Embedding 模型"
          @change="emit('updateMessageEmbeddingModel', readSelectValue($event))"
        >
          <option
            v-for="option in messageEmbeddingOptions"
            :key="option.value"
            :value="option.value"
          >
            {{ option.label }}
          </option>
        </select>
      </section>
    </div>

    <section class="atlas-message-stats" data-testid="vue-message-index-stats">
      <h2>索引统计</h2>
      <div v-if="!messageIndexConfigured" class="atlas-message-empty">
        <strong>消息索引未配置</strong>
        <p>启用并选择 Embedding 模型后，对话消息将自动向量化索引</p>
      </div>
      <div v-else class="atlas-admin-grid">
        <article v-for="stat in messageIndexStats" :key="stat.label">
          <strong>{{ stat.label }}</strong>
          <span>{{ stat.value }}</span>
          <p>{{ stat.note }}</p>
        </article>
      </div>
    </section>

    <section class="atlas-admin-boundary">
      <strong>Production boundary</strong>
      <p>
        当前消息管理仅保存本地 mock 开关；不持久化真实聊天历史、不执行真实 embedding、
        不写入真实向量库，也不会绕过 ModelAdapter 或 VectorAdapter 边界。
      </p>
    </section>
  </section>
</template>
