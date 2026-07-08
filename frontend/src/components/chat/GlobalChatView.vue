<script setup lang="ts">
import type {
  ProductAskAnswer,
  ProductAskMode,
  ProductSpaceCard,
  VueModelConfig
} from '@/domain/viewModels'

defineProps<{
  productSpaceCards: ProductSpaceCard[]
  selectedChatSpaces: string[]
  productAskQuestion: string
  productAskMode: ProductAskMode
  visibleModels: VueModelConfig[]
  productAskAnswer: ProductAskAnswer
  askQualityChips: string[]
  askQualityError: string
}>()

const emit = defineEmits<{
  toggleChatSpace: [spaceName: string]
  updateProductAskQuestion: [question: string]
  updateProductAskMode: [mode: ProductAskMode]
  submitProductAsk: []
}>()

function readTextareaValue(event: unknown) {
  const target =
    event && typeof event === 'object' && 'target' in event
      ? (event as { target?: { value?: unknown } }).target
      : null
  return typeof target?.value === 'string' ? target.value : ''
}

function readAskMode(event: unknown) {
  const target =
    event && typeof event === 'object' && 'target' in event
      ? (event as { target?: { value?: unknown } }).target
      : null
  return typeof target?.value === 'string' ? (target.value as ProductAskMode) : 'answered'
}
</script>

<template>
  <section class="atlas-chat-view" data-testid="vue-global-chat">
    <h1>Atlas，让你的知识触手可及</h1>
    <p>选择一个或多个知识库作为上下文，再开始基于来源的可信问答。</p>
    <div class="atlas-chat-spaces">
      <button
        v-for="space in productSpaceCards"
        :key="space.id"
        :class="{ active: selectedChatSpaces.includes(space.name) }"
        type="button"
        @click="emit('toggleChatSpace', space.name)"
      >
        {{ selectedChatSpaces.includes(space.name) ? '✓' : '□' }} {{ space.name }}
      </button>
    </div>
    <div class="atlas-chat-box">
      <textarea
        :value="productAskQuestion"
        data-testid="vue-ask-question"
        @input="emit('updateProductAskQuestion', readTextareaValue($event))"
      ></textarea>
      <div>
        <span>知识库({{ selectedChatSpaces.length }})</span>
        <label>
          <span>模型</span>
          <select aria-label="Ask model selector">
            <option v-for="model in visibleModels" :key="model.id">
              {{ model.displayName }} · {{ model.sourceLabel ?? 'sample adapter' }}
            </option>
          </select>
        </label>
        <label>
          <span>场景</span>
          <select
            :value="productAskMode"
            data-testid="vue-ask-mode"
            @change="emit('updateProductAskMode', readAskMode($event))"
          >
            <option value="answered">可信回答</option>
            <option value="refusal">证据不足拒答</option>
            <option value="review-warning">Review-required warning</option>
          </select>
        </label>
        <button data-testid="vue-api-ask-submit" type="button" @click="emit('submitProductAsk')">
          API Ask
        </button>
      </div>
    </div>
    <section class="atlas-ask-answer" data-testid="vue-trusted-ask-answer">
      <header>
        <span>{{ productAskAnswer.status }}</span>
        <h2>{{ productAskAnswer.title }}</h2>
      </header>
      <p>{{ productAskAnswer.body }}</p>
      <p class="source-line">
        {{ productAskAnswer.governanceLabel }} · {{ productAskAnswer.reuseHint }}
      </p>
      <p class="source-line">{{ productAskAnswer.governanceReason }}</p>
      <div v-if="askQualityChips.length > 0" class="atlas-quality-signals">
        <span v-for="chip in askQualityChips" :key="chip">{{ chip }}</span>
      </div>
      <small v-if="askQualityError" class="atlas-inline-warning">{{ askQualityError }}</small>
      <strong>Evidence citations</strong>
      <ul v-if="productAskAnswer.evidence.length > 0">
        <li v-for="evidence in productAskAnswer.evidence" :key="evidence">{{ evidence }}</li>
      </ul>
      <p v-else class="atlas-inline-warning">No approved evidence citations available.</p>
      <small>{{ productAskAnswer.warning }}</small>
    </section>
    <small>未通过处理中心门禁的解析失败、低置信或缺少 source_trace 内容不会进入对话索引。</small>
  </section>
</template>
